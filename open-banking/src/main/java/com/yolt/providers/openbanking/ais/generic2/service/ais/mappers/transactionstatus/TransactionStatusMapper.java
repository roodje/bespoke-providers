package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBEntryStatus1Code;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;

public interface TransactionStatusMapper {

    TransactionStatus mapToTransactionStatus(OBEntryStatus1Code status);
}
