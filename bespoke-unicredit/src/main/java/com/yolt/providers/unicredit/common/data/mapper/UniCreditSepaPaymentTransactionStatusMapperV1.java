package com.yolt.providers.unicredit.common.data.mapper;

import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;

public class UniCreditSepaPaymentTransactionStatusMapperV1 implements UniCreditSepaPaymentTransactionStatusMapper {

    @Override
    public SepaPaymentStatus mapToSepaPaymentStatus(final String transactionStatus) {
        switch (transactionStatus) {
            case "ACCC":
            case "ACSC":
                return SepaPaymentStatus.COMPLETED;
            case "RCVD":
            case "PDNG":
                return SepaPaymentStatus.INITIATED;
            case "RJCT":
            case "CANC":
                return SepaPaymentStatus.REJECTED;
            default:
                return SepaPaymentStatus.ACCEPTED;
        }
    }
}
