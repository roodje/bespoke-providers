package com.yolt.providers.stet.generic.service.registration;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;

import java.util.Map;

public interface RegistrationService {

    Map<String, BasicAuthenticationMean> register(HttpClient httpClient, RegistrationRequest registrationRequest);
}
