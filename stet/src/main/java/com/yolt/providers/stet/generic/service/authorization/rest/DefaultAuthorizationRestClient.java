package com.yolt.providers.stet.generic.service.authorization.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.error.HttpErrorHandler;
import com.yolt.providers.stet.generic.mapper.token.TokenRequestMapper;
import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.request.RefreshTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.error.DefaultAuthorizationHttpErrorHandler;
import com.yolt.providers.stet.generic.service.authorization.rest.header.AuthorizationHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.GET_ACCESS_TOKEN;
import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.REFRESH_TOKEN;
import static org.springframework.http.HttpMethod.POST;

@RequiredArgsConstructor
public class DefaultAuthorizationRestClient implements AuthorizationRestClient {

    protected final AuthorizationHttpHeadersFactory headersFactory;
    protected final TokenRequestMapper tokenRequestMapper;
    protected final HttpErrorHandler errorHandler;

    public DefaultAuthorizationRestClient(AuthorizationHttpHeadersFactory headersFactory,
                                          TokenRequestMapper tokenRequestMapper) {
        this.headersFactory = headersFactory;
        this.tokenRequestMapper = tokenRequestMapper;
        this.errorHandler = new DefaultAuthorizationHttpErrorHandler();
    }

    @Override
    public <T, U extends AccessTokenRequest> T getAccessToken(HttpClient httpClient,
                                                              U accessTokenRequestDTO,
                                                              DefaultAuthenticationMeans authMeans,
                                                              Class<T> accessTokenClass) throws TokenInvalidException {
        String url = accessTokenRequestDTO.getTokenUrl();
        HttpMethod method = POST;
        String prometheusPath = GET_ACCESS_TOKEN;
        MultiValueMap<String, String> body = tokenRequestMapper.mapAccessTokenRequest(accessTokenRequestDTO);
        HttpHeaders headers = headersFactory.createAccessTokenHeaders(authMeans, body, accessTokenRequestDTO);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ExecutionInfo executionInfo = new ExecutionInfo(url, method, headers, prometheusPath);
        return errorHandler.executeAndHandle(() -> httpClient.exchangeForBody(url, method, entity, prometheusPath, accessTokenClass), executionInfo);
    }

    @Override
    public <T, U extends RefreshTokenRequest> T refreshAccessToken(HttpClient httpClient,
                                                                   U refreshTokenRequestDTO,
                                                                   DefaultAuthenticationMeans authMeans,
                                                                   Class<T> refreshTokenClass) throws TokenInvalidException {
        String url = refreshTokenRequestDTO.getTokenUrl();
        HttpMethod method = POST;
        String prometheusPath = REFRESH_TOKEN;
        MultiValueMap<String, String> body = tokenRequestMapper.mapRefreshTokenRequest(refreshTokenRequestDTO);
        HttpHeaders headers = headersFactory.createAccessTokenHeaders(authMeans, body, refreshTokenRequestDTO);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ExecutionInfo executionInfo = new ExecutionInfo(url, method, headers, prometheusPath);
        return errorHandler.executeAndHandle(() -> httpClient.exchangeForBody(url, method, entity, prometheusPath, refreshTokenClass), executionInfo);
    }
}
