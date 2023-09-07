package com.yolt.providers.ing.common.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.ing.common.dto.SepaCreditTransfer;
import lombok.RequiredArgsConstructor;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

@RequiredArgsConstructor
public class DefaultInitiatePaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<DefaultInitiatePaymentPreExecutionResult, SepaCreditTransfer> {

    private final DefaultInitiatePaymentHttpRequestBodyMapper bodyMapper;

    @Override
    public SepaCreditTransfer provideHttpRequestBody(final DefaultInitiatePaymentPreExecutionResult preExecutionResult) {
        SepaInitiatePaymentRequestDTO requestDTO = preExecutionResult.getRequestDTO();
        return toSepaCreditTransfer(requestDTO);
    }

    private SepaCreditTransfer toSepaCreditTransfer(final SepaInitiatePaymentRequestDTO request) {
        SepaCreditTransfer sepaCreditTransfer = new SepaCreditTransfer();
        if (request.getDebtorAccount() != null) {
            sepaCreditTransfer.setDebtorAccount(bodyMapper.toDebtorAccount(request.getDebtorAccount()));
        }
        sepaCreditTransfer.setCreditorAccount(bodyMapper.toCreditorAccount(request.getCreditorAccount()));
        sepaCreditTransfer.setCreditorName(request.getCreditorName());
        sepaCreditTransfer.setEndToEndIdentification(request.getEndToEndIdentification());
        sepaCreditTransfer.setInstructedAmount(bodyMapper.toInstructedAmount(request.getInstructedAmount()));
        sepaCreditTransfer.setRemittanceInformationUnstructured(request.getRemittanceInformationUnstructured());

        if (request.getExecutionDate() != null) {
            sepaCreditTransfer.setRequestedExecutionDate(request.getExecutionDate().format(ISO_LOCAL_DATE));
        }

        return sepaCreditTransfer;
    }
}
