package com.yolt.providers.monorepogroup.libragroup.common.ais.data;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV3;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans.SigningData;
import com.yolt.providers.monorepogroup.libragroup.common.LibraSigningService;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.Accounts;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.Balances;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.Transactions;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class LibraGroupDataHttpClientV1 extends DefaultHttpClientV3 implements LibraGroupDataHttpClient {

    private static final String CONSENT_ID = "Consent-ID";
    private static final String ACCOUNTS_ENDPOINT = "/ACCOUNTS_API/v1";
    private static final String TRANSACTIONS_ENDPOINT_TEMPLATE = "/ACCOUNTS_API/v1/{IBAN}/transactions?dateFrom={dateFrom}";
    private static final String BALANCES_ENDPOINT_TEMPLATE = "/ACCOUNTS_API/v1/{IBAN}/balances";
    private static final String X_REQUEST_ID = "X-Request-ID";

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private final HttpErrorHandlerV2 errorHandler;
    private final LibraSigningService signingService;

    public LibraGroupDataHttpClientV1(HttpErrorHandlerV2 errorHandler,
                                      LibraSigningService signingService,
                                      MeterRegistry registry,
                                      RestTemplate restTemplate,
                                      String provider) {
        super(registry, restTemplate, provider);
        this.errorHandler = errorHandler;
        this.signingService = signingService;
    }


    @Override
    public Accounts getAccounts(String consentId,
                                String accessToken,
                                SigningData signingData,
                                Signer signer) throws TokenInvalidException {
        return exchange(ACCOUNTS_ENDPOINT,
                getSignedHeaders(consentId, accessToken, signingData, signer),
                ProviderClientEndpoints.GET_ACCOUNTS,
                Accounts.class);
    }

    @Override
    public Balances getBalances(String accountId,
                                String consentId,
                                String accessToken) throws TokenInvalidException {
        return exchange(BALANCES_ENDPOINT_TEMPLATE,
                getHeaders(consentId, accessToken),
                ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID,
                Balances.class,
                accountId);
    }

    @Override
    public Transactions getAccountTransactions(String accountId,
                                               String consentId,
                                               String accessToken,
                                               Instant transactionsFetchStartTime) throws TokenInvalidException {

        return exchange(TRANSACTIONS_ENDPOINT_TEMPLATE,
                getHeaders(consentId, accessToken),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                Transactions.class,
                accountId,
                DATE_FORMATTER.format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC)));
    }

    private <T> T exchange(String endpoint,
                           HttpHeaders headers,
                           String prometheusEndpoint,
                           Class<T> returnType,
                           String... urlParameters) throws TokenInvalidException {

        HttpEntity<Object> entity = new HttpEntity<>(headers);
        return exchange(endpoint,
                HttpMethod.GET,
                entity,
                prometheusEndpoint,
                returnType,
                errorHandler,
                urlParameters).getBody();
    }

    private HttpHeaders getHeaders(String consentId,
                                   String accessToken) {
        HttpHeaders headers = getDefaultHeaders(consentId, accessToken);
        headers.add(X_REQUEST_ID, ExternalTracingUtil.createLastExternalTraceId());
        return headers;
    }

    private HttpHeaders getSignedHeaders(String consentId,
                                         String accessToken,
                                         SigningData signingData,
                                         Signer signer) throws TokenInvalidException {

        HttpHeaders headers = getDefaultHeaders(consentId, accessToken);
        headers.addAll(signingService.getSigningHeaders(
                new LinkedMultiValueMap<>(),
                signingData.getSigningCertificateSerialNumber(),
                signingData.getSigningKeyId(),
                signingData.getSigningCertificateBase64(),
                signer));
        return headers;
    }

    private HttpHeaders getDefaultHeaders(String consentId,
                                          String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONSENT_ID, consentId);
        headers.setBearerAuth(accessToken);
        return headers;
    }
}
