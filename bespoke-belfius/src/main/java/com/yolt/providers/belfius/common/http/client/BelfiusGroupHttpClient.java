package com.yolt.providers.belfius.common.http.client;

import com.yolt.providers.belfius.common.auth.BelfiusGroupAuthMeans;
import com.yolt.providers.belfius.common.configuration.BelfiusBaseProperties;
import com.yolt.providers.belfius.common.exception.ConsentResponseSizeException;
import com.yolt.providers.belfius.common.http.HttpErrorHandler;
import com.yolt.providers.belfius.common.model.BelfiusGroupLoginUrlResponse;
import com.yolt.providers.belfius.common.model.ais.Account;
import com.yolt.providers.belfius.common.model.ais.TransactionResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class BelfiusGroupHttpClient {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String CODE_CHALLENGE_METHOD_HEADER_NAME = "Code-Challenge-Method";
    private static final String CODE_CHALLENGE_HEADER_NAME = "Code-Challenge";
    private static final String CLIENT_ID_HEADER_NAME = "Client-ID";
    private static final String REDIRECT_URI_HEADER_NAME = "Redirect-URI";
    private static final String LOGICAL_ID_PARAM = "logical-id";
    private static final String DATE_FROM_PARAM = "date_from";
    private static final String NEXT_PARAM = "next";

    private final RestTemplate restTemplate;
    private final BelfiusGroupAuthMeans authMeans;
    private final String language;
    private final String baseClientRedirectUrl;
    private final BelfiusBaseProperties properties;
    private final Clock clock;

    public String getLoginUrl(UrlCreateAccessMeansRequest urlCreateAccessMeans, OAuth2ProofKeyCodeExchange oAuth2ProofKeyCodeExchange) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CODE_CHALLENGE_METHOD_HEADER_NAME, oAuth2ProofKeyCodeExchange.getCodeChallengeMethod());
        httpHeaders.add(CODE_CHALLENGE_HEADER_NAME, oAuth2ProofKeyCodeExchange.getCodeChallenge());
        httpHeaders.add(HttpHeaders.ACCEPT, "application/vnd.belfius.api+json; version=3");
        httpHeaders.add(HttpHeaders.ACCEPT_LANGUAGE, language);
        httpHeaders.add(CLIENT_ID_HEADER_NAME, authMeans.getClientId());
        httpHeaders.add(REDIRECT_URI_HEADER_NAME, baseClientRedirectUrl);

        ResponseEntity<BelfiusGroupLoginUrlResponse[]> consentResponse = restTemplate.exchange("/consent-uris?scope=AIS&iban={iban}",
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                BelfiusGroupLoginUrlResponse[].class,
                urlCreateAccessMeans.getFilledInUserSiteFormValues().get("Iban").toUpperCase());
        int contentResponseLength = consentResponse.getBody().length;
        if (contentResponseLength != 1) {
            throw new ConsentResponseSizeException("Consent response should contain only one element, but has " + contentResponseLength);
        }
        return consentResponse.getBody()[0].getConsentUri();
    }

    public Account getAccountForGivenLogicalId(String logicalId, String accessToken) throws TokenInvalidException {
        HttpHeaders httpHeaders = getAccountsDataHttpHeaders(accessToken);

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put(LOGICAL_ID_PARAM, logicalId);

        return getData("/accounts/{logical-id}",
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                Account.class,
                uriVariables);
    }

    public TransactionResponse getTransactionsForGivenLogicalId(String logicalId, String accessToken, Instant dateFrom) throws TokenInvalidException {
        HttpHeaders httpHeaders = getTransactionsDataHttpHeaders(accessToken);

        String startDate = DATE_FORMATTER.format(OffsetDateTime.ofInstant(narrowTransactionFetchStartTime(dateFrom), ZoneOffset.UTC));
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put(LOGICAL_ID_PARAM, logicalId);
        uriVariables.put(DATE_FROM_PARAM, startDate);

        return getData("/accounts/{logical-id}/transactions?date_from={date_from}",
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                TransactionResponse.class,
                uriVariables);
    }

    public TransactionResponse getTransactionsNextPageForGivenLogicalId(String logicalId, String accessToken, String next) throws TokenInvalidException {
        HttpHeaders httpHeaders = getTransactionsDataHttpHeaders(accessToken);

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put(LOGICAL_ID_PARAM, logicalId);
        uriVariables.put(NEXT_PARAM, next);

        return getData("/accounts/{logical-id}/transactions?next={next}",
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                TransactionResponse.class,
                uriVariables);
    }

    private HttpHeaders getTransactionsDataHttpHeaders(String accessToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);
        httpHeaders.add(HttpHeaders.ACCEPT, properties.getTransactionsAcceptHeaderValue());
        httpHeaders.add(HttpHeaders.ACCEPT_LANGUAGE, language);
        httpHeaders.add(CLIENT_ID_HEADER_NAME, authMeans.getClientId());
        httpHeaders.add(REDIRECT_URI_HEADER_NAME, baseClientRedirectUrl);
        return httpHeaders;
    }

    private HttpHeaders getAccountsDataHttpHeaders(String accessToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);
        httpHeaders.add(HttpHeaders.ACCEPT, properties.getAcceptHeaderValue());
        httpHeaders.add(HttpHeaders.ACCEPT_LANGUAGE, language);
        httpHeaders.add(CLIENT_ID_HEADER_NAME, authMeans.getClientId());
        httpHeaders.add(REDIRECT_URI_HEADER_NAME, baseClientRedirectUrl);
        return httpHeaders;
    }

    private Instant narrowTransactionFetchStartTime(Instant transactionFetchStartTime) {
        final Instant daysAgo = Instant.now(clock).minus(Period.ofDays(89));
        if (transactionFetchStartTime.isBefore(daysAgo)) {
            return daysAgo;
        }
        return transactionFetchStartTime;
    }

    private <T> T getData(String url, HttpMethod httpMethod, HttpEntity httpEntity, Class<T> responseType, Map<String, String> uriVariables) throws TokenInvalidException {
        try {
            return restTemplate.exchange(url, httpMethod, httpEntity, responseType, uriVariables).getBody();
        } catch (HttpStatusCodeException e) {
            HttpErrorHandler.handleNon2xxResponseCode(e.getStatusCode());
            throw e;
        }
    }
}
