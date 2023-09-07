package com.yolt.providers.openbanking.ais.kbciegroup.common.oauth2.client;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;

import java.util.function.Function;

public class KbcIeGroupBasicOauthClient<T> extends BasicOauthClient<T> {


    private final Function<DefaultAuthMeans, String> createTokenAuthenticationHeaderSupplier;
    private final Function<DefaultAuthMeans, String> refreshTokenAuthenticationHeaderSupplier;
    private final TokenRequestBodyProducer<T> tokenRequestBodySupplier;
    private final boolean isInPisProvider; //TODO should be fixed with C4PO-6078

    public KbcIeGroupBasicOauthClient(String oAuthTokenUrl, Function refreshTokenAuthenticationHeaderSupplier, Function createTokenAuthenticationHeaderSupplier, Function createCredentialsAuthenticationHeaderSupplier, TokenRequestBodyProducer tokenRequestBodySupplier, boolean isInPisProvider) {
        super(oAuthTokenUrl, refreshTokenAuthenticationHeaderSupplier, createTokenAuthenticationHeaderSupplier, createCredentialsAuthenticationHeaderSupplier, tokenRequestBodySupplier, isInPisProvider);
        this.createTokenAuthenticationHeaderSupplier = createTokenAuthenticationHeaderSupplier;
        this.refreshTokenAuthenticationHeaderSupplier = refreshTokenAuthenticationHeaderSupplier;
        this.tokenRequestBodySupplier = tokenRequestBodySupplier;
        this.isInPisProvider = isInPisProvider;
    }

    @Override
    public AccessTokenResponseDTO createAccessToken(final HttpClient httpClient,
                                                    final DefaultAuthMeans authenticationMeans,
                                                    final String authorizationCode,
                                                    final String redirectURI,
                                                    final TokenScope scope,
                                                    final Signer signer) throws TokenInvalidException {
        T body = tokenRequestBodySupplier.getCreateAccessTokenBody(authenticationMeans, authorizationCode, redirectURI, scope.getAuthorizationUrlScope());
        return createToken(httpClient, authenticationMeans, body,
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
        T body = tokenRequestBodySupplier.getRefreshAccessTokenBody(authenticationMeans, refreshToken, redirectURI, scope.getAuthorizationUrlScope());
        return createToken(httpClient, authenticationMeans, body, ProviderClientEndpoints.REFRESH_TOKEN,
                refreshTokenAuthenticationHeaderSupplier);
    }
}
