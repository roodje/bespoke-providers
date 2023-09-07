package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticStandingOrderConsentResponse1;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

public class YoltBankUkAuthorizationUrlPeriodicPaymentExtractor implements PaymentAuthorizationUrlExtractor<InitiatePaymentConsentResponse, YoltBankUkInitiatePeriodicPaymentPreExecutionResult> {

    private static final String CLIENT_ID = "client_id";
    private static final String TOKEN = "token";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String STATE = "state";
    private static final String CONSENT_ID = "consent_id";
    private final ObjectMapper objectMapper;

    public YoltBankUkAuthorizationUrlPeriodicPaymentExtractor(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String extractAuthorizationUrl(InitiatePaymentConsentResponse initiatePaymentConsentResponse, YoltBankUkInitiatePeriodicPaymentPreExecutionResult result) {
        if (initiatePaymentConsentResponse == null) {
            return createUrl(result);
        }
        OBWriteDomesticStandingOrderConsentResponse1 consentResponse = readConsentResponse(initiatePaymentConsentResponse);
        return createUrl(result, consentResponse.getData().getConsentId(), initiatePaymentConsentResponse.getConsentUri());
    }

    private OBWriteDomesticStandingOrderConsentResponse1 readConsentResponse(final InitiatePaymentConsentResponse initiatePaymentConsentResponse) {
        try {
            return objectMapper.readValue(initiatePaymentConsentResponse.getPaymentConsent(), OBWriteDomesticStandingOrderConsentResponse1.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private String createUrl(YoltBankUkInitiatePeriodicPaymentPreExecutionResult result, String consentId, String consentUri) {
        JsonWebSignature jws = createTemporaryPaymentConsentToken(
                result.getAuthenticationMeans().getClientId(),
                consentId,
                result.getAuthenticationMeans().getPublicKid());
        String signedJws = result.getSigner().sign(jws, result.getAuthenticationMeans().getSigningKid(),
                SignatureAlgorithm.findByJsonSignatureAlgorithmOrThrowException(AlgorithmIdentifiers.RSA_USING_SHA256)).getCompactSerialization();

        return UriComponentsBuilder
                .fromUriString(consentUri)
                .queryParam(CLIENT_ID, result.getAuthenticationMeans().getClientId().toString())
                .queryParam(TOKEN, signedJws)
                .queryParam(REDIRECT_URI, result.getBaseClientRedirectUrl())
                .queryParam(STATE, result.getState())
                .build()
                .toUriString();
    }

    private String createUrl(YoltBankUkInitiatePeriodicPaymentPreExecutionResult result) {
        return createUrl(result, result.getConsentId(), result.getConsentUri());
    }

    private JsonWebSignature createTemporaryPaymentConsentToken(UUID clientId, String consentId, UUID publicKeyId) {
        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setClaim(CONSENT_ID, consentId);
        jwtClaims.setClaim(CLIENT_ID, clientId);
        jwtClaims.setIssuedAtToNow();
        JsonWebSignature newJws = new JsonWebSignature();

        newJws.setPayload(jwtClaims.toJson());
        newJws.setKeyIdHeaderValue(publicKeyId.toString());
        newJws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

        return newJws;
    }
}
