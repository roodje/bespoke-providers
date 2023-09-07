package com.yolt.providers.bunq.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class BunqAuthenticationMeansV2 {

    public static final String CLIENT_ID = "client-id";
    public static final String CLIENT_SECRET_STRING = "client-secret-string";
    public static final String PSD2_USER_ID = "psd2-user-id";
    public static final String OAUTH_USER_ID = "oauth-user-id";
    public static final String PSD2_API_KEY = "psd2-api-key";
    public static final String SIGNING_CERTIFICATE = "signing-certificate";
    public static final String SIGNING_PRIVATE_KEY_ID = "client-signing-private-keyid";
    public static final String SIGNING_CERTIFICATE_CHAIN = "signing-certificate-chain";

    private final String clientId;
    private final String clientSecret;
    private final long psd2UserId;
    private final long oauthUserId;
    private final String psd2apiKey;

    public static Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeansMap.put(CLIENT_SECRET_STRING, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeansMap.put(PSD2_USER_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeansMap.put(OAUTH_USER_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeansMap.put(PSD2_API_KEY, TypedAuthenticationMeans.API_KEY_STRING);
        typedAuthenticationMeansMap.put(SIGNING_CERTIFICATE, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(SIGNING_PRIVATE_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeansMap.put(SIGNING_CERTIFICATE_CHAIN, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATES_CHAIN_PEM);
        return typedAuthenticationMeansMap;
    }

    public static BunqAuthenticationMeansV2 fromAuthenticationMeans(Map<String, BasicAuthenticationMean> authenticationMeans, String provider) {
        String clientId = getAuthenticationMeanValue(authenticationMeans, CLIENT_ID, provider);
        String clientSecret = getAuthenticationMeanValue(authenticationMeans, CLIENT_SECRET_STRING, provider);
        long psd2UserId = Long.parseLong(getAuthenticationMeanValue(authenticationMeans, PSD2_USER_ID, provider));
        long oauthUserId = Long.parseLong(getAuthenticationMeanValue(authenticationMeans, OAUTH_USER_ID, provider));
        String psd2ApiKey = getAuthenticationMeanValue(authenticationMeans, PSD2_API_KEY, provider);
        return new BunqAuthenticationMeansV2(clientId, clientSecret, psd2UserId, oauthUserId, psd2ApiKey);
    }

    private static String getAuthenticationMeanValue(final Map<String, BasicAuthenticationMean> authenticationMeansMap,
                                                     final String key,
                                                     final String provider) {
        BasicAuthenticationMean authenticationMean = authenticationMeansMap.get(key);
        if (authenticationMean == null) {
            throw new MissingAuthenticationMeansException(provider, key);
        }
        return authenticationMean.getValue();
    }
}
