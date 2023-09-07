package com.yolt.providers.dkbgroup.common.dynamicflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.dkbgroup.common.auth.AuthenticationService;
import com.yolt.providers.dkbgroup.common.http.DKBGroupHttpClient;
import com.yolt.providers.dkbgroup.common.model.DKBAccessMeans;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.ExplanationField;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.TextField;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;

import static com.yolt.providers.dkbgroup.common.DKBGroupProvider.EXPLANATION;

@RequiredArgsConstructor
public class DKBGroupDynamicFlowHandler implements DynamicFlowHandler {

    private static final Duration OTP_STEP_EXPIRY_DURATION = Duration.ofMinutes(5);
    private static final String OTP = "otp";
    private static final int TOKEN_EXPIRATION_TIME = 300;

    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public AccessMeansOrStepDTO handle(final UrlCreateAccessMeansRequest urlCreateAccessMeans,
                                       final DKBGroupHttpClient httpClient) throws TokenInvalidException {
        if (urlCreateAccessMeans.getFilledInUserSiteFormValues().getValueMap().containsKey(OTP)) {
            return authoriseConsent(urlCreateAccessMeans, httpClient);
        }
        return createConsentAndReturnOTPFormStep(urlCreateAccessMeans, httpClient);
    }

    private AccessMeansOrStepDTO authoriseConsent(final UrlCreateAccessMeansRequest urlCreateAccessMeans,
                                                  final DKBGroupHttpClient httpClient) throws TokenInvalidException {
        DKBAccessMeans accessMeans;
        try {
            accessMeans = objectMapper.readValue(urlCreateAccessMeans.getProviderState(), DKBAccessMeans.class);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Could not deserialize access means.");
        }
        var otp = urlCreateAccessMeans.getFilledInUserSiteFormValues().get(OTP);
        authenticationService.authoriseConsent(httpClient, otp, urlCreateAccessMeans.getPsuIpAddress(), accessMeans);
        var now = Instant.now(clock);
        var updated = Date.from(now);
        var expirationTime = Date.from(now.plusSeconds(TOKEN_EXPIRATION_TIME));
        return new AccessMeansOrStepDTO(new AccessMeansDTO(urlCreateAccessMeans.getUserId(), urlCreateAccessMeans.getProviderState(), updated, expirationTime));
    }

    private AccessMeansOrStepDTO createConsentAndReturnOTPFormStep(final UrlCreateAccessMeansRequest urlCreateAccessMeans,
                                                                   final DKBGroupHttpClient httpClient) throws TokenInvalidException {
        String embeddedToken = authenticationService.getEmbeddedToken(httpClient, urlCreateAccessMeans.getFilledInUserSiteFormValues());
        String psuIpAddress = urlCreateAccessMeans.getPsuIpAddress();
        String consentId = authenticationService.getConsentId(httpClient, embeddedToken, psuIpAddress);
        var startSCAProcessResponse = authenticationService.startSCAProcess(httpClient, embeddedToken, psuIpAddress, consentId);
        var accessMeans = new DKBAccessMeans(embeddedToken, consentId, startSCAProcessResponse.getAuthorisationId());
        String providerState;
        try {
            providerState = objectMapper.writeValueAsString(accessMeans);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Could not serialize access means.");
        }
        var otp = new TextField(OTP, OTP, 20, 50, false, false);
        String explanation = authenticationService.chooseSCAMethod(httpClient, embeddedToken, psuIpAddress, consentId, startSCAProcessResponse);
        var explanationField = new ExplanationField(EXPLANATION, EXPLANATION, explanation);
        var form = new Form(Collections.singletonList(otp), explanationField, null);
        var fromStep = new FormStep(form, EncryptionDetails.noEncryption(), Instant.now(clock).plus(OTP_STEP_EXPIRY_DURATION), providerState);
        return new AccessMeansOrStepDTO(fromStep);
    }
}
