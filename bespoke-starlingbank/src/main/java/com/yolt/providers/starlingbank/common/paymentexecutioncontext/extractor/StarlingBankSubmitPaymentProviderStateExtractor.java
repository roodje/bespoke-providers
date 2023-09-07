package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.starlingbank.common.model.PaymentSubmissionResponse;
import com.yolt.providers.starlingbank.common.model.UkDomesticPaymentProviderState;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StarlingBankSubmitPaymentProviderStateExtractor implements UkPaymentProviderStateExtractor<PaymentSubmissionResponse, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult> {

    @Override
    public UkProviderState extractUkProviderState(PaymentSubmissionResponse httpResponseBody, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult preExecutionResult) throws PaymentExecutionTechnicalException {
        return new UkProviderState(
                null,
                PaymentType.SINGLE,
                UkDomesticPaymentProviderState.builder()
                        .externalPaymentId(httpResponseBody.getPaymentOrderUid().toString())
                        .accessTokenExpiresIn(preExecutionResult.getExpiresIn())
                        .refreshToken(preExecutionResult.getRefreshToken())
                        .accessToken(preExecutionResult.getToken())
                        .build());
    }
}
