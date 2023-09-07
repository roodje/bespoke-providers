package com.yolt.providers.stet.cicgroup.common.service.registration;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.cicgroup.common.service.registration.rest.CicGroupRegistrationRestClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.service.registration.DefaultRegistrationService;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.stream.Collectors;

public class CicGroupRegistrationService extends DefaultRegistrationService {

    private final DefaultProperties properties;
    private final CicGroupRegistrationRestClient registrationRestClient;

    public CicGroupRegistrationService(CicGroupRegistrationRestClient registrationRestClient,
                                       ExtendedAuthenticationMeansSupplier authenticationMeansSupplier,
                                       DefaultProperties properties) {
        super(registrationRestClient, authenticationMeansSupplier);
        this.properties = properties;
        this.registrationRestClient = registrationRestClient;
    }

    @Override
    protected Map<String, BasicAuthenticationMean> enhanceRegistrationForAlreadyRegisteredAuthMeans(HttpClient httpClient,
                                                                                                    RegistrationRequest registrationRequest) {
        DefaultAuthenticationMeans authMeans = registrationRequest.getAuthMeans();

        String url = UriComponentsBuilder.fromUriString(properties.getRegistrationUrl())
                .path("/" + authMeans.getClientId())
                .toUriString();

        ObjectNode registrationResponse = registrationRestClient.updateRegistration(httpClient, url, registrationRequest);
        Map<String, String> registeredAuthMeans = authMeansSupplier.getRegisteredAuthMeans(registrationResponse);

        return authMeansSupplier.getAutoConfiguredTypedAuthMeans()
                .entrySet()
                .stream()
                .filter(entry -> registeredAuthMeans.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                        new BasicAuthenticationMean(entry.getValue().getType(), registeredAuthMeans.get(entry.getKey()))));
    }

    public void deleteRegistration(HttpClient httpClient, DefaultAuthenticationMeans authMeans) {
        String url = UriComponentsBuilder.fromUriString(properties.getRegistrationUrl())
                .path("/" + authMeans.getClientId())
                .toUriString();

        registrationRestClient.deleteRegistration(httpClient, url);
    }
}
