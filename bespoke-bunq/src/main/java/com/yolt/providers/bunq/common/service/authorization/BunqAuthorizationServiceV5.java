package com.yolt.providers.bunq.common.service.authorization;

import com.bunq.sdk.security.SecurityUtils;
import com.yolt.providers.bunq.common.auth.BunqApiContext;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.http.BunqHttpServiceV5;
import com.yolt.providers.bunq.common.model.InstallationResponse;
import com.yolt.providers.bunq.common.model.OauthAccessTokenResponse;
import com.yolt.providers.bunq.common.model.SessionServerResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.KeyPair;
import java.util.Map;

@RequiredArgsConstructor
public class BunqAuthorizationServiceV5 {

    private static final String AUTHORIZATION_REQUEST_URL_FORMAT = "%s?response_type=code&client_id=%s&redirect_uri=%s&state=%s";
    private final BunqProperties properties;

    public String getLoginUrl(final String clientId,
                              final String redirectUrl,
                              final String loginState) {
        return String.format(
                AUTHORIZATION_REQUEST_URL_FORMAT,
                properties.getOauthAuthorizationBaseUrl(),
                clientId,
                redirectUrl,
                loginState);
    }

    public BunqApiContext authorizeUserAndStartSession(final BunqAuthenticationMeansV2 authenticationMeans,
                                                       final UrlCreateAccessMeansRequest urlCreateAccessMeans,
                                                       final BunqHttpServiceV5 httpService) {
        try {
            Map<String, String> urlQueryParameters = UriComponentsBuilder
                    .fromUriString(urlCreateAccessMeans.getRedirectUrlPostedBackFromSite())
                    .build()
                    .getQueryParams()
                    .toSingleValueMap();
            String authorizationCode = urlQueryParameters.get("code");
            if (authorizationCode == null) {
                throw new MissingDataException("Missing authorization code in redirect url query parameters");
            }

            String redirectUriWithoutQueryParams = urlCreateAccessMeans.getRedirectUrlPostedBackFromSite().substring(0, urlCreateAccessMeans.getRedirectUrlPostedBackFromSite().indexOf('?'));
            // Retrieve access_token based on authorization code
            OauthAccessTokenResponse oAuthAccessTokenResponse = httpService.postAccessCodeWithPsd2OauthMeans(authenticationMeans, authorizationCode, redirectUriWithoutQueryParams);
            KeyPair keyPair = SecurityUtils.generateKeyPair();
            // Create a new installation and device registration at bunq for the new user
            InstallationResponse installation = httpService.createInstallation(keyPair);
            String installationToken = installation.getToken().getTokenString();
            String accessToken = oAuthAccessTokenResponse.getAccessToken();
            httpService.createDeviceServer(keyPair, installationToken, accessToken);
            // Create a new session, this is equivalent to "logging in", every time we have to refresh the accessmeans we have to recreate the session
            SessionServerResponse session = httpService.createSessionServer(keyPair, installationToken, accessToken);
            return new BunqApiContext(
                    session.getBunqId(),
                    installationToken,
                    keyPair,
                    accessToken,
                    session.getToken().getTokenString(),
                    session.getExpiryTimeInSeconds());
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    public BunqApiContext refreshSessionAtBunq(final BunqApiContext context, final BunqHttpServiceV5 httpService) throws TokenInvalidException {
        SessionServerResponse session = httpService.createSessionServer(context.getKeyPair(), context.getServerToken(), context.getOauthToken());
        return new BunqApiContext(
                session.getBunqId(),
                context.getServerToken(),
                context.getKeyPair(),
                context.getOauthToken(),
                session.getToken().getTokenString(),
                session.getExpiryTimeInSeconds());
    }
}
