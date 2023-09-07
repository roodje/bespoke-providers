package com.yolt.providers.bancacomercialaromana.common.http;

import com.yolt.providers.bancacomercialaromana.common.configuration.BcrGroupProperties;
import com.yolt.providers.bancacomercialaromana.common.model.Token;
import com.yolt.providers.bancacomercialaromana.common.model.fetchdata.*;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.GET_ACCOUNTS;
import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID;
import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

public class BcrGroupHttpClient extends DefaultHttpClient {

    private static final String ACCOUNTS_ENDPOINT = "/v1/accounts";
    private static final String BALANCES_TEMPLATE = "/v1/accounts/%s/balances";

    private final BcrGroupHeadersFactory headersFactory;
    private final BcrGroupProperties properties;

    BcrGroupHttpClient(MeterRegistry registry,
                       RestTemplate restTemplate,
                       String provider,
                       BcrGroupHeadersFactory headersFactory,
                       BcrGroupProperties properties) {
        super(registry, restTemplate, provider);
        this.headersFactory = headersFactory;
        this.properties = properties;

    }

    public Token postForToken(String clientId,
                              String clientSecret,
                              String providerClientEndpoint,
                              MultiValueMap<String, String> params) throws TokenInvalidException {
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headersFactory.getTokenHeaders(clientId, clientSecret));
        String url = UriComponentsBuilder
                .fromHttpUrl(properties.getTokenUrl())
                .queryParams(params)
                .toUriString();

        return exchangeForBody(url, POST, entity, providerClientEndpoint, Token.class);
    }

    public AccountsResponse getAccounts(String accessToken, String webApiKey) throws TokenInvalidException {
        HttpHeaders headers = headersFactory.getRequestHeaders(accessToken, webApiKey);
        return exchangeForBody(ACCOUNTS_ENDPOINT, GET, new HttpEntity<>(headers), GET_ACCOUNTS, AccountsResponse.class);
    }

    public BalancesResponse getBalances(String accessToken, String webApiKey, String accountId) throws TokenInvalidException {
        String url = String.format(BALANCES_TEMPLATE, accountId);
        HttpHeaders headers = headersFactory.getRequestHeaders(accessToken, webApiKey);
        return exchangeForBody(url, GET, new HttpEntity<>(headers), GET_BALANCES_BY_ACCOUNT_ID, BalancesResponse.class);
    }

    public TransactionsResponse getTransactions(String url, String accessToken, String webApiKey) throws TokenInvalidException {
        HttpHeaders headers = headersFactory.getRequestHeaders(accessToken, webApiKey);
        return exchangeForBody(url, GET, new HttpEntity<>(headers), GET_TRANSACTIONS_BY_ACCOUNT_ID, TransactionsResponse.class);
    }
}
