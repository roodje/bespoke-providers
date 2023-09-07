package com.yolt.providers.openbanking.ais.generic2.oauth2;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Optional;
import java.util.function.Function;

import static com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpErrorHandler.DEFAULT_HTTP_ERROR_HANDLER;

@AllArgsConstructor
public class BasicOauthClient<T> implements Oauth2Client {

    private final String oAuthTokenUrl;
    private final Function<DefaultAuthMeans, String> refreshTokenAuthenticationHeaderSupplier;
    private final Function<DefaultAuthMeans, String> createTokenAuthenticationHeaderSupplier;
    private final Function<DefaultAuthMeans, String> createCredentialsAuthenticationHeaderSupplier;
    private final TokenRequestBodyProducer<T> tokenRequestBodySupplier;
    private final boolean isInPisProvider; //TODO should be fixed with C4PO-6078

    public BasicOauthClient(String oAuthTokenUrl,
                            Function<DefaultAuthMeans, String> authenticationHeaderSupplier,
                            TokenRequestBodyProducer<T> tokenRequestBodySupplier,
                            boolean isInPisProvider) {

        this.oAuthTokenUrl = oAuthTokenUrl;
        this.tokenRequestBodySupplier = tokenRequestBodySupplier;
        this.refreshTokenAuthenticationHeaderSupplier = authenticationHeaderSupplier;
        this.createTokenAuthenticationHeaderSupplier = authenticationHeaderSupplier;
        this.createCredentialsAuthenticationHeaderSupplier = authenticationHeaderSupplier;
        this.isInPisProvider = isInPisProvider;
    }

    @Override
    public AccessTokenResponseDTO refreshAccessToken(final HttpClient httpClient,
                                                     final DefaultAuthMeans authenticationMeans,
                                                     final String refreshToken,
                                                     final String redirectURI,
                                                     final TokenScope scope,
                                                     final Signer signer) throws TokenInvalidException {
        T body = tokenRequestBodySupplier.getRefreshAccessTokenBody(authenticationMeans, refreshToken);
        return createToken(httpClient, authenticationMeans, body, ProviderClientEndpoints.REFRESH_TOKEN,
                refreshTokenAuthenticationHeaderSupplier);
    }

    @Override
    public AccessTokenResponseDTO createAccessToken(final HttpClient httpClient,
                                                    final DefaultAuthMeans authenticationMeans,
                                                    final String authorizationCode,
                                                    final String redirectURI,
                                                    final TokenScope scope,
                                                    final Signer signer) throws TokenInvalidException {
        T body = tokenRequestBodySupplier.getCreateAccessTokenBody(authenticationMeans, authorizationCode, redirectURI);
        return createToken(httpClient, authenticationMeans, body,
                isInPisProvider ? ProviderClientEndpoints.GET_PIS_ACCESS_TOKEN : ProviderClientEndpoints.GET_AIS_ACCESS_TOKEN,
                createTokenAuthenticationHeaderSupplier);
    }

    @Override
    public AccessTokenResponseDTO createClientCredentials(final HttpClient httpClient,
                                                          final DefaultAuthMeans authenticationMeans,
                                                          final TokenScope scope,
                                                          final Signer signer) throws TokenInvalidException {
        T body = tokenRequestBodySupplier.getCreateClientCredentialsBody(authenticationMeans, scope);
        return createToken(httpClient, authenticationMeans, body, ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT,
                createCredentialsAuthenticationHeaderSupplier);
    }

    protected AccessTokenResponseDTO createToken(final HttpClient httpClient,
                                                 final DefaultAuthMeans authenticationMeans,
                                                 final T body,
                                                 final String endpointIdentifier,
                                                 Function<DefaultAuthMeans, String> authenticationHeaderSupplier) throws TokenInvalidException {
        HttpHeaders headers = getHeaders(authenticationHeaderSupplier.apply(authenticationMeans), authenticationMeans.getInstitutionId());
        return httpClient.exchange(oAuthTokenUrl,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                endpointIdentifier,
                AccessTokenResponseDTO.class,
                getErrorHandler()).getBody();
    }

    protected HttpErrorHandler getErrorHandler() {
        return DEFAULT_HTTP_ERROR_HANDLER;
    }

    protected HttpHeaders getHeaders(final String authenticationHeader, final String institutionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpExtraHeaders.FINANCIAL_ID_HEADER_NAME, institutionId);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Optional.ofNullable(authenticationHeader)
                .ifPresent(authentication -> headers.add(HttpHeaders.AUTHORIZATION, authentication));

        return headers;
    }
}
