package com.yolt.providers.fabric.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.fabric.common.model.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class FabricDefaultHttpClient extends DefaultHttpClientV2 {
    private static final String URL_PREFIX = "/api/fabrick/psd2";

    private static final String CONSENT = URL_PREFIX + "/{version}/consents";
    private static final String CONSENT_DELETE = URL_PREFIX + "/{version}/consents/{consentId}";
    private static final String CONSENT_AUTHORIZATION = URL_PREFIX + "/{version}/consents/{consent-id}/authorisations";

    private static final String ACCOUNTS_ENDPOINT = URL_PREFIX + "/{version}/accounts";
    private static final String BALANCES_ENDPOINT = URL_PREFIX + "/{version}/accounts/{accountId}/balances";
    private static final String TRANSACTIONS_ENDPOINT = URL_PREFIX + "/{version}/accounts/{accountId}/transactions";

    private final String endpointVersion;
    private final DefaultAuthorizationRequestHeadersProducer headersProducer;
    private final HttpErrorHandlerV2 errorHandler;

    public FabricDefaultHttpClient(MeterRegistry meterRegistry,
                                   String endpointVersion,
                                   DefaultAuthorizationRequestHeadersProducer headersProducer,
                                   RestTemplate restTemplateWithManagedMutualTLSTemplate,
                                   String providerDisplayName,
                                   HttpErrorHandlerV2 errorHandler) {
        super(meterRegistry, restTemplateWithManagedMutualTLSTemplate, providerDisplayName);
        this.endpointVersion = endpointVersion;
        this.headersProducer = headersProducer;
        this.errorHandler = errorHandler;
    }

    public ConsentResponse initiateConsent(final String redirectUrl, final String psuIpAddress, final ConsentRequest consentRequest) throws TokenInvalidException {
        HttpHeaders headers = headersProducer.createConsentAndAuthorizationCreationHeaders(redirectUrl, psuIpAddress);
        return exchange(CONSENT, HttpMethod.POST, new HttpEntity<ConsentRequest>(consentRequest, headers), ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT, ConsentResponse.class, errorHandler, endpointVersion).getBody();
    }

    public AuthorizationConsentResourceResponse initiateAuthorizationResource(final String redirectUrl, final String psuIpAddress, final String consentId) throws TokenInvalidException {
        HttpHeaders headers = headersProducer.createConsentAndAuthorizationCreationHeaders(redirectUrl, psuIpAddress);
        return exchange(CONSENT_AUTHORIZATION, HttpMethod.POST, new HttpEntity<>(headers), ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT, AuthorizationConsentResourceResponse.class, errorHandler, endpointVersion, consentId).getBody();
    }

    public void deleteConsent(final String consentId) throws TokenInvalidException {
        HttpHeaders headers = headersProducer.createConsentDeletionHeaders();
        exchange(CONSENT_DELETE, HttpMethod.DELETE, new HttpEntity<>(headers), ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT, Void.class, errorHandler, endpointVersion, consentId);
    }

    public Accounts getAccounts(final String consentId, final String psuIpAddress) throws TokenInvalidException {
        HttpHeaders headers = headersProducer.createFetchDataHeaders(consentId, psuIpAddress);
        return exchange(ACCOUNTS_ENDPOINT, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_ACCOUNTS, Accounts.class, psuIpAddress, errorHandler, endpointVersion).getBody();
    }

    public Transactions getTransactions(String accountId, Instant fromDate, String consentId, String psuIpAddress) throws TokenInvalidException {
        HttpHeaders headers = headersProducer.createFetchDataHeaders(consentId, psuIpAddress);
        return exchange(assembleTransactionUri(TRANSACTIONS_ENDPOINT, DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC).format(fromDate)), HttpMethod.GET,
                new HttpEntity<>(headers), ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID, Transactions.class, psuIpAddress, errorHandler, endpointVersion, accountId).getBody();
    }

    public Balances getBalances(String consentId, String accountId, String psuIpAddress) throws TokenInvalidException {
        HttpHeaders headers = headersProducer.createFetchDataHeaders(consentId, psuIpAddress);
        return exchange(BALANCES_ENDPOINT, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID, Balances.class, psuIpAddress, errorHandler, endpointVersion, accountId).getBody();
    }

    private String assembleTransactionUri(final String uri, final String dateFrom) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("dateFrom", dateFrom)
                .queryParam("bookingStatus", "both");
        return uriBuilder.build().toString();
    }
}
