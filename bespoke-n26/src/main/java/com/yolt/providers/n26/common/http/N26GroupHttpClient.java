package com.yolt.providers.n26.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.n26.common.dto.N26GroupProviderState;
import com.yolt.providers.n26.common.dto.ais.consent.ConsentCreationRequest;
import com.yolt.providers.n26.common.dto.ais.consent.ConsentCreationResponse;
import com.yolt.providers.n26.common.dto.ais.consent.ConsentStatusResponse;
import com.yolt.providers.n26.common.dto.ais.fetchdata.AccountsResponse;
import com.yolt.providers.n26.common.dto.ais.fetchdata.BalancesResponse;
import com.yolt.providers.n26.common.dto.ais.fetchdata.TransactionsResponse;
import com.yolt.providers.n26.common.dto.token.TokenResponse;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;
import static org.springframework.http.HttpMethod.*;

@Slf4j
public class N26GroupHttpClient extends DefaultHttpClient {

    private static final String TOKEN_ENDPOINT = "?role=%s";
    private static final String CONSENT_ENDPOINT = "/v1/consents";
    private static final String CONSENT_STATUS_TEMPLATE = "/v1/consents/{consentId}/status";
    private static final String ACCOUNTS_ENDPOINT = "/v1/accounts";
    private static final String BALANCES_TEMPLATE = "/v1/accounts/{accountId}/balances";
    private static final String TRANSACTIONS_TEMPLATE = "/v1/accounts/{accountId}/transactions";
    private static final String DELETE_CONSENT_TEMPLATE = "/v1/consents/{consentId}";

    private final N26GroupHttpHeadersProducer headersProducer;

    N26GroupHttpClient(MeterRegistry meterRegistry,
                       RestTemplate restTemplate,
                       String providerDisplayName,
                       N26GroupHttpHeadersProducer headersProducer) {
        super(meterRegistry, restTemplate, providerDisplayName);
        this.headersProducer = headersProducer;
    }

    public String getTransactionsTemplatePath() {
        return TRANSACTIONS_TEMPLATE;
    }

    public String getBalancesTemplatePath() {
        return BALANCES_TEMPLATE;
    }

    public ConsentCreationResponse postConsentCreation(ConsentCreationRequest request, N26GroupProviderState providerState) throws TokenInvalidException {
        HttpEntity<ConsentCreationRequest> entity = new HttpEntity<>(request, headersProducer.createConsentHeaders(providerState));
        return exchangeForBody(CONSENT_ENDPOINT, POST, entity, GET_ACCOUNT_ACCESS_CONSENT, ConsentCreationResponse.class);
    }

    public ResponseEntity<String> getAuthorize(String url) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createAuthorizeHeaders());
        return exchange(url, GET, entity, "get_authorize", String.class);
    }

    public ConsentStatusResponse getConsentStatus(N26GroupProviderState providerState) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createConsentHeaders(providerState));
        return exchangeForBody(CONSENT_STATUS_TEMPLATE, GET, entity, RETRIEVE_ACCOUNT_ACCESS_CONSENT, ConsentStatusResponse.class, providerState.getConsentId());
    }

    public TokenResponse postAccessToken(String url, MultiValueMap<String, String> body, String role) throws TokenInvalidException {
        return postToken(url + TOKEN_ENDPOINT, body, GET_ACCESS_TOKEN, role);
    }

    public TokenResponse postRefreshToken(String url, MultiValueMap<String, String> body, String role) throws TokenInvalidException {
        return postToken(url + TOKEN_ENDPOINT, body, REFRESH_TOKEN, role);
    }

    private TokenResponse postToken(String url, MultiValueMap<String, String> body, String providerClientEndpoint, String role) throws TokenInvalidException {
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headersProducer.createTokenHeaders());
        String endpoint = String.format(url, role);
        return exchangeForBody(endpoint, POST, entity, providerClientEndpoint, TokenResponse.class);
    }

    public AccountsResponse getAccounts(N26GroupProviderState providerState, String psuIpAddress) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(providerState, psuIpAddress));
        return exchangeForBody(ACCOUNTS_ENDPOINT, GET, entity, GET_ACCOUNTS, AccountsResponse.class);
    }

    public BalancesResponse getBalances(String url, N26GroupProviderState providerState, String psuIpAddress) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(providerState, psuIpAddress));
        return exchangeForBody(url, GET, entity, GET_BALANCES_BY_ACCOUNT_ID, BalancesResponse.class);
    }

    public TransactionsResponse getTransactions(String url, N26GroupProviderState providerState, String psuIpAddress) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(providerState, psuIpAddress));
        return exchangeForBody(url, GET, entity, GET_TRANSACTIONS_BY_ACCOUNT_ID, TransactionsResponse.class);
    }

    public void deleteConsent(N26GroupProviderState providerState) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createConsentHeaders(providerState));
        exchange(DELETE_CONSENT_TEMPLATE, DELETE, entity, DELETE_ACCOUNT_ACCESS_CONSENT, Void.class, providerState.getConsentId());
    }
}