package com.yolt.providers.openbanking.ais.monzogroup.common.service.ais.extendedtransactionmapper;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.AccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.BalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedtransaction.DefaultExtendedTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.TransactionStatusMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBTransaction6;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;

import java.time.ZoneId;


public class MonzoExtendedTransactionMapperV3 extends DefaultExtendedTransactionMapper {

    public MonzoExtendedTransactionMapperV3(AccountReferenceTypeMapper accountReferenceTypeMapper,
                                            TransactionStatusMapper transactionStatusMapper,
                                            BalanceAmountMapper balanceAmountMapper,
                                            boolean generatesTransactionId,
                                            ZoneId zoneId) {
        super(accountReferenceTypeMapper, transactionStatusMapper, balanceAmountMapper, generatesTransactionId, zoneId);
    }

    @Override
    protected ExtendedTransactionDTO.ExtendedTransactionDTOBuilder getTransactionBuilder(OBTransaction6 transaction) {
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder builder = super.getTransactionBuilder(transaction);
        return builder.entryReference(transaction.getTransactionId());
    }
}