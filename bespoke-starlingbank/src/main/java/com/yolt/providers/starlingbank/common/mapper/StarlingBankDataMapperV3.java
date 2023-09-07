package com.yolt.providers.starlingbank.common.mapper;

import com.yolt.providers.starlingbank.common.model.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StarlingBankDataMapperV3 {

    private static final String STARLING_ACCOUNT_NAME = "Starling Bank";
    private static final String STARLING_TRANSACTION_DESCRIPTION_NOT_AVAILABLE = "N/A";

    public static ProviderAccountDTO convertAccount(final AccountV2 account,
                                                    final AccountIdentifiersV2 accountIdentifiers,
                                                    final BalancesResponseV2 balancesResponse,
                                                    final TransactionsResponseV2 allTransactions,
                                                    final AccountHolderNameV2 accountHolderName,
                                                    final Clock clock) {
        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(balancesResponse.getEffectiveBalance().getMinorUnits()
                        .divide(new BigDecimal(100), 2, RoundingMode.UNNECESSARY))
                .currentBalance(balancesResponse.getClearedBalance().getMinorUnits()
                        .divide(new BigDecimal(100), 2, RoundingMode.UNNECESSARY))
                .currency(CurrencyCode.valueOf(balancesResponse.getClearedBalance().getCurrency()))
                .accountId(account.getAccountUid())
                .name(STARLING_ACCOUNT_NAME)
                .transactions(convertTransactions(allTransactions))
                .extendedAccount(ExtendedModelMapperV3.mapToExtendedModelAccount(account, accountIdentifiers, balancesResponse))
                .accountNumber(mapAccountNumber(accountIdentifiers, accountHolderName))
                .build();
    }

    private static ProviderAccountNumberDTO mapAccountNumber(AccountIdentifiersV2 accountIdentifiers, AccountHolderNameV2 accountHolderName) {
        ProviderAccountNumberDTO providerAccountNumber = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, accountIdentifiers.getIban().replace(" ", ""));
        if (accountHolderName != null) {
            providerAccountNumber.setHolderName(accountHolderName.getAccountHolderName());
        }
        providerAccountNumber.setSecondaryIdentification(accountIdentifiers.getBankIdentifier() + accountIdentifiers.getAccountIdentifier());
        return providerAccountNumber;
    }

    private static List<ProviderTransactionDTO> convertTransactions(final TransactionsResponseV2 transactionsResponse) {
        List<FeedItemV2> allTransactions = transactionsResponse.getFeedItems();
        List<ProviderTransactionDTO> providerTransactions = new ArrayList<>(allTransactions.size());
        for (FeedItemV2 transaction : allTransactions) {
            BigDecimal amount = transaction.getAmount().getMinorUnits().divide(new BigDecimal(100), 2, RoundingMode.UNNECESSARY);
            ProviderTransactionType transactionType = FeedItemDirection.OUT == transaction.getDirection() ? ProviderTransactionType.DEBIT :
                    ProviderTransactionType.CREDIT;
            if (TransactionStatusV2.SETTLED == transaction.getStatus() || TransactionStatusV2.PENDING == transaction.getStatus()) {
                providerTransactions.add(ProviderTransactionDTO.builder()
                        .externalId(transaction.getFeedItemUid())
                        .dateTime(ZonedDateTime.parse(transaction.getTransactionTime()).withZoneSameInstant(ZoneId.of("Europe/London")))
                        .amount(amount.abs())
                        .status((TransactionStatusV2.SETTLED == transaction.getStatus()) ? TransactionStatus.BOOKED : TransactionStatus.PENDING)
                        .type(transactionType)
                        .category(YoltCategory.GENERAL)
                        .description(getTransactionDescription(transaction))
                        .extendedTransaction(ExtendedModelMapperV3.mapToExtendedModelTransaction(transaction))
                        .build());
            }
        }
        return providerTransactions;
    }

    private static String getTransactionDescription(final FeedItemV2 transaction) {
        if (StringUtils.isNotBlank(transaction.getCounterPartyName())) {
            return transaction.getCounterPartyName();
        }
        if (StringUtils.isNotBlank(transaction.getReference())) {
            return transaction.getReference();
        }
        if (StringUtils.isNotBlank(transaction.getUserNote())) {
            return transaction.getUserNote();
        }
        return STARLING_TRANSACTION_DESCRIPTION_NOT_AVAILABLE;
    }
}
