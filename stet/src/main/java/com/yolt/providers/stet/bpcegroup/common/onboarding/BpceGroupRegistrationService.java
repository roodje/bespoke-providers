package com.yolt.providers.stet.bpcegroup.common.onboarding;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.service.registration.DefaultRegistrationService;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.service.registration.rest.RegistrationRestClient;

import java.util.Map;
import java.util.stream.Collectors;

public class BpceGroupRegistrationService extends DefaultRegistrationService {

    public BpceGroupRegistrationService(RegistrationRestClient registrationRestClient, ExtendedAuthenticationMeansSupplier authMeansSupplier) {
        super(registrationRestClient, authMeansSupplier);
    }

    @Override
    protected Map<String, BasicAuthenticationMean> enhanceRegistrationForAlreadyRegisteredAuthMeans(HttpClient httpClient, RegistrationRequest registrationRequest) {
        ObjectNode registrationResponse = registrationRestClient.updateRegistration(httpClient, "", registrationRequest);
        Map<String, String> registeredAuthMeans = authMeansSupplier.getRegisteredAuthMeans(registrationResponse);

        return authMeansSupplier.getAutoConfiguredTypedAuthMeans()
                .entrySet()
                .stream()
                .filter(entry -> registeredAuthMeans.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                        new BasicAuthenticationMean(entry.getValue().getType(), registeredAuthMeans.get(entry.getKey()))));
    }
}
