package com.yolt.providers.raiffeisenbank.common;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV3;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.*;
import com.yolt.providers.raiffeisenbank.common.ais.config.RaiffeisenBankProperties;
import com.yolt.providers.raiffeisenbank.common.ais.data.dto.Accounts;
import com.yolt.providers.raiffeisenbank.common.ais.data.dto.Transactions;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;

import static com.yolt.providers.common.constants.OAuth.*;
import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;

public class RaiffeisenBankHttpClient extends DefaultHttpClientV3 {


    private static final String CLIENT_ID = "client_id";
    private static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
    private static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    private static final String TPP_REDIRECT_PREFERRED = "TPP-Redirect-Preferred";
    private static final String CONSENT_ID = "Consent-ID";
    private static final String PSU_ID = "PSU-ID";

    private static final String TOKEN_ENDPOINT = "/aisp/oauth2/token";
    private static final String CONSENT_ENDPOINT = "/psd2-bgs-consent-api-1.3.2-rbro/v1/consents";
    public static final String ACCOUNTS_URL = "/psd2-accounts-api-1.3.2-rbro/v1/accounts?withBalance=true";
    public static final String ACCOUNT_TRANSACTIONS_URL_TEMPLATE = "/psd2-accounts-api-1.3.2-rbro/v1/accounts/{id}/transactions?dateFrom={dateFrom}&bookingStatus=both";

    private static final int MAX_FREQUENCY_PER_DAY = 4;
    private static final int CONSENT_VALIDITY_IN_DAYS = 90;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RaiffeisenBankProperties properties;
    private final HttpErrorHandlerV2 errorHandler;
    private final Clock clock;

    public RaiffeisenBankHttpClient(RaiffeisenBankProperties properties,
                                    HttpErrorHandlerV2 errorHandler,
                                    MeterRegistry registry,
                                    RestTemplate restTemplate,
                                    Clock clock,
                                    String provider) {
        super(registry, restTemplate, provider);
        this.properties = properties;
        this.errorHandler = errorHandler;
        this.clock = clock;
    }

    public Optional<String> createConsentId(String clientId,
                                            String redirectUrl,
                                            String psuIp,
                                            String iban,
                                            String accountLogin) throws TokenInvalidException {

        var headers = createStandardHeaders(clientId, psuIp);
        headers.add(TPP_REDIRECT_URI, redirectUrl);
        headers.add(TPP_REDIRECT_PREFERRED, "true");
        headers.add(PSU_ID, accountLogin);
        headers.setContentType(MediaType.APPLICATION_JSON);
        var createConsentBody = getCreateConsentBody(iban);

        var httpEntity = new HttpEntity<>(createConsentBody, headers);
        Consent response = exchange(CONSENT_ENDPOINT,
                HttpMethod.POST,
                httpEntity,
                RETRIEVE_ACCOUNT_ACCESS_CONSENT,
                Consent.class,
                errorHandler).getBody();
        if (response == null) {
            return Optional.empty();
        } else {
            return Optional.of(response.getConsentId());
        }
    }

