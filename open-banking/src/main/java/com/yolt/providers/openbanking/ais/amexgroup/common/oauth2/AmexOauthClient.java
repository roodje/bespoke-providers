package com.yolt.providers.openbanking.ais.amexgroup.common.oauth2;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import java.util.Optional;
import java.util.function.Function;

public class AmexOauthClient extends BasicOauthClient<MultiValueMap<String, String>> {

    private static final String CLIENT_CREDENTIALS_ENDPOINT = "/oauth/v1/token/cc/access";
    private static final String CREATE_ACCESS_TOKEN_ENDPOINT = "/apiplatform/v8/oauth/token/bearer";
    private static final String REFRESH_ACCESS_TOKEN_ENDPOINT = "/apiplatform/v8/oauth/token_refresh/bearer";

    private final String oAuthTokenUrl;
    private final TokenRequestBodyProducer<MultiValueMap<String, String>> tokenRequestBodySupplier;
    private final Function<DefaultAuthMeans, String> refreshTokenAuthenticationHeaderSupplier;
    private final Function<DefaultAuthMeans, String> createTokenAuthenticationHeaderSupplier;
    private final Function<DefaultAuthMeans, String> createCredentialsAuthenticationHeaderSupplier;
    private final boolean isInPisProvider;

    public AmexOauthClient(String oAuthTokenUrl,
                           Function<DefaultAuthMeans, String> authenticationHeaderSupplier,
                           TokenRequestBodyProducer<MultiValueMap<String, String>> tokenRequestBodySupplier,
                           boolean isInPisProvider) {
        super(oAuthTokenUrl, authenticationHeaderSupplier, tokenRequestBodySupplier, isInPisProvider);
        this.oAuthTokenUrl = oAuthTokenUrl;
        this.tokenRequestBodySupplier = tokenRequestBodySupplier;
        this.refreshTokenAuthenticationHeaderSupplier = authenticationHeaderSupplier;
        this.createTokenAuthenticationHeaderSupplier = authenticationHeaderSupplier;
        this.createCredentialsAuthenticationHeaderSupplier = authenticationHeaderSupplier;
        this.isInPisProvider = isInPisProvider;
    }

    @Override
    public AccessTokenResponseDTO createClientCredentials(final HttpClient httpClient,
                                                          final DefaultAuthMeans authenticationMeans,
                                                          final TokenScope scope,
                                                          final Signer signer) throws TokenInvalidException {
        MultiValueMap<String, String> body = tokenRequestBodySupplier.getCreateClientCredentialsBody(authenticationMeans, scope);
        return createTokenWithCustomUrl(oAuthTokenUrl + CLIENT_CREDENTIALS_ENDPOINT, httpClient, authenticationMeans, body, ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT,
                createCredentialsAuthenticationHeaderSupplier);
    }

    private AccessTokenResponseDTO createTokenWithCustomUrl(final String url,
                                                            final HttpClient httpClient,
                                                            final DefaultAuthMeans authenticationMeans,
                                                            final MultiValueMap<String, String> body,
                                                            final String endpointIdentifier,
                                                            Function<DefaultAuthMeans, String> authenticationHeaderSupplier) throws TokenInvalidException {
        HttpHeaders headers = getHeaders(authenticationHeaderSupplier.apply(authenticationMeans));
        return httpClient.exchange(url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                endpointIdentifier,
                AccessTokenResponseDTO.class,
                getErrorHandler()).getBody();
    }

    private HttpHeaders getHeaders(final String authenticationHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Optional.ofNullable(authenticationHeader)
                .ifPresent(authentication -> headers.add("x-amex-api-key", authentication));

        return headers;
    }

    public AccessTokenResponseDTO createAccessToken(HttpClient httpClient,
                                                    DefaultAuthMeans authenticationMeans,
                                                    String authorizationCode,
                                                    String redirectUrl,
                                                    TokenScope scope,
                                                    String codeVerifier) throws TokenInvalidException {
        MultiValueMap<String, String> body = tokenRequestBodySupplier.getCreateAccessTokenBody(authenticationMeans,
                authorizationCode,
                redirectUrl,
                scope.getGrantScope(),
                codeVerifier);
        return createTokenWithCustomUrl(oAuthTokenUrl + CREATE_ACCESS_TOKEN_ENDPOINT, httpClient, authenticationMeans, body,
                isInPisProvider ? ProviderClientEndpoints.GET_PIS_ACCESS_TOKEN : ProviderClientEndpoints.GET_AIS_ACCESS_TOKEN,
                createTokenAuthenticationHeaderSupplier);
    }

    @Override
    public AccessTokenResponseDTO refreshAccessToken(final HttpClient httpClient,
                                                     final DefaultAuthMeans authenticationMeans,
                                                     final String refreshToken,
                                                     final String redirectURI,
                                                     final TokenScope scope,
                                                     final Signer signer) throws TokenInvalidException {
        MultiValueMap<String, String> body = tokenRequestBodySupplier.getRefreshAccessTokenBody(authenticationMeans, refreshToken);
        return createTokenWithCustomUrl(oAuthTokenUrl + REFRESH_ACCESS_TOKEN_ENDPOINT, httpClient, authenticationMeans, body, ProviderClientEndpoints.REFRESH_TOKEN,
                refreshTokenAuthenticationHeaderSupplier);
    }
}
