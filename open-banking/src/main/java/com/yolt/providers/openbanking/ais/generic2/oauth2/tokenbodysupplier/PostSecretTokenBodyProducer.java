package com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.util.Arrays;
import java.util.List;

import static com.yolt.providers.common.constants.OAuth.*;

public class PostSecretTokenBodyProducer implements TokenRequestBodyProducer<String> {
    @Override
    public String getRefreshAccessTokenBody(DefaultAuthMeans authenticationMeans, String refreshToken, String... args) {
        List<NameValuePair> queryParameters = Arrays.asList(new BasicNameValuePair(GRANT_TYPE, "refresh_token"),
                new BasicNameValuePair(REFRESH_TOKEN, refreshToken),
                new BasicNameValuePair(CLIENT_ID, authenticationMeans.getClientId()),
                new BasicNameValuePair(CLIENT_SECRET, authenticationMeans.getClientSecret()));
        return URLEncodedUtils.format((Iterable<? extends NameValuePair>) queryParameters, null);
    }

    @Override
    public String getCreateAccessTokenBody(DefaultAuthMeans authenticationMeans, String authorizationCode, String redirectURI, String... args) {
        List<NameValuePair> queryParameters = Arrays.asList(new BasicNameValuePair(GRANT_TYPE, "authorization_code"),
                new BasicNameValuePair(REDIRECT_URI, redirectURI),
                new BasicNameValuePair(CODE, authorizationCode),
                new BasicNameValuePair(CLIENT_ID, authenticationMeans.getClientId()),
                new BasicNameValuePair(CLIENT_SECRET, authenticationMeans.getClientSecret()));
        return URLEncodedUtils.format((Iterable<? extends NameValuePair>) queryParameters, null);
    }

    @Override
    public String getCreateClientCredentialsBody(DefaultAuthMeans authenticationMeans, TokenScope scope, String... args) {
        List<NameValuePair> queryParameters = Arrays.asList(new BasicNameValuePair(GRANT_TYPE, "client_credentials"),
                new BasicNameValuePair(SCOPE, scope.getGrantScope()));
        return URLEncodedUtils.format((Iterable<? extends NameValuePair>) queryParameters, null);
    }
}
