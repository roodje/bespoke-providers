package com.yolt.providers.unicredit.common.data.mapper;

import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;

public interface UniCreditSepaPaymentTransactionStatusMapper {
    SepaPaymentStatus mapToSepaPaymentStatus(String transactionStatus);
}
