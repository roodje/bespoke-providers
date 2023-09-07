package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.service;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.dto.Accounts;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.dto.Transactions;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class BankVanBredaGroupDataHttpClient extends DefaultHttpClientV2 {

    private static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    private static final String CONSENT_ID = "Consent-ID";
    private static final String ACCOUNTS_ENDPOINT = "/berlingroup/v1/accounts?withBalance=true";
    private static final String TRANSACTIONS_ENDPOINT_TEMPLATE = "/berlingroup/v1/accounts/{accountId}/transactions?dateFrom={dateFrom}&bookingStatus=both";

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private final HttpErrorHandlerV2 errorHandler;

    public BankVanBredaGroupDataHttpClient(HttpErrorHandlerV2 errorHandler,
                                           MeterRegistry registry,
                                           RestTemplate restTemplate,
                                           String provider) {
        super(registry, restTemplate, provider);
        this.errorHandler = errorHandler;
    }

    public Accounts getAccounts(String consentId,
                                String psuIp,
                                String accessToken) throws TokenInvalidException {
        return exchange(ACCOUNTS_ENDPOINT,
                consentId,
                psuIp,
                accessToken,
                ProviderClientEndpoints.GET_ACCOUNTS,
                Accounts.class);
    }

    public Transactions getAccountTransactions(String accountId,
                                               String consentId,
                                               String psuIp,
                                               String accessToken,
                                               Instant transactionsFetchStartTime) throws TokenInvalidException {

        return exchange(TRANSACTIONS_ENDPOINT_TEMPLATE,
                consentId,
                psuIp,
                accessToken,
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                Transactions.class,
                accountId,
                DATE_FORMATTER.format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC)));
    }

    public Transactions getNextAccountTransactionsNextPage(String nextPageUrl,
                                                           String consentId,
                                                           String psuIp,
                                                           String accessToken) throws TokenInvalidException {
        return exchange(nextPageUrl,
                consentId,
                psuIp,
                accessToken,
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                Transactions.class);
    }

    private <T> T exchange(String endpoint,
                           String consentId,
                           String psuIp,
                           String accessToken,
                           String prometheusEndpoint,
                           Class<T> returnType,
                           String... additionalUrlParameters) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.isNotEmpty(psuIp)) {
            headers.add(PSU_IP_ADDRESS, psuIp);
        }
        headers.add(CONSENT_ID, consentId);
        headers.setBearerAuth(accessToken);
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        return exchange(endpoint,
                HttpMethod.GET,
                entity,
                prometheusEndpoint,
                returnType,
                errorHandler,
                additionalUrlParameters).getBody();
    }
}
