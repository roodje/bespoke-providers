package com.yolt.providers.stet.bnpparibasgroup.common.service.registration;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.bnpparibasgroup.common.service.registration.rest.BnpParibasGroupRegistrationRestClient;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.service.registration.DefaultRegistrationService;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;

@Slf4j
public class BnpParibasGroupRegistrationService extends DefaultRegistrationService {

    private final DefaultProperties properties;

    public BnpParibasGroupRegistrationService(BnpParibasGroupRegistrationRestClient registrationRestClient,
                                              ExtendedAuthenticationMeansSupplier authMeansSupplier,
                                              DefaultProperties properties) {
        super(registrationRestClient, authMeansSupplier);
        this.properties = properties;
    }

    @Override
    protected Map<String, BasicAuthenticationMean> enhanceRegistrationForAlreadyRegisteredAuthMeans(HttpClient httpClient, RegistrationRequest registrationRequest) {
        registrationRestClient.updateRegistration(httpClient, properties.getRegistrationUrl(), registrationRequest);
        log.warn("Update in BNP Paribas Group does not return updated object. Approval required on bank's side. Double check the response in RDD");
        return Collections.emptyMap();
    }
}
