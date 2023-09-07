package com.yolt.providers.starlingbank.common.mapper;

import com.yolt.providers.starlingbank.common.model.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExtendedModelMapperV3 {

    private static final String STARLING_ACCOUNT_NAME = "Starling Bank";

    static ExtendedAccountDTO mapToExtendedModelAccount(final AccountV2 account,
                                                        final AccountIdentifiersV2 accountIdentifiers,
                                                        final BalancesResponseV2 balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getAccountUid())
                .status(Status.ENABLED)
                .usage(UsageType.PRIVATE)
                .bic(accountIdentifiers.getBic())
                .accountReferences(mapToAccountReferences(accountIdentifiers))
                .balances(extractBalances(balances))
                .currency(CurrencyCode.valueOf(account.getCurrency()))
                .name(STARLING_ACCOUNT_NAME)
                .build();
    }

    static ExtendedTransactionDTO mapToExtendedModelTransaction(final FeedItemV2 transaction) {
        String counterPartyName = getCounterPartyName(transaction);
        AccountReferenceDTO counterPartyAccountReference = getCounterPartyAccountReference(transaction);
        ExtendedTransactionDTO.ExtendedTransactionDTOBuilder extendedTransactionBuilder = ExtendedTransactionDTO.builder()
                .bookingDate(ZonedDateTime.parse(transaction.getTransactionTime())
                                     .withZoneSameInstant(ZoneId.of("Europe/London")))
                .transactionAmount(mapToTransactionAmount(transaction))
                .remittanceInformationUnstructured(transaction.getUserNote())
                .proprietaryBankTransactionCode(getBankTransactionCode(transaction));
        if (FeedItemDirection.OUT == transaction.getDirection()) {
            extendedTransactionBuilder.creditorName(counterPartyName);
            extendedTransactionBuilder.creditorAccount(counterPartyAccountReference);
        } else {
            extendedTransactionBuilder.debtorName(counterPartyName);
            extendedTransactionBuilder.debtorAccount(counterPartyAccountReference);
        }
        return extendedTransactionBuilder.build();
    }

    private static String getBankTransactionCode(FeedItemV2 transaction) {
        return String.format("%s %s", transaction.getSource(), transaction.getSourceSubType());
    }

    private static BalanceAmountDTO mapToTransactionAmount(final FeedItemV2 transaction) {
        BigDecimal amount = transaction.getAmount().getMinorUnits().divide(new BigDecimal(100), 2, RoundingMode.UNNECESSARY).abs();
        return BalanceAmountDTO.builder()
                .currency(CurrencyCode.valueOf(transaction.getAmount().getCurrency()))
                .amount(transaction.getDirection().getValue().equals(FeedItemDirection.IN.getValue()) ? amount : amount.negate())
                .build();
    }

    private static List<AccountReferenceDTO> mapToAccountReferences(final AccountIdentifiersV2 accountIdentifiers) {
        List<AccountReferenceDTO> accountRefs = new ArrayList<>();

        if (StringUtils.isNotBlank(accountIdentifiers.getIban())) {
            accountRefs.add(AccountReferenceDTO.builder()
                                    .type(AccountReferenceType.IBAN)
                                    .value(accountIdentifiers.getIban().replace(" ", ""))
                                    .build());
        }
        if (StringUtils.isNotBlank(accountIdentifiers.getAccountIdentifier()) && StringUtils.isNotBlank(accountIdentifiers.getBankIdentifier())) {
            accountRefs.add(AccountReferenceDTO.builder()
                                    .type(AccountReferenceType.SORTCODEACCOUNTNUMBER)
                                    .value(accountIdentifiers.getBankIdentifier() + accountIdentifiers.getAccountIdentifier())
                                    .build());
        }
        return accountRefs;
    }

    private static List<BalanceDTO> extractBalances(final BalancesResponseV2 balancesResponse) {
        List<BalanceDTO> result = new ArrayList<>();
        result.add(BalanceDTO.builder()
                           .balanceAmount(BalanceAmountDTO.builder()
                                                  .amount(balancesResponse.getEffectiveBalance().getMinorUnits().divide(new BigDecimal(100), 2, RoundingMode.UNNECESSARY))
                                                  .currency(CurrencyCode.valueOf(balancesResponse.getEffectiveBalance().getCurrency()))
                                                  .build())
                           .balanceType(BalanceType.EXPECTED)
                           .build());
        result.add(BalanceDTO.builder()
                           .balanceAmount(BalanceAmountDTO.builder()
                                                  .amount(balancesResponse.getClearedBalance().getMinorUnits().divide(new BigDecimal(100), 2, RoundingMode.UNNECESSARY))
                                                  .currency(CurrencyCode.valueOf(balancesResponse.getClearedBalance().getCurrency()))
                                                  .build())
                           .balanceType(BalanceType.INTERIM_AVAILABLE)
                           .build());
        return result;
    }

    private static String getCounterPartyName(final FeedItemV2 transaction) {
        return transaction.getCounterPartyName();
    }

    private static AccountReferenceDTO getCounterPartyAccountReference(final FeedItemV2 transaction) {
        // Agreed with Andrew Curran that we do not pass type (however so far we have observed only AccountReferenceType.SORTCODEACCOUNTNUMBER
        return AccountReferenceDTO.builder()
                .value(transaction.getCounterPartySubEntityIdentifier() + transaction.getCounterPartySubEntitySubIdentifier())
                .build();
    }
}