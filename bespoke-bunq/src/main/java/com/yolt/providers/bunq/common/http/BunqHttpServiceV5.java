package com.yolt.providers.bunq.common.http;

import com.yolt.providers.bunq.common.auth.BunqApiContext;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.model.*;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.ResponseEntity;

import java.security.KeyPair;
import java.util.Arrays;

@RequiredArgsConstructor
public class BunqHttpServiceV5 {

    private static final String PUBLIC_KEY_FORMAT = "-----BEGIN PUBLIC KEY-----\n%s\n-----END PUBLIC KEY-----\n";
    private static final String INSTALLATION = "/installation";
    private static final String PAYMENT_SERVICE_PROVIDER_CREDENTIAL = "/payment-service-provider-credential";
    private static final String DEVICE_SERVER = "/device-server";
    private static final String SESSION_SERVER = "/session-server";
    private static final String OAUTH_REGISTRATION = "/user/%d/oauth-client";
    private static final String OAUTH_CLIENT_DETAILS = "/user/%d/oauth-client/%d";
    private static final String OAUTH_CLIENT_DETAILS_LIST = "/user/%d/oauth-client";
    private static final String OAUTH_ADD_CALLBACK_URL = "/user/%d/oauth-client/%d/callback-url";
    private static final String OAUTH_REMOVE_CALLBACK_URL = "/user/%d/oauth-client/%d/callback-url/%d";

    private static final String ACCESS_TOKEN_EXCHANGE_FORMAT = "?grant_type=authorization_code&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s";
    private static final String ACCOUNTS = "/user/%s/monetary-account?count=%s";
    private static final String TRANSACTIONS = "/user/%s/monetary-account/%s/payment?count=%s";
    private static final String OLDER_ID_PATH = "&older_id=%s";

    public static final String POST_CREATE_INSTALLATION = "post_create_installation";
    public static final String POST_CREATE_DEVICE_SERVER = "post_create_device_server";
    public static final String POST_CREATE_SESSION_SERVER = "post_create_session_server";
    public static final String AUTOONBOARDING = "autoonboarding";
    private static final String GET_ACCOUNTS = "get_accounts";
    private static final String GET_TRANSACTIONS = "get_transactions";

    private final BunqProperties properties;
    private final BunqHttpClientV5 restClient;

    public InstallationResponse createInstallation(final KeyPair keyPair) throws TokenInvalidException {
        byte[] encodedPublicKey = keyPair.getPublic().getEncoded();
        String formattedPublicKey = String.format(PUBLIC_KEY_FORMAT, new String(Base64.encode(encodedPublicKey)));
        return restClient.postUnsignedRequest(INSTALLATION, new InstallationRequest(formattedPublicKey), InstallationResponse.class, POST_CREATE_INSTALLATION).getBody();
    }

    public Psd2ProviderResponse registerProvider(final KeyPair keyPair, final Psd2ProviderRequest request, final String installationToken) throws TokenInvalidException {
        return restClient.buildSignedPostRequest(
                properties.getBaseUrl() + PAYMENT_SERVICE_PROVIDER_CREDENTIAL,
                keyPair,
                installationToken,
                request,
                Psd2ProviderResponse.class,
                AUTOONBOARDING).getBody();
    }

    public DeviceServerResponse createDeviceServer(final KeyPair keyPair, final String installationToken, final String apiToken) throws TokenInvalidException {
        return restClient.buildSignedPostRequest(
                properties.getBaseUrl() + DEVICE_SERVER,
                keyPair,
                installationToken,
                new DeviceServerRequest("description", apiToken, Arrays.asList(properties.getOurExternalIpAddress(), "*")),
                DeviceServerResponse.class,
                POST_CREATE_DEVICE_SERVER).getBody();
    }

    public SessionServerResponse createSessionServer(final KeyPair keyPair, final String installationToken, final String apiToken) throws TokenInvalidException {
        return restClient.buildSignedPostRequest(
                properties.getBaseUrl() + SESSION_SERVER,
                keyPair,
                installationToken,
                new SessionServerRequest(apiToken),
                SessionServerResponse.class,
                POST_CREATE_SESSION_SERVER).getBody();
    }

    public Psd2SessionResponse createPsd2SessionServer(final KeyPair keyPair, final String installationToken, final String apiToken) throws TokenInvalidException {
        return restClient.buildSignedPostRequest(
                properties.getBaseUrl() + SESSION_SERVER,
                keyPair,
                installationToken,
                new SessionServerRequest(apiToken),
                Psd2SessionResponse.class,
                AUTOONBOARDING).getBody();
    }

