package com.yolt.providers.monorepogroup.chebancagroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.NonNull;
import lombok.Value;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Value
public class ChebancaGroupCavageHttpSigning {

    @NonNull Signer signer;
    @NonNull UUID privateKid;
    @NonNull SignatureAlgorithm signatureAlgorithm;

    /**
     * Helper method for signing requests according to cavage-http-signatures specification.
     * <p>
     * https://datatracker.ietf.org/doc/draft-cavage-http-signatures/?include_text=1
     *
     * @param headers      all headers that need to be (and will be) signed.
     * @param headerKeyId  header value for signature.keyId.
     * @param httpMethod   http method of request.
     * @param httpEndpoint http endpoint (path) of request.
     * @return signed (delegated to {@link Signer#sign(byte[], UUID, SignatureAlgorithm)}) Signature-header value.
     */
    public String signHeaders(@NonNull final Map<String, String> headers,
                              @NonNull final String headerKeyId,
                              @NonNull final String httpMethod,
                              @NonNull final String httpEndpoint) {
        final List<String> headersKeys = new LinkedList<>();
        final List<String> headersData = new LinkedList<>();

        // Signing header specific addition.
        headersKeys.add("(request-target)");
        headersData.add(String.format("(request-target): %s %s", httpMethod.toLowerCase(), httpEndpoint));

        // Only loop once and keep header order the same.
        headers.forEach((key, value) -> {
            headersKeys.add(key.toLowerCase());
            headersData.add(key.toLowerCase() + ": " + value);
        });

        final String payload = String.join("\n", headersData);
        final String signature = signer.sign(payload.getBytes(), privateKid, signatureAlgorithm);

        return String.format("keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"",
            headerKeyId, signatureAlgorithm.getHttpSignatureAlgorithm(), String.join(" ", headersKeys), signature);
    }
}
