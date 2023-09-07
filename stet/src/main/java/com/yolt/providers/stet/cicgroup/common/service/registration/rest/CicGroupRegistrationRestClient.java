package com.yolt.providers.stet.cicgroup.common.service.registration.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.cicgroup.common.dto.CicGroupRegistrationRequestDTO;
import com.yolt.providers.stet.cicgroup.common.mapper.registration.CicGroupRegistrationRequestMapper;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.service.registration.rest.DefaultRegistrationRestClient;
import com.yolt.providers.stet.generic.service.registration.rest.header.RegistrationHttpHeadersFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public class CicGroupRegistrationRestClient extends DefaultRegistrationRestClient {

    private final CicGroupRegistrationRequestMapper registrationRequestMapper;

    public CicGroupRegistrationRestClient(RegistrationHttpHeadersFactory headersFactory,
                                          CicGroupRegistrationRequestMapper registrationRequestMapper,
                                          DefaultProperties properties) {
        super(headersFactory, registrationRequestMapper, properties);
        this.registrationRequestMapper = registrationRequestMapper;
    }

    @Override
    public ObjectNode updateRegistration(HttpClient httpClient, String url, RegistrationRequest registrationRequest) {
        HttpMethod method = HttpMethod.PUT;
        String prometheusPath = "update_registration";
        CicGroupRegistrationRequestDTO currentRegistration = readRegistration(httpClient, url, registrationRequest);

        Object body = registrationRequestMapper.mapToUpdateRegistrationRequest(registrationRequest, currentRegistration);
        HttpHeaders headers = headersFactory.createRegistrationHttpHeaders(registrationRequest, body, method, url);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        ExecutionInfo executionInfo = new ExecutionInfo(url, method, headers, prometheusPath);
        return errorHandler.executeAndHandle(() -> httpClient.exchangeForBody(url, method, entity, prometheusPath, ObjectNode.class), executionInfo);
    }

    public CicGroupRegistrationRequestDTO readRegistration(HttpClient httpClient, String url, RegistrationRequest registrationRequest) {
        HttpMethod method = HttpMethod.GET;
        String prometheusPath = "read_registration";
        HttpHeaders headers = headersFactory.createRegistrationHttpHeaders(registrationRequest, null, method, url);
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        ExecutionInfo executionInfo = new ExecutionInfo(url, method, headers, prometheusPath);
        return errorHandler.executeAndHandle(() -> httpClient.exchangeForBody(url, method, entity, prometheusPath, CicGroupRegistrationRequestDTO.class), executionInfo);
    }

    public void deleteRegistration(HttpClient httpClient, String url) {
        HttpMethod method = HttpMethod.DELETE;
        String prometheusPath = "delete_registration";
        HttpHeaders headers = headersFactory.createRegistrationHttpHeaders(null, null, method, url);
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        ExecutionInfo executionInfo = new ExecutionInfo(url, method, headers, prometheusPath);
        errorHandler.executeAndHandle(() -> httpClient.exchange(url, method, entity, prometheusPath, Void.class), executionInfo);
    }
}
