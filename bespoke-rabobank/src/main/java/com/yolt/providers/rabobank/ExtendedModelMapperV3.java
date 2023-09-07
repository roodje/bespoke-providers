package com.yolt.providers.rabobank;


import com.yolt.providers.rabobank.dto.external.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.account.Status;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Converts a Rabobank account response to Yolt extended account model. The balance list is provided separately, because
 * it is the result of a different call on the Rabobank API.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ExtendedModelMapperV3 {

    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneOffset.UTC);
    private static final ZoneId AMSTERDAM_TIMEZONE = ZoneId.of("Europe/Amsterdam");

    /**
     * Not all fields from the response object are mapped, because not all fields are provided by Rabobank.
     *
     * @param sourceAccount
     * @param sourceBalanceList
     * @return
     */
    static ExtendedAccountDTO mapToExtendedModelAccount(final AccountDetails sourceAccount, BalanceList sourceBalanceList) {

        return ExtendedAccountDTO.builder()
                .resourceId(sourceAccount.getResourceId())
                .balances(mapToExtendedBalances(sourceBalanceList))
                .accountReferences(mapAccountDetailsToExtendedAccountReferences(sourceAccount))
                .status(Status.fromName(sourceAccount.getStatus().name()))
                .currency(CurrencyCode.valueOf(sourceAccount.getCurrency()))
                .name(sourceAccount.getOwnerName())
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .build();
    }

    public static ExtendedTransactionDTO mapToExtendedTransaction(final TransactionDetails transactionDetails,
                                                                  final TransactionStatus status) {

        return ExtendedTransactionDTO.builder()
                .bookingDate(parseDate(transactionDetails.getBookingDate()))
                .creditorAccount(mapAccountReferenceToExtendedAccountReferences(transactionDetails.getCreditorAccount()))
                .creditorId(transactionDetails.getCreditorId())
                .creditorName(transactionDetails.getCreditorName())
                .debtorAccount(mapAccountReferenceToExtendedAccountReferences(transactionDetails.getDebtorAccount()))
                .debtorName(transactionDetails.getDebtorName())
                .entryReference(transactionDetails.getEntryReference())
                .endToEndId(transactionDetails.getEndToEndId())
                .mandateId(transactionDetails.getMandateId())
                .purposeCode(transactionDetails.getPurposeCode() != null ? transactionDetails.getPurposeCode().toString() : null)
                .remittanceInformationStructured(transactionDetails.getRemittanceInformationStructured())
                .remittanceInformationUnstructured(transactionDetails.getRemittanceInformationUnstructured())
                .status(status)
                .transactionAmount(mapToTransactionAmount(transactionDetails))
                .transactionIdGenerated(true)
                .valueDate(parseDate(transactionDetails.getValueDate()))
                .build();
    }

    private static ZonedDateTime parseDate(String date) {
        return StringUtils.isNotBlank(date) ? LocalDate.parse(date, DATE_ONLY_FORMATTER).atStartOfDay(AMSTERDAM_TIMEZONE) : null;
    }

    private static List<BalanceDTO> mapToExtendedBalances(final BalanceList sourceBalanceList) {
        List<BalanceDTO> yoltExtendedModelBalances = new ArrayList<>();
        for (Balance sourceBalance : sourceBalanceList) {
            BalanceDTO yoltBalance = mapBalanceToBalanceDTO(sourceBalance);
            yoltExtendedModelBalances.add(yoltBalance);
        }
        return yoltExtendedModelBalances;
    }

    private static BalanceDTO mapBalanceToBalanceDTO(final Balance sourceBalance) {
        // Create Yolt balance amount
        String sourceCurrency = sourceBalance.getBalanceAmount().getCurrency();
        CurrencyCode yoltCurrencyCode = CurrencyCode.valueOf(sourceCurrency);
        BigDecimal yoltAmount = new BigDecimal(sourceBalance.getBalanceAmount().getAmount());
        BalanceAmountDTO yoltBalanceAmount = new BalanceAmountDTO(yoltCurrencyCode, yoltAmount);
        // Create Yolt balance type
        nl.ing.lovebird.extendeddata.account.BalanceType yoltBalanceType =
                nl.ing.lovebird.extendeddata.account.BalanceType.fromName(sourceBalance.getBalanceType().name());
        // Convert last change datetime
        ZonedDateTime lastChangeDateTime = sourceBalance.getLastChangeDateTime().toZonedDateTime();
        return new BalanceDTO(yoltBalanceAmount, yoltBalanceType, lastChangeDateTime,
                null, null);
    }

    private static BalanceAmountDTO mapToTransactionAmount(final TransactionDetails transaction) {
        return BalanceAmountDTO.builder()
                .currency(CurrencyCode.valueOf(transaction.getTransactionAmount().getCurrency()))
                .amount(new BigDecimal(transaction.getTransactionAmount().getAmount()))
                .build();
    }

    private static List<AccountReferenceDTO> mapAccountDetailsToExtendedAccountReferences(final AccountDetails sourceAccount) {
        List<AccountReferenceDTO> accounts = new ArrayList<>();
        mapToExtendedAccountReference(sourceAccount.getIban(), AccountReferenceType.IBAN).ifPresent(accounts::add);
        return accounts;
    }

    private static AccountReferenceDTO mapAccountReferenceToExtendedAccountReferences(final AccountReference sourceAccount) {
        if (sourceAccount != null) {
            if (StringUtils.isNotBlank(sourceAccount.getIban())) {
                return new AccountReferenceDTO(AccountReferenceType.IBAN, sourceAccount.getIban());
            } else if (StringUtils.isNotBlank(sourceAccount.getBban())) {
                return new AccountReferenceDTO(AccountReferenceType.BBAN, sourceAccount.getBban());
            }
        }
        return null;
    }

    private static Optional<AccountReferenceDTO> mapToExtendedAccountReference(String accountNumber, AccountReferenceType type) {
        if (StringUtils.isNotBlank(accountNumber)) {
            return Optional.of(AccountReferenceDTO.builder()
                    .type(type)
                    .value(accountNumber.replace(" ", ""))
                    .build());
        }

        return Optional.empty();
    }
}
