package com.yolt.providers.abancagroup.common;

import com.yolt.providers.abancagroup.common.ais.auth.dto.AbancaAuthData;
import com.yolt.providers.abancagroup.common.ais.config.AbancaGroupProperties;
import com.yolt.providers.abancagroup.common.ais.data.dto.Accounts;
import com.yolt.providers.abancagroup.common.ais.data.dto.Balance;
import com.yolt.providers.abancagroup.common.ais.data.dto.Transactions;
import com.yolt.providers.abancagroup.common.ais.data.service.AbancaSigningService;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static com.yolt.providers.common.constants.OAuth.*;
import static org.springframework.http.HttpMethod.GET;

public class AbancaHttpClient extends DefaultHttpClientV2 {

    private static final String TOKEN_ENDPOINT = "/oauth2/token";
    private static final String ACCOUNTS_ENDPOINT = "/v2/psd2/me/accounts";
    private static final String AUTH_KEY = "AuthKey";
    private static final String DIGEST_HEADER_NAME = "Digest";
    private static final String SIGNATURE = "Signature";
    private static final String X_REQUEST_ID = "X-Request-ID";
    private static final String EMPTY_STRING_DIGEST = "SHA-256=47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=";

    private static final String DATE_FORMAT = "YYYY-MM-dd";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final String ACCOUNT_ID_PLACEHOLDER = "{accountId}";
    private static final String TRANSACTIONS_ENDPOINT_TEMPLATE = "/v2/psd2/me/accounts/" + ACCOUNT_ID_PLACEHOLDER + "/transactions";
    private static final String TRANSACTIONS_PARAMETERS_TEMPLATE = "?dateFrom={dateFrom}";
    private static final String BALANCES_ENDPOINT_TEMPLATE = "/v2/psd2/me/accounts/" + ACCOUNT_ID_PLACEHOLDER + "/balance";
    private final AbancaSigningService signingUtil;
    private final AbancaGroupProperties properties;
    private final HttpErrorHandlerV2 errorHandler;

    public AbancaHttpClient(AbancaSigningService signingUtil,
                            AbancaGroupProperties properties,
                            HttpErrorHandlerV2 errorHandler,
                            MeterRegistry registry,
                            RestTemplate restTemplate,
                            String provider) {
        super(registry, restTemplate, provider);
        this.properties = properties;
        this.errorHandler = errorHandler;
        this.signingUtil = signingUtil;
    }

    public Optional<AbancaAuthData> getUserToken(UUID clientId,
                                                 UUID apiKey,
                                                 String authCode) throws TokenInvalidException {
        MultiValueMap<String, Object> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, AUTHORIZATION_CODE);
        requestPayload.add(CODE, authCode);
        requestPayload.add("APPLICATION", clientId);
        HttpHeaders headers = getDefaultHttpHeaders(apiKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity httpEntity = new HttpEntity(requestPayload, headers);
        return fetchTokenResponse(httpEntity, ProviderClientEndpoints.GET_ACCESS_TOKEN);
    }

    public Optional<AbancaAuthData> refreshUserToken(UUID clientId,
                                                     UUID apiKey,
                                                     String refreshToken) throws TokenInvalidException {
        MultiValueMap<String, Object> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, REFRESH_TOKEN);
        requestPayload.add(REFRESH_TOKEN, refreshToken);
        requestPayload.add("APPLICATION", clientId);
        HttpHeaders headers = getDefaultHttpHeaders(apiKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity httpEntity = new HttpEntity(requestPayload, headers);
        return fetchTokenResponse(httpEntity, REFRESH_TOKEN);
    }


    public Balance getAccountBalances(String resourceId,
                                      String accessToken,
                                      UUID apiKey,
                                      String signatureKeyId,
                                      UUID signingKeyId,
                                      Signer signer,
                                      Clock clock) throws TokenInvalidException {
        HttpHeaders headers = getDefaultHttpHeaders(apiKey);
        headers.setBearerAuth(accessToken);
        String url = BALANCES_ENDPOINT_TEMPLATE.replace(ACCOUNT_ID_PLACEHOLDER, resourceId);
        addSigningHeaders(headers, clock, GET, url, signatureKeyId, signingKeyId, signer);
        HttpEntity httpEntity = new HttpEntity(headers);
        return exchange(url,
                HttpMethod.GET,
                httpEntity,
                ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID,
                Balance.class,
                errorHandler).getBody();
    }

