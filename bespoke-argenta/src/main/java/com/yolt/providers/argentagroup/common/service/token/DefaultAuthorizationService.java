package com.yolt.providers.argentagroup.common.service.token;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.common.service.ProviderState;
import com.yolt.providers.argentagroup.common.service.ProviderStateMapper;
import com.yolt.providers.argentagroup.common.service.consent.AuthorizationCodeExtractor;
import com.yolt.providers.argentagroup.dto.AccessTokenResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@RequiredArgsConstructor
public class DefaultAuthorizationService implements AuthorizationService {

    private final Clock clock;
    private final String tokensEndpointPath;
    private final CreateAccessMeansRequestBodyProvider createAccessMeansRequestBodyProvider;
    private final RefreshAccessMeansRequestBodyProvider refreshAccessMeansRequestBodyProvider;
    private final AccessMeansHttpHeadersProvider accessMeansHttpHeadersProvider;
    private final ProviderStateMapper providerStateMapper;
    private final AccessMeansMapper accessMeansMapper;
    private final AuthorizationCodeExtractor authorizationCodeExtractor;
    private final HttpErrorHandler tokensEndpointHttpErrorHandler;

    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest request,
                                                     final DefaultAuthenticationMeans authenticationMeans,
                                                     final HttpClient httpClient) throws TokenInvalidException {
        ProviderState providerState = providerStateMapper.deserializeProviderState(request.getProviderState());
        String authorizationCode = authorizationCodeExtractor.extractFromRedirectUrl(request.getRedirectUrlPostedBackFromSite());

        MultiValueMap<String, String> requestBody = createAccessMeansRequestBodyProvider.provideRequestBody(
                authenticationMeans,
                authorizationCode,
                providerState.getProofKeyCodeExchange().getCodeVerifier(),
                request.getBaseClientRedirectUrl()
        );

        HttpHeaders requestHeaders = accessMeansHttpHeadersProvider.provideRequestHeaders(authenticationMeans);

        AccessTokenResponse response = httpClient.exchange(
                tokensEndpointPath,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, requestHeaders),
                ProviderClientEndpoints.GET_ACCESS_TOKEN,
                AccessTokenResponse.class,
                tokensEndpointHttpErrorHandler
        ).getBody();

        AccessMeans accessMeans = new AccessMeans(
                response.getAccessToken(),
                response.getRefreshToken(),
                response.getScope(),
                providerState.getConsentId(),
                response.getExpiresIn().longValue()
        );
        String serializedAccessMeans = accessMeansMapper.serializeAccessMeans(accessMeans);

        return new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        request.getUserId(),
                        serializedAccessMeans,
                        new Date(),
                        Date.from(Instant.now(clock).plusSeconds(accessMeans.getExpiresIn()))
                )
        );
    }

    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest request,
                                             final DefaultAuthenticationMeans authenticationMeans,
                                             final HttpClient httpClient) throws TokenInvalidException {
        AccessMeansDTO oldAccessMeans = request.getAccessMeans();
        AccessMeans deserializedAccessMeans = accessMeansMapper.deserializeAccessMeans(oldAccessMeans.getAccessMeans());

        MultiValueMap<String, String> requestBody = refreshAccessMeansRequestBodyProvider.provideRequestBody(
                authenticationMeans, deserializedAccessMeans);

        HttpHeaders requestHeaders = accessMeansHttpHeadersProvider.provideRequestHeaders(authenticationMeans);

        AccessTokenResponse response = httpClient.exchange(
                tokensEndpointPath,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, requestHeaders),
                ProviderClientEndpoints.REFRESH_TOKEN,
                AccessTokenResponse.class,
                tokensEndpointHttpErrorHandler
        ).getBody();

        String refreshToken = response.getRefreshToken() == null ? deserializedAccessMeans.getRefreshToken() : response.getRefreshToken();
        AccessMeans newAccessMeans = new AccessMeans(
                response.getAccessToken(),
                refreshToken,
                response.getScope(),
                deserializedAccessMeans.getConsentId(),
                response.getExpiresIn().longValue()
        );

        String newSerializedAccessMeans = accessMeansMapper.serializeAccessMeans(newAccessMeans);

        return new AccessMeansDTO(
                oldAccessMeans.getUserId(),
                newSerializedAccessMeans,
                new Date(),
                Date.from(Instant.now(clock).plusSeconds(newAccessMeans.getExpiresIn())));
    }
}
