package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.restclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.exception.UnexpectedJsonElementException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.config.LloydsBankingGroupPropertiesV2;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.http.LloydsBankingGroupErrorHandlerV2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

import static com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.http.LloydsBankingGroupDeleteAccountRequestErrorHandlerV2.LLOYDS_BANKING_GROUP_DELETE_ACCOUNT_REQUEST_ERROR_HANDLERERROR_HANDLER;

public class LloydsBankingGroupRestClientV7 extends DefaultRestClient {

    private static final String X_LBG_CHANNEL_HEADER_NAME = "x-lbg-channel";

    private final ObjectMapper objectMapper;
    private final LloydsBankingGroupPropertiesV2 properties;

    public LloydsBankingGroupRestClientV7(PaymentRequestSigner paymentRequestSigner,
                                          ObjectMapper objectMapper,
                                          final LloydsBankingGroupPropertiesV2 properties) {
        super(paymentRequestSigner);
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    protected HttpHeaders getHeaders(final AccessMeans clientAccessToken, final String institutionId) {
        return prepareHeaders(clientAccessToken, institutionId);
    }

    @Override
    public void deleteAccountAccessConsent(final HttpClient httpClient,
                                           final String exchangePath,
                                           final AccessMeans clientAccessToken,
                                           final String consentId,
                                           final DefaultAuthMeans authMeans) throws TokenInvalidException {
        httpClient.exchange(exchangePath + "/" + consentId,
                HttpMethod.DELETE,
                new HttpEntity<>(getHeaders(clientAccessToken, authMeans.getInstitutionId())),
                ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT,
                Void.class,
                LLOYDS_BANKING_GROUP_DELETE_ACCOUNT_REQUEST_ERROR_HANDLERERROR_HANDLER);
    }

    @Override
    protected HttpErrorHandler getErrorHandler() {
        return LloydsBankingGroupErrorHandlerV2.LLOYDS_BANKING_GROUP_ERROR_HANDLER;
    }

    private HttpHeaders prepareHeaders(final AccessMeans clientAccessToken, final String institutionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(clientAccessToken.getAccessToken());
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add(HttpExtraHeaders.FINANCIAL_ID_HEADER_NAME, institutionId);
        headers.add(X_LBG_CHANNEL_HEADER_NAME, "RC");
        return headers;
    }

    @Override
    protected HttpHeaders getPaymentHttpHeaders(final AccessMeans clientAccessToken, final DefaultAuthMeans authMeans, final Object requestBody, final Signer signer) {
        HttpHeaders headers = getHeaders(clientAccessToken, authMeans.getInstitutionId());
        headers.add(HttpExtraHeaders.SIGNATURE_HEADER_NAME, payloadSigner.createRequestSignature(requestBody, authMeans, signer));
        return headers;
    }

    @Override
    public <T> T createPayment(final HttpClient httpClient, final String exchangePath, final AccessMeans clientAccessToken, final DefaultAuthMeans authMeans, final Object requestBody, final Class<T> responseType, final Signer signer) throws TokenInvalidException {
        HttpHeaders headers = getPaymentHttpHeaders(clientAccessToken, authMeans, requestBody, signer);
        headers.add(HttpExtraHeaders.IDEMPOTENT_KEY, calculateIdempotencyKey(requestBody));

        return httpClient.exchange(properties.getPaymentsUrl() + exchangePath,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                ProviderClientEndpoints.INITIATE_PAYMENT,
                responseType,
                getErrorHandler()).getBody();
    }

    @Override
    public <T> T submitPayment(final HttpClient httpClient, final String exchangePath, final AccessMeans userAccessToken, final DefaultAuthMeans authMeans, final Object requestBody, final Class<T> responseType, final Signer signer) throws TokenInvalidException {
        HttpHeaders headers = getPaymentHttpHeaders(userAccessToken, authMeans, requestBody, signer);
        headers.add(HttpExtraHeaders.IDEMPOTENT_KEY, calculateIdempotencyKey(requestBody));

        return httpClient.exchange(properties.getPaymentsUrl() + exchangePath,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                ProviderClientEndpoints.SUBMIT_PAYMENT,
                responseType,
                getErrorHandler()).getBody();
    }

    private String calculateIdempotencyKey(final Object requestBody) {
        byte[] data;
        try {
            data = objectMapper.writeValueAsString(requestBody).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Could not convert request body to byte array");
        }
        return DigestUtils.md5DigestAsHex(data);
    }

}
