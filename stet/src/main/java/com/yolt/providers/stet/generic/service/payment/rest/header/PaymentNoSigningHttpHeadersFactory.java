package com.yolt.providers.stet.generic.service.payment.rest.header;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.service.payment.request.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.function.Supplier;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
@RequiredArgsConstructor
public class PaymentNoSigningHttpHeadersFactory implements PaymentHttpHeadersFactory {

    private final Supplier<String> lastExternalTraceIdSupplier;

    public PaymentNoSigningHttpHeadersFactory() {
        this.lastExternalTraceIdSupplier = ExternalTracingUtil::createLastExternalTraceId;
    }

    @Override
    public HttpHeaders createClientTokenHeaders(DefaultAuthenticationMeans authMeans, MultiValueMap<String, String> body, Signer signer, String url) {
        return HttpHeadersBuilder.builder()
                .withAccept(Collections.singletonList(MediaType.APPLICATION_JSON))
                .withBasicAuthorization(authMeans.getClientId(), authMeans.getClientSecret())
                .withContentType(MediaType.APPLICATION_FORM_URLENCODED)
                .build();
    }

    @Override
    public HttpHeaders createPaymentInitiationHeaders(HttpMethod method, PaymentRequest paymentRequest, Object requestBody) {
        return prepareCommonHttpHeaders(paymentRequest);
    }

    @Override
    public HttpHeaders createPaymentConfirmationHeaders(HttpMethod method, PaymentRequest paymentRequest, Object requestBody) {
        return prepareCommonHttpHeaders(paymentRequest);
    }

    @Override
    public HttpHeaders createPaymentStatusHeaders(HttpMethod method, PaymentRequest paymentRequest) {
        return prepareCommonHttpHeaders(paymentRequest);
    }

    protected HttpHeaders prepareCommonHttpHeaders(PaymentRequest paymentRequest) {
        return HttpHeadersBuilder.builder()
                .withAccept(Collections.singletonList(APPLICATION_JSON))
                .withBearerAuthorization(paymentRequest.getAccessToken())
                .withContentType(APPLICATION_JSON)
                .withPsuIpAddress(paymentRequest.getPsuIpAddress())
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .build();
    }
}
