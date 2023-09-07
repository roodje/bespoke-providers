package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.config.RaiffeisenAtGroupProperties;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.*;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.RaiffeisenAtGroupDateMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;

public class DefaultRaiffeisenAtGroupHttpClient extends DefaultHttpClientV2 implements RaiffeisenAtGroupHttpClient {

    private static final String POST_CONSENT_URL = "/v1/consents";
    private static final String CONSENT_URL = "/v1/consents/{consentId}";

    private static final String ACCOUNT_URL = "/v1/accounts";
    private static final String TRANSACTION_URL = "/v1/accounts/{accountId}/transactions";

    private final RaiffeisenAtGroupHttpHeadersProducer httpHeadersProducer;
    private final RaiffeisenAtGroupDateMapper dateMapper;
    private final RaiffeisenAtGroupProperties properties;

    private final HttpErrorHandlerV2 httpErrorHandler;

    public DefaultRaiffeisenAtGroupHttpClient(MeterRegistry registry, RestTemplate restTemplate, String provider, RaiffeisenAtGroupHttpHeadersProducer httpHeadersProducer, RaiffeisenAtGroupDateMapper dateMapper, RaiffeisenAtGroupProperties properties, HttpErrorHandlerV2 httpErrorHandler) {
        super(registry, restTemplate, provider);
        this.httpHeadersProducer = httpHeadersProducer;
        this.dateMapper = dateMapper;
        this.properties = properties;
        this.httpErrorHandler = httpErrorHandler;
    }

    @Override
    public Token createClientCredentialToken(final MultiValueMap<String, String> requestBody) throws TokenInvalidException {
        HttpHeaders headers = httpHeadersProducer.createClientCredentialTokenHttpHeaders();
        return exchange(properties.getTokenUrl(), HttpMethod.POST, new HttpEntity<>(requestBody, headers), ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT, Token.class, httpErrorHandler).getBody();
    }

    @Override
    public CreateConsentResponse createUserConsent(final String clientCredentialToken, final ConsentRequest consentRequest, final String redirectUri, final String psuIpAddress) throws TokenInvalidException {
        HttpHeaders headers = httpHeadersProducer.createUserConsentHeaders(clientCredentialToken, redirectUri, psuIpAddress);
        return exchange(POST_CONSENT_URL, HttpMethod.POST, new HttpEntity<>(consentRequest, headers), ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT, CreateConsentResponse.class, httpErrorHandler).getBody();
    }

    @Override
    public GetConsentResponse getConsentStatus(final String clientCredentialToken, final String consentId, final String psuIpAddress) throws TokenInvalidException {
        HttpHeaders headers = httpHeadersProducer.getUserConsentHeaders(clientCredentialToken, psuIpAddress);
        return exchange(CONSENT_URL, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.RETRIEVE_ACCOUNT_ACCESS_CONSENT, GetConsentResponse.class, httpErrorHandler, consentId).getBody();
    }

    @Override
    public void deleteUserConsent(final String clientCredentialToken, final String consentId, final String psuIpAddress) throws TokenInvalidException {
        HttpHeaders headers = httpHeadersProducer.deleteUserConsentHeaders(clientCredentialToken, psuIpAddress);
        exchange(CONSENT_URL, HttpMethod.DELETE, new HttpEntity<>(headers), ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT, Void.class, httpErrorHandler, consentId);
    }

    @Override
    public AccountResponse fetchAccounts(final String clientAccessToken, final String consentId, final String psuIpAddress) throws TokenInvalidException {
        String endpoint = UriComponentsBuilder.fromPath(ACCOUNT_URL)
                .queryParam("withBalance", true)
                .build().toUriString();
        HttpHeaders headers = httpHeadersProducer.getFetchDataHeaders(clientAccessToken, consentId, psuIpAddress);
        return exchange(endpoint, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_ACCOUNTS, AccountResponse.class, httpErrorHandler).getBody();
    }

    @Override
    public TransactionResponse fetchTransaction(final String resourceId, final String clientAccessToken, final String consentId, final String psuIpAddress, final Instant transactionsFetchStartTime) throws TokenInvalidException {
        String endpoint = UriComponentsBuilder.fromPath(TRANSACTION_URL)
                .queryParam("bookingStatus", "both")
                .queryParam("dateFrom", dateMapper.toDateFormat(transactionsFetchStartTime))
                .build().toUriString();
        HttpHeaders headers = httpHeadersProducer.getFetchDataHeaders(clientAccessToken, consentId, psuIpAddress);
        return exchange(endpoint, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID, TransactionResponse.class, httpErrorHandler, resourceId).getBody();
    }

    @Override
    public TransactionResponse fetchTransaction(String url, String clientAccessToken, String consentId, String psuIpAddress) throws TokenInvalidException {
        HttpHeaders headers = httpHeadersProducer.getFetchDataHeaders(clientAccessToken, consentId, psuIpAddress);
        return exchange(url, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID, TransactionResponse.class, httpErrorHandler).getBody();
    }

    @Override
    public RegistrationResponse register() throws TokenInvalidException {
        HttpHeaders headers = httpHeadersProducer.getRegistrationHeaders();
        return exchange(properties.getRegistrationUrl(), HttpMethod.POST, new HttpEntity<>(headers), ProviderClientEndpoints.REGISTER, RegistrationResponse.class, httpErrorHandler).getBody();
    }
}
