package com.yolt.providers.deutschebank.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import com.yolt.providers.deutschebank.common.domain.model.consent.ConsentCreationRequest;
import com.yolt.providers.deutschebank.common.domain.model.consent.ConsentCreationResponse;
import com.yolt.providers.deutschebank.common.domain.model.consent.ConsentStatusResponse;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.AccountsResponse;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.BalancesResponse;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.TransactionsResponse;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Slf4j
public class DeutscheBankGroupHttpClientV2 extends DefaultHttpClientV2 implements DeutscheBankGroupHttpClient {

    private final DeutscheBankGroupHttpHeadersProducer headersProducer;
    private final HttpErrorHandlerV2 errorHandler;

    DeutscheBankGroupHttpClientV2(MeterRegistry meterRegistry,
                                  RestTemplate restTemplate,
                                  String providerDisplayName,
                                  DeutscheBankGroupHttpHeadersProducer headersProducer,
                                  HttpErrorHandlerV2 errorHandler) {
        super(meterRegistry, restTemplate, providerDisplayName);
        this.headersProducer = headersProducer;
        this.errorHandler = errorHandler;
    }

    @Override
    public ConsentCreationResponse postConsentCreation(String url, ConsentCreationRequest request, String psuIpAddress, String psuId, String redirectUri, String nokRedirectUri) throws TokenInvalidException {
        HttpEntity<ConsentCreationRequest> entity = new HttpEntity<>(request, headersProducer.createConsentHeaders(psuIpAddress, psuId, redirectUri, nokRedirectUri));
        return exchange(url, POST, entity, GET_ACCOUNT_ACCESS_CONSENT, ConsentCreationResponse.class, errorHandler).getBody();
    }

    @Override
    public ConsentStatusResponse getConsentStatus(String url, String psuIpAddress) throws TokenInvalidException {
        HttpEntity entity = new HttpEntity<>(headersProducer.createConsentStatusHeaders(psuIpAddress));
        return exchange(url, GET, entity, RETRIEVE_ACCOUNT_ACCESS_CONSENT, ConsentStatusResponse.class, errorHandler).getBody();
    }

    @Override
    public AccountsResponse getAccounts(String url, DeutscheBankGroupProviderState providerState, String psuIpAddress) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(providerState, psuIpAddress));
        return exchange(url, GET, entity, GET_ACCOUNTS, AccountsResponse.class, errorHandler).getBody();
    }

    @Override
    public BalancesResponse getBalances(String url, DeutscheBankGroupProviderState providerState, String psuIpAddress) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(providerState, psuIpAddress));
        return exchange(url, GET, entity, GET_BALANCES_BY_ACCOUNT_ID, BalancesResponse.class, errorHandler).getBody();
    }

    @Override
    public TransactionsResponse getTransactions(String url, DeutscheBankGroupProviderState providerState, String psuIpAddress) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(providerState, psuIpAddress));
        return exchange(url, GET, entity, GET_TRANSACTIONS_BY_ACCOUNT_ID, TransactionsResponse.class, errorHandler).getBody();
    }
}