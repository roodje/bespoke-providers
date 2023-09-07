package com.yolt.providers.monorepogroup.libragroup.common.ais.data;

import com.yolt.providers.monorepogroup.libragroup.common.ais.data.dto.*;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.UsageType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static nl.ing.lovebird.extendeddata.account.ExternalCashAccountType.CURRENT;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;

@RequiredArgsConstructor
public class LibraDataMapperV1 implements LibraDataMapper {
    private static final DateTimeFormatter LAST_CHANGER_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Clock clock;
    private final ZoneId zoneId = ZoneId.of("Europe/Brussels");

    @Override
    public List<ProviderTransactionDTO> mapTransactions(Transactions transactions) {
        if (CollectionUtils.isEmpty(transactions.getTransactions())) {
            return Collections.emptyList();
        }
        return transactions.getTransactions().stream().map(transaction -> {
            BigDecimal amount = transaction.getAmount();
            return ProviderTransactionDTO.builder()
                    .externalId(transaction.getTransactionId())
                    .dateTime(ZonedDateTime.of(transaction.getValueDate(), LocalTime.MIN, zoneId))
                    .description(transaction.getRemittanceInformationUnstructured())
                    .amount(amount.abs())
                    .type(amount.compareTo(BigDecimal.ZERO) > 0 ? CREDIT : DEBIT)
                    .status(BOOKED)
                    .category(YoltCategory.GENERAL)
                    .extendedTransaction(mapExtendedTransaction(transaction))
                    .build();
        }).toList();
    }

    private ExtendedTransactionDTO mapExtendedTransaction(Transaction transaction) {
        String debtorIban = transaction.getDebtorIban();
        String creditorIban = transaction.getCreditorIban();
        return ExtendedTransactionDTO.builder()
                .entryReference(transaction.getTransactionId())
                .status(BOOKED)
                .valueDate(ZonedDateTime.of(transaction.getValueDate(), LocalTime.MIN, zoneId))
                .bookingDate(ZonedDateTime.of(transaction.getBookingDate(), LocalTime.MIN, zoneId))
                .transactionAmount(BalanceAmountDTO.builder()
                        .amount(transaction.getAmount())
                        .currency(toCurrencyCode(transaction.getCurrency()))
                        .build())
                .creditorName(transaction.getCreditorName())
                .debtorName(transaction.getDebtorName())
                .debtorAccount(debtorIban != null ? new AccountReferenceDTO(AccountReferenceType.IBAN, debtorIban) : null)
                .creditorAccount(creditorIban != null ? new AccountReferenceDTO(AccountReferenceType.IBAN, creditorIban) : null)
                .remittanceInformationUnstructured(transaction.getRemittanceInformationUnstructured())
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

    @Override
    public ProviderAccountDTO mapAccountData(Account account, Balances balances, List<ProviderTransactionDTO> mappedTransactions) {
        List<Balance> balanceList = balances.getBalances();
        ProviderAccountNumberDTO accountNumber = getAccountNumber(account);
        return ProviderAccountDTO.builder()
                .yoltAccountType(CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .accountId(account.getResourceId())
                .accountNumber(accountNumber)
                .currency(toCurrencyCode(account.getCurrency()))
                .name(account.getAccountName())
                .availableBalance(toBalance(account.getWorkingBalance(), balanceList))
                .currentBalance(toBalance(account.getOnlineActualBalance(), balanceList))
                .transactions(mappedTransactions)
                .extendedAccount(mapExtendedAccountData(account, balances))
                .build();
    }

    private BigDecimal toBalance(String balance, List<Balance> balances) {
        if (StringUtils.isNotEmpty(balance)) {
            return new BigDecimal(balance);
        }
        return getExpectedBalance(balances);
    }

    private ProviderAccountNumberDTO getAccountNumber(Account account) {
        if (account.getIban() == null) {
            return null;
        }
        ProviderAccountNumberDTO accountNumber = new ProviderAccountNumberDTO(IBAN, account.getIban());
        accountNumber.setHolderName(account.getCustomerName());
        return accountNumber;
    }

    private ExtendedAccountDTO mapExtendedAccountData(Account account, Balances balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .accountReferences(getAccountReference(account))
                .currency(toCurrencyCode(account.getCurrency()))
                .usage(mapUsage(account.getCategory()))
                .name(account.getAccountName())
                .cashAccountType(CURRENT)
                .balances(balances.getBalances()
                        .stream()
                        .map(balance -> BalanceDTO.builder()
                                .balanceAmount(BalanceAmountDTO.builder()
                                        .amount(balance.getAmount())
                                        .currency(toCurrencyCode(balance.getCurrency()))
                                        .build())
                                .balanceType(BalanceType.fromName(balance.getBalanceType()))
                                .lastChangeDateTime(ZonedDateTime.of(LocalDateTime.parse(balance.getLastChangeDateTime(), LAST_CHANGER_DATE_TIME_FORMATTER), zoneId))
                                .referenceDate(ZonedDateTime.of(balance.getReferenceDate(), LocalTime.MIN, zoneId))
                                .build())
                        .toList())
                .build();
    }

    /**
     * Mapping done based on Libra email "RE: [C4PO-10288] Libra API questions" from 21.07.2022 9:22
     */
    private UsageType mapUsage(String category) {
        if (ObjectUtils.isEmpty(category)) {
            return null;
        }
        return switch (category) {
            case "1122", "1123", "1127", "1139", "1001", "1005", "1022", "1023", "1111", "1116", "1121", "1052", "1053", "1145" -> UsageType.CORPORATE;
            case "1025", "1126", "1138", "1000", "1002", "1004", "1020", "1021", "1024", "1110", "1112", "1115", "1043", "1044", "1045", "1047" -> UsageType.PRIVATE;
            default -> null;
        };
    }

    private List<AccountReferenceDTO> getAccountReference(Account account) {
        if (account.getIban() == null) {
            return Collections.emptyList();
        }
        AccountReferenceDTO accountNumber = new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban());
        return List.of(accountNumber);
    }

    private BigDecimal getExpectedBalance(List<Balance> accountBalances) {
        BigDecimal balanceValue = null;
        LocalDate balanceReferenceDate = null;
        for (Balance balance : accountBalances) {
            if (BalanceType.EXPECTED.getName().equals(balance.getBalanceType()) && (balanceValue == null || balance.getReferenceDate().isAfter(balanceReferenceDate))) {
                balanceValue = balance.getAmount();
                balanceReferenceDate = balance.getReferenceDate();
            }
        }
        return balanceValue;
    }
}
