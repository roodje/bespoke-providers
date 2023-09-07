package com.yolt.providers.bancatransilvania.common.http;

import com.yolt.providers.bancatransilvania.common.domain.BancaTransilvaniaGroupProviderState;
import com.yolt.providers.bancatransilvania.common.domain.model.consent.ConsentCreationRequest;
import com.yolt.providers.bancatransilvania.common.domain.model.consent.ConsentCreationResponse;
import com.yolt.providers.bancatransilvania.common.domain.model.consent.ConsentStatusResponse;
import com.yolt.providers.bancatransilvania.common.domain.model.fetchdata.AccountsResponse;
import com.yolt.providers.bancatransilvania.common.domain.model.fetchdata.TransactionsResponse;
import com.yolt.providers.bancatransilvania.common.domain.model.registration.RegistrationRequest;
import com.yolt.providers.bancatransilvania.common.domain.model.registration.RegistrationResponse;
import com.yolt.providers.bancatransilvania.common.domain.model.token.TokenResponse;
import com.yolt.providers.bancatransilvania.common.http.error.BancaTransilvaniaFetchDataHttpErrorHandler;
import com.yolt.providers.bancatransilvania.common.http.error.BancaTransilvaniaRegistrationHttpErrorHandler;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

public class BancaTransilvaniaGroupHttpClient extends DefaultHttpClient {

    private static final String REGISTRATION_ENDPOINT = "/oauth-prd/register";
    private static final String TOKEN_ENDPOINT = "/oauth-prd/token";
    private static final String AISP_PATH = "/bt-psd2-aisp-prd";
    private static final String CONSENT_ENDPOINT = AISP_PATH + "/v1/consents";
    private static final String CONSENT_STATUS_TEMPLATE = AISP_PATH + "/v1/consents/{consentId}/status";
    private static final String ACCOUNTS_WITH_BALANCES_ENDPOINT = AISP_PATH + "/v1/accounts?withBalance=true";
    private static final String TRANSACTIONS_TEMPLATE = AISP_PATH + "/v1/accounts/{accountId}/transactions";

    private static final HttpErrorHandler REGISTRATION_HTTP_ERROR_HANDLER = new BancaTransilvaniaRegistrationHttpErrorHandler();
    private static final HttpErrorHandler FETCH_DATA_HTTP_ERROR_HANDLER = new BancaTransilvaniaFetchDataHttpErrorHandler();

    private final BancaTransilvaniaGroupHttpHeadersProducer headersProducer;

    BancaTransilvaniaGroupHttpClient(MeterRegistry meterRegistry,
                                     RestTemplate restTemplate,
                                     String providerDisplayName,
                                     BancaTransilvaniaGroupHttpHeadersProducer headersProducer) {
        super(meterRegistry, restTemplate, providerDisplayName);
        this.headersProducer = headersProducer;
    }

    public String getTransactionsTemplatePath() {
        return TRANSACTIONS_TEMPLATE;
    }

    @SneakyThrows(TokenInvalidException.class)
    public RegistrationResponse postRegistration(RegistrationRequest request) {
        HttpEntity<RegistrationRequest> entity = new HttpEntity<>(request, headersProducer.createRegistrationHeaders());
        return exchange(REGISTRATION_ENDPOINT, POST, entity, REGISTER, RegistrationResponse.class, REGISTRATION_HTTP_ERROR_HANDLER)
                .getBody();
    }

    public ConsentCreationResponse postConsentCreation(ConsentCreationRequest request, String psuIpAddress) throws TokenInvalidException {
        HttpEntity<ConsentCreationRequest> entity = new HttpEntity<>(request, headersProducer.createConsentHeaders(psuIpAddress));
        return exchangeForBody(CONSENT_ENDPOINT, POST, entity, GET_ACCOUNT_ACCESS_CONSENT, ConsentCreationResponse.class);
    }

    public ConsentStatusResponse getConsentStatus(BancaTransilvaniaGroupProviderState providerState, String psuIpAddress) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createConsentStatusHeaders(providerState, psuIpAddress));
        return exchangeForBody(CONSENT_STATUS_TEMPLATE, GET, entity, RETRIEVE_ACCOUNT_ACCESS_CONSENT, ConsentStatusResponse.class, providerState.getConsentId());
    }

    public TokenResponse postAccessToken(MultiValueMap<String, String> body) throws TokenInvalidException {
        return postToken(body, GET_ACCESS_TOKEN);
    }

    public TokenResponse postRefreshToken(MultiValueMap<String, String> body) throws TokenInvalidException {
        return postToken(body, REFRESH_TOKEN);
    }

    private TokenResponse postToken(MultiValueMap<String, String> body, String providerClientEndpoint) throws TokenInvalidException {
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headersProducer.createTokenHeaders());
        return exchangeForBody(TOKEN_ENDPOINT, POST, entity, providerClientEndpoint, TokenResponse.class);
    }

    public AccountsResponse getAccounts(BancaTransilvaniaGroupProviderState providerState, String psuIpAddress) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(providerState, psuIpAddress));
        return exchange(ACCOUNTS_WITH_BALANCES_ENDPOINT, GET, entity, GET_ACCOUNTS, AccountsResponse.class, FETCH_DATA_HTTP_ERROR_HANDLER).getBody();
    }

    public TransactionsResponse getTransactions(String url, BancaTransilvaniaGroupProviderState providerState, String psuIpAddress) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createFetchDataHeaders(providerState, psuIpAddress));
        return exchange(url, GET, entity, GET_TRANSACTIONS_BY_ACCOUNT_ID, TransactionsResponse.class, FETCH_DATA_HTTP_ERROR_HANDLER).getBody();
    }
}