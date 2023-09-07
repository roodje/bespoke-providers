package com.yolt.providers.alpha.common.http;

import com.yolt.providers.alpha.common.auth.dto.AccountsRequestsResponse;
import com.yolt.providers.alpha.common.auth.dto.AlphaToken;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV3;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class AlphaHttpClient extends DefaultHttpClientV3 {

    private static final String CLIENT_TOKEN_ENDPOINT = "/auth/token";
    private static final String CLIENT_CREDENTIALS_TOKEN_ENDPOINT = "/auth/ccToken";
    private static final String ACCOUNT_REQUESTS_ENDPOINT = "/accounts/v1/account-requests";
    private final HttpErrorHandlerV2 errorHandlerV2;

    public AlphaHttpClient(final MeterRegistry registry,
                           final RestTemplate restTemplate,
                           final String provider,
                           HttpErrorHandlerV2 errorHandler) {
        super(registry, restTemplate, provider);
        errorHandlerV2 = errorHandler;
    }

    public AlphaToken postForCCToken(String authUrl, final HttpHeaders headers,
                                     final MultiValueMap<String, String> body,
                                     String prometheusPathOverride) throws TokenInvalidException {
        return exchange(authUrl + CLIENT_CREDENTIALS_TOKEN_ENDPOINT, HttpMethod.POST, new HttpEntity<>(body, headers), prometheusPathOverride, AlphaToken.class, errorHandlerV2).getBody();
    }

    public AlphaToken postForToken(String authUrl, final HttpHeaders headers,
                                   final MultiValueMap<String, String> body,
                                   String prometheusPathOverride) throws TokenInvalidException {
        return exchange(authUrl + CLIENT_TOKEN_ENDPOINT, HttpMethod.POST, new HttpEntity<>(body, headers), prometheusPathOverride, AlphaToken.class, errorHandlerV2).getBody();
    }

    public AccountsRequestsResponse postForAccountsRequests(final HttpHeaders headers,
                                                            final String body, final String prometheusPathOverride) throws TokenInvalidException {
        return exchange(ACCOUNT_REQUESTS_ENDPOINT, HttpMethod.POST, new HttpEntity<>(body, headers), prometheusPathOverride, AccountsRequestsResponse.class, errorHandlerV2).getBody();
    }
}
