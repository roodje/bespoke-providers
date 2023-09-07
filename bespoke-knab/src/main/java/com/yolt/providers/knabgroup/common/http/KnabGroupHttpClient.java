package com.yolt.providers.knabgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.knabgroup.common.configuration.KnabGroupProperties;
import com.yolt.providers.knabgroup.common.dto.external.*;
import com.yolt.providers.knabgroup.common.dto.internal.ConsentRequest;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class KnabGroupHttpClient extends DefaultHttpClient {

    private static final String TOKEN_ENDPOINT = "/connect/token";
    private static final String CONSENTS_ENDPOINT = "/v2/consents";

    private static final String ACCOUNTS_ENDPOINT = "/v1/accounts";
    private static final String ACCOUNT_ID_PARAMETER = "{accountId}";
    private static final String BALANCES_ENDPOINT = ACCOUNTS_ENDPOINT + "/" + ACCOUNT_ID_PARAMETER + "/balances";
    private static final String TRANSACTIONS_ENDPOINT = ACCOUNTS_ENDPOINT + "/" + ACCOUNT_ID_PARAMETER + "/transactions?bookingStatus=booked";
    private static final String DATE_FROM_REQUEST_PARAMETER = "&dateFrom=";

    private final KnabGroupProperties properties;
    private final HttpErrorHandler getLoginInfoErrorHandler;
    private final HttpErrorHandler createAccessMeansErrorHandler;
    private final HttpErrorHandler refreshAccessMeansErrorHandler;
    private final HttpErrorHandler fetchDataErrorHandler;


    public KnabGroupHttpClient(MeterRegistry registry,
                               RestTemplate restTemplate,
                               String provider,
                               KnabGroupProperties properties,
                               HttpErrorHandler getLoginInfoErrorHandler,
                               HttpErrorHandler createAccessMeansErrorHandler,
                               HttpErrorHandler refreshAccessMeansErrorHandler,
                               HttpErrorHandler fetchDataErrorHandler) {
        super(registry, restTemplate, provider);
        this.properties = properties;
        this.getLoginInfoErrorHandler = getLoginInfoErrorHandler;
        this.createAccessMeansErrorHandler = createAccessMeansErrorHandler;
        this.refreshAccessMeansErrorHandler = refreshAccessMeansErrorHandler;
        this.fetchDataErrorHandler = fetchDataErrorHandler;
    }


    public AuthData postForClientToken(final HttpEntity<Map<String, String>> request) {
        AuthData authData = null;
        try {
            authData = exchange(properties.getAuthorizationUrl() + TOKEN_ENDPOINT, HttpMethod.POST, request, ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT, AuthData.class, getLoginInfoErrorHandler).getBody();
        } catch (TokenInvalidException ignored) {
        }
        return authData;
    }

    public ConsentResponse postForConsent(final HttpEntity<ConsentRequest> request) {
        ConsentResponse consentResponse = null;
        try {
            consentResponse = exchange(CONSENTS_ENDPOINT, HttpMethod.POST, request, ProviderClientEndpoints.RETRIEVE_ACCOUNT_ACCESS_CONSENT, ConsentResponse.class, getLoginInfoErrorHandler).getBody();
        } catch (TokenInvalidException ignored) {
        }
        return consentResponse;
    }

    public AuthData postForUserTokenWithAuthorizationCode(final HttpEntity<Map<String, String>> request) {
        AuthData authData = null;
        try {
            authData = exchange(properties.getAuthorizationUrl() + TOKEN_ENDPOINT, HttpMethod.POST, request, ProviderClientEndpoints.GET_ACCESS_TOKEN, AuthData.class, createAccessMeansErrorHandler).getBody();
        } catch (TokenInvalidException ignored) {
        }
        return authData;
    }

    public AuthData postForUserTokenWithRefreshToken(final HttpEntity<Map<String, String>> request) throws TokenInvalidException {
        return exchange(properties.getAuthorizationUrl() + TOKEN_ENDPOINT, HttpMethod.POST, request, ProviderClientEndpoints.REFRESH_TOKEN, AuthData.class, refreshAccessMeansErrorHandler).getBody();
    }

    public Accounts fetchAccounts(final HttpEntity<?> request) throws TokenInvalidException {
        return exchange(ACCOUNTS_ENDPOINT, HttpMethod.GET, request, ProviderClientEndpoints.GET_ACCOUNTS, Accounts.class, fetchDataErrorHandler).getBody();
    }

    public Balances fetchBalances(final HttpEntity<?> request, String accountId) throws TokenInvalidException {
        return exchange(BALANCES_ENDPOINT, HttpMethod.GET, request, ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID, Balances.class, fetchDataErrorHandler, accountId).getBody();
    }

    public Transactions fetchTransactions(final HttpEntity<?> request, String dateFrom, String accountId) throws TokenInvalidException {
        String requestPath = TRANSACTIONS_ENDPOINT;
        if (StringUtils.isNotEmpty(dateFrom)) {
            requestPath = requestPath + DATE_FROM_REQUEST_PARAMETER + dateFrom;
        }
        return exchange(requestPath, HttpMethod.GET, request, ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID, Transactions.class, fetchDataErrorHandler, accountId).getBody();
    }
}
