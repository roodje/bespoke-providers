package com.yolt.providers.monorepogroup.olbgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.consent.ConsentCreationRequest;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.consent.ConsentCreationResponse;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.fetchdata.AccountsResponse;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.fetchdata.BalancesResponse;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.fetchdata.TransactionsResponse;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Slf4j
public class OlbGroupHttpClientV1 extends DefaultHttpClientV2 implements OlbGroupHttpClient {

    private static final String CONSENT_ENDPOINT = "/v1/consents";
    private static final String ACCOUNTS_ENDPOINT = "/v1/accounts";
    private static final String BALANCES_TEMPLATE = "/v1/accounts/{accountId}/balances";
    private static final String TRANSACTIONS_TEMPLATE = "/v1/accounts/{accountId}/transactions";

    private final OlbGroupHttpHeadersProducer headersProducer;
    private final HttpErrorHandlerV2 errorHandler;

    OlbGroupHttpClientV1(MeterRegistry meterRegistry,
                         RestTemplate restTemplate,
                         String providerDisplayName,
                         OlbGroupHttpHeadersProducer headersProducer,
                         HttpErrorHandlerV2 errorHandler) {
        super(meterRegistry, restTemplate, providerDisplayName);
        this.headersProducer = headersProducer;
        this.errorHandler = errorHandler;
    }

    @Override
    public ConsentCreationResponse createConsent(ConsentCreationRequest request,
                                                 String psuIpAddress,
                                                 String psuId,
                                                 String redirectUri) throws TokenInvalidException {
        HttpEntity<ConsentCreationRequest> entity = new HttpEntity<>(request, headersProducer.createConsentHeaders(psuIpAddress, psuId, redirectUri));
        return exchange(CONSENT_ENDPOINT, POST, entity, GET_ACCOUNT_ACCESS_CONSENT, ConsentCreationResponse.class, errorHandler).getBody();
    }

    @Override
    public void deleteConsent(String consentId) throws TokenInvalidException {
        exchange( CONSENT_ENDPOINT + "/" + consentId,
                HttpMethod.DELETE,
                null,
                DELETE_ACCOUNT_ACCESS_CONSENT,
                Void.class,
                errorHandler
        );
    }

    @Override
    public AccountsResponse getAccounts(String consentId, String psuIpAddress) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(consentId, psuIpAddress));
        return exchange(ACCOUNTS_ENDPOINT, GET, entity, GET_ACCOUNTS, AccountsResponse.class, errorHandler).getBody();
    }

    @Override
    public BalancesResponse getBalances(String accountId, String consentId, String psuIpAddress) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(consentId, psuIpAddress));
        return exchange(BALANCES_TEMPLATE, GET, entity, GET_BALANCES_BY_ACCOUNT_ID, BalancesResponse.class, errorHandler, accountId).getBody();
    }

    @Override
    public TransactionsResponse getTransactions(String url, String consentId, String psuIpAddress) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(consentId, psuIpAddress));
        return exchange(url, GET, entity, GET_TRANSACTIONS_BY_ACCOUNT_ID, TransactionsResponse.class, errorHandler).getBody();
    }

    @Override
    public TransactionsResponse getTransactions(String accountId, String consentId, String psuIpAddress, String dateFrom) throws TokenInvalidException {
        String url = UriComponentsBuilder.fromUriString(TRANSACTIONS_TEMPLATE)
                .queryParam("bookingStatus", "both")
                .queryParam("dateFrom", dateFrom)
                .buildAndExpand(accountId)
                .toUriString();
        return getTransactions(url, consentId, psuIpAddress);
    }
}