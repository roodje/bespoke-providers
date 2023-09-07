package com.yolt.providers.abnamrogroup.common.data;

import com.yolt.providers.abnamro.dto.BalanceResponse;
import com.yolt.providers.abnamro.dto.DetailsResponse;
import com.yolt.providers.abnamro.dto.TransactionResponseTransactions;
import com.yolt.providers.common.cryptography.HashUtils;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AbnAmroDataMapper {

    private static final String DEFAULT_ACCOUNT_NAME = "Current Account";

    private final TransactionDateExtractor transactionDateExtractor;
    private final Clock clock;

    public ProviderAccountDTO convertAccount(@NotNull final DetailsResponse detailsResponse,
                                             final BalanceResponse balanceResponse,
                                             @NotNull final List<TransactionResponseTransactions> transactions) {

        BalanceDTO balanceDTO = balanceResponse != null ? convertBalance(balanceResponse) : null;

        List<ProviderTransactionDTO> transactionDTOs = transactions.stream()
                .map(this::convertTransaction)
                .collect(Collectors.toList());

        ProviderAccountNumberDTO accountNumberDTO = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, detailsResponse.getAccountNumber());
        accountNumberDTO.setHolderName(detailsResponse.getAccountHolderName());

        return ProviderAccountDTO.builder()
                .accountId(HashUtils.sha256Hash(detailsResponse.getAccountNumber()))
                .accountNumber(accountNumberDTO)
                .currency(CurrencyCode.valueOf(detailsResponse.getCurrency()))
                .currentBalance(balanceDTO != null ? balanceDTO.getBalanceAmount().getAmount() : null)
                .extendedAccount(convertExtendedAccount(detailsResponse, balanceDTO))
                .lastRefreshed(Instant.now(clock).atZone(ZoneOffset.UTC))
                .name(DEFAULT_ACCOUNT_NAME) // account name is not provided by the bank, so setting it to default one
                .transactions(transactionDTOs)
                // ABN support confirmed that within AIS service they return only current accounts
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .build();
    }

    private ExtendedAccountDTO convertExtendedAccount(final DetailsResponse detailsResponse, final BalanceDTO balanceDTO) {
        return ExtendedAccountDTO.builder()
                .accountReferences(Collections.singletonList(convertAccountReference(detailsResponse)))
                .balances(balanceDTO != null ? Collections.singletonList(balanceDTO) : null)
                // ABN support confirmed that within AIS service they return only current accounts
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .currency(CurrencyCode.valueOf(detailsResponse.getCurrency()))
                .name(DEFAULT_ACCOUNT_NAME) // account name is not provided by the bank, so setting it to default one
                .build();
    }

    private BalanceDTO convertBalance(@NotNull final BalanceResponse balanceResponse) {
        return BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_BOOKED)
                .balanceAmount(BalanceAmountDTO.builder()
                        .amount(convertAmount(balanceResponse.getAmount()))
                        .currency(CurrencyCode.valueOf(balanceResponse.getCurrency()))
                        .build())
                .build();
    }

    private AccountReferenceDTO convertAccountReference(final DetailsResponse detailsResponse) {
        return AccountReferenceDTO.builder()
                .type(AccountReferenceType.IBAN)
                .value(detailsResponse.getAccountNumber().replace(" ", ""))
                .build();
    }

    private ProviderTransactionDTO convertTransaction(@NotNull final TransactionResponseTransactions transaction) {
        List<String> descriptionLines = transaction.getDescriptionLines() == null ? new ArrayList<>() : transaction.getDescriptionLines();
        return ProviderTransactionDTO.builder()
                .externalId(transaction.getTransactionId())
                .amount(convertAbsoluteAmount(transaction.getAmount()))
                .category(YoltCategory.GENERAL)
                .dateTime(transactionDateExtractor.extractTransactionDate(transaction))
                .description(String.join(", ", descriptionLines))
                .extendedTransaction(convertExtendedTransaction(transaction))
                .merchant(AbnAmroCounterPartyNameExtractor.extractCounterPartyName(transaction))
                .status(convertTransactionStatus(transaction.getStatus()))
                .type(transaction.getAmount() < 0 ? ProviderTransactionType.DEBIT : ProviderTransactionType.CREDIT)
                .build();
    }

    private TransactionStatus convertTransactionStatus(TransactionResponseTransactions.StatusEnum obtained) {
        if (obtained == null || obtained == TransactionResponseTransactions.StatusEnum.EXECUTED) {
            return TransactionStatus.BOOKED;
        }
        return null;
    }

    private BigDecimal convertAmount(final Double amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal convertAbsoluteAmount(final Double amount) {
        return BigDecimal.valueOf(Math.abs(amount)).setScale(2, RoundingMode.HALF_UP);
    }

    private ExtendedTransactionDTO convertExtendedTransaction(@NotNull final TransactionResponseTransactions transaction) {
        List<String> descriptionLines = transaction.getDescriptionLines() == null ? new ArrayList<>() : transaction.getDescriptionLines();
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder builder = ExtendedTransactionDTO.builder()
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(convertAmount(transaction.getAmount()))
                        .currency(CurrencyCode.valueOf(transaction.getCurrency()))
                        .build())
                .status(convertTransactionStatus(transaction.getStatus()))
                .remittanceInformationUnstructured(String.join(", ", descriptionLines))
                .bookingDate(transactionDateExtractor.extractTransactionDate(transaction));

        AccountReferenceDTO counterPartyAccount = AccountReferenceDTO.builder()
                .type(AccountReferenceType.IBAN)
                .value(transaction.getCounterPartyAccountNumber().replace(" ", ""))
                .build();

        if (transaction.getAmount() < 0) {
            builder.creditorName(AbnAmroCounterPartyNameExtractor.extractCounterPartyName(transaction))
                    .creditorAccount(counterPartyAccount);
        } else if (transaction.getAmount() > 0) {
            builder.debtorName(AbnAmroCounterPartyNameExtractor.extractCounterPartyName(transaction))
                    .debtorAccount(counterPartyAccount);
        }

        return builder.build();
    }

}
