package com.yolt.providers.yoltprovider.pis.sepa;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Note that for SEPA PIS YoltProvider we are using somewhat simplified version of signature header of initially described
 * approach in official documentation - https://tools.ietf.org/pdf/draft-cavage-http-signatures-10.pdf
 * <p>
 * SignatureDTO header structure is as follows:
 * signature="keyId="...",algorithm="...",signature="base64(signed(clientId,digest))""
 * - keyId - id of public key with which we are going to verify signature
 * - algorithm - algorithm used when creating signature
 * - signature - base64 encoded signature of clientId and digest
 * - digest - digest of request body
 */
@RequiredArgsConstructor
public class SignatureDTO {

    private static final String KEY_ID_NAME = "keyId";
    private static final String ALGORITHM_NAME = "algorithm";
    private static final String SIGNATURE_NAME = "signature";

    private final UUID keyId;
    private final String signature;

    public String getSignatureHeader() {
        final String keyIdString = keyId != null ? formPair(KEY_ID_NAME, keyId.toString()) : null;
        final String algorithmString = formPair(ALGORITHM_NAME, SigningUtils.SIGNATURE_ALGORITHM.getJvmAlgorithm());
        final String signatureString = !StringUtils.isEmpty(signature) ? formPair(SIGNATURE_NAME, signature) : null;

        final String signatureHeader = Stream.of(keyIdString, algorithmString, signatureString)
                .filter(value -> !StringUtils.isEmpty(value))
                .collect(Collectors.joining(","));

        return StringUtils.isEmpty(signatureHeader) ? null : "\"" + signatureHeader + "\"";
    }

    private static String formPair(final String key, final String value) {
        return key + "=\"" + value + "\"";
    }
}
