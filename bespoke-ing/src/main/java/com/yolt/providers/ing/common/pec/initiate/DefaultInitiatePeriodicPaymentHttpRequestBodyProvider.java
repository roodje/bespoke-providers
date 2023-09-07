package com.yolt.providers.ing.common.pec.initiate;

import com.yolt.providers.common.pis.common.PeriodicPaymentFrequency;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.ing.common.dto.SepaCreditTransfer;
import lombok.RequiredArgsConstructor;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

@RequiredArgsConstructor
public class DefaultInitiatePeriodicPaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<DefaultInitiatePaymentPreExecutionResult, SepaCreditTransfer> {

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
        sepaCreditTransfer.setStartDate(request.getPeriodicPaymentInfo().getStartDate().format(ISO_LOCAL_DATE));
        sepaCreditTransfer.setEndDate(request.getPeriodicPaymentInfo().getEndDate().format(ISO_LOCAL_DATE));
        sepaCreditTransfer.setFrequency(toFrequencyStr(request.getPeriodicPaymentInfo().getFrequency()));

        return sepaCreditTransfer;
    }

    private String toFrequencyStr(PeriodicPaymentFrequency frequency) {
        return switch(frequency) {
            case DAILY -> "DAIL";
            case WEEKLY ->"WEEK";
            case MONTHLY ->"MNTH";
            case YEARLY ->"YEAR";
        };
    }
}
