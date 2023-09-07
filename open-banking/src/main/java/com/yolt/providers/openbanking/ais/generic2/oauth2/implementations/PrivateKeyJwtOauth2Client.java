package com.yolt.providers.openbanking.ais.generic2.oauth2.implementations;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion.ClientAssertionProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.function.Function;


@AllArgsConstructor
public class PrivateKeyJwtOauth2Client<T> implements Oauth2Client {

    private final String oAuthTokenUrl;
    private final TokenRequestBodyProducer<T> tokenRequestBodyProducer;
    private final ClientAssertionProducer clientAssertionProducer;
    private final boolean isInPisProvider; //TODO should be fixed with C4PO-6078

    @Override
    public AccessTokenResponseDTO refreshAccessToken(final HttpClient httpClient,
                                                     final DefaultAuthMeans authenticationMeans,
                                                     final String refreshToken,
                                                     final String redirectURI,
                                                     final TokenScope scope,
                                                     final Signer signer) throws TokenInvalidException {
        if (StringUtils.isEmpty(refreshToken)) {
            throw new IllegalArgumentException("RefreshToken is required to fetch a new access token.");
        }
        T body = tokenRequestBodyProducer.getRefreshAccessTokenBody(authenticationMeans, refreshToken, redirectURI, scope.getGrantScope(),
                clientAssertionProducer.createNewClientRequestToken(authenticationMeans, signer));
        return createToken(httpClient, authenticationMeans, body, ProviderClientEndpoints.REFRESH_TOKEN,
                any -> null);
    }

    @Override
    public AccessTokenResponseDTO createAccessToken(final HttpClient httpClient,
                                                    final DefaultAuthMeans authenticationMeans,
                                                    final String authorizationCode,
                                                    final String redirectURI,
                                                    final TokenScope scope,
                                                    final Signer signer) throws TokenInvalidException {
        if (StringUtils.isEmpty(authorizationCode)) {
            throw new IllegalArgumentException("AuthorizationCode is required to fetch an access token.");
        }
        T body = tokenRequestBodyProducer.getCreateAccessTokenBody(authenticationMeans, authorizationCode, redirectURI, scope.getGrantScope(),
                clientAssertionProducer.createNewClientRequestToken(authenticationMeans, signer));
        return createToken(httpClient, authenticationMeans, body,
                isInPisProvider ? ProviderClientEndpoints.GET_PIS_ACCESS_TOKEN : ProviderClientEndpoints.GET_AIS_ACCESS_TOKEN,
                any -> authorizationCode);
    }

    @Override
    public AccessTokenResponseDTO createClientCredentials(final HttpClient httpClient,
                                                          final DefaultAuthMeans authenticationMeans,
                                                          final TokenScope scope,
                                                          final Signer signer) throws TokenInvalidException {
        T body = tokenRequestBodyProducer.getCreateClientCredentialsBody(authenticationMeans, scope,
                clientAssertionProducer.createNewClientRequestToken(authenticationMeans, signer));
        return createToken(httpClient, authenticationMeans, body, ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT,
                any -> null);
    }

    private AccessTokenResponseDTO createToken(final HttpClient httpClient,
                                               final DefaultAuthMeans authenticationMeans,
                                               final T body,
                                               final String endpointIdentifier,
                                               Function<DefaultAuthMeans, String> authenticationHeaderSupplier) throws TokenInvalidException {
        HttpHeaders headers = getHeaders(authenticationHeaderSupplier.apply(authenticationMeans), authenticationMeans.getInstitutionId());
        return httpClient.exchange(oAuthTokenUrl,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                endpointIdentifier,
                AccessTokenResponseDTO.class).getBody();
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
