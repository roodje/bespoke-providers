package com.yolt.providers.stet.bpcegroup.common.onboarding;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.mapper.registration.RegistrationRequestMapper;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.service.registration.rest.DefaultRegistrationRestClient;
import com.yolt.providers.stet.generic.service.registration.rest.header.RegistrationHttpHeadersFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Collections;

public class BpceGroupRegistrationRestClient extends DefaultRegistrationRestClient {

    public BpceGroupRegistrationRestClient(RegistrationHttpHeadersFactory headersFactory, RegistrationRequestMapper registrationRequestMapper, DefaultProperties properties) {
        super(headersFactory, registrationRequestMapper, properties);
    }

    @Override
    public ObjectNode registerClient(HttpClient httpClient, RegistrationRequest registrationRequest) {
        var accessToken = fetchAccessToken(httpClient);
        var authorizedRegistrationRequest = new BpceGroupRegistrationRequest(accessToken, registrationRequest);
        return super.registerClient(httpClient, authorizedRegistrationRequest);
    }

    @Override
    public ObjectNode updateRegistration(HttpClient httpClient, String url, RegistrationRequest registrationRequest) {
        var registrationUrl = properties.getRegistrationUrl() + "/" + registrationRequest.getAuthMeans().getClientId();
        var accessToken = fetchAccessToken(httpClient);
        var authorizedRegistrationRequest = new BpceGroupRegistrationRequest(accessToken, registrationRequest);
        return super.updateRegistration(httpClient, registrationUrl, authorizedRegistrationRequest);
    }

    private String fetchAccessToken(HttpClient httpClient) {
        var tokensUrl = properties.getRegions().stream().findFirst().orElseThrow(() -> new IllegalStateException("No region defined! Cannot obtain token url!")).getTokenUrl();
        var method = HttpMethod.POST;
        var prometheusPath = ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT;

        var body = new LinkedMultiValueMap<>();
        body.set(OAuth.GRANT_TYPE, OAuth.CLIENT_CREDENTIALS);
        body.set(OAuth.SCOPE, "manageRegistration");
        body.set(OAuth.CLIENT_ID, "PSD2_TPPRegister");

        var headers = HttpHeadersBuilder.builder()
                .withContentType(MediaType.APPLICATION_FORM_URLENCODED)
                .withAccept(Collections.singletonList(MediaType.APPLICATION_JSON))
                .build();

        var entity = new HttpEntity<>(body, headers);
        var executionInfo = new ExecutionInfo(tokensUrl, method, headers, prometheusPath);
        var result = errorHandler.executeAndHandle(() -> httpClient.exchangeForBody(tokensUrl, method, entity, prometheusPath, ObjectNode.class), executionInfo);
        return result.get("access_token").textValue();
    }
}
