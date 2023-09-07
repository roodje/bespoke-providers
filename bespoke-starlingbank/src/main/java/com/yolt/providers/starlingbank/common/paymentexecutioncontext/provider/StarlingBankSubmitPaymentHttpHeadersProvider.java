package com.yolt.providers.starlingbank.common.paymentexecutioncontext.provider;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpHeadersProducer;
import com.yolt.providers.starlingbank.common.http.signer.StarlingBankHttpCavageSigner;
import com.yolt.providers.starlingbank.common.model.PaymentRequest;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.time.Clock;
import java.util.UUID;

import static com.yolt.securityutils.signing.SignatureAlgorithm.SHA256_WITH_RSA;

@RequiredArgsConstructor
public class StarlingBankSubmitPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult, PaymentRequest> {

    private final Clock clock;

    @Override
    public HttpHeaders provideHttpHeaders(StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult starlingBankSubmitPaymentExecutionContextPreExecutionResult, PaymentRequest paymentRequest) throws PaymentExecutionTechnicalException {
        StarlingBankAuthenticationMeans authMeans = starlingBankSubmitPaymentExecutionContextPreExecutionResult.getAuthenticationMeans();
        Signer signer = starlingBankSubmitPaymentExecutionContextPreExecutionResult.getSigner();
        StarlingBankHttpHeadersProducer headersProducer = createHeadersProducer(authMeans, signer);
        String token = starlingBankSubmitPaymentExecutionContextPreExecutionResult.getToken();

        return headersProducer.createSigningHeaders(
                token,
                paymentRequest,
                authMeans.getSigningKeyHeaderId(),
                starlingBankSubmitPaymentExecutionContextPreExecutionResult.getUrl());
    }

    public StarlingBankHttpHeadersProducer createHeadersProducer(StarlingBankAuthenticationMeans authMeans, Signer signer) {
        UUID signingPrivateKeyId = authMeans.getSigningPrivateKeyId() == null ? null : UUID.fromString(authMeans.getSigningPrivateKeyId());
        StarlingBankHttpCavageSigner cavageSigner = new StarlingBankHttpCavageSigner(signer, signingPrivateKeyId, SHA256_WITH_RSA);
        return new StarlingBankHttpHeadersProducer(cavageSigner, clock);
    }
}
