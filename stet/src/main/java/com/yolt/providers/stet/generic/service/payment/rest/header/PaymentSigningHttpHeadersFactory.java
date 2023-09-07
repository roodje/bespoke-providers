package com.yolt.providers.stet.generic.service.payment.rest.header;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
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
public class PaymentSigningHttpHeadersFactory implements PaymentHttpHeadersFactory {

    private final HttpSigner httpSigner;
    private final Supplier<String> lastExternalTraceIdSupplier;

    public PaymentSigningHttpHeadersFactory(HttpSigner httpSigner) {
        this.httpSigner = httpSigner;
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
        SignatureData signatureData = prepareSignatureData(method, paymentRequest);
        return prepareCommonHttpHeaders(paymentRequest, signatureData, requestBody);
    }

    @Override
    public HttpHeaders createPaymentConfirmationHeaders(HttpMethod method, PaymentRequest paymentRequest, Object body) {
        SignatureData signatureData = prepareSignatureData(method, paymentRequest);
        return prepareCommonHttpHeaders(paymentRequest, signatureData, body);
    }

    @Override
    public HttpHeaders createPaymentStatusHeaders(HttpMethod method, PaymentRequest paymentRequest) {
        SignatureData signatureData = prepareSignatureData(method, paymentRequest);
        return prepareCommonHttpHeaders(paymentRequest, signatureData, new byte[0]);
    }

    protected SignatureData prepareSignatureData(HttpMethod method, PaymentRequest paymentRequest) {
        DefaultAuthenticationMeans authMeans = paymentRequest.getAuthMeans();
        return new SignatureData(
                paymentRequest.getSigner(),
                authMeans.getSigningKeyIdHeader(),
                authMeans.getClientSigningKeyId(),
                authMeans.getClientSigningCertificate(),
                method,
                paymentRequest.getUrl());
    }

    protected HttpHeaders prepareCommonHttpHeaders(PaymentRequest paymentRequest, SignatureData signatureData, Object requestBody) {
        return HttpHeadersBuilder.builder(httpSigner)
                .withAccept(Collections.singletonList(APPLICATION_JSON))
                .withBearerAuthorization(paymentRequest.getAccessToken())
                .withContentType(APPLICATION_JSON)
                .withPsuIpAddress(paymentRequest.getPsuIpAddress())
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .signAndBuild(signatureData, requestBody);
    }
}
