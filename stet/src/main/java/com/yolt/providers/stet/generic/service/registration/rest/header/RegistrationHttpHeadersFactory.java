package com.yolt.providers.stet.generic.service.registration.rest.header;

import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public interface RegistrationHttpHeadersFactory {

    <T extends RegistrationRequest> HttpHeaders createRegistrationHttpHeaders(T registerRequest, Object body, HttpMethod httpMethod, String url);
}
