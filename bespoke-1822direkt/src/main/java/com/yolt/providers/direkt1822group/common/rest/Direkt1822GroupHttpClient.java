package com.yolt.providers.direkt1822group.common.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.direkt1822group.common.dto.AccountReference;
import com.yolt.providers.direkt1822group.common.dto.AccountsResponse;
import com.yolt.providers.direkt1822group.common.dto.BalancesResponse;
import com.yolt.providers.direkt1822group.common.dto.ConsentAccess;
import com.yolt.providers.direkt1822group.common.dto.ConsentCreationRequest;
import com.yolt.providers.direkt1822group.common.dto.ConsentCreationResponse;
import com.yolt.providers.direkt1822group.common.dto.Link;
import com.yolt.providers.direkt1822group.common.dto.Links;
import com.yolt.providers.direkt1822group.common.dto.Transactions;
import com.yolt.providers.direkt1822group.common.dto.TransactionsResponse;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Direkt1822GroupHttpClient extends DefaultHttpClient {

    public static final String TPP_REDIRECT_URI_HEADER_NAME = "TPP-Redirect-URI";
    public static final String TPP_ERROR_REDIRECT_URI_HEADER_NAME = "TPP-Nok-Redirect-URI";
    public static final String PSU_IP_ADDRESS_HEADER_NAME = "PSU-IP-Address";
    private static final String CONSENT_ID_HEADER_NAME = "Consent-ID";

    private static final String CONSENT_ENDPOINT = "/r1/v1/consents";
    private static final String CONSENT_DELETION_ENDPOINT = "/r1/v1/consents/{consentId}";
    private static final String FETCH_ACCOUNTS_ENDPOINT = "/r1/v1/accounts";
    private static final String FETCH_BALANCES_ENDPOINT = "/r1/v1/accounts/{accountId}/balances";
    private static final String FETCH_TRANSACTIONS_ENDPOINT = "/r1/v1/accounts/%s/transactions?bookingStatus=both&dateFrom=%s";

    private static final int MAX_FREQUENCY_PER_DAY = 4;
    private static final int CONSENT_VALIDITY_IN_DAYS = 90;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TRANSACTION_FROM_TIME_FORMATTER_PARAMETER = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);

    private final HttpErrorHandler httpErrorHandler;
    private final Clock clock;

    public Direkt1822GroupHttpClient(MeterRegistry registry,
                                     RestTemplate restTemplate,
                                     String provider,
                                     HttpErrorHandler httpErrorHandler,
                                     Clock clock
    ) {
        super(registry, restTemplate, provider);
        this.httpErrorHandler = httpErrorHandler;
        this.clock = clock;
    }

    public ConsentCreationResponse createConsent(String redirectUrl,
                                                 String psuIpAddress,
                                                 String iban,
                                                 String state) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add(TPP_REDIRECT_URI_HEADER_NAME, redirectUrl + "?state=" + state);
        headers.add(TPP_ERROR_REDIRECT_URI_HEADER_NAME, redirectUrl + "?error=true" + "&state=" + state);
        headers.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);

        List<AccountReference> accountReferences = Collections.singletonList(new AccountReference(iban));
        ConsentAccess consentAccess = new ConsentAccess(accountReferences, accountReferences, accountReferences);

        ConsentCreationRequest createConsentBody = new ConsentCreationRequest(consentAccess,
                true,
                LocalDate.now(clock).plusDays(CONSENT_VALIDITY_IN_DAYS).format(DATE_TIME_FORMATTER),
                MAX_FREQUENCY_PER_DAY,
                false
        );

        return exchange(
                CONSENT_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(createConsentBody, headers),
                ProviderClientEndpoints.GET_ACCOUNT_REQUEST_ID,
                ConsentCreationResponse.class).getBody();
    }

    public void deleteConsent(String psuIpAddress,
                              String consentId) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);
        exchange(CONSENT_DELETION_ENDPOINT,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT,
                Void.class,
                consentId);
    }

    public AccountsResponse fetchAccounts(String consentId,
                                          String psuIpAddress) throws TokenInvalidException {
        return exchange(FETCH_ACCOUNTS_ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(createFetchDataHeaders(consentId, psuIpAddress)),
                ProviderClientEndpoints.GET_ACCOUNTS,
                AccountsResponse.class)
                .getBody();
    }

    public BalancesResponse fetchBalances(String consentId,
                                          String accountId,
                                          String psuIpAddress) throws TokenInvalidException {
        return exchange(FETCH_BALANCES_ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(createFetchDataHeaders(consentId, psuIpAddress)),
                ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID,
                BalancesResponse.class,
                httpErrorHandler,
                accountId)
                .getBody();
    }

    public Transactions fetchTransactions(String consentId,
                                          String accountId,
                                          Instant transactionsFetchStartTime,
                                          String psuIpAddress) throws TokenInvalidException {
        String nextPage = String.format(FETCH_TRANSACTIONS_ENDPOINT, accountId, TRANSACTION_FROM_TIME_FORMATTER_PARAMETER.format(Instant.ofEpochSecond(transactionsFetchStartTime.getEpochSecond())));

        final Transactions resultTransactions = new Transactions(new ArrayList<>(), new ArrayList<>(), null);

        do {
            TransactionsResponse transactionsResponse = exchange(nextPage,
                    HttpMethod.GET,
                    new HttpEntity<>(createFetchDataHeaders(consentId, psuIpAddress)),
                    ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                    TransactionsResponse.class)
                    .getBody();

            if (transactionsResponse != null) {
                Optional<Transactions> receivedtransactions = Optional.ofNullable(transactionsResponse.getTransactions());
                receivedtransactions.ifPresent(newTransactions -> {
                    resultTransactions.getPending().addAll(newTransactions.getPending());
                    resultTransactions.getBooked().addAll(newTransactions.getBooked());
                });
                nextPage = receivedtransactions
                        .map(Transactions::getLinks)
                        .map(Links::getNext)
                        .map(Link::getHref)
                        .orElse("");
            } else {
                nextPage = "";
            }

        } while (!nextPage.isEmpty());

        return resultTransactions;
    }

    private HttpHeaders createFetchDataHeaders(String consentId, String psuIpAddress) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CONSENT_ID_HEADER_NAME, consentId);
        if (!StringUtils.isEmpty(psuIpAddress)) {
            httpHeaders.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);
        }
        return httpHeaders;
    }
}
