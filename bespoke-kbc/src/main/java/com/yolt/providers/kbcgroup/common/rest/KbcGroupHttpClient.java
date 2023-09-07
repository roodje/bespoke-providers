package com.yolt.providers.kbcgroup.common.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.kbcgroup.common.KbcGroupProperties;
import com.yolt.providers.kbcgroup.common.dto.AccountReference;
import com.yolt.providers.kbcgroup.common.dto.ConsentAccess;
import com.yolt.providers.kbcgroup.common.dto.ConsentRequest;
import com.yolt.providers.kbcgroup.common.dto.KbcGroupTokenResponse;
import com.yolt.providers.kbcgroup.common.exception.KbcGroupHttpErrorHandler;
import com.yolt.providers.kbcgroup.dto.InlineResponse200;
import com.yolt.providers.kbcgroup.dto.InlineResponse2003;
import com.yolt.providers.kbcgroup.dto.InlineResponse201;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static com.yolt.providers.kbcgroup.common.rest.KbcGroupHttpConstants.PSU_IP_ADDRESS_HEADER_NAME;
import static com.yolt.providers.kbcgroup.common.rest.KbcGroupHttpConstants.REDIRECT_URI_QUERY_PARAM_NAME;

@RequiredArgsConstructor
public class KbcGroupHttpClient {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final int MAX_FREQUENCY_PER_DAY = 4;
    private static final int CONSENT_VALIDITY_IN_DAYS = 90;

    public static final String CODE_QUERY_PARAM_NAME = "code";
    public static final String CODE_VERIFIER_QUERY_PARAM_NAME = "code_verifier";
    public static final String CLIENT_ID_QUERY_PARAM_NAME = "client_id";
    public static final String CONSENT_ID_HEADER_NAME = "Consent-ID";
    public static final String TPP_REDIRECT_URI_HEADER_NAME = "TPP-Redirect-URI";
    public static final String GRANT_TYPE_PARAM_NAME = "grant_type";
    public static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";

    public static final String ACCOUNTS_URL = "/accounts?withBalance=true";
    public static final String ACCOUNT_TRANSACTIONS_URL_TEMPLATE = "/accounts/{id}/transactions?dateFrom={dateFrom}";
    public static final String ENDPOINTS_VERSION = "/psd2/v2";

    private final RestTemplate restTemplate;
    private final KbcGroupProperties properties;
    private final Clock clock;

    public InlineResponse201 createConsent(String iban,
                                           String redirectUrl,
                                           String psuIpAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(TPP_REDIRECT_URI_HEADER_NAME, redirectUrl);
        headers.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);

        AccountReference accountReference = AccountReference.builder()
                .iban(StringUtils.deleteWhitespace(iban))
                .build();
        ConsentAccess consentsAccess = ConsentAccess.builder()
                .balances(Collections.singletonList(accountReference))
                .transactions(Collections.singletonList(accountReference))
                .build();
        ConsentRequest createConsentBody = ConsentRequest.builder()
                .access(consentsAccess)
                .combinedServiceIndicator(false)
                .frequencyPerDay(MAX_FREQUENCY_PER_DAY)
                .recurringIndicator(true)
                .validUntil(LocalDate.now(clock).plusDays(CONSENT_VALIDITY_IN_DAYS).format(DATE_TIME_FORMATTER))
                .build();

        return restTemplate.exchange(
                "/consents",
                HttpMethod.POST,
                new HttpEntity<>(createConsentBody, headers),
                InlineResponse201.class).getBody();
    }

    public KbcGroupTokenResponse createAccessToken(String redirectUrl,
                                                   String authorizationCode,
                                                   String codeVerifier,
                                                   String providerIdentifier) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add(REDIRECT_URI_QUERY_PARAM_NAME, redirectUrl);
        payload.add(CODE_QUERY_PARAM_NAME, authorizationCode);
        payload.add(CODE_VERIFIER_QUERY_PARAM_NAME, codeVerifier);
        payload.add(CLIENT_ID_QUERY_PARAM_NAME, providerIdentifier);
        payload.add(GRANT_TYPE_PARAM_NAME, AUTHORIZATION_CODE_GRANT_TYPE);

        return restTemplate.exchange(
                properties.getTokenUrl(),
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                KbcGroupTokenResponse.class
        ).getBody();
    }

    public KbcGroupTokenResponse refreshAccessToken(String refreshToken, String providerIdentifier) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add(GRANT_TYPE_PARAM_NAME, "refresh_token");
        payload.add("refresh_token", refreshToken);
        payload.add(CLIENT_ID_QUERY_PARAM_NAME, providerIdentifier);

        KbcGroupTokenResponse kbcGroupTokenResponse = null;
        try {
            kbcGroupTokenResponse = restTemplate.exchange(
                    properties.getTokenUrl(),
                    HttpMethod.POST,
                    new HttpEntity<>(payload, headers),
                    KbcGroupTokenResponse.class
            ).getBody();
        } catch (HttpStatusCodeException e) {
            KbcGroupHttpErrorHandler.handleNon2xxResponseCodeForTokenExchange(e.getStatusCode());
        }
        return kbcGroupTokenResponse;
    }

    public InlineResponse200 getAccounts(String accessToken, String consentId, String psuIpAddress) {
        return restTemplate.exchange(ACCOUNTS_URL,
                HttpMethod.GET,
                new HttpEntity<>(createFetchDataHeaders(accessToken, consentId, psuIpAddress)),
                InlineResponse200.class).getBody();
    }

    public InlineResponse2003 getAccountTransactions(String accessToken,
                                                     String consentId,
                                                     String resourceId,
                                                     Instant transactionsFetchStartTime,
                                                     String psuIpAddress) {
        return restTemplate.exchange(ACCOUNT_TRANSACTIONS_URL_TEMPLATE,
                HttpMethod.GET,
                new HttpEntity<>(createFetchDataHeaders(accessToken, consentId, psuIpAddress)),
                InlineResponse2003.class,
                resourceId,
                DATE_TIME_FORMATTER.format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC))).getBody();
    }

    public InlineResponse2003 getNextPageOfAccountTransactions(String accessToken, String consentId, String psuIpAddress, String nextPageUrl) {
        return restTemplate.exchange(adjustTransactionsNextPage(nextPageUrl),
                HttpMethod.GET,
                new HttpEntity<>(createFetchDataHeaders(accessToken, consentId, psuIpAddress)),
                InlineResponse2003.class).getBody();
    }

    private String adjustTransactionsNextPage(String nextPage) {
        if (nextPage != null && nextPage.startsWith(ENDPOINTS_VERSION)) {
            return nextPage.substring(ENDPOINTS_VERSION.length());
        }
        return nextPage;
    }

    private HttpHeaders createFetchDataHeaders(String accessToken, String consentId, String psuIpAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.add(CONSENT_ID_HEADER_NAME, consentId);
        return headers;
    }
}
