package com.yolt.providers.openbanking.ais.revolutgroup.common.signer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.JsonWebSignature;

@RequiredArgsConstructor
public class RevolutPaymentRequestSigner implements PaymentRequestSigner {

    public static final String CRITICAL_HEADER_OPENBANKING_TAN = "http://openbanking.org.uk/tan";

    private final ObjectMapper objectMapper;
    private final String jwsAlgorithm;
    private final String jwksRootDomain;

    @Override
    public <T> String createRequestSignature(T request, DefaultAuthMeans authMeans, Signer signer) {
        try {
            String requestAsJson = objectMapper.writeValueAsString(request);
            JsonWebSignature jws = new JsonWebSignature();
            jws.setAlgorithmHeaderValue(jwsAlgorithm);
            jws.getHeaders().setObjectHeaderValue(CRITICAL_HEADER_OPENBANKING_TAN, jwksRootDomain);
            jws.setCriticalHeaderNames(CRITICAL_HEADER_OPENBANKING_TAN);
            jws.setPayload(requestAsJson);
            jws.setKeyIdHeaderValue(authMeans.getSigningKeyIdHeader());

            return signer.sign(jws, authMeans.getSigningPrivateKeyId(), SignatureAlgorithm.findByJsonSignatureAlgorithmOrThrowException(jwsAlgorithm))
                    .getDetachedContentCompactSerialization();
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException("Error during parsing request to JSON.");
        }
    }
}
