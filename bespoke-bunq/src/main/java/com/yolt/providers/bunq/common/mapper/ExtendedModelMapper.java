package com.yolt.providers.bunq.common.mapper;

import com.yolt.providers.bunq.common.model.MonetaryAccountResponse.MonetaryAccount;
import com.yolt.providers.bunq.common.model.TransactionsResponse.Transaction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Collections.singletonList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExtendedModelMapper {

    private static final String BUNQ_BIC = "BUNQNL2A";

    public static ExtendedAccountDTO mapToExtendedModelAccount(final MonetaryAccount monetaryAccount) {
        return ExtendedAccountDTO.builder()
                .resourceId(monetaryAccount.getId())
                .status(mapToStatus(monetaryAccount.getStatus()))
                .usage(UsageType.PRIVATE)
                .bic(BUNQ_BIC)
                .accountReferences(mapToAccountReferences(monetaryAccount))
                .balances(singletonList(mapToBalance(monetaryAccount)))
                .currency(CurrencyCode.valueOf(monetaryAccount.getCurrency()))
                .name(monetaryAccount.getDescription())
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .build();
    }

    public static ExtendedTransactionDTO mapToExtendedModelTransaction(final Transaction transaction, Function<String, ZonedDateTime> dateTimeFunction) {
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder transactionBuilder = ExtendedTransactionDTO.builder()
                .bookingDate(dateTimeFunction.apply(transaction.getCreated()))
                .status(TransactionStatus.BOOKED)
                .transactionAmount(mapToTransactionAmount(transaction))
                .remittanceInformationUnstructured(transaction.getDescription());

        boolean positiveAmount = transaction.getAmount().getValue().compareTo(BigDecimal.ZERO) > 0;
        if (positiveAmount) {
            setDebtorAndCreditorInfo(transactionBuilder,
                    transaction.getCounterpartyAliasIban(), transaction.getCounterpartyAliasDisplayName(),
                    transaction.getAliasIban(), transaction.getAliasDisplayName());
        } else {
            setDebtorAndCreditorInfo(transactionBuilder,
                    transaction.getAliasIban(), transaction.getAliasDisplayName(),
                    transaction.getCounterpartyAliasIban(), transaction.getCounterpartyAliasDisplayName());
        }

        return transactionBuilder.build();
    }

    private static void setDebtorAndCreditorInfo(final ExtendedTransactionDTO.ExtendedTransactionDTOBuilder transactionBuilder,
                                                 final String debtorIban, final String debtorName,
                                                 final String creditorIban, final String creditorName) {
        getIbanAccountReference(debtorIban).ifPresent(transactionBuilder::debtorAccount);
        transactionBuilder.debtorName(debtorName);
        getIbanAccountReference(creditorIban).ifPresent(transactionBuilder::creditorAccount);
        transactionBuilder.creditorName(creditorName);
    }

    private static Optional<AccountReferenceDTO> getIbanAccountReference(final String iban) {
        if (StringUtils.isNotBlank(iban)) {
            return Optional.of(new AccountReferenceDTO(AccountReferenceType.IBAN, iban));
        }

        return Optional.empty();
    }

    private static Status mapToStatus(String bunqStatus) {
        switch (bunqStatus) {
            case "BLOCKED":
            case "PENDING_REOPEN":
                return Status.BLOCKED;
            case "CANCELLED":
                return Status.DELETED;
            case "ACTIVE":
                return Status.ENABLED;
            default:
                return Status.ENABLED;
        }
    }

    private static List<AccountReferenceDTO> mapToAccountReferences(final MonetaryAccount monetaryAccount) {
        if (StringUtils.isNotBlank(monetaryAccount.getIban())) {
            return singletonList(AccountReferenceDTO.builder()
                    .type(AccountReferenceType.IBAN)
                    .value(monetaryAccount.getIban().replace(" ", ""))
                    .build());
        }

        return Collections.emptyList();
    }

    private static BalanceDTO mapToBalance(final MonetaryAccount monetaryAccount) {
        return BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_BOOKED)
                .balanceAmount(new BalanceAmountDTO(
                        CurrencyCode.valueOf(monetaryAccount.getBalance().getCurrency()),
                        monetaryAccount.getBalance().getValue())
                ).build();
    }

    private static BalanceAmountDTO mapToTransactionAmount(final Transaction transaction) {
        return new BalanceAmountDTO(
                CurrencyCode.valueOf(transaction.getAmount().getCurrency()),
                transaction.getAmount().getValue());
    }
}
