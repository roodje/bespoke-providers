package com.yolt.providers.stet.generic.service.payment.rest;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.service.payment.request.PaymentRequest;
import com.yolt.providers.stet.generic.service.payment.rest.error.DefaultPaymentHttpErrorHandler;
import com.yolt.providers.stet.generic.service.payment.rest.header.PaymentHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;
import static org.springframework.http.HttpMethod.POST;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
@RequiredArgsConstructor
public class DefaultPaymentRestClient implements PaymentRestClient {

    private final PaymentHttpHeadersFactory httpHeadersFactory;
    protected final DefaultPaymentHttpErrorHandler errorHandler;

    public DefaultPaymentRestClient(PaymentHttpHeadersFactory httpHeadersFactory) {
        this.httpHeadersFactory = httpHeadersFactory;
        this.errorHandler = new DefaultPaymentHttpErrorHandler();
    }

    @Override
    public TokenResponseDTO getClientToken(HttpClient httpClient,
                                           String url,
                                           DefaultAuthenticationMeans authMeans,
                                           MultiValueMap<String, String> body,
                                           Signer signer) {
        HttpMethod method = POST;
        String prometheusPath = CLIENT_CREDENTIALS_GRANT;
        HttpHeaders headers = httpHeadersFactory.createClientTokenHeaders(authMeans, body, signer, url);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ExecutionInfo executionInfo = new ExecutionInfo(url, method, headers, prometheusPath);
        return errorHandler.executeAndHandle(() -> httpClient.exchangeForBody(url, method, entity, prometheusPath, TokenResponseDTO.class), executionInfo);
    }

    @Override
    public StetPaymentInitiationResponseDTO initiatePayment(HttpClient httpClient,
                                                            PaymentRequest paymentRequest,
                                                            StetPaymentInitiationRequestDTO body) {
        String url = paymentRequest.getUrl();
        HttpMethod method = HttpMethod.POST;
        String prometheusPath = INITIATE_PAYMENT;
        HttpHeaders headers = httpHeadersFactory.createPaymentInitiationHeaders(method, paymentRequest, body);
        HttpEntity<StetPaymentInitiationRequestDTO> entity = new HttpEntity<>(body, headers);

        ExecutionInfo executionInfo = new ExecutionInfo(url, method, headers, prometheusPath);
        return errorHandler.executeAndHandle(() -> httpClient.exchangeForBody(url, method, entity, prometheusPath, StetPaymentInitiationResponseDTO.class), executionInfo);
    }

    @Override
    public StetPaymentConfirmationResponseDTO confirmPayment(HttpClient httpClient,
                                                             PaymentRequest paymentRequest,
                                                             StetPaymentConfirmationRequestDTO body) {
        String url = paymentRequest.getUrl();
        HttpMethod method = HttpMethod.POST;
        String prometheusPath = SUBMIT_PAYMENT;
        HttpHeaders headers = httpHeadersFactory.createPaymentConfirmationHeaders(method, paymentRequest, body);
        HttpEntity<StetPaymentConfirmationRequestDTO> entity = new HttpEntity<>(body, headers);

        ExecutionInfo executionInfo = new ExecutionInfo(url, method, headers, prometheusPath);
        return errorHandler.executeAndHandle(() -> httpClient.exchangeForBody(url, method, entity, prometheusPath, StetPaymentConfirmationResponseDTO.class), executionInfo);
    }

    @Override
    public StetPaymentStatusResponseDTO getPaymentStatus(HttpClient httpClient, PaymentRequest paymentRequest) {
        String url = paymentRequest.getUrl();
        HttpMethod method = HttpMethod.GET;
        String prometheusPath = GET_PAYMENT_STATUS;
        HttpHeaders headers = httpHeadersFactory.createPaymentStatusHeaders(method, paymentRequest);
        HttpEntity entity = new HttpEntity(headers);

        ExecutionInfo executionInfo = new ExecutionInfo(url, method, headers, prometheusPath);
        return errorHandler.executeAndHandle(() -> httpClient.exchangeForBody(url, method, entity, prometheusPath, StetPaymentStatusResponseDTO.class), executionInfo);
    }
}
