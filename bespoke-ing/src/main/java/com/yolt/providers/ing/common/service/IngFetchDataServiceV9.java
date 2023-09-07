package com.yolt.providers.ing.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngUserAccessMeans;
import com.yolt.providers.ing.common.config.IngProperties;
import com.yolt.providers.ing.common.dto.Accounts;
import com.yolt.providers.ing.common.dto.Accounts.Account;
import com.yolt.providers.ing.common.dto.Balances;
import com.yolt.providers.ing.common.dto.CardTransactions;
import com.yolt.providers.ing.common.dto.Transactions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yolt.providers.ing.common.service.HttpErrorHandler.handleNon2xxResponseCodeFetchData;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@Slf4j
public class IngFetchDataServiceV9 implements IngFetchDataService {

    private static final String DIGEST = "Digest";
    private static final String X_REQUEST_ID = "X-Request-ID";
    private static final String X_ING_RESPONSE_ID = "X-ING-Response-ID";
    private static final String SIGNATURE = "Signature";

    private static final String CARD_ACCOUNTS_ENDPOINT_PART = "card-accounts";

    private static final String ACCOUNTS = "/v3/accounts";

    private static final DateTimeFormatter ING_DATETIME_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT"));
    private static final DateTimeFormatter TRANSACTION_FROM_TIME_FORMATTER_PARAMETER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);

    private static final String TRANSACTIONS_LIMIT_QUERY_PARAM = "limit";

    private final IngDataMapperServiceV6 mapperService;
    private final IngClientAwareRestTemplateService clientAwareRestTemplateService;
    private final IngSigningUtil ingSigningUtil;
    private final IngProperties properties;
    private final Clock clock;

    public DataProviderResponse fetchData(final IngUserAccessMeans accessMeans,
                                          final IngAuthenticationMeans authenticationMeans,
                                          final RestTemplateManager restTemplateManager,
                                          final Signer signer,
                                          final Instant transactionFetchStartTime,
                                          final Clock clock)
            throws ProviderFetchDataException, TokenInvalidException {
        RestTemplate restTemplate = clientAwareRestTemplateService.buildRestTemplate(authenticationMeans, restTemplateManager);
        DataFetchResult result = fetchData(restTemplate, signer, accessMeans, authenticationMeans, transactionFetchStartTime);
        return new DataProviderResponse(result.getResponseAccounts());
    }

    private DataFetchResult fetchData(final RestTemplate restTemplate, final Signer signer, final IngUserAccessMeans accessMeans, final IngAuthenticationMeans authenticationMeans, final Instant transactionsFetchStartTime) throws TokenInvalidException, ProviderFetchDataException {
        DataFetchResult result = new DataFetchResult();
        Accounts accountsResponse = getAccounts(restTemplate, signer, accessMeans, authenticationMeans, ACCOUNTS);
        for (Account account : accountsResponse.getData()) {//NOSONAR restTemplate has internal nonNull assert assuring result is not null
            try {
                ProviderAccountDTO accountDTO = fetchAccountData(restTemplate, signer, accessMeans, authenticationMeans, account, transactionsFetchStartTime);
                result.addFetchedAccount(accountDTO);
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return result;
    }

    private ProviderAccountDTO fetchAccountData(final RestTemplate restTemplate, final Signer signer, final IngUserAccessMeans accessMeans, final IngAuthenticationMeans authenticationMeans, final Account account, final Instant transactionsFetchStartTime) throws TokenInvalidException, ProviderFetchDataException {
        String accountId = account.getId();
        Balances accountBalances = getTransactionsOrBalances(restTemplate, signer, accessMeans, authenticationMeans, account.getBalancesLink(), Balances.class);
        ProviderAccountDTO accountDTO = mapperService.mapToProviderAccountDTO(account, accountBalances, clock);

        String url = account.getTransactionLink() + (account.getTransactionLink().contains("?") ? '&' : '?')
                + calculateDateFromParameter(transactionsFetchStartTime);
        List<ProviderTransactionDTO> transactionsConverted = isCreditAccount(account) ?
                retrieveCardTransactions(restTemplate, signer, accessMeans, authenticationMeans, accountId, url) :
                retrieveTransactions(restTemplate, signer, accessMeans, authenticationMeans, accountId, applyTransactionsPageSizeLimit(url));
        accountDTO.getTransactions().addAll(transactionsConverted);
        return accountDTO;
    }

    private String calculateDateFromParameter(final Instant transactionsFetchStartTime) {
        return transactionsFetchStartTime != null ? "dateFrom=" + TRANSACTION_FROM_TIME_FORMATTER_PARAMETER.format(Instant.ofEpochSecond(transactionsFetchStartTime.getEpochSecond())) : "";
    }

    private String applyTransactionsPageSizeLimit(String originalUrl) {
        int transactionsPageSizeLimit = properties.getTransactionsPageSizeLimit();
        return transactionsPageSizeLimit > 0 ?
                UriComponentsBuilder.fromUriString(originalUrl)
                        .queryParam(TRANSACTIONS_LIMIT_QUERY_PARAM, transactionsPageSizeLimit)
                        .build()
                        .toString()
                : originalUrl;
    }

    private boolean isCreditAccount(final Account account) {
        return account.getTransactionLink() != null && account.getTransactionLink().contains(CARD_ACCOUNTS_ENDPOINT_PART);
    }

    private List<ProviderTransactionDTO> retrieveTransactions(final RestTemplate restTemplate, final Signer signer, final IngUserAccessMeans accessMeans, final IngAuthenticationMeans authenticationMeans, final String accountId, String nextPageUrl) throws TokenInvalidException, ProviderFetchDataException {
        final List<ProviderTransactionDTO> transactionsConverted = new ArrayList<>();
        int pageCounter = 1;
        do {
            Transactions transactions = getTransactionsOrBalances(restTemplate, signer, accessMeans, authenticationMeans, nextPageUrl, Transactions.class);
            transactionsConverted.addAll(mapperService.mapToProviderTransactionDTO(transactions));
            nextPageUrl = transactions.getNextPageUrl(); //NOSONAR restTemplate has internal nonNull assert assuring result is not null
            pageCounter++;
        } while (nextPageUrl != null && pageCounter <= properties.getPaginationLimit());
        return transactionsConverted;
    }

    private List<ProviderTransactionDTO> retrieveCardTransactions(final RestTemplate restTemplate, final Signer signer, final IngUserAccessMeans accessMeans, final IngAuthenticationMeans authenticationMeans, final String accountId, String nextPageUrl) throws TokenInvalidException, ProviderFetchDataException {
        final List<ProviderTransactionDTO> transactionsConverted = new ArrayList<>();
        int pageCounter = 1;
        do {
            CardTransactions transactions = getTransactionsOrBalances(restTemplate, signer, accessMeans, authenticationMeans, nextPageUrl, CardTransactions.class);
            transactionsConverted.addAll(mapperService.mapToProviderCardTransactionDTO(transactions));
            nextPageUrl = transactions.getNextPageUrl(); //NOSONAR restTemplate has internal nonNull assert assuring result is not null
            pageCounter++;
        } while (nextPageUrl != null && pageCounter <= properties.getPaginationLimit());
        return transactionsConverted;
    }

    private <T> T getTransactionsOrBalances(final RestTemplate restTemplate, final Signer signer, final IngUserAccessMeans accessMeans, final IngAuthenticationMeans authenticationMeans, final String endpoint, Class<T> clazz) throws TokenInvalidException, ProviderFetchDataException {
        URI uri = constructUri(endpoint);
        String query = uri.getQuery() == null ? "" : "?" + uri.getQuery();
        String path = uri.getPath();
        return getData(restTemplate,
                uri.toString(),
                HttpMethod.GET,
                clazz,
                properties.getFetchDataRetryLimit(),
                signer,
                accessMeans,
                path + query,
                authenticationMeans.getSigningKeyId());
    }

    private Accounts getAccounts(final RestTemplate restTemplate, final Signer signer, final IngUserAccessMeans accessMeans, final IngAuthenticationMeans authenticationMeans, final String endpoint) throws TokenInvalidException, ProviderFetchDataException {
        String url = constructUri(endpoint).getPath();
        return getDataForAccounts(restTemplate,
                properties.getBaseUrl() + endpoint,
                HttpMethod.GET,
                Accounts.class,
                properties.getFetchDataRetryLimit(),
                signer,
                accessMeans,
                url,
                authenticationMeans.getSigningKeyId());
    }

    private <T> T getData(final RestTemplate restTemplate,
                          String url,
                          HttpMethod method,
                          Class<T> returnedClass,
                          int retryLimit,
                          Signer signer,
                          IngUserAccessMeans accessMeans,
                          String endpoint,
                          UUID signignKeyId) throws TokenInvalidException, ProviderFetchDataException {
        return getData(restTemplate, url, method, returnedClass, retryLimit, signer, accessMeans, endpoint, signignKeyId, 200);
    }

    private <T> T getDataForAccounts(final RestTemplate restTemplate,
                                     String url,
                                     HttpMethod method,
                                     Class<T> returnedClass,
                                     int retryLimit,
                                     Signer signer,
                                     IngUserAccessMeans accessMeans,
                                     String endpoint,
                                     UUID signignKeyId) throws TokenInvalidException, ProviderFetchDataException {
        return getDataForAccounts(restTemplate, url, method, returnedClass, retryLimit, signer, accessMeans, endpoint, signignKeyId, 200);
    }

    @SneakyThrows(InterruptedException.class)
    private <T> T getData(final RestTemplate restTemplate,
                          String url,
                          HttpMethod method,
                          Class<T> returnedClass,
                          int retryLimit,
                          Signer signer,
                          IngUserAccessMeans accessMeans,
                          String endpoint,
                          UUID signignKeyId,
                          long retryDelayInMillis) throws TokenInvalidException, ProviderFetchDataException {
        HttpHeaders headers = createHeaders(signer, accessMeans, endpoint, signignKeyId);
        HttpEntity request = new HttpEntity<>(headers);
        T result = null;
        try {
            result = restTemplate.exchange(url, method, request, returnedClass).getBody();
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().equals(NOT_FOUND) && retryLimit > 0) {
                // TODO: To be taken caren of in C4PO-8177
                TimeUnit.MILLISECONDS.sleep(retryDelayInMillis);
                result = getData(restTemplate, url, method, returnedClass, --retryLimit, signer, accessMeans, endpoint, signignKeyId, getDelay(retryDelayInMillis));
            } else {
                handleNon2xxResponseCodeFetchData(e.getStatusCode());
            }
        }
        return result;
    }

    @SneakyThrows(InterruptedException.class)
    private <T> T getDataForAccounts(final RestTemplate restTemplate,
                                     String url,
                                     HttpMethod method,
                                     Class<T> returnedClass,
                                     int retryLimit,
                                     Signer signer,
                                     IngUserAccessMeans accessMeans,
                                     String endpoint,
                                     UUID signignKeyId,
                                     long retryDelayInMillis) throws TokenInvalidException, ProviderFetchDataException {
        HttpHeaders headers = createHeaders(signer, accessMeans, endpoint, signignKeyId);
        HttpEntity request = new HttpEntity<>(headers);
        T result = null;
        try {
            result = restTemplate.exchange(url, method, request, returnedClass).getBody();
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().equals(NOT_FOUND)) {
                // TODO C4PO-8170 gathering Date, X-Request-ID and X-ING-Response-ID for ING support
                HttpHeaders errorHeaders = e.getResponseHeaders();
                if (errorHeaders != null) {
                    // TODO C4PO-8170 should be removed when the task is done
                    log.info("ING 404 support logs:\n {}:\"{}\", {}:\"{}\", {}:\"{}\"",
                            HttpHeaders.DATE, errorHeaders.get(HttpHeaders.DATE),
                            X_REQUEST_ID, errorHeaders.get(X_REQUEST_ID),
                            X_ING_RESPONSE_ID, errorHeaders.get(X_ING_RESPONSE_ID));
                }
                if (retryLimit > 0) {
                    // TODO: To be taken caren of in C4PO-8177
                    TimeUnit.MILLISECONDS.sleep(retryDelayInMillis);
                    return getDataForAccounts(restTemplate, url, method, returnedClass, --retryLimit, signer, accessMeans, endpoint, signignKeyId, getDelay(retryDelayInMillis));
                }
            }
            handleNon2xxResponseCodeFetchData(e.getStatusCode());
        }
        return result;
    }

    private long getDelay(long initialDelayInMillis) {
        return initialDelayInMillis >= 15000 ? 30000 : initialDelayInMillis * 2;
    }

    private URI constructUri(final String endpoint) {
        try {
            URI uri = new URI(endpoint);
            if (uri.isAbsolute()) {
                return uri;
            }
            return new URI(properties.getBaseUrl() + endpoint);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Error constructing url during ING call");
        }
    }

    private HttpHeaders createHeaders(final Signer signer, final IngUserAccessMeans accessMeans, final String url, final UUID signingKeyId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.DATE, ING_DATETIME_FORMATTER.format(Instant.now(clock)));
        headers.add(DIGEST, ingSigningUtil.getDigest(new LinkedMultiValueMap()));
        headers.add(X_REQUEST_ID, ExternalTracingUtil.createLastExternalTraceId());
        headers.setBearerAuth(accessMeans.getAccessToken());
        headers.add(SIGNATURE, ingSigningUtil.getSignature(HttpMethod.GET, url, headers, accessMeans.getClientAccessMeans().getClientId(), signingKeyId, signer));
        return headers;
    }

    @Getter
    private static class DataFetchResult {

        private final List<ProviderAccountDTO> responseAccounts = new ArrayList<>();

        private void addFetchedAccount(final ProviderAccountDTO account) {
            responseAccounts.add(account);
        }

    }
}
