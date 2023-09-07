package com.yolt.providers.rabobank;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpHeaders;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SigningUtil {

    private static final String SIGNATURE_HEADER_FORMAT = "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"";

    /**
     * Computes signing string according to Rabobank signature (see https://developer-sandbox.rabobank.nl/signing-requests-account-information-v3,
     * section '3. Create the signing string') formatted according to section '5. Signature header'. In version 3.0.3 of
     * the API only 'date', 'digest' and 'x-request-id' headers are used as the signature computing input. Note that only
     * SHA-512-RSA algorithm is supported despite what documentation states.
     *
     * @param signer                  The service that outsources the signing for us.
     * @param headers                 Headers of the request. They are going to be filtered only to the ones used for computing a signature.
     * @param certificateSerialNumber Needed for signature header.
     * @param signingKid              The id of the private key to use for signing.
     * @return Signing string formatted according to documentation requirements.
     */
    public static String getSigningString(final Signer signer, final HttpHeaders headers, final String certificateSerialNumber, final UUID signingKid, final List<String> headersToSign) {
        Map<String, String> signingHeaders = getSigningHeadersWithValue(headers, headersToSign);
        String allHeaders = String.join(" ", signingHeaders.keySet());
        String signingString = createSigningString(signingHeaders);
        String signedHeaders = signer.sign(signingString.getBytes(), signingKid, SignatureAlgorithm.SHA512_WITH_RSA);

        return String.format(SIGNATURE_HEADER_FORMAT, certificateSerialNumber, "rsa-sha512", allHeaders, signedHeaders);
    }

    /**
     * Filter and sorts in natural ordering headers that are going to be signed. Note that natural ordering is used only
     * for convenience as Rabobank requires the headers to be in order: 'date', 'digest' and 'x-request-id'. Natural
     * ordering is going to be broken once new header is added in specification that violates this constraint.
     *
     * @param headers Headers not used in signature computation are going to be filtered out.
     * @return Sorted map of headers used for signature computation.
     */
    private static Map<String, String> getSigningHeadersWithValue(final HttpHeaders headers, List<String> headersToSign) {
        return headers.toSingleValueMap()
                .entrySet()
                .stream()
                .filter(header -> headersToSign.contains(header.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue,
                        (v1, v2) -> {
                            throw new IllegalStateException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new));
    }

    private static String createSigningString(final Map<String, String> signatureHeaders) {
        return signatureHeaders.entrySet()
                .stream()
                .map(header -> header.getKey() + ": " + header.getValue())
                .collect(Collectors.joining("\n"));
    }

    /**
     * This allows to compute a digest according to Rabobank requirements
     * (https://developer-sandbox.rabobank.nl/signing-requests-account-information-v3; section '2. Create the digest').
     * The computing steps reflects the algorithm that is supplied within the documentation. Note that SHA-256 is not
     * supported in 3.0.3 version of the API despite documentation states so.
     *
     * @param body Body of the request that is going to be digest computing input. It may be an empty array according to
     *             the documentation.
     * @return Digest encoded as Base64 string.
     */
    public static String getDigest(final byte[] body) {
        try {
            return "sha-512=" + Base64.toBase64String(MessageDigest.getInstance("SHA-512").digest(body));
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }
}
