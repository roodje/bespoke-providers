package com.yolt.providers.abancagroup.common.ais.data.service;

import com.yolt.providers.abancagroup.common.ais.data.dto.Account;
import com.yolt.providers.abancagroup.common.ais.data.dto.Balance;
import com.yolt.providers.abancagroup.common.ais.data.dto.Transaction;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;

@RequiredArgsConstructor
public class AbancaDataMapper {
    private static final String TITULAR_PARTICIPANT_TYPE_CODE = "1";
    private final Clock clock;
    private final ZoneId zoneId = ZoneId.of("Europe/Madrid");

    public List<ProviderTransactionDTO> mapTransactions(List<Transaction> transactions) {
        if (CollectionUtils.isEmpty(transactions)) {
            return Collections.emptyList();
        }
        return transactions.stream().map(transaction -> {
            BigDecimal amount = transaction.getAmount();
            return ProviderTransactionDTO.builder()
                    .externalId(transaction.getTransactionId())
                    .dateTime(ZonedDateTime.of(transaction.getValueDate(), zoneId))
                    .amount(amount)
                    .status(TransactionStatus.BOOKED)
                    .type(amount.compareTo(BigDecimal.ZERO) > 0 ? CREDIT : DEBIT)
                    .description(transaction.getConcept())
                    .category(YoltCategory.GENERAL)
                    .extendedTransaction(mapExtendedTransaction(transaction))
                    .build();
        }).collect(Collectors.toList());
    }

    private ExtendedTransactionDTO mapExtendedTransaction(Transaction transaction) {
        return ExtendedTransactionDTO.builder()
                .status(TransactionStatus.BOOKED)
                .entryReference(transaction.getTransactionId())
                .bookingDate(ZonedDateTime.of(transaction.getOperationDate(), zoneId))
                .valueDate(ZonedDateTime.of(transaction.getValueDate(), zoneId))
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(transaction.getAmount())
                        .currency(toCurrencyCode(transaction.getCurrency()))
                        .build())
                .transactionIdGenerated(false)
                .build();
    }

    private static CurrencyCode toCurrencyCode(final String currencyCode) {
        if (ObjectUtils.isEmpty(currencyCode)) {
            return null;
        }
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    public ProviderAccountDTO mapAccountData(Account account, List<ProviderTransactionDTO> transactions, Balance balance) {
        ProviderAccountNumberDTO accountNumber = getAccountNumber(account);
        accountNumber.setHolderName(getOwnerName(account));
        AccountType yoltAccountType = mapAccountType(account.getAccountType());
        return ProviderAccountDTO.builder()
                .yoltAccountType(yoltAccountType)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(balance.getAmount())
                .currentBalance(balance.getAmount())
                .accountId(account.getAccountId())
                .accountNumber(accountNumber)
                .name(account.getAccountId())
                .currency(toCurrencyCode(balance.getCurrency()))
                .transactions(transactions)
                .creditCardData(CREDIT_CARD.equals(yoltAccountType) ? new ProviderCreditCardDTO() : null)
                .extendedAccount(mapExtendedAccountData(account, balance))
                .build();
    }

    private AccountType mapAccountType(String accountType) {
        return switch (accountType) {
            case "C000" -> CURRENT_ACCOUNT;
            case "R000" -> CREDIT_CARD;
            default -> null;
        };
    }

    private String getOwnerName(Account account) {
        return account.getParticipants()
                .stream()
                .filter(participant -> participant.getParticipantTypeCode().equals(TITULAR_PARTICIPANT_TYPE_CODE))
                .map(participant ->
                        Stream.of(participant.getName(), participant.getSurname(), participant.getSecondSurname())
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining(" ")))
                .collect(Collectors.joining(", "));
    }

    private ProviderAccountNumberDTO getAccountNumber(Account account) {
        return switch (account.getIdentifierType()) {
            case "IBAN" -> new ProviderAccountNumberDTO(IBAN, account.getIdentifierNumber());
            case "sortCodeAccountNumber" -> new ProviderAccountNumberDTO(SORTCODEACCOUNTNUMBER, account.getIdentifierNumber());
            default -> null;
        };
    }

    private ExtendedAccountDTO mapExtendedAccountData(Account account, Balance balance) {
        CurrencyCode currency = toCurrencyCode(balance.getCurrency());
        return ExtendedAccountDTO.builder()
                .resourceId(account.getAccountId())
                .accountReferences(getAccountReference(account))
                .currency(currency)
                .name(account.getAccountId())
                .balances(List.of(BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(balance.getAmount())
                                .currency(currency)
                                .build())
                        .balanceType(BalanceType.AVAILABLE)
                        .build()))
                .build();
    }

    private List<AccountReferenceDTO> getAccountReference(Account account) {
        return switch (account.getIdentifierType()) {
            case "IBAN" -> List.of(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIdentifierNumber()));
            case "sortCodeAccountNumber" -> List.of(new AccountReferenceDTO(AccountReferenceType.SORTCODEACCOUNTNUMBER, account.getIdentifierNumber()));
            default -> null;
        };
    }
}
