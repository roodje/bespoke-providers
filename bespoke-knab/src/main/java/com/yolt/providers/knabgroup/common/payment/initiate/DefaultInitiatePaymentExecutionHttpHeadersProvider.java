package com.yolt.providers.knabgroup.common.payment.initiate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.knabgroup.common.exception.UnexpectedJsonElementException;
import com.yolt.providers.knabgroup.common.payment.DefaultCommonPaymentHttpHeadersProvider;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.InitiatePaymentPreExecutionResult;
import com.yolt.providers.knabgroup.common.payment.dto.external.InitiatePaymentRequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class DefaultInitiatePaymentExecutionHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<InitiatePaymentPreExecutionResult, InitiatePaymentRequestBody> {

    private static final String TPP_REDIRECT_URI_HEADER_NAME = "TPP-Redirect-URI";
    private static final String STATE_PARAM_NAME = "state";

    private final DefaultCommonPaymentHttpHeadersProvider commonHttpHeadersProvider;
    private final ObjectMapper objectMapper;

    @Override
    public HttpHeaders provideHttpHeaders(final InitiatePaymentPreExecutionResult preExecutionResult, final InitiatePaymentRequestBody paymentBody) {
        String serializedBody = getSerializedBody(paymentBody);
        HttpHeaders commonHttpHeaders = commonHttpHeadersProvider.provideHttpHeaders(
                preExecutionResult.getAccessToken(),
                preExecutionResult.getAuthenticationMeans().getSigningData(preExecutionResult.getSigner()),
                serializedBody.getBytes(StandardCharsets.UTF_8),
                preExecutionResult.getPsuIpAddress());

        return enrichWithTppRedirectHeader(commonHttpHeaders, preExecutionResult);
    }

    private String getSerializedBody(InitiatePaymentRequestBody paymentBody) {
        try {
            return objectMapper.writeValueAsString(paymentBody);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Couldn't convert payment body to string");
        }
    }

    private HttpHeaders enrichWithTppRedirectHeader(final HttpHeaders commonHeaders, final InitiatePaymentPreExecutionResult preExecutionResult) {
        String baseClientRedirectUrl = preExecutionResult.getBaseClientRedirectUrl();
        String redirectWithState = UriComponentsBuilder.fromHttpUrl(baseClientRedirectUrl)
                .queryParam(STATE_PARAM_NAME, preExecutionResult.getState())
                .toUriString();
        commonHeaders.add(TPP_REDIRECT_URI_HEADER_NAME, redirectWithState);
        return commonHeaders;
    }
}
