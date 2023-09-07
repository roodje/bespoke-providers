package com.yolt.providers.fineco.pis.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.fineco.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
public class FinecoInitiatePaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<FinecoInitiatePaymentPreExecutionResult, PaymentRequest> {

    private static final String TPP_REDIRECT_URI_HEADER = "TPP-Redirect-URI";
    private static final String PSU_IP_ADDRESS = "PSU-IP-Address";

    @Override
    public HttpHeaders provideHttpHeaders(FinecoInitiatePaymentPreExecutionResult preExecutionResult, PaymentRequest paymentRequest) {
        var redirectUrlWithState = UriComponentsBuilder.fromUriString(preExecutionResult.getBaseClientRedirectUrl())
                .queryParam("state", preExecutionResult.getState()).toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(TPP_REDIRECT_URI_HEADER, redirectUrlWithState);
        headers.add(PSU_IP_ADDRESS, preExecutionResult.getPsuIpAddress());
        return headers;
    }
}
