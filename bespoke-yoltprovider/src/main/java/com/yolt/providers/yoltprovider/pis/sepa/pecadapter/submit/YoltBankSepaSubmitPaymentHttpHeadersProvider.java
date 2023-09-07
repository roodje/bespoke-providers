package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.yoltprovider.pis.sepa.SignatureDTO;
import com.yolt.providers.yoltprovider.pis.sepa.SigningUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RequiredArgsConstructor
public class YoltBankSepaSubmitPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<YoltBankSepaSubmitPreExecutionResult, Void> {

    @Override
    public HttpHeaders provideHttpHeaders(YoltBankSepaSubmitPreExecutionResult preExecutionResult, Void unused) {
        String paymentId = preExecutionResult.getPaymentId();
        final String digest = SigningUtils.prepareDigest(paymentId.getBytes());
        final String signature = SigningUtils.prepareSignature(
                preExecutionResult.getAuthenticationMeans().getClientId(),
                digest,
                preExecutionResult.getSigner(),
                preExecutionResult.getAuthenticationMeans().getSigningKid());

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add("client-id", preExecutionResult.getAuthenticationMeans().getClientId().toString());
        httpHeaders.add("digest", digest);
        httpHeaders.add("signature", new SignatureDTO(preExecutionResult.getAuthenticationMeans().getPublicKid(), signature).getSignatureHeader());

        return httpHeaders;
    }
}
