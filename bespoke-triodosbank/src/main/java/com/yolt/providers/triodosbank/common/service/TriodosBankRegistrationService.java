package com.yolt.providers.triodosbank.common.service;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.triodosbank.common.model.http.RegistrationRequest;
import com.yolt.providers.triodosbank.common.model.http.RegistrationResponse;
import com.yolt.providers.triodosbank.common.model.http.RegistrationTokenResponse;
import com.yolt.providers.triodosbank.common.rest.TriodosBankHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;
import static com.yolt.providers.triodosbank.common.auth.TriodosBankAuthenticationMeans.CLIENT_ID_STRING_NAME;
import static com.yolt.providers.triodosbank.common.auth.TriodosBankAuthenticationMeans.CLIENT_SECRET_STRING_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriodosBankRegistrationService {

    public Map<String, BasicAuthenticationMean> register(TriodosBankHttpClient httpClient,
                                                         List<String> baseClientRedirectUrl,
                                                         String providerIdentifier) {
        Map<String, BasicAuthenticationMean> registeredAuthMeans = new HashMap<>();
        try {
            RegistrationTokenResponse tokenResponse = httpClient.getRegistrationToken();
            validateRegistrationTokenResponse(tokenResponse, providerIdentifier);

            RegistrationResponse registrationResponse = httpClient.getRegistrationResponse(
                    tokenResponse.getRegistrationUrl(),
                    createRegistrationRequest(tokenResponse.getAccessToken(), baseClientRedirectUrl));

            if (Objects.nonNull(registrationResponse)) {
                registeredAuthMeans.put(CLIENT_ID_STRING_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), registrationResponse.getClientId()));
                registeredAuthMeans.put(CLIENT_SECRET_STRING_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), registrationResponse.getClientSecret()));
                log.info("Registration succeeded");
            }
        } catch (HttpStatusCodeException e) {
            throw new AutoOnboardingException(providerIdentifier, String.format("Registration failed. HTTP status: %s", e.getStatusCode()), e);
        }
        return registeredAuthMeans;
    }

    private void validateRegistrationTokenResponse(RegistrationTokenResponse tokenResponse, String providerIdentifier) {
        if (Objects.isNull(tokenResponse)) {
            throw new IllegalStateException("Token response for registration is empty");
        }
        if (StringUtils.isEmpty(tokenResponse.getAccessToken())) {
            String errorMessage = "Initial access token for registration is empty";
            throw new AutoOnboardingException(providerIdentifier, errorMessage, new IllegalStateException(errorMessage));
        }
    }

    private RegistrationRequest createRegistrationRequest(String accessToken, List<String> baseClientRedirectUrl) {
        return RegistrationRequest.builder()
                .redirectUris(baseClientRedirectUrl)
                .registrationToken(accessToken)
                .sectorIdentifierUri("")
                .build();
    }
}
