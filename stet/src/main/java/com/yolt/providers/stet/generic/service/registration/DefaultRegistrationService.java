package com.yolt.providers.stet.generic.service.registration;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.service.registration.rest.RegistrationRestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultRegistrationService implements RegistrationService {

    protected final RegistrationRestClient registrationRestClient;
    protected final ExtendedAuthenticationMeansSupplier authMeansSupplier;

    @Override
    public Map<String, BasicAuthenticationMean> register(HttpClient httpClient, RegistrationRequest registrationRequest) {
        if (StringUtils.hasText(registrationRequest.getAuthMeans().getClientId())) {
            return enhanceRegistrationForAlreadyRegisteredAuthMeans(httpClient, registrationRequest);
        }
        ObjectNode registrationResponse = registrationRestClient.registerClient(httpClient, registrationRequest);
        Map<String, String> registeredAuthMeans = authMeansSupplier.getRegisteredAuthMeans(registrationResponse);

        return authMeansSupplier.getAutoConfiguredTypedAuthMeans()
                .entrySet()
                .stream()
                .filter(entry -> registeredAuthMeans.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                        new BasicAuthenticationMean(entry.getValue().getType(), registeredAuthMeans.get(entry.getKey()))));
    }

    protected Map<String, BasicAuthenticationMean> enhanceRegistrationForAlreadyRegisteredAuthMeans(HttpClient httpClient, //NOSONAR It is provided to customize logic for already registered means
                                                                                                    RegistrationRequest registrationRequest) {
        return Collections.emptyMap();
    }
}
