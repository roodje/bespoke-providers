package com.yolt.providers.rabobank.pis.pec.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;
import com.yolt.providers.rabobank.pis.pec.RabobankCommonHttpHeaderProvider;
import com.yolt.providers.rabobank.pis.pec.RabobankPisHeadersSigner;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class RabobankSepaSumbitPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<RabobankSepaSubmitPaymentPreExecutionResult, Void> {

    private final RabobankCommonHttpHeaderProvider commonHttpHeaderProvider;
    private final RabobankPisHeadersSigner pisHeadersSigner;

    @Override
    @SneakyThrows
    public HttpHeaders provideHttpHeaders(RabobankSepaSubmitPaymentPreExecutionResult preExecutionResult, Void unused) {
        HttpHeaders headers = commonHttpHeaderProvider.providerCommonHttpHeaders(preExecutionResult.getPsuIpAddress(), preExecutionResult.getAuthenticationMeans().getClientId());
        RabobankAuthenticationMeans authenticationMeans = preExecutionResult.getAuthenticationMeans();
        return pisHeadersSigner.signHeaders(headers,
                new byte[]{},
                preExecutionResult.getSigner(),
                authenticationMeans.getSigningKid(),
                authenticationMeans.getClientSigningCertificate());
    }
}
