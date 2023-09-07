package com.yolt.providers.unicredit.common.service;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans;
import com.yolt.providers.unicredit.common.config.UniCreditRegistrationProperties;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditAuthMeansMapper;
import com.yolt.providers.unicredit.common.dto.RegisterRequestDTO;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClient;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClientFactory;
import com.yolt.providers.unicredit.common.util.ProviderInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans.REGISTRATION_STATUS;

@RequiredArgsConstructor
public class UniCreditAutoOnboardingServiceV1 implements UniCreditAutoOnboardingService {

    private final UniCreditHttpClientFactory httpClientFactory;
    private final UniCreditAuthMeansMapper authMeansMapper;
    private final UniCreditRegistrationProperties registrationProperties;

    @Override
    public Map<String, BasicAuthenticationMean> autoConfigureMeans(final UrlAutoOnboardingRequest urlAutoOnboardingRequest, final ProviderInfo providerInfo) throws TokenInvalidException {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        if (!isAlreadyRegistered(authenticationMeans)) {
            return registerClient(urlAutoOnboardingRequest, providerInfo, authenticationMeans);
        }
        return authenticationMeans;
    }

    private Map<String, BasicAuthenticationMean> registerClient(final UrlAutoOnboardingRequest urlAutoOnboardingRequest, final ProviderInfo providerInfo, final Map<String, BasicAuthenticationMean> authenticationMeans) throws TokenInvalidException {
        // Note: if we use different cert, but with the same TPP ID,
        // then UC accepts it and there is no need to register that certificate separately (we will receive 404 for API call in that situation)
        UniCreditAuthMeans authMeans = authMeansMapper.fromBasicAuthenticationMeans(urlAutoOnboardingRequest.getAuthenticationMeans(), providerInfo.getIdentifier());
        UniCreditHttpClient httpClient = httpClientFactory.createHttpClient(authMeans, urlAutoOnboardingRequest.getRestTemplateManager(), providerInfo.getIdentifier(), registrationProperties.getBaseUrl());
        ResponseEntity<String> registrationResponse = httpClient.register(RegisterRequestDTO.builder()
                .userEmail(authMeans.getClientEmail())
                .build());
        validateRegistration(registrationResponse, providerInfo.getIdentifier());
        authenticationMeans.put(REGISTRATION_STATUS, new BasicAuthenticationMean(TypedAuthenticationMeans.TPP_ID.getType(), "REGISTERED"));
        return authenticationMeans;
    }

    private void validateRegistration(final ResponseEntity<String> registrationResponse, final String providerIdentifier) {
        if (!registrationResponse.getStatusCode().is2xxSuccessful()) {
            throw new AutoOnboardingException(providerIdentifier,
                    String.format("Auto-onboarding process failed with code %s", registrationResponse.getStatusCodeValue()), null);
        }
    }

    private boolean isAlreadyRegistered(final Map<String, BasicAuthenticationMean> authenticationMeans) {
        return authenticationMeans.containsKey(REGISTRATION_STATUS);
    }
}
