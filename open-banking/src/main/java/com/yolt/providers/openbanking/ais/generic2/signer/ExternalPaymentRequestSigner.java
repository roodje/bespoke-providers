package com.yolt.providers.openbanking.ais.generic2.signer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwx.HeaderParameterNames;

@RequiredArgsConstructor
public class ExternalPaymentRequestSigner implements PaymentRequestSigner {

    public static final String CRITICAL_HEADER_OPENBANKING_IAT = "http://openbanking.org.uk/iat";
    public static final String CRITICAL_HEADER_OPENBANKING_ISS = "http://openbanking.org.uk/iss";
    public static final String CRITICAL_HEADER_OPENBANKING_TAN = "http://openbanking.org.uk/tan";
    public static final String OPENBANKING_ORG_UK = "openbanking.org.uk";

    @Getter
    private final ObjectMapper objectMapper;
    @Getter
    private final String jwsAlgorithm;

    @Override
    public <T> String createRequestSignature(T request, DefaultAuthMeans authMeans, Signer signer) {
        try {
            String requestAsJson = objectMapper.writeValueAsString(request);
            JsonWebSignature jws = new JsonWebSignature();
            jws.setAlgorithmHeaderValue(jwsAlgorithm);
            jws.getHeaders().setObjectHeaderValue(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD, false);
            jws.getHeaders().setObjectHeaderValue(CRITICAL_HEADER_OPENBANKING_IAT, NumericDate.now().getValue());
            jws.getHeaders().setObjectHeaderValue(CRITICAL_HEADER_OPENBANKING_TAN, OPENBANKING_ORG_UK);
            String iss = authMeans.getOrganizationId() + "/" + authMeans.getSoftwareId();
            jws.getHeaders().setObjectHeaderValue(CRITICAL_HEADER_OPENBANKING_ISS, iss);
            jws.setCriticalHeaderNames(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD, CRITICAL_HEADER_OPENBANKING_IAT,
                    CRITICAL_HEADER_OPENBANKING_ISS, CRITICAL_HEADER_OPENBANKING_TAN);
            jws.setPayload(requestAsJson);
            jws.setKeyIdHeaderValue(authMeans.getSigningKeyIdHeader());
            adjustJWSHook(jws, authMeans);

            return signer.sign(jws, authMeans.getSigningPrivateKeyId(), SignatureAlgorithm.findByJsonSignatureAlgorithmOrThrowException(jwsAlgorithm)).getDetachedContentCompactSerialization();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error during parsing request to JSON.");
        }
    }

    protected void adjustJWSHook(JsonWebSignature jws, DefaultAuthMeans authMeans) {
        //This method allows to change JWS in extending classes with bank specific changes
    }
}
