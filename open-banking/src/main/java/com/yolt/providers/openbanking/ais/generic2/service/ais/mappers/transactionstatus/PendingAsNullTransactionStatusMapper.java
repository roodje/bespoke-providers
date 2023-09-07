package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBEntryStatus1Code;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;

public class PendingAsNullTransactionStatusMapper implements TransactionStatusMapper {

    @Override
    public TransactionStatus mapToTransactionStatus(OBEntryStatus1Code status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case BOOKED:
                return TransactionStatus.BOOKED;
            case PENDING:
                return TransactionStatus.PENDING;
            default:
                return null;
        }
    }
}
