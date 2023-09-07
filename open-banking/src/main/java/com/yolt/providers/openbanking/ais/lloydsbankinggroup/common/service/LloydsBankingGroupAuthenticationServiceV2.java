package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.exception.LoginNotFoundException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.claims.LLoydsBankingGroupNonceProvider;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.client.utils.URIBuilder;

import java.time.Clock;
import java.util.UUID;

public class LloydsBankingGroupAuthenticationServiceV2 extends DefaultAuthenticationService {

    public static final String OIDC_HYBRID_FLOW_RESPONSE_TYPE = "code id_token";

    private final String oauthAuthorizationUrl;
    private final LLoydsBankingGroupNonceProvider nonceProvider;

    public LloydsBankingGroupAuthenticationServiceV2(final String oauthAuthorizationUrl,
                                                     final Oauth2Client oauth2Client,
                                                     final UserRequestTokenSigner userRequestTokenSigner,
                                                     final TokenClaimsProducer tokenClaimsProducer,
                                                     final LLoydsBankingGroupNonceProvider nonceProvider,
                                                     final Clock clock) {
        super(oauthAuthorizationUrl, oauth2Client, userRequestTokenSigner, tokenClaimsProducer, clock);
        this.oauthAuthorizationUrl = oauthAuthorizationUrl;
        this.nonceProvider = nonceProvider;
    }

    @Override
    public String generateAuthorizationUrl(final DefaultAuthMeans authenticationMeans,
                                           final String accountRequestId,
                                           final String secretState,
                                           final String redirectUrl,
                                           final TokenScope scope,
                                           final Signer signer) {
        try {
            String userRequestToken = generateUserRequestToken(authenticationMeans, accountRequestId, secretState,
                    redirectUrl, signer, scope);

            URIBuilder uriBuilder = new URIBuilder(oauthAuthorizationUrl)
                    .addParameter(OAuth.RESPONSE_TYPE, OIDC_HYBRID_FLOW_RESPONSE_TYPE)
                    .addParameter(OAuth.CLIENT_ID, authenticationMeans.getClientId())
                    .addParameter(OAuth.STATE, secretState)
                    .addParameter(OAuth.SCOPE, scope.getAuthorizationUrlScope())
                    .addParameter(OAuth.NONCE, nonceProvider.prepareNonce(secretState))
                    .addParameter(OAuth.REDIRECT_URI, redirectUrl)
                    .addParameter(OAuth.REQUEST, userRequestToken);
            return uriBuilder.toString();
        } catch (Exception e) {
            throw new LoginNotFoundException(e);
        }
    }

    @Override
    public AccessMeans refreshAccessToken(HttpClient httpClient, DefaultAuthMeans authenticationMeans, UUID userId, String refreshToken, String redirectUrl, TokenScope scope, Signer signer) throws TokenInvalidException {
        if (ObjectUtils.isEmpty(refreshToken)) {
            //TODO C4PO-11529 before 30.09.2022 Lloyds doesn't support refresh token. It will be introduce as part of FCA-SCA 90-days re-authentication exemption
            //after 30.09.2022 new consents should have refresh tokens
            throw new TokenInvalidException();
        }
        return super.refreshAccessToken(httpClient, authenticationMeans, userId, refreshToken, redirectUrl, scope, signer);
    }


}