    public OauthClientRegistrationResponse registerOAuthClient(final KeyPair keyPair, final String sessionToken, final long userId) throws TokenInvalidException {
        String path = String.format(OAUTH_REGISTRATION, userId);
        return restClient.buildSignedPostRequest(
                properties.getBaseUrl() + path,
                keyPair,
                sessionToken,
                new OauthClientRegistrationRequest("ACTIVE"),
                OauthClientRegistrationResponse.class,
                AUTOONBOARDING).getBody();
    }

    public OauthAddCallbackUrlResponse addCalbackUrl(final KeyPair keyPair, final String sessionToken, final long userId, final long oauthUserId, final String callbackUrl) throws TokenInvalidException {
        String path = String.format(OAUTH_ADD_CALLBACK_URL, userId, oauthUserId);
        return restClient.buildSignedPostRequest(
                properties.getBaseUrl() + path,
                keyPair,
                sessionToken,
                new OauthAddCallbackUrlRequest(callbackUrl),
                OauthAddCallbackUrlResponse.class,
                AUTOONBOARDING).getBody();
    }

    public OauthClientDetailsResponse getOAuthClientDetails(final KeyPair keyPair, final String sessionToken, final long userId, final long oauthUserId) throws TokenInvalidException {
        String path = String.format(OAUTH_CLIENT_DETAILS, userId, oauthUserId);
        return restClient.get(
                properties.getBaseUrl() + path,
                keyPair,
                sessionToken,
                OauthClientDetailsResponse.class,
                AUTOONBOARDING).getBody();
    }

    public OauthClientDetailsResponse getOAuthClientList(final KeyPair keyPair, final String sessionToken, final long userId) throws TokenInvalidException {
        String path = String.format(OAUTH_CLIENT_DETAILS_LIST, userId);
        return restClient.get(
                properties.getBaseUrl() + path,
                keyPair,
                sessionToken,
                OauthClientDetailsResponse.class,
                AUTOONBOARDING).getBody();
    }

    public void removeCallbackUrl(final KeyPair keyPair, final String sessionToken, final long userId, final long oauthUserId, final long callbackUrlId) throws TokenInvalidException {
        String path = String.format(OAUTH_REMOVE_CALLBACK_URL, userId, oauthUserId, callbackUrlId);
        restClient.delete(
                properties.getBaseUrl() + path,
                keyPair,
                sessionToken,
                AUTOONBOARDING);
    }

    public OauthAccessTokenResponse postAccessCodeWithPsd2OauthMeans(final BunqAuthenticationMeansV2 authenticationMeans,
                                                                     final String authorizationCode,
                                                                     final String redirectUri) throws TokenInvalidException {
        String fullPath = properties.getOauthTokenUrl() + String.format(ACCESS_TOKEN_EXCHANGE_FORMAT, authorizationCode, redirectUri, authenticationMeans.getClientId(), authenticationMeans.getClientSecret());

        return restClient.postEmptyBody(fullPath, OauthAccessTokenResponse.class, AUTOONBOARDING).getBody();
    }

    public MonetaryAccountResponse getAccounts(final String fromId, final BunqApiContext context) throws TokenInvalidException {
        String path = properties.getBaseUrl() + String.format(ACCOUNTS, context.getBunqUserId(), properties.getAccountsPerPage());
        return getFromId(path, fromId, context, MonetaryAccountResponse.class, GET_ACCOUNTS).getBody();
    }

    public TransactionsResponse getTransactions(final String fromId, final BunqApiContext context, final String monetaryAccountId) throws TokenInvalidException {
        String path = properties.getBaseUrl() + String.format(TRANSACTIONS, context.getBunqUserId(), monetaryAccountId, properties.getTransactionsPerPage());
        return getFromId(path, fromId, context, TransactionsResponse.class, GET_TRANSACTIONS).getBody();
    }

    private <T> ResponseEntity<T> getFromId(final String fullPath,
                                            final String fromId,
                                            final BunqApiContext context,
                                            final Class<T> responseType,
                                            final String prometheusMeter) throws TokenInvalidException {
        String uriPath = fullPath;
        if (fromId != null) {
            uriPath = uriPath + String.format(OLDER_ID_PATH, fromId);
        }
        return restClient.get(uriPath, context.getKeyPair(), context.getSessionToken(), responseType, prometheusMeter);
    }
}
