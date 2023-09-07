package com.yolt.providers.openbanking.ais.kbciegroup.common.oauth2.token;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.PostSecretTokenBodyProducer;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.util.Arrays;
import java.util.List;

import static com.yolt.providers.common.constants.OAuth.*;

public class KbcIeGroupOauthTokenBodyProducer extends PostSecretTokenBodyProducer {

    @Override
    public String getCreateClientCredentialsBody(DefaultAuthMeans authenticationMeans, TokenScope scope, String... args) {
        List<NameValuePair> queryParameters = Arrays.asList(new BasicNameValuePair(GRANT_TYPE, CLIENT_CREDENTIALS),
                new BasicNameValuePair(SCOPE, scope.getGrantScope()),
                new BasicNameValuePair(CLIENT_ID, authenticationMeans.getClientId()),
                new BasicNameValuePair(CLIENT_SECRET, authenticationMeans.getClientSecret()));
        return URLEncodedUtils.format((Iterable<? extends NameValuePair>) queryParameters, null);
    }

    @Override
    public String getCreateAccessTokenBody(DefaultAuthMeans authenticationMeans, String authorizationCode, String redirectURI, String... args) {
        List<NameValuePair> queryParameters = Arrays.asList(new BasicNameValuePair(GRANT_TYPE, AUTHORIZATION_CODE),
                new BasicNameValuePair(CLIENT_ID, authenticationMeans.getClientId()),
                new BasicNameValuePair(CLIENT_SECRET, authenticationMeans.getClientSecret()),
                new BasicNameValuePair(REDIRECT_URI, redirectURI),
                new BasicNameValuePair(SCOPE, args[0]),
                new BasicNameValuePair(CODE, authorizationCode));

        return URLEncodedUtils.format((Iterable<? extends NameValuePair>) queryParameters, null);
    }

    @Override
    public String getRefreshAccessTokenBody(DefaultAuthMeans authenticationMeans, String refreshToken, String... args) {
        List<NameValuePair> queryParameters = Arrays.asList(new BasicNameValuePair(GRANT_TYPE, REFRESH_TOKEN),
                new BasicNameValuePair(REFRESH_TOKEN, refreshToken),
                new BasicNameValuePair(CLIENT_ID, authenticationMeans.getClientId()),
                new BasicNameValuePair(CLIENT_SECRET, authenticationMeans.getClientSecret()),
                new BasicNameValuePair(REDIRECT_URI, args[0]),
                new BasicNameValuePair(SCOPE, args[1]));
        return URLEncodedUtils.format((Iterable<? extends NameValuePair>) queryParameters, null);
    }
}
