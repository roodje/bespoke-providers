package com.yolt.providers.axabanque.common.fetchdata.http.client;

import com.yolt.providers.axabanque.common.fetchdata.http.headerproducer.FetchDataRequestHeadersProducer;
import com.yolt.providers.axabanque.common.model.external.Accounts;
import com.yolt.providers.axabanque.common.model.external.Balances;
import com.yolt.providers.axabanque.common.model.external.Transactions;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class DefaultFetchDataHttpClientV2 extends DefaultHttpClientV2 implements FetchDataHttpClient {

    private static final String ACCOUNTS_ENDPOINT = "/{version}/accounts?withBalance=true";
    private static final String BALANCES_ENDPOINT = "/{version}/accounts/{accountId}/balances";
    private static final String TRANSACTIONS_ENDPOINT = "/{version}/accounts/{accountId}/transactions";

    private final String endpointVersion;
    private final FetchDataRequestHeadersProducer headersProducer;
    private final DateTimeFormatter transactionFromTimeFormatter;
    private final HttpErrorHandlerV2 errorHandler;

    public DefaultFetchDataHttpClientV2(MeterRegistry registry,
                                        RestTemplate restTemplate,
                                        String provider,
                                        String endpointVersion,
                                        FetchDataRequestHeadersProducer headersProducer,
                                        DateTimeFormatter transactionFromTimeFormatter,
                                        HttpErrorHandlerV2 errorHandler) {
        super(registry, restTemplate, provider);
        this.endpointVersion = endpointVersion;
        this.headersProducer = headersProducer;
        this.transactionFromTimeFormatter = transactionFromTimeFormatter;
        this.errorHandler = errorHandler;
    }

    @Override
    public Accounts getAccounts(String accessToken, String consentId, String xRequestId, String psuIpAddress) throws TokenInvalidException {
        HttpHeaders headers = headersProducer.getAccountsHeaders(accessToken, consentId, xRequestId, psuIpAddress);
        return exchange(ACCOUNTS_ENDPOINT, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_ACCOUNTS, Accounts.class, psuIpAddress, errorHandler, endpointVersion).getBody();
    }

    @Override
    public Balances getBalances(String accessToken, String consentId, String accountId, String xRequestId, String psuIpAddress) throws TokenInvalidException {
        HttpHeaders headers = headersProducer.getFetchBalancesHeaders(accessToken, consentId, xRequestId, psuIpAddress);
        return exchange(BALANCES_ENDPOINT, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID, Balances.class, psuIpAddress, errorHandler, endpointVersion, accountId).getBody();
    }

    @Override
    public Transactions getTransactions(String accountId, Instant fromDate, String accessToken, String consentId, String uri, String xRequestId, int pageNumber, String psuIpAddress) throws TokenInvalidException {
        HttpHeaders headers = headersProducer.getTransactionsHeaders(accessToken, consentId, xRequestId, psuIpAddress);
        return exchange(assembleTransactionUri(uri, pageNumber, transactionFromTimeFormatter.format(fromDate)), HttpMethod.GET,
                new HttpEntity<>(headers), ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID, Transactions.class, psuIpAddress, errorHandler, endpointVersion, accountId).getBody();
    }

    public String getTransactionsEndpoint() {
        return TRANSACTIONS_ENDPOINT;
    }

    private String assembleTransactionUri(String uri, int pageNumber, String dateFrom) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("withBalance", false)
                .queryParam("dateFrom", dateFrom)
                .queryParam("bookingStatus", "both");
        if (pageNumber != 0) {
            uriBuilder.queryParam("page", pageNumber);
        }
        return uriBuilder.build().toString();
    }
}
