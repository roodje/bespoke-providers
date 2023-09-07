package com.yolt.providers.sparkassenandlandesbanks.common.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.sparkassenandlandesbanks.common.Department;
import com.yolt.providers.sparkassenandlandesbanks.common.dto.*;
import com.yolt.providers.sparkassenandlandesbanks.common.dto.transactions.Document;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

public class SparkassenAndLandesbanksHttpClient extends DefaultHttpClient {

    public static final String CODE_QUERY_PARAM_NAME = "code";
    public static final String CODE_VERIFIER_QUERY_PARAM_NAME = "code_verifier";
    public static final String REDIRECT_URI_QUERY_PARAM_NAME = "redirect_uri";
    public static final String TPP_REDIRECT_URI_HEADER_NAME = "TPP-Redirect-URI";
    public static final String TPP_ERROR_REDIRECT_URI_HEADER_NAME = "TPP-Nok-Redirect-URI";
    public static final String TPP_REDIRECT_PREFERRED_HEADER_NAME = "TPP-Redirect-Preferred";
    public static final String GRANT_TYPE_PARAM_NAME = "grant_type";
    public static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";
    public static final String PSU_IP_ADDRESS_HEADER_NAME = "PSU-IP-Address";
    public static final String CONSENT_ID_HEADER_NAME = "Consent-ID";
    public static final String ACCOUNTS_URL = "/{bankCode}/v1/accounts?withBalance=true";
    public static final String ACCOUNT_TRANSACTIONS_URL_TEMPLATE = "/{bankCode}/v1/accounts/{id}/transactions?dateFrom={dateFrom}&bookingStatus=both&deltaList=false";

    private static final String CLIENT_ID_QUERY_PARAM_NAME = "client_id";
    private static final String CONSENT_ENDPOINT = "/{bankCode}/v1/consents";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_FREQUENCY_PER_DAY = 4;
    private static final int CONSENT_VALIDITY_IN_DAYS = 90;

    private final Clock clock;

    public SparkassenAndLandesbanksHttpClient(MeterRegistry registry,
                                              RestTemplate restTemplate,
                                              String provider,
                                              Clock clock
    ) {
        super(registry, restTemplate, provider);
        this.clock = clock;
    }

    public ConsentResponse createConsent(Department department,
                                         String redirectUrl,
                                         String psuIpAddress) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(TPP_REDIRECT_PREFERRED_HEADER_NAME, "true");
        headers.add(TPP_REDIRECT_URI_HEADER_NAME, redirectUrl);
        headers.add(TPP_ERROR_REDIRECT_URI_HEADER_NAME, redirectUrl + "?error=true");
        headers.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);

        CreateConsentRequest createConsentBody = CreateConsentRequest.builder()
                .access(ConsentAccess.builder().availableAccountsWithBalance("allAccounts").build())
                .combinedServiceIndicator(false)
                .frequencyPerDay(MAX_FREQUENCY_PER_DAY)
                .recurringIndicator(true)
                .validUntil(LocalDate.now(clock).plusDays(CONSENT_VALIDITY_IN_DAYS).format(DATE_TIME_FORMATTER))
                .build();

        return exchange(
                CONSENT_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(createConsentBody, headers),
                ProviderClientEndpoints.GET_ACCOUNT_REQUEST_ID,
                ConsentResponse.class,
                department.getBankCode()).getBody();
    }

    public OAuthLinksResponse getDepartmentOAuthLinks(String wellKnownEndpoint) throws TokenInvalidException {

        HttpHeaders headers = new HttpHeaders();

        return exchange(wellKnownEndpoint,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ProviderClientEndpoints.GET_ACCOUNTS,
                OAuthLinksResponse.class).getBody();
    }

    public SparkassenAndLandesbanksTokenResponse createAccessToken(String redirectUrl,
                                                                   String authorizationCode,
                                                                   String codeVerifier,
                                                                   String providerIdentifier,
                                                                   String tokenEndpoint) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add(REDIRECT_URI_QUERY_PARAM_NAME, redirectUrl);
        payload.add(CODE_QUERY_PARAM_NAME, authorizationCode);
        payload.add(CODE_VERIFIER_QUERY_PARAM_NAME, codeVerifier);
        payload.add(CLIENT_ID_QUERY_PARAM_NAME, providerIdentifier);
        payload.add(GRANT_TYPE_PARAM_NAME, AUTHORIZATION_CODE_GRANT_TYPE);

        return exchange(
                tokenEndpoint,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                ProviderClientEndpoints.GET_ACCESS_TOKEN,
                SparkassenAndLandesbanksTokenResponse.class
        ).getBody();
    }

    public SparkassenAndLandesbanksTokenResponse refreshAccessToken(String refreshToken,
                                                                    String providerIdentifier,
                                                                    String tokenEndpoint) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add(GRANT_TYPE_PARAM_NAME, "refresh_token");
        payload.add("refresh_token", refreshToken);
        payload.add(CLIENT_ID_QUERY_PARAM_NAME, providerIdentifier);

        SparkassenAndLandesbanksTokenResponse sparkassenAndLandesbanksTokenResponse;
        sparkassenAndLandesbanksTokenResponse = exchange(
                tokenEndpoint,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                ProviderClientEndpoints.REFRESH_TOKEN,
                SparkassenAndLandesbanksTokenResponse.class
        ).getBody();
        return sparkassenAndLandesbanksTokenResponse;
    }

    public AccountsResponse getAccounts(Department department,
                                        String accessToken,
                                        String consentId,
                                        String psuIpAddress) throws TokenInvalidException {
        return exchange(ACCOUNTS_URL,
                HttpMethod.GET,
                new HttpEntity<>(createFetchDataHeaders(accessToken, consentId, psuIpAddress, MediaType.APPLICATION_JSON)),
                ProviderClientEndpoints.GET_ACCOUNTS,
                AccountsResponse.class,
                department.getBankCode()).getBody();
    }

    public Document getAccountTransactions(Department department,
                                           String accessToken,
                                           String consentId,
                                           String resourceId,
                                           Instant transactionsFetchStartTime,
                                           String psuIpAddress) throws TokenInvalidException {
        return exchange(ACCOUNT_TRANSACTIONS_URL_TEMPLATE,
                HttpMethod.GET,
                new HttpEntity<>(createFetchDataHeaders(accessToken, consentId, psuIpAddress, MediaType.APPLICATION_XML)),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                Document.class,
                department.getBankCode(),
                resourceId,
                DATE_TIME_FORMATTER.format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC))).getBody();
    }

    private HttpHeaders createFetchDataHeaders(String accessToken, String consentId, String psuIpAddress, MediaType accept) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(accept));
        headers.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.add(CONSENT_ID_HEADER_NAME, consentId);
        return headers;
    }
}
