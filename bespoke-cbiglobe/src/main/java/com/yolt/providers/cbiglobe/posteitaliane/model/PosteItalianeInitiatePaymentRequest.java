package com.yolt.providers.cbiglobe.posteitaliane.model;

import com.yolt.providers.cbiglobe.common.model.InitiatePaymentRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PosteItalianeInitiatePaymentRequest extends InitiatePaymentRequest {
    private CreditorAddress creditorAddress;

    public PosteItalianeInitiatePaymentRequest(InitiatePaymentRequest paymentRequest) {
        setDebtorAccount(paymentRequest.getDebtorAccount());
        setCreditorAccount(paymentRequest.getCreditorAccount());
        setCreditorName(paymentRequest.getCreditorName());
        setEndToEndIdentification(paymentRequest.getEndToEndIdentification());
        setInstructedAmount(paymentRequest.getInstructedAmount());
        setRemittanceInformationUnstructured(paymentRequest.getRemittanceInformationUnstructured());
    }

    @Data
    @AllArgsConstructor
    public static class CreditorAddress {
        private String country;
    }
}
