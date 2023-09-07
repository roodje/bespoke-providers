package com.yolt.providers.stet.generic.service.registration.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;

public interface RegistrationRestClient {

    ObjectNode registerClient(HttpClient httpClient, RegistrationRequest registrationRequest);

    ObjectNode updateRegistration(HttpClient httpClient, String registrationEndpoint, RegistrationRequest registrationRequest);
}
