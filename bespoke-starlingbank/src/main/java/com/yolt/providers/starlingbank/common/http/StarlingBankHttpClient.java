package com.yolt.providers.starlingbank.common.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.starlingbank.common.model.*;
import com.yolt.providers.starlingbank.common.model.domain.Token;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;
import static org.springframework.http.HttpMethod.*;

public class StarlingBankHttpClient extends DefaultHttpClient {

    private final StarlingBankHttpHeadersProducer headersProducer;
    private final HttpErrorHandler errorHandler;

    private static final String GET_HOLDER_NAME = "get_holder_name";

    StarlingBankHttpClient(MeterRegistry meterRegistry,
                           RestTemplate restTemplate,
                           String providerDisplayName,
                           StarlingBankHttpHeadersProducer headersProducer,
                           HttpErrorHandler errorHandler) {
        super(meterRegistry, restTemplate, providerDisplayName);
        this.headersProducer = headersProducer;
        this.errorHandler = errorHandler;
    }

    public Token grantToken(String url, MultiValueMap<String, String> body) throws TokenInvalidException {
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headersProducer.createEncodedHeaders());
        return exchange(url, POST, entity, GET_ACCESS_TOKEN, Token.class);
    }

    public Token refreshToken(String url, MultiValueMap<String, String> body) throws TokenInvalidException {
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headersProducer.createEncodedHeaders());
        return exchange(url, POST, entity, REFRESH_TOKEN, Token.class);
    }

    public AccountsResponseV2 fetchAccounts(String url, String accessToken) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createAuthorizationHeaders(accessToken));
        return exchange(url, GET, entity, GET_ACCOUNTS, AccountsResponseV2.class);
    }

    public BalancesResponseV2 fetchBalances(String url, String accessToken) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createAuthorizationHeaders(accessToken));
        return exchange(url, GET, entity, GET_BALANCES_BY_ACCOUNT_ID, BalancesResponseV2.class);
    }

    public AccountIdentifiersV2 fetchIdentifiers(String url, String accessToken) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createAuthorizationHeaders(accessToken));
        return exchange(url, GET, entity, GET_ACCOUNT_REQUEST_ID, AccountIdentifiersV2.class);
    }

    public TransactionsResponseV2 fetchTransactions(String url, String accessToken) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createAuthorizationHeaders(accessToken));
        return exchange(url, GET, entity, GET_TRANSACTIONS_BY_ACCOUNT_ID, TransactionsResponseV2.class);
    }

    public AccountHolderNameV2 fetchHolderName(String url, String accessToken) throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createAuthorizationHeaders(accessToken));
        return exchange(url, GET, entity, GET_HOLDER_NAME, AccountHolderNameV2.class);
    }

    public ResponseEntity<JsonNode> submitPayment(String relativeUrl, HttpEntity<PaymentRequest> entity) throws TokenInvalidException {
        return exchange(relativeUrl, PUT, entity, SUBMIT_PAYMENT, JsonNode.class, errorHandler);
    }

    public ResponseEntity<JsonNode> paymentStatus(String url, String accessToken)  throws TokenInvalidException {
        HttpEntity<?> entity = new HttpEntity<>(headersProducer.createAuthorizationHeaders(accessToken));
        return exchange(url, GET, entity, GET_PAYMENT_STATUS, JsonNode.class, errorHandler);
    }

    private <T> T exchange(String url, HttpMethod method, HttpEntity entity, String prometheusPath, Class<T> responseType) throws TokenInvalidException {
        return exchange(url, method, entity, prometheusPath, responseType, errorHandler).getBody();
    }
}
