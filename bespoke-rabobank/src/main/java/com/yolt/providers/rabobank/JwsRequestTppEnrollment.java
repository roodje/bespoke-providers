package com.yolt.providers.rabobank;

import com.yolt.providers.common.cryptography.Signer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

import static com.yolt.securityutils.signing.SignatureAlgorithm.SHA256_WITH_RSA;

/**
 * Implements the initial signed call we need to make to Rabobank to start the enrollment procedure.  Result of this
 * step is that Rabobank will send an e-mail with which the receiver can onboard on their api portal.
 * <p>
 * Step-by-step: https://developer.rabobank.nl/jws-request-tpp-enrollment
 * API definition: https://developer.rabobank.nl/reference/third-party-providers/1-0-1
 */
public class JwsRequestTppEnrollment {

    @SneakyThrows(CertificateEncodingException.class)
    public static boolean sendJwsRequestTppEnrollment(
            @NonNull String email,
            @NonNull X509Certificate qSealCert,
            @NonNull UUID qSealPrivateKid,
            @NonNull Signer signer,
            @NonNull RestTemplate restTemplate,
            Instant expiryTime,
            Clock clock
    ) {
        if (expiryTime == null) {
            expiryTime = Instant.now(clock).plus(7, ChronoUnit.DAYS);
        }
        // From https://developer.rabobank.nl/jws-request-tpp-enrollment

        // 1. Creating the Protected Header

        // The protected header is required to contain the algorithm and the public certificate obtained from your QTSP.
        // Currently RS256 is the only supported algorithm.
        // The x5c parameter (RFC 7515 section 4.1.6) is expected to contain no more and no less than 1 valid QSeal eIDAS certificate.
        // This needs to be a Base64 encoded DER (NOT URL-safe Base64).
        String qSealBase64EncodedDER = b64enc(qSealCert.getEncoded());
        // PROTECTED = { "alg": "RS256", "x5c": [BASE64(DER_CONTENT)] }
        String header = "{\"alg\":\"RS256\",\"x5c\":[\"" + qSealBase64EncodedDER + "\"]}";

        // 2. Creating the Payload

        // The payload must contain the email address (ptc_email) and a timestamp in seconds (exp) which indicates until when the request is allowed to be handled.
        long expiry = expiryTime.toEpochMilli() / 1_000;

        // PAYLOAD = { "ptc_email": "YOUR_EMAIL", "exp": TIMESTAMP_INTEGER }
        String payload = "{\"ptc_email\":\"" + email + "\",\"exp\":" + expiry + "}";

        // 3. Creating the signature
        // A signature is created with three elements: a signing string, the public certificate and the private key.

        // 3.1 Signing string
        // For creating the signing string you need the protected header and the payload.
        // Both elements need to be URL-safe Base64 encoded (RFC 4648) and then concatenated with a period (.) in between.
        // SIGNING_STRING = BASE64URL(PROTECTED) + "." + BASE64URL(PAYLOAD)
        String signingString = b64URLenc(header) + "." + b64URLenc(payload);

        // 3.2 Signing algorithm
        //
        // Use the QSeal eIDAS certificate and its corresponding private key to sign the string using RSA SHA256:
        //
        // SIGNATURE = RSA_SHA256(
        //    SIGNING_STRING,
        //    PUBLIC_CERTIFICATE,
        //    PRIVATE_KEY
        // )
        String base64EncodedSignature = signer.sign(signingString.getBytes(StandardCharsets.UTF_8), qSealPrivateKid, SHA256_WITH_RSA);

        // 4. Creating the API request body
        // Finally create the body of the API request by URL-safe Base64 encoding (RFC 4648) each part of the JWS:
        //
        // {
        //    "protected": BASE64URL(PROTECTED),
        //    "payload":   BASE64URL(PAYLOAD),
        //    "signature": BASE64URL(SIGNATURE)
        // }
        Request request = new Request(
                b64URLenc(header),
                b64URLenc(payload),
                b64URLenc(b64dec(base64EncodedSignature))
        );

        // Below this comment we need this resource:
        // https://developer.rabobank.nl/reference/third-party-providers/1-0-1

        String endpoint = "https://api.rabobank.nl/openapi/open-banking/third-party-providers";

        HttpHeaders httpHeaders = new HttpHeaders();
        // X-IBM-Client-Id: Use the following client id for enrollment: 64f38624-718d-4732-b579-b8979071fcb0
        httpHeaders.add("X-IBM-Client-Id", "64f38624-718d-4732-b579-b8979071fcb0");

        final ResponseEntity<Void> response;
        try {
            response = restTemplate.exchange(endpoint, HttpMethod.POST, new HttpEntity<>(request, httpHeaders), Void.class);
        } catch (RestClientException e) {
            return false;
        }

        return response.getStatusCode() == HttpStatus.ACCEPTED;
    }

    private static String b64enc(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }

    private static byte[] b64dec(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    private static String b64URLenc(byte[] input) {
        return Base64.getUrlEncoder().encodeToString(input)
                // Strip equality signs.  I've ported this over from the shell script with which I onboarded Yolt on production.
                // Java's RFC 4648 encoder doesn't do this by default but it is permitted (and as I recall, necessary) to
                // get the request to go through to Rabobank.
                //
                // This is allowed as per RFC 4648:
                //
                // From https://tools.ietf.org/html/rfc4648#section-3.2
                //
                // In some circumstances, the use of padding ("=") in base-encoded data
                // is not required or used.  In the general case, when assumptions about
                // the size of transported data cannot be made, padding is required to
                // yield correct decoded data.
                //
                // Implementations MUST include appropriate pad characters at the end of
                // encoded data unless the specification referring to this document
                // explicitly states otherwise.
                //
                // The base64 and base32 alphabets use padding, as described below in
                // sections 4 and 6, but the base16 alphabet does not need it; see
                // section 8.
                //
                //
                // From the JWS spec at https://tools.ietf.org/html/rfc7515#section-2
                //
                // Base64url Encoding
                //   Base64 encoding using the URL- and filename-safe character set
                //   defined in Section 5 of RFC 4648 [RFC4648], with all trailing '='
                //   characters omitted (as permitted by Section 3.2) and without the
                //   inclusion of any line breaks, whitespace, or other additional
                //   characters.  Note that the base64url encoding of the empty octet
                //   sequence is the empty string.  (See Appendix C for notes on
                //   implementing base64url encoding without padding.)
                //
                .replaceAll("=", "");
    }

    private static String b64URLenc(String input) {
        return b64URLenc(input.getBytes(StandardCharsets.UTF_8));
    }

}

@RequiredArgsConstructor
class Request {
    // protected is a modifier keyword, hence the underscore
    private final String protected_;
    private final String payload;
    private final String signature;

    public String getProtected() {
        return protected_;
    }

    public String getPayload() {
        return payload;
    }

    public String getSignature() {
        return signature;
    }
}