    public Optional<RaiffeisenAuthData> getUserToken(String clientId,
                                                     String clientSecret,
                                                     String authCode,
                                                     String redirectUri) throws TokenInvalidException {
        MultiValueMap<String, Object> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, AUTHORIZATION_CODE);
        requestPayload.add(CODE, authCode);
        requestPayload.add(REDIRECT_URI, redirectUri);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);
        var httpEntity = new HttpEntity<>(requestPayload, headers);
        return fetchTokenResponse(httpEntity, GET_ACCESS_TOKEN);
    }

    public Optional<RaiffeisenAuthData> refreshUserToken(String clientId,
                                                         String clientSecret,
                                                         String refreshToken) throws TokenInvalidException {
        MultiValueMap<String, Object> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, OAuth.REFRESH_TOKEN);
        requestPayload.add(OAuth.REFRESH_TOKEN, refreshToken);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);
        var httpEntity = new HttpEntity<>(requestPayload, headers);

        return fetchTokenResponse(httpEntity, ProviderClientEndpoints.REFRESH_TOKEN);
    }

    public void deleteConsent(String consentId,
                              String accessToken,
                              String clientId) throws TokenInvalidException {
        var headers = new HttpHeaders();
        headers.add(CLIENT_ID, clientId);
        headers.setBearerAuth(accessToken);
        var httpEntity = new HttpEntity<>(headers);
        exchange(CONSENT_ENDPOINT + "/" + consentId,
                HttpMethod.DELETE,
                httpEntity,
                DELETE_ACCOUNT_ACCESS_CONSENT,
                Void.class,
                errorHandler);
    }

    public Accounts getAccounts(String accessToken,
                                String clientId,
                                String psuIp,
                                String consentId) throws TokenInvalidException {
        var headers = createStandardHeaders(clientId, psuIp);
        headers.setBearerAuth(accessToken);
        headers.add(CONSENT_ID, consentId);
        var httpEntity = new HttpEntity<>(headers);
        return exchange(ACCOUNTS_URL,
                HttpMethod.GET,
                httpEntity,
                GET_ACCOUNTS,
                Accounts.class,
                errorHandler).getBody();

    }

    public Transactions getAccountTransactions(String resourceId,
                                               String accessToken,
                                               String clientId,
                                               String psuIp,
                                               String consentId,
                                               Instant transactionsFetchStartTime) throws TokenInvalidException {
        var headers = createStandardHeaders(clientId, psuIp);
        headers.setBearerAuth(accessToken);
        headers.add(CONSENT_ID, consentId);

        var httpEntity = new HttpEntity<>(headers);
        var dateFromFormatted = DATE_TIME_FORMATTER.format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC));
        return exchange(ACCOUNT_TRANSACTIONS_URL_TEMPLATE,
                HttpMethod.GET,
                httpEntity,
                GET_TRANSACTIONS_BY_ACCOUNT_ID,
                Transactions.class,
                errorHandler,
                resourceId,
                dateFromFormatted).getBody();
    }

    public Transactions getAccountTransactionsNextPage(String nextPageUrl,
                                                       String accessToken,
                                                       String clientId,
                                                       String psuIp,
                                                       String consentId) throws TokenInvalidException {
        var headers = createStandardHeaders(clientId, psuIp);
        headers.setBearerAuth(accessToken);
        headers.add(CONSENT_ID, consentId);

        var httpEntity = new HttpEntity<>(headers);
        return exchange(nextPageUrl,
                HttpMethod.GET,
                httpEntity,
                GET_TRANSACTIONS_BY_ACCOUNT_ID,
                Transactions.class,
                errorHandler).getBody();
    }

    private ConsentRequest getCreateConsentBody(String iban) {
        var accountReference = new AccountReference(iban);
        var consentsAccess = new ConsentAccess(
                Collections.singletonList(accountReference),
                Collections.singletonList(accountReference),
                Collections.singletonList(accountReference)
        );
        return new ConsentRequest(
                consentsAccess,
                true,
                LocalDate.now(clock).plusDays(CONSENT_VALIDITY_IN_DAYS).format(DATE_TIME_FORMATTER),
                MAX_FREQUENCY_PER_DAY,
                false);
    }

    private Optional<RaiffeisenAuthData> fetchTokenResponse(HttpEntity tokenRequest, String prometheusPath) throws TokenInvalidException {
        return Optional.ofNullable(exchange(
                properties.getOAuthBaseUrl() + TOKEN_ENDPOINT,
                HttpMethod.POST,
                tokenRequest,
                prometheusPath,
                RaiffeisenAuthData.class,
                errorHandler
        ).getBody());
    }


    private HttpHeaders createStandardHeaders(String clientId, String psuIp) {
        var headers = new HttpHeaders();
        headers.add(CLIENT_ID, clientId);
        if (StringUtils.hasText(psuIp)) {
            headers.add(PSU_IP_ADDRESS, psuIp);
        }
        return headers;
    }
}
