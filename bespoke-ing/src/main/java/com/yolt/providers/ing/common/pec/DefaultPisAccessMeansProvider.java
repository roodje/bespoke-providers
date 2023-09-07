package com.yolt.providers.ing.common.pec;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.ing.common.auth.IngAuthData;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import com.yolt.providers.ing.common.config.IngProperties;
import com.yolt.providers.ing.common.http.HttpClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Clock;

@RequiredArgsConstructor
public class DefaultPisAccessMeansProvider {

    private static final String GRANT_TYPE = "grant_type";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

    private final HttpClientFactory httpClientFactory;
    private final HttpErrorHandler httpErrorHandler;
    private final IngProperties properties;
    private final String providerIdentifier;
    private final DefaultAuthorizationHeadersProvider headersProvider;

    @SneakyThrows(TokenInvalidException.class)
    public IngClientAccessMeans getClientAccessMeans(final IngAuthenticationMeans authenticationMeans,
                                                     final RestTemplateManager restTemplateManager,
                                                     final Signer signer,
                                                     final Clock clock) {
        IngAuthData token = doClientCredentialsGrant(authenticationMeans, restTemplateManager, signer);
        return new IngClientAccessMeans(token, null, clock);
    }

    private IngAuthData doClientCredentialsGrant(final IngAuthenticationMeans authenticationMeans,
                                                 final RestTemplateManager restTemplateManager,
                                                 final Signer signer) throws TokenInvalidException {
        HttpClient httpClient = httpClientFactory.createPisHttpClient(authenticationMeans, restTemplateManager, providerIdentifier);
        MultiValueMap<String, Object> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, GRANT_TYPE_CLIENT_CREDENTIALS);
        HttpHeaders headers = headersProvider.provideHttpHeaders(requestPayload, authenticationMeans, signer);

        return fetchTokenResponse(httpClient, new HttpEntity<>(requestPayload, headers));
    }


    private IngAuthData fetchTokenResponse(final HttpClient httpClient,
                                           final HttpEntity<MultiValueMap<String, Object>> tokenRequest) throws TokenInvalidException {
        return httpClient.exchange(
                properties.getOAuthTokenEndpoint(),
                HttpMethod.POST,
                tokenRequest,
                ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT,
                IngAuthData.class,
                httpErrorHandler
        ).getBody();
    }
}
