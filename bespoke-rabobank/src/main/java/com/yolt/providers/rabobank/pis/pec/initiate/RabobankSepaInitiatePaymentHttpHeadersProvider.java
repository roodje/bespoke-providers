package com.yolt.providers.rabobank.pis.pec.initiate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;
import com.yolt.providers.rabobank.dto.external.SepaCreditTransfer;
import com.yolt.providers.rabobank.pis.pec.RabobankCommonHttpHeaderProvider;
import com.yolt.providers.rabobank.pis.pec.RabobankPisHeadersSigner;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

@AllArgsConstructor
public class RabobankSepaInitiatePaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<RabobankSepaInitiatePreExecutionResult, SepaCreditTransfer> {

    private final ObjectMapper objectMapper;
    private final RabobankCommonHttpHeaderProvider commonHttpHeaderProvider;
    private final RabobankPisHeadersSigner pisHeaderSigner;

    @SneakyThrows
    @Override
    public HttpHeaders provideHttpHeaders(RabobankSepaInitiatePreExecutionResult preExecutionResult, SepaCreditTransfer sepaCreditTransfer) {
        HttpHeaders headers = commonHttpHeaderProvider.providerCommonHttpHeaders(preExecutionResult.getPsuIpAddress(), preExecutionResult.getAuthenticationMeans().getClientId());
        UriComponentsBuilder redirectUrlWithStateBuilder = UriComponentsBuilder.fromUriString(preExecutionResult.getBaseClientRedirectUrl())
                .queryParam("state", preExecutionResult.getState());
        headers.add("tpp-redirect-uri", redirectUrlWithStateBuilder.build().toUriString());
        UriComponentsBuilder redirectUrlWithStateAndErrorBuilder = redirectUrlWithStateBuilder
                .queryParam("error", "denied");
        headers.add("tpp-nok-redirect-uri", redirectUrlWithStateAndErrorBuilder.build().toUriString());
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        byte[] bodyBytes = objectMapper.writeValueAsBytes(sepaCreditTransfer);
        RabobankAuthenticationMeans authenticationMeans = preExecutionResult.getAuthenticationMeans();
        return pisHeaderSigner.signHeaders(headers,
                bodyBytes,
                preExecutionResult.getSigner(),
                authenticationMeans.getSigningKid(),
                authenticationMeans.getClientSigningCertificate());
    }
}
