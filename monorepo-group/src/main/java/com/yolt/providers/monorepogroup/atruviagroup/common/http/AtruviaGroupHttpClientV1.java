package com.yolt.providers.monorepogroup.atruviagroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV3;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.external.*;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.UUID;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;
import static org.springframework.http.HttpMethod.*;

@Slf4j
public class AtruviaGroupHttpClientV1 extends DefaultHttpClientV3 implements AtruviaGroupHttpClient {

    private static final String CONSENTS_ENDPOINT = "/v1/consents";
    private static final String CONSENTS_AUTHORISATION_ENDPOINT = "/v1/consents/{consentId}/authorisations";
    private static final String CONSENTS_AUTHORISATION_UPDATE_ENDPOINT = "/v1/consents/{consentId}/authorisations/{authorisationId}";
    private static final String ACCOUNTS_ENDPOINT = "/v1/accounts";
    private static final String BALANCES_TEMPLATE = "/v1/accounts/{accountId}/balances";
    private static final String TRANSACTIONS_TEMPLATE = "/v1/accounts/{accountId}/transactions";

    private final AtruviaGroupHttpHeadersProducer headersProducer;
    private final HttpErrorHandlerV2 errorHandler;
    private final String baseUrl;

    AtruviaGroupHttpClientV1(MeterRegistry meterRegistry,
                             RestTemplate restTemplate,
                             String providerDisplayName,
                             AtruviaGroupHttpHeadersProducer headersProducer,
                             HttpErrorHandlerV2 errorHandler,
                             String baseUrl) {
        super(meterRegistry, restTemplate, providerDisplayName);
        this.headersProducer = headersProducer;
        this.errorHandler = errorHandler;
        this.baseUrl = baseUrl;
    }


    @Override
    public ConsentsResponse201 createConsentForAllAccounts(LocalDate validity,
                                                           String psuIdName,
                                                           String psuIpAddress,
                                                           X509Certificate signingCertificate,
                                                           UUID signingKeyId,
                                                           Signer signer) {
        var consents = Consents.builder()
                .access(Consents.AccountAccess.builder()
                        .allPsd2(Consents.AccountAccess.AllPsd2Enum.ALLACCOUNTS)
                        .build())
                .combinedServiceIndicator(Boolean.FALSE)
                .frequencyPerDay(4)
                .recurringIndicator(Boolean.TRUE)
                .validUntil(validity)
                .build();
        var url = baseUrl + CONSENTS_ENDPOINT;
        HttpEntity<?> entity = new HttpEntity<>(consents, headersProducer.createAuthorizationHeaders(psuIdName, psuIpAddress, signingCertificate, signingKeyId, signer, consents));
        try {
            return exchange(url, POST, entity, GET_ACCOUNT_ACCESS_CONSENT, ConsentsResponse201.class, errorHandler).getBody();
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException("Unable to create consent");
        }
    }

    @Override
    public StartScaProcessResponse createAuthorisationForConsent(String consentId,
                                                                 String psuIdName,
                                                                 String psuIdPassword,
                                                                 X509Certificate signingCertificate,
                                                                 UUID signingKeyId,
                                                                 Signer signer) {
        var url = UriComponentsBuilder.fromUriString(baseUrl + CONSENTS_AUTHORISATION_ENDPOINT)
                .buildAndExpand(consentId)
                .toUriString();
        var updatePsuAuthentication = new UpdatePsuAuthentication();
        updatePsuAuthentication.setPsuData(new UpdatePsuAuthentication.PsuData().password(psuIdPassword));
        var entity = new HttpEntity<>(updatePsuAuthentication, headersProducer.createAuthorizationHeaders(psuIdName, null, signingCertificate, signingKeyId, signer, updatePsuAuthentication));
        try {
            return exchange(url, POST, entity, "get_account_access_consent_authorisation", StartScaProcessResponse.class, errorHandler, consentId).getBody();
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException("Unable to create consent authorisation");
        }
    }

