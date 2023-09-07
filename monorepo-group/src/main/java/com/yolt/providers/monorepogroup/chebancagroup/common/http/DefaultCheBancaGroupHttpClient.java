package com.yolt.providers.monorepogroup.chebancagroup.common.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClientV3;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.monorepogroup.chebancagroup.common.CheBancaGroupProperties;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.*;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.internal.SignatureDTO;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

public class DefaultCheBancaGroupHttpClient extends DefaultHttpClientV3 implements CheBancaGroupHttpClient {

    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.SHA256_WITH_RSA;
    private static final String CUSTOMER_ID_INFO_URL = "/v1/private/customers/customerid-info";
    private static final String ACCOUNT_URL = "/v1/private/customers/{customerId}/accounts";
    private static final String TRANSACTIONS_URL = "/v1/private/customers/{customerId}/products/{productId}/transactions/retrieve";
    private static final String BALANCES_URL = "/v1/private/customers/{customerId}/products/{productId}/balance/retrieve";
    private final CheBancaGroupHttpHeadersProducer httpHeadersProducer;
    private final CheBancaGroupProperties properties;
    private final HttpErrorHandlerV2 httpErrorHandler;
    private final ObjectMapper objectMapper;

    public DefaultCheBancaGroupHttpClient(final MeterRegistry registry,
                                          final RestTemplate restTemplate,
                                          final String provider,
                                          final CheBancaGroupHttpHeadersProducer httpHeadersProducer,
                                          final CheBancaGroupProperties properties,
                                          final HttpErrorHandlerV2 httpErrorHandler,
                                          final ObjectMapper objectMapper) {
        super(registry, restTemplate, provider);
        this.httpHeadersProducer = httpHeadersProducer;
        this.properties = properties;
        this.httpErrorHandler = httpErrorHandler;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<String> createAuthorizationSession(final Signer signer, final String authorizationUrl, final CheBancaGroupAuthenticationMeans authenticationMeans) throws TokenInvalidException {
        SignatureDTO signatureDTO = createSignatureForRequest(authenticationMeans, authorizationUrl.replace(properties.getBaseUrl(), ""), GET);
        HttpHeaders headers = httpHeadersProducer.createAuthorizationHttpHeaders(signatureDTO, signer, null);
        return exchange(authorizationUrl, GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT, String.class, httpErrorHandler);
    }

    @Override
    public CheBancaGroupToken createClientCredentialToken(final Signer signer, final MultiValueMap<String, String> requestBody, final CheBancaGroupAuthenticationMeans authenticationMeans) throws TokenInvalidException {
        SignatureDTO signatureDTO = createSignatureForRequest(authenticationMeans, properties.getTokenUrl(), POST);
        HttpHeaders headers = httpHeadersProducer.createGetTokenHttpHeaders(signatureDTO, signer, getSerializedRequestBody(requestBody));
        return exchange(properties.getTokenUrl(), POST, new HttpEntity<>(requestBody, headers), ProviderClientEndpoints.GET_ACCESS_TOKEN, CheBancaGroupToken.class, httpErrorHandler).getBody();
    }

    @Override
    public CheBancaGroupToken createRefreshToken(final Signer signer, final MultiValueMap<String, String> requestBody, final CheBancaGroupAuthenticationMeans authenticationMeans) throws TokenInvalidException {
        SignatureDTO signatureDTO = createSignatureForRequest(authenticationMeans, properties.getTokenUrl(), POST);
        HttpHeaders headers = httpHeadersProducer.createGetTokenHttpHeaders(signatureDTO, signer, getSerializedRequestBody(requestBody));
        return exchange(properties.getTokenUrl(), POST, new HttpEntity<>(requestBody, headers), ProviderClientEndpoints.REFRESH_TOKEN, CheBancaGroupToken.class, httpErrorHandler).getBody();
    }

    @Override
    public AccountResponse fetchAccounts(final Signer signer, final CheBancaGroupAuthenticationMeans authenticationMeans, final String clientAccessToken, final String customerId) throws TokenInvalidException {
        SignatureDTO signatureDTO = createSignatureForRequest(authenticationMeans, properties.getTokenUrl(), POST);
        HttpHeaders headers = httpHeadersProducer.getFetchDataHeaders(signatureDTO, signer, null, clientAccessToken);
        return exchange(ACCOUNT_URL, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_ACCOUNTS, AccountResponse.class, httpErrorHandler, customerId).getBody();
    }

    @Override
    public CustomerIdResponse getCustomerId(Signer signer, CheBancaGroupAuthenticationMeans authenticationMeans, String clientAccessToken) throws TokenInvalidException {
        SignatureDTO signatureDTO = createSignatureForRequest(authenticationMeans, properties.getTokenUrl(), POST);
        HttpHeaders headers = httpHeadersProducer.getFetchDataHeaders(signatureDTO, signer, null, clientAccessToken);
        return exchange(CUSTOMER_ID_INFO_URL, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_ACCOUNTS, CustomerIdResponse.class, httpErrorHandler).getBody();
    }

    @Override
    public TransactionResponse fetchTransactions(final Signer signer, final CheBancaGroupAuthenticationMeans authenticationMeans, final String clientAccessToken, final String customerId, final String productId, final Instant transactionsFetchStartTime) throws TokenInvalidException {
        SignatureDTO signatureDTO = createSignatureForRequest(authenticationMeans, properties.getTokenUrl(), POST);
        HttpHeaders headers = httpHeadersProducer.getFetchDataHeaders(signatureDTO, signer, null, clientAccessToken);
        return exchange(TRANSACTIONS_URL, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID, TransactionResponse.class, httpErrorHandler, customerId, productId).getBody();
    }

    @Override
    public TransactionResponse fetchTransactions(final Signer signer, final CheBancaGroupAuthenticationMeans authenticationMeans, final String clientAccessToken, final String nextHrefUrl) throws TokenInvalidException {
        SignatureDTO signatureDTO = createSignatureForRequest(authenticationMeans, properties.getTokenUrl(), POST);
        HttpHeaders headers = httpHeadersProducer.getFetchDataHeaders(signatureDTO, signer, null, clientAccessToken);
        return exchange(nextHrefUrl, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID, TransactionResponse.class, httpErrorHandler).getBody();
    }

    @Override
    public Balances fetchBalances(final Signer signer, final CheBancaGroupAuthenticationMeans authenticationMeans, final String clientAccessToken, final String customerId, final String productId) throws TokenInvalidException {
        SignatureDTO signatureDTO = createSignatureForRequest(authenticationMeans, properties.getTokenUrl(), POST);
        HttpHeaders headers = httpHeadersProducer.getFetchDataHeaders(signatureDTO, signer, null, clientAccessToken);
        return exchange(BALANCES_URL, HttpMethod.GET, new HttpEntity<>(headers), ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID, Balances.class, httpErrorHandler, customerId, productId).getBody();
    }

    private SignatureDTO createSignatureForRequest(final CheBancaGroupAuthenticationMeans authenticationMeans,
                                                   final String url,
                                                   final HttpMethod method) {

        String keyId = authenticationMeans.getClientAppId();
        UUID signingKid = authenticationMeans.getSigningCertificateId();
        return SignatureUtils.constructSignatureDTO(keyId, signingKid, url, method, SIGNATURE_ALGORITHM);
    }

    private <T> byte[] getSerializedRequestBody(final T requestBody) throws TokenInvalidException {
        try {
            return objectMapper.writeValueAsString(requestBody).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Serialization request body failed");
        }
    }
}
