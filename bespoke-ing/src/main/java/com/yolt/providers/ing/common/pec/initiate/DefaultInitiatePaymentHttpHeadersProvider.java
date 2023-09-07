package com.yolt.providers.ing.common.pec.initiate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.ing.common.dto.SepaCreditTransfer;
import com.yolt.providers.ing.common.pec.DefaultCommonHttpHeadersProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

import static com.yolt.providers.ing.common.pec.IngPecConstants.INITIATE_PAYMENT_ENDPOINT;
import static com.yolt.providers.ing.common.pec.IngPecConstants.INITIATE_PAYMENT_HTTP_METHOD;

/**
 * {@inheritDoc}
 *
 * @deprecated Use {@link DefaultInitiatePaymentHttpHeadersProviderV2} instead
 */
@Deprecated
@RequiredArgsConstructor
public class DefaultInitiatePaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<DefaultInitiatePaymentPreExecutionResult, SepaCreditTransfer> {

    private static final String TPP_REDIRECT_URI_HEADER_NAME = "TPP-Redirect-URI";
    private static final String STATE_PARAM_NAME = "state";

    private final DefaultCommonHttpHeadersProvider commonHttpHeadersProvider;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows(JsonProcessingException.class)
    public HttpHeaders provideHttpHeaders(final DefaultInitiatePaymentPreExecutionResult preExecutionResult, final SepaCreditTransfer sepaCreditTransfer) {
        String serializedBody = objectMapper.writeValueAsString(sepaCreditTransfer);
        HttpHeaders commonHttpHeaders = commonHttpHeadersProvider.provideHttpHeaders(
                preExecutionResult,
                serializedBody.getBytes(StandardCharsets.UTF_8),
                INITIATE_PAYMENT_HTTP_METHOD,
                INITIATE_PAYMENT_ENDPOINT);

        return enrichWithTppRedirectHeader(commonHttpHeaders, preExecutionResult);
    }

    private HttpHeaders enrichWithTppRedirectHeader(final HttpHeaders commonHeaders, final DefaultInitiatePaymentPreExecutionResult preExecutionResult) {
        String baseClientRedirectUrl = preExecutionResult.getBaseClientRedirectUrl();
        String redirectWithState = UriComponentsBuilder.fromHttpUrl(baseClientRedirectUrl)
                .queryParam(STATE_PARAM_NAME, preExecutionResult.getState())
                .toUriString();
        commonHeaders.add(TPP_REDIRECT_URI_HEADER_NAME, redirectWithState);
        return commonHeaders;
    }


}
