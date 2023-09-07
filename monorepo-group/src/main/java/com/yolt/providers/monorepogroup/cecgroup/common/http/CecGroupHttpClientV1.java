package com.yolt.providers.monorepogroup.cecgroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV3;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.monorepogroup.cecgroup.common.CecGroupAccessMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.consent.ConsentCreationRequest;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.consent.ConsentCreationResponse;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data.AccountsResponse;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data.TransactionsResponse;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.token.TokenResponse;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Slf4j
public class CecGroupHttpClientV1 extends DefaultHttpClientV3 implements CecGroupHttpClient {

    private static final String CONSENT_ENDPOINT = "/psd29c/v1/consents";
    private static final String TOKEN_ENDPOINT = "/oauthcec/oauth2/token";
    private static final String ACCOUNTS_ENDPOINT = "/psd29c/v1/accounts?withBalance=true";
    private static final String ACCOUNT_TRANSACTIONS_ENDPOINT = "/psd29c/v1/accounts/{accountId}/transactions?dateFrom={dateFrom}&bookingStatus=both";

    private final CecGroupHttpHeadersProducer headersProducer;
    private final CecGroupHttpBodyProducer bodyProducer;
    private final HttpErrorHandlerV2 errorHandler;

    CecGroupHttpClientV1(MeterRegistry meterRegistry,
                         RestTemplate restTemplate,
                         String providerDisplayName,
                         CecGroupHttpHeadersProducer headersProducer,
                         CecGroupHttpBodyProducer bodyProducer,
                         HttpErrorHandlerV2 errorHandler) {
        super(meterRegistry, restTemplate, providerDisplayName);
        this.headersProducer = headersProducer;
        this.bodyProducer = bodyProducer;
        this.errorHandler = errorHandler;
    }

    @Override
    public ConsentCreationResponse createConsent(CecGroupAuthenticationMeans authMeans,
                                                 Signer signer,
                                                 LocalDate consentTo,
                                                 String psuIpAddress,
                                                 String redirectUri,
                                                 String state) throws TokenInvalidException {
        HttpHeaders consentHeaders = headersProducer.createConsentHeaders(psuIpAddress, redirectUri, state, authMeans,
                new byte[0], signer);
        HttpEntity<ConsentCreationRequest> entity = new HttpEntity<>(consentHeaders);
        return exchange(CONSENT_ENDPOINT, POST, entity, GET_ACCOUNT_ACCESS_CONSENT, ConsentCreationResponse.class, errorHandler)
                .getBody();
    }

    @Override
    public TokenResponse createToken(String clientId, String clientSecret, String redirectUri, String authCode) throws TokenInvalidException {
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(
                bodyProducer.createTokenBody(clientId, clientSecret, redirectUri, authCode),
                headersProducer.tokenHeaders(clientId));
        return exchange(TOKEN_ENDPOINT, POST, entity, GET_ACCESS_TOKEN, TokenResponse.class, errorHandler)
                .getBody();
    }

    @Override
    public TokenResponse refreshToken(String clientId, String refreshToken) throws TokenInvalidException {
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(
                bodyProducer.refreshTokenBody(refreshToken),
                headersProducer.tokenHeaders(clientId));
        return exchange(TOKEN_ENDPOINT, POST, entity, REFRESH_TOKEN, TokenResponse.class, errorHandler)
                .getBody();
    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public AccountsResponse fetchAccounts(CecGroupAuthenticationMeans authenticationMeans,
                                          CecGroupAccessMeans cecGroupAccessMeans,
                                          Signer signer,
                                          String psuIpAddress) {
        HttpEntity<Void> entity = new HttpEntity<>(headersProducer.fetchDataHeaders(
                psuIpAddress, authenticationMeans, cecGroupAccessMeans, signer));

        return exchange(ACCOUNTS_ENDPOINT,
                GET,
                entity,
                GET_ACCOUNTS,
                AccountsResponse.class,
                errorHandler).getBody();
    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public TransactionsResponse fetchFirstPageOfTransactions(CecGroupAuthenticationMeans authenticationMeans,
                                                             CecGroupAccessMeans cecGroupAccessMeans,
                                                             Signer signer,
                                                             String psuIpAddress,
                                                             String accountId,
                                                             String dateFrom) {
        HttpEntity<Void> entity = new HttpEntity<>(headersProducer.fetchDataHeaders(
                psuIpAddress, authenticationMeans, cecGroupAccessMeans, signer));

        return exchange(ACCOUNT_TRANSACTIONS_ENDPOINT,
                GET,
                entity,
                GET_TRANSACTIONS_BY_ACCOUNT_ID,
                TransactionsResponse.class,
                errorHandler,
                accountId,
                dateFrom).getBody();
    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public TransactionsResponse fetchNextPageOfTransactions(String nextPageUrl,
                                                            CecGroupAuthenticationMeans authenticationMeans,
                                                            CecGroupAccessMeans cecGroupAccessMeans,
                                                            Signer signer,
                                                            String psuIpAddress) {
        HttpEntity<Void> entity = new HttpEntity<>(headersProducer.fetchDataHeaders(
                psuIpAddress, authenticationMeans, cecGroupAccessMeans, signer));

        return exchange(nextPageUrl,
                GET,
                entity,
                GET_TRANSACTIONS_BY_ACCOUNT_ID,
                TransactionsResponse.class,
                errorHandler).getBody();
    }
}