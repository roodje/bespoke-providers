package com.yolt.providers.brdgroup.common.http;

import com.yolt.providers.brdgroup.common.BrdGroupAccessMeans;
import com.yolt.providers.brdgroup.common.dto.consent.CreateConsentRequest;
import com.yolt.providers.brdgroup.common.dto.consent.CreateConsentResponse;
import com.yolt.providers.brdgroup.common.dto.consent.GetConsentResponse;
import com.yolt.providers.brdgroup.common.dto.fetchdata.AccountsResponse;
import com.yolt.providers.brdgroup.common.dto.fetchdata.BalancesResponse;
import com.yolt.providers.brdgroup.common.dto.fetchdata.TransactionsResponse;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.springframework.http.HttpMethod.*;

@Slf4j
public class BrdGroupHttpClientV1 extends DefaultHttpClientV2 implements BrdGroupHttpClient {

    private static final String POST_CONSENT_ENDPOINT = "/v1/consents";
    private static final String GET_CONSENT_STATUS_ENDPOINT = "/v1/consents/{consentId}";
    private static final String GET_ACCOUNTS_ENDPOINT = "/v1/accounts";
    private static final String GET_ACCOUNT_BALANCES_ENDPOINT = "/v1/accounts/{accountId}/balances";
    private static final String GET_ACCOUNT_TRANSACTIONS_ENDPOINT = "/v1/accounts/{accountId}/transactions?dateFrom={dateFrom}&bookingStatus=both";

    private static final String PSU_IP_ADDRESS_HEADER_NAME = "psu-ip-address";
    private static final String PSU_ID_HEADER_NAME = "psu-id";
    private static final String CONSENT_ID_HEADER_NAME = "consent-id";

    private static final DefaultHttpErrorHandlerV2 ERROR_HANDLER_V2 = new DefaultHttpErrorHandlerV2();

    public BrdGroupHttpClientV1(MeterRegistry registry,
                                RestTemplate restTemplate,
                                String provider) {
        super(registry, restTemplate, provider);
    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public CreateConsentResponse postConsentCreation(CreateConsentRequest request, String psuIpAddress, String psuId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add(PSU_ID_HEADER_NAME, psuId);
        headers.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);

        return exchange(POST_CONSENT_ENDPOINT,
                POST,
                new HttpEntity<>(request, headers),
                ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT,
                CreateConsentResponse.class,
                ERROR_HANDLER_V2
        ).getBody();
    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public GetConsentResponse getConsentStatus(String consentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        return exchange(GET_CONSENT_STATUS_ENDPOINT,
                GET,
                new HttpEntity<>(headers),
                ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT,
                GetConsentResponse.class,
                ERROR_HANDLER_V2,
                consentId
        ).getBody();
    }

    @Override
    public void deleteConsent(String consentId) {
        try {
            exchange(GET_CONSENT_STATUS_ENDPOINT,
                    DELETE,
                    null,
                    ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT,
                    Void.class,
                    ERROR_HANDLER_V2,
                    consentId
            );
        } catch (Exception e) {
            log.info("Exception occurred during consent removal: {}", e.getClass().getName());
        }

    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public AccountsResponse getAccounts(BrdGroupAccessMeans accessMeans, String psuIpAddress) {
        return exchange(GET_ACCOUNTS_ENDPOINT,
                GET,
                new HttpEntity<>(getFetchDataHeaders(accessMeans.getConsentId(), psuIpAddress)),
                ProviderClientEndpoints.GET_ACCOUNTS,
                AccountsResponse.class,
                ERROR_HANDLER_V2
        ).getBody();
    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public BalancesResponse getBalances(BrdGroupAccessMeans accessMeans, String psuIpAddress, String accountId) {
        return exchange(GET_ACCOUNT_BALANCES_ENDPOINT,
                GET,
                new HttpEntity<>(getFetchDataHeaders(accessMeans.getConsentId(), psuIpAddress)),
                ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID,
                BalancesResponse.class,
                ERROR_HANDLER_V2,
                accountId
        ).getBody();
    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public TransactionsResponse getTransactions(BrdGroupAccessMeans accessMeans,
                                                String accountId,
                                                String dateFrom,
                                                String psuIpAddress) {
        return exchange(GET_ACCOUNT_TRANSACTIONS_ENDPOINT,
                GET,
                new HttpEntity<>(getFetchDataHeaders(accessMeans.getConsentId(), psuIpAddress)),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                TransactionsResponse.class,
                ERROR_HANDLER_V2,
                accountId,
                dateFrom
        ).getBody();
    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public TransactionsResponse getTransactionsNextPage(String nextPageEndpoint, BrdGroupAccessMeans accessMeans, String psuIpAddress) {
        return exchange(nextPageEndpoint,
                GET,
                new HttpEntity<>(getFetchDataHeaders(accessMeans.getConsentId(), psuIpAddress)),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                TransactionsResponse.class,
                ERROR_HANDLER_V2
        ).getBody();
    }

    private HttpHeaders getFetchDataHeaders(String consentId, String psuIpAddress) {
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(psuIpAddress)) {
            headers.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);
        }
        headers.add(CONSENT_ID_HEADER_NAME, consentId);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }
}
