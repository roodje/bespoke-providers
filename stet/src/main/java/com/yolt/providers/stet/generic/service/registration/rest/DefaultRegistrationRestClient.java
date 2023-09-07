package com.yolt.providers.stet.generic.service.registration.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.mapper.registration.RegistrationRequestMapper;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.service.registration.rest.error.DefaultRegistrationHttpErrorHandler;
import com.yolt.providers.stet.generic.service.registration.rest.header.RegistrationHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.REGISTER;

@RequiredArgsConstructor
public class DefaultRegistrationRestClient implements RegistrationRestClient {

    protected final RegistrationHttpHeadersFactory headersFactory;
    protected final RegistrationRequestMapper registrationRequestMapper;
    protected final DefaultProperties properties;
    protected final DefaultRegistrationHttpErrorHandler errorHandler;

    public DefaultRegistrationRestClient(RegistrationHttpHeadersFactory headersFactory,
                                         RegistrationRequestMapper registrationRequestMapper,
                                         DefaultProperties properties) {
        this.headersFactory = headersFactory;
        this.registrationRequestMapper = registrationRequestMapper;
        this.properties = properties;
        this.errorHandler = new DefaultRegistrationHttpErrorHandler();
    }

    @Override
    public ObjectNode registerClient(HttpClient httpClient, RegistrationRequest registrationRequest) {
        String url = properties.getRegistrationUrl();
        HttpMethod method = HttpMethod.POST;
        String prometheusPath = REGISTER;
        Object body = registrationRequestMapper.mapToRegistrationRequest(registrationRequest);
        HttpHeaders headers = headersFactory.createRegistrationHttpHeaders(registrationRequest, body, method, url);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        ExecutionInfo executionInfo = new ExecutionInfo(url, method, headers, prometheusPath);
        return errorHandler.executeAndHandle(() -> httpClient.exchangeForBody(url, method, entity, prometheusPath, ObjectNode.class), executionInfo);
    }

    @Override
    public ObjectNode updateRegistration(HttpClient httpClient, String url, RegistrationRequest registrationRequest) {
        HttpMethod method = HttpMethod.PUT;
        String prometheusPath = "update_registration";
        Object body = registrationRequestMapper.mapToUpdateRegistrationRequest(registrationRequest);
        HttpHeaders headers = headersFactory.createRegistrationHttpHeaders(registrationRequest, body, method, url);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        ExecutionInfo executionInfo = new ExecutionInfo(url, method, headers, prometheusPath);
        return errorHandler.executeAndHandle(() -> httpClient.exchangeForBody(url, method, entity, prometheusPath, ObjectNode.class), executionInfo);
    }
}
