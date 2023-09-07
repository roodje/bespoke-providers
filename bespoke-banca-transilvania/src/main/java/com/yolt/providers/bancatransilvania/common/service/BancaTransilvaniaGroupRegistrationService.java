package com.yolt.providers.bancatransilvania.common.service;

import com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupAuthenticationMeans;
import com.yolt.providers.bancatransilvania.common.domain.model.registration.RegistrationRequest;
import com.yolt.providers.bancatransilvania.common.domain.model.registration.RegistrationResponse;
import com.yolt.providers.bancatransilvania.common.http.BancaTransilvaniaGroupHttpClient;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import lombok.RequiredArgsConstructor;

import java.util.*;

import static com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupAuthenticationMeansProducerV1.CLIENT_ID_NAME;
import static com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupAuthenticationMeansProducerV1.CLIENT_SECRET_NAME;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;

@RequiredArgsConstructor
public class BancaTransilvaniaGroupRegistrationService {

    public Map<String, BasicAuthenticationMean> register(BancaTransilvaniaGroupHttpClient httpClient,
                                                         BancaTransilvaniaGroupAuthenticationMeans authMeans,
                                                         UrlAutoOnboardingRequest request) {
        Map<String, BasicAuthenticationMean> basicAuthMeans = request.getAuthenticationMeans();
        if (isAlreadyRegistered(basicAuthMeans)) {
            return basicAuthMeans;
        }
        RegistrationRequest registrationRequest = createRegistrationRequest(authMeans, request.getRedirectUrls());
        RegistrationResponse registrationResponse = httpClient.postRegistration(registrationRequest);

        if (Objects.isNull(registrationResponse)) {
            throw new IllegalStateException("Registration failed. Registration response is null");
        }
        return prepareBasicAuthMeans(basicAuthMeans, registrationResponse);
    }

    private boolean isAlreadyRegistered(Map<String, BasicAuthenticationMean> basicAuthMeans) {
        return basicAuthMeans.containsKey(CLIENT_ID_NAME) && basicAuthMeans.containsKey(CLIENT_SECRET_NAME);
    }

    private RegistrationRequest createRegistrationRequest(BancaTransilvaniaGroupAuthenticationMeans authMeans,
                                                          List<String> redirectUrls) {
        return RegistrationRequest.builder()
                .redirectUris(redirectUrls)
                .clientName(authMeans.getClientName())
                .companyName(authMeans.getClientCompanyName())
                .companyUrl(authMeans.getClientWebsiteUrl())
                .contactPerson(authMeans.getClientContactPerson())
                .emailAddress(authMeans.getClientEmail())
                .phoneNumber(authMeans.getClientPhoneNumber())
                .build();
    }

    private Map<String, BasicAuthenticationMean> prepareBasicAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans,
                                                                       RegistrationResponse registrationResponse) {
        Map<String, BasicAuthenticationMean> filledBasicAuthMeans = new HashMap<>(basicAuthMeans);
        filledBasicAuthMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), registrationResponse.getClientId()));
        filledBasicAuthMeans.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), registrationResponse.getClientSecret()));
        return filledBasicAuthMeans;
    }
}