    public Accounts getAccounts(String accessToken,
                                UUID apiKey,
                                String signatureKeyId,
                                UUID signingKeyId,
                                Signer signer,
                                Clock clock) throws TokenInvalidException {
        HttpHeaders headers = getDefaultHttpHeaders(apiKey);
        headers.setBearerAuth(accessToken);
        addSigningHeaders(headers, clock, GET, ACCOUNTS_ENDPOINT, signatureKeyId, signingKeyId, signer);
        HttpEntity httpEntity = new HttpEntity(headers);
        return exchange(ACCOUNTS_ENDPOINT,
                GET,
                httpEntity,
                ProviderClientEndpoints.GET_ACCOUNTS,
                Accounts.class,
                errorHandler).getBody();
    }

    public Transactions getNextAccountTransactionsNextPage(String nextUrl,
                                                           String accessToken,
                                                           UUID apiKey,
                                                           String signatureKeyId,
                                                           UUID signingKeyId,
                                                           Signer signer,
                                                           Clock clock) throws TokenInvalidException {
        return getAccountTransactions(
                nextUrl,
                accessToken,
                apiKey,
                null,
                signatureKeyId,
                signingKeyId,
                signer,
                clock);
    }

    public Transactions getFirstAccountTransactionPage(String accessToken,
                                                       UUID apiKey,
                                                       String resourceId,
                                                       Instant transactionsFetchStartTime,
                                                       String signatureKeyId,
                                                       UUID signingKeyId,
                                                       Signer signer,
                                                       Clock clock) throws TokenInvalidException {
        return getAccountTransactions(
                TRANSACTIONS_ENDPOINT_TEMPLATE.replace(ACCOUNT_ID_PLACEHOLDER, resourceId),
                accessToken,
                apiKey,
                transactionsFetchStartTime,
                signatureKeyId,
                signingKeyId,
                signer,
                clock);
    }

    private Transactions getAccountTransactions(String url,
                                                String accessToken,
                                                UUID apiKey,
                                                Instant transactionsFetchStartTime,
                                                String signatureKeyId,
                                                UUID signingKeyId,
                                                Signer signer,
                                                Clock clock) throws TokenInvalidException {
        HttpHeaders headers = getDefaultHttpHeaders(apiKey);
        headers.setBearerAuth(accessToken);
        String createdUrl = url;
        if (transactionsFetchStartTime != null) {
            createdUrl += TRANSACTIONS_PARAMETERS_TEMPLATE.replace("{dateFrom}",
                    DATE_FORMATTER.format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC)));
        }
        addSigningHeaders(headers, clock, GET, createdUrl, signatureKeyId, signingKeyId, signer);
        HttpEntity httpEntity = new HttpEntity(headers);
        return exchange(decodeUrl(createdUrl),
                HttpMethod.GET,
                httpEntity,
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                Transactions.class,
                errorHandler).getBody();
    }


    private String decodeUrl(String rawNextPage) {
        if (rawNextPage == null) {
            return null;
        }
        try {
            return URLDecoder.decode(rawNextPage, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Error while decoding next transaction page url");
        }
    }

    private void addSigningHeaders(HttpHeaders currentHeaders,
                                   Clock clock,
                                   HttpMethod method,
                                   String requestTargetUrl,
                                   String signatureKeyId,
                                   UUID signingKeyId,
                                   Signer signer) {
        currentHeaders.setDate(clock.instant());
        currentHeaders.add(DIGEST_HEADER_NAME, EMPTY_STRING_DIGEST);
        currentHeaders.add("Request-Target", method.name().toLowerCase() + " " + requestTargetUrl);
        currentHeaders.add(SIGNATURE, signingUtil.getSignature(new HttpHeaders(currentHeaders), signatureKeyId, signingKeyId, signer));
    }

    private Optional<AbancaAuthData> fetchTokenResponse(HttpEntity tokenRequest, String prometheusPath) throws TokenInvalidException {
        return Optional.ofNullable(exchange(
                properties.getBaseUrl() + TOKEN_ENDPOINT,
                HttpMethod.POST,
                tokenRequest,
                prometheusPath,
                AbancaAuthData.class,
                errorHandler
        ).getBody());
    }

    private HttpHeaders getDefaultHttpHeaders(UUID apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTH_KEY, apiKey.toString());
        headers.add(X_REQUEST_ID, UUID.randomUUID().toString());
        return headers;
    }
}
