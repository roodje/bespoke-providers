package com.yolt.providers.openbanking.ais.barclaysgroup.common.service;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.barclaysgroup.barclays.config.BarclaysPropertiesV3;
import com.yolt.providers.openbanking.ais.exception.LoginNotFoundException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import org.apache.http.client.utils.URIBuilder;

import java.time.Clock;

public class BarclaysGroupAuthenticationServiceV4 extends DefaultAuthenticationService {

    //Base authorization url differ between customer types. In AIS it is handled via dynamic form
    //For PIS it is assumed that payment is used only by private banking customers;
    private static final String PRIVATE_CUSTOMER_TYPE_CODE = "PERSONAL";

    private final String oAuthAuthorizationUrl;


    public BarclaysGroupAuthenticationServiceV4(final BarclaysPropertiesV3 properties,
                                                final Oauth2Client oauth2Client,
                                                final UserRequestTokenSigner userRequestTokenSigner,
                                                final TokenClaimsProducer tokenClaimsProducer,
                                                final Clock clock) {
        super(properties.getCustomerTypeByCode(PRIVATE_CUSTOMER_TYPE_CODE).getAuthorizationUrl(), oauth2Client, userRequestTokenSigner, tokenClaimsProducer, clock);
        this.oAuthAuthorizationUrl = properties.getCustomerTypeByCode(PRIVATE_CUSTOMER_TYPE_CODE).getAuthorizationUrl();
    }

    public String generateAuthorizationUrlBasedOnForm(final DefaultAuthMeans authenticationMeans,
                                                      final String resourceId,
                                                      final String secretState,
                                                      final String redirectUrl,
                                                      final TokenScope scope,
                                                      final Signer signer,
                                                      final String baseAuthorizationUrl) {
        try {
            String userRequestToken = generateUserRequestToken(authenticationMeans, resourceId, secretState,
                    redirectUrl, signer, scope, baseAuthorizationUrl);
            return new URIBuilder(baseAuthorizationUrl)
                    .addParameter(OAuth.RESPONSE_TYPE, "code id_token")
                    .addParameter(OAuth.CLIENT_ID, authenticationMeans.getClientId())
                    .addParameter(OAuth.STATE, secretState)
                    .addParameter(OAuth.SCOPE, scope.getAuthorizationUrlScope())
                    .addParameter(OAuth.NONCE, secretState)
                    .addParameter(OAuth.REDIRECT_URI, redirectUrl)
                    .addParameter(OAuth.REQUEST, userRequestToken)
                    .toString();
        } catch (Exception e) {
            throw new LoginNotFoundException(e);
        }
    }

    @Override
    public String generateAuthorizationUrl(DefaultAuthMeans authenticationMeans,
                                           String resourceId,
                                           String secretState,
                                           String redirectUrl,
                                           TokenScope scope,
                                           Signer signer) {
        try {
            String adjustedRedirectUrl = adjustRedirectUrl(redirectUrl);
            String userRequestToken = generateUserRequestToken(authenticationMeans, resourceId, secretState,
                    adjustedRedirectUrl, signer, scope, oAuthAuthorizationUrl);

            URIBuilder uriBuilder = new URIBuilder(oAuthAuthorizationUrl)
                    .addParameter(OAuth.RESPONSE_TYPE, "code id_token")
                    .addParameter(OAuth.CLIENT_ID, authenticationMeans.getClientId())
                    .addParameter(OAuth.STATE, secretState)
                    .addParameter(OAuth.SCOPE, scope.getAuthorizationUrlScope())
                    .addParameter(OAuth.NONCE, secretState)
                    .addParameter(OAuth.REDIRECT_URI, adjustedRedirectUrl)
                    .addParameter(OAuth.REQUEST, userRequestToken);

            return uriBuilder.toString();
        } catch (Exception e) {
            throw new LoginNotFoundException(e);
        }
    }
}
