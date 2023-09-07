package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.yoltprovider.pis.sepa.SignatureDTO;
import com.yolt.providers.yoltprovider.pis.sepa.SigningUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class YoltBankSepaInitiatePaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<YoltBankSepaInitiatePaymentPreExecutionResult, byte[]> {

    private static final String CLIENT_ID = "client-id";
    private static final String DIGEST = "digest";
    private static final String SIGNATURE = "signature";
    private static final String REDIRECT_URL = "redirect_url";

    @Override
    public HttpHeaders provideHttpHeaders(YoltBankSepaInitiatePaymentPreExecutionResult preExecutionResult, byte[] body) {
        final String digest = SigningUtils.prepareDigest(body);
        final String signature = SigningUtils.prepareSignature(
                preExecutionResult.getAuthenticationMeans().getClientId(),
                digest,
                preExecutionResult.getSigner(),
                preExecutionResult.getAuthenticationMeans().getSigningKid());

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add(CLIENT_ID, preExecutionResult.getAuthenticationMeans().getClientId().toString());
        httpHeaders.add(DIGEST, digest);
        httpHeaders.add(SIGNATURE, new SignatureDTO(preExecutionResult.getAuthenticationMeans().getPublicKid(), signature).getSignatureHeader());
        httpHeaders.add(REDIRECT_URL, prepareRedirectUrl(preExecutionResult));
        return httpHeaders;
    }

    private String prepareRedirectUrl(final YoltBankSepaInitiatePaymentPreExecutionResult preExecutionResult) {
        return preExecutionResult.getBaseClientRedirectUrl() + "?state=" + preExecutionResult.getState();
    }
}