    @Override
    public SelectPsuAuthenticationMethodResponse selectSCAForConsentAndAuthorisation(String consentId,
                                                                                     String authorisationId,
                                                                                     String authenticationid,
                                                                                     X509Certificate signingCertificate,
                                                                                     UUID signingKeyId,
                                                                                     Signer signer) {
        var url = UriComponentsBuilder.fromUriString(baseUrl + CONSENTS_AUTHORISATION_UPDATE_ENDPOINT)
                .buildAndExpand(consentId, authorisationId)
                .toUriString();
        var body = new SelectPsuAuthenticationMethod().authenticationMethodId(authenticationid);
        var entity = new HttpEntity<>(body, headersProducer.createAuthorizationHeaders(null, null, signingCertificate, signingKeyId, signer, body));
        try {
            return exchange(url, PUT, entity, "update_account_access_consent_authorisation", SelectPsuAuthenticationMethodResponse.class, errorHandler).getBody();
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException("Unable to select authorisation method");
        }
    }

    @Override
    public ScaStatusResponse putAuthenticationData(String consentId,
                                                   String authorisationId,
                                                   String authenticationData,
                                                   X509Certificate signingCertificate,
                                                   UUID signingKeyId,
                                                   Signer signer) {
        var url = UriComponentsBuilder.fromUriString(baseUrl + CONSENTS_AUTHORISATION_UPDATE_ENDPOINT)
                .buildAndExpand(consentId, authorisationId)
                .toUriString();

        var body = new TransactionAuthorisation().scaAuthenticationData(authenticationData);
        var entity = new HttpEntity<>(body, headersProducer.createAuthorizationHeaders(null, null, signingCertificate, signingKeyId, signer, body));
        try {
            return exchange(url, PUT, entity, "update_account_access_consent_authorisation", ScaStatusResponse.class, errorHandler).getBody();
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException("Unable to select authorisation method");
        }
    }

    @Override
    public AccountsResponse getAccounts(String consentId,
                                        String psuIpAddress,
                                        X509Certificate signingCertificate,
                                        UUID signingKeyId,
                                        Signer signer) throws TokenInvalidException {
        var url = baseUrl + ACCOUNTS_ENDPOINT;
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(consentId, psuIpAddress, signingCertificate, signingKeyId, signer));
        return exchange(url, GET, entity, GET_ACCOUNTS, AccountsResponse.class, errorHandler).getBody();
    }

    @Override
    public BalancesResponse getBalances(String accountId, String consentId,
                                        String psuIpAddress,
                                        X509Certificate signingCertificate,
                                        UUID signingKeyId,
                                        Signer signer) throws TokenInvalidException {
        var url = baseUrl + BALANCES_TEMPLATE;
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(consentId, psuIpAddress, signingCertificate, signingKeyId, signer));
        return exchange(url, GET, entity, GET_BALANCES_BY_ACCOUNT_ID, BalancesResponse.class, errorHandler, accountId).getBody();
    }

    @Override
    public TransactionsResponse getTransactions(String url, String consentId,
                                                String psuIpAddress,
                                                X509Certificate signingCertificate,
                                                UUID signingKeyId,
                                                Signer signer) throws TokenInvalidException {
        if (!url.startsWith(baseUrl)) {
            url = baseUrl + url;
        }
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(consentId, psuIpAddress, signingCertificate, signingKeyId, signer));
        return exchange(url, GET, entity, GET_TRANSACTIONS_BY_ACCOUNT_ID, TransactionsResponse.class, errorHandler).getBody();
    }

    @Override
    public TransactionsResponse getTransactions(String accountId,
                                                String consentId,
                                                String psuIpAddress,
                                                String dateFrom,
                                                X509Certificate signingCertificate,
                                                UUID signingKeyId,
                                                Signer signer) throws TokenInvalidException {
        String url = UriComponentsBuilder.fromUriString(baseUrl + TRANSACTIONS_TEMPLATE)
                .queryParam("bookingStatus", "booked")
                .queryParam("dateFrom", dateFrom)
                .buildAndExpand(accountId)
                .toUriString();
        return getTransactions(url, consentId, psuIpAddress, signingCertificate, signingKeyId, signer);
    }
}