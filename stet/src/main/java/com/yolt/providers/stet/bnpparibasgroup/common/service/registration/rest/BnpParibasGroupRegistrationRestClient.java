package com.yolt.providers.stet.bnpparibasgroup.common.service.registration.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.registration.BnpParibasGroupRegistrationRequestMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.registration.BnpParibasRegistrationHttpHeadersFactory;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.service.registration.rest.DefaultRegistrationRestClient;
import com.yolt.providers.stet.generic.service.registration.rest.header.RegistrationHttpHeadersFactory;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public class BnpParibasGroupRegistrationRestClient extends DefaultRegistrationRestClient {

    public BnpParibasGroupRegistrationRestClient(RegistrationHttpHeadersFactory headersFactory,
                                                 BnpParibasGroupRegistrationRequestMapper registrationRequestMapper,
                                                 DefaultProperties properties) {
        super(headersFactory, registrationRequestMapper, properties);
    }

    @SneakyThrows
    @Override
    public ObjectNode updateRegistration(HttpClient httpClient, String url, RegistrationRequest registrationRequest) {
        if (!(headersFactory instanceof BnpParibasRegistrationHttpHeadersFactory)) {
            throw new IllegalStateException("Bnp Paribas updateRegistration requires headersFactory.");
        }
        var bnpParibasRegistrationHttpHeadersFactory = (BnpParibasRegistrationHttpHeadersFactory) headersFactory;
        HttpMethod method = HttpMethod.PUT;
        var fullUrl = url + "/" + registrationRequest.getAuthMeans().getClientId();
        String prometheusPath = "update_registration";
        Object body = registrationRequestMapper.mapToRegistrationRequest(registrationRequest);
        HttpHeaders headers = bnpParibasRegistrationHttpHeadersFactory.createUpdateRegistrationHttpHeaders(registrationRequest, body, method, fullUrl);

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        ExecutionInfo executionInfo = new ExecutionInfo(fullUrl, method, headers, prometheusPath);
        return errorHandler.executeAndHandle(() -> httpClient.exchangeForBody(fullUrl, method, entity, prometheusPath, ObjectNode.class), executionInfo);
    }

}
