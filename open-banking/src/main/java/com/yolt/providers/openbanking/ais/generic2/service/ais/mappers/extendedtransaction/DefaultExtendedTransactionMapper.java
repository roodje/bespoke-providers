package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedtransaction;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.AccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.BalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.TransactionStatusMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBCashAccount60;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBCashAccount61;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBTransaction6;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import org.springframework.util.ObjectUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

@AllArgsConstructor
public class DefaultExtendedTransactionMapper implements Function<OBTransaction6, ExtendedTransactionDTO> {
    private final AccountReferenceTypeMapper accountReferenceTypeMapper;
    private final TransactionStatusMapper transactionStatusMapper;
    private final BalanceAmountMapper balanceAmountMapper;
    private final boolean generatesTransactionId;
    private final ZoneId zoneId;

    @Override
    public final ExtendedTransactionDTO apply(OBTransaction6 transaction) {
        return getTransactionBuilder(transaction)
                .build();
    }

    protected ExtendedTransactionDTO.ExtendedTransactionDTOBuilder getTransactionBuilder(OBTransaction6 transaction) {
        OBCashAccount60 creditorAccount = transaction.getCreditorAccount();
        OBCashAccount61 debtorAccount = transaction.getDebtorAccount();
        return ExtendedTransactionDTO.builder()
                .bookingDate(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(transaction.getBookingDateTime())).withZoneSameInstant(zoneId))
                .valueDate(transaction.getValueDateTime() == null ? null : ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(transaction.getValueDateTime())).withZoneSameInstant(zoneId))
                .status(transactionStatusMapper.mapToTransactionStatus(transaction.getStatus()))
                .transactionAmount(balanceAmountMapper.apply(transaction))
                .proprietaryBankTransactionCode(transaction.getProprietaryBankTransactionCode() == null ? null : transaction.getProprietaryBankTransactionCode().getCode())
                .remittanceInformationUnstructured(transaction.getTransactionInformation())
                .entryReference(transaction.getTransactionReference())
                .debtorAccount(ObjectUtils.isEmpty(debtorAccount) ? null : mapToAccount(debtorAccount.getSchemeName(), debtorAccount.getIdentification()))
                .creditorAccount(ObjectUtils.isEmpty(creditorAccount) ? null : mapToAccount(creditorAccount.getSchemeName(), creditorAccount.getIdentification()))
                .transactionIdGenerated(generatesTransactionId);
    }

    protected AccountReferenceDTO mapToAccount(String schemeName, String identification) {
        return accountReferenceTypeMapper.map(schemeName, identification)
                .map(accountReferenceType -> new AccountReferenceDTO(accountReferenceType, identification))
                .orElse(null);
    }
}