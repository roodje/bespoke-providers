package com.yolt.providers.knabgroup.common.payment;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.dto.external.AuthData;
import com.yolt.providers.knabgroup.common.http.KnabGroupHttpClient;
import com.yolt.providers.knabgroup.common.http.KnabGroupHttpClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.SCOPE;

@RequiredArgsConstructor
public class DefaultPisAccessTokenProvider {

    private static final String GRANT_TYPE = "grant_type";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private static final String PSD2 = "psd2";

    private final KnabGroupHttpClientFactory httpClientFactory;

    public String getClientAccessToken(final KnabGroupAuthenticationMeans authenticationMeans,
                                       final RestTemplateManager restTemplateManager) {
        AuthData token = doClientCredentialsGrant(authenticationMeans, restTemplateManager);
        return token.getAccessToken();
    }

    private AuthData doClientCredentialsGrant(final KnabGroupAuthenticationMeans authenticationMeans,
                                              final RestTemplateManager restTemplateManager) {
        KnabGroupHttpClient httpClient = httpClientFactory.createKnabGroupHttpClient(restTemplateManager, authenticationMeans);
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, GRANT_TYPE_CLIENT_CREDENTIALS);
        requestPayload.add(SCOPE, PSD2);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setCacheControl(CacheControl.noCache());
        headers.setBasicAuth(authenticationMeans.getClientId(), authenticationMeans.getClientSecret());

        return httpClient.postForClientToken(new HttpEntity(requestPayload, headers));
    }
}
