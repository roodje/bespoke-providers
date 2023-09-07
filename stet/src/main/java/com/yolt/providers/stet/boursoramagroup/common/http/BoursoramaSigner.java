package com.yolt.providers.stet.boursoramagroup.common.http;

import com.yolt.providers.common.cryptography.CavageHttpSigning;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * this class was introduced because {@link CavageHttpSigning} adds whitespaces between signature header parameters
 */
@RequiredArgsConstructor
public class BoursoramaSigner {

    private final Signer signer;
    private final UUID privateKid;
    private final SignatureAlgorithm signatureAlgorithm;

    public String signHeaders(@NonNull final Map<String, String> headers,
                              @NonNull final String headerKeyId,
                              @NonNull final String httpMethod,
                              @NonNull final String httpEndpoint) {
        final List<String> headersKeys = new LinkedList<>();
        final List<String> headersData = new LinkedList<>();

        headersKeys.add("(request-target)");
        headersData.add(String.format("(request-target): %s %s", httpMethod.toLowerCase(), httpEndpoint));

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