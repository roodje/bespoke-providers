package com.yolt.providers.n26.common.service.mapper;

import com.yolt.providers.n26.common.dto.ais.fetchdata.Account;
import com.yolt.providers.n26.common.dto.ais.fetchdata.Balance;
import com.yolt.providers.n26.common.util.N26GroupDateUtil;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static com.yolt.providers.n26.common.util.N26GroupDateUtil.getNullableZonedDateTime;
import static java.util.Collections.singletonList;
import static nl.ing.lovebird.extendeddata.account.BalanceType.*;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

@AllArgsConstructor
public class N26GroupAccountMapper {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String CURRENT_ACCOUNT = "CACC";
    private static final String DEFAULT_ACCOUNT_NAME = "N26 - ";
    private final Clock clock;

    public ProviderAccountDTO mapProviderAccountDTO(Account account,
                                                    List<Balance> balances,
                                                    List<ProviderTransactionDTO> transactions) {
        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .name(defaultIfEmpty(account.getName(), DEFAULT_ACCOUNT_NAME + account.getProduct()))
                .availableBalance(findBalanceAmount(balances, account.getCurrency()))
                .currentBalance(findBalanceAmount(balances, account.getCurrency()))
                .yoltAccountType(toAccountType(account))
                .lastRefreshed(N26GroupDateUtil.getCurrentZoneDateTime(clock))
                .accountNumber(account.getIban() != null ? new ProviderAccountNumberDTO(Scheme.IBAN, account.getIban()) : null)
                .currency(toCurrencyCode(account.getCurrency()))
                .transactions(transactions)
                .extendedAccount(mapExtendedAccountDTO(account, balances))
                .build();
    }

    private BigDecimal findBalanceAmount(List<Balance> balances, String currency) {
        if (!balances.isEmpty()) {
            List<Balance> balancesWithSameCurrency = balances.stream()
                    .filter(balance -> currency.equals(balance.getCurrency()))
                    .collect(Collectors.toList());

            if (balancesWithSameCurrency.size() == 1) {
                return balancesWithSameCurrency.get(0).getDecimalAmount();
            }
            for (Balance balance : balancesWithSameCurrency) {
                if (EXPECTED.equals(toBalanceType(balance.getBalanceType()))) {
                    return balance.getDecimalAmount();
                }
            }
        }
        return null;
    }

    private AccountType toAccountType(Account account) {
        return CURRENT_ACCOUNT.equals(account.getCashAccountType()) ? AccountType.CURRENT_ACCOUNT : null;
    }

    private ExtendedAccountDTO mapExtendedAccountDTO(Account account, List<Balance> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .accountReferences(singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .balances(mapToBalancesDTO(balances))
                .currency(toCurrencyCode(account.getCurrency()))
                .name(defaultIfEmpty(account.getName(), DEFAULT_ACCOUNT_NAME + account.getProduct()))
                .build();
    }

    private List<BalanceDTO> mapToBalancesDTO(List<Balance> balances) {
        return balances.stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(balance.getDecimalAmount())
                                .currency(toCurrencyCode(balance.getCurrency()))
                                .build())
                        .balanceType(toBalanceType(balance.getBalanceType()))
                        .lastChangeDateTime(getNullableZonedDateTime(balance.getReferenceDate(), DATE_FORMAT))
                        .build())
                .collect(Collectors.toList());
    }

    private BalanceType toBalanceType(String balanceType) {
        switch (balanceType) {
            case "closingBooked":
                return CLOSING_BOOKED;
            case "expected":
                return EXPECTED;
            case "openingBooked":
                return OPENING_BOOKED;
            case "interimAvailable":
                return INTERIM_AVAILABLE;
            case "interimBooked":
                return INTERIM_BOOKED;
            case "forwardAvailable":
                return FORWARD_AVAILABLE;
            case "nonInvoiced":
                return NON_INVOICED;
            default:
                return null;
        }
    }

    private CurrencyCode toCurrencyCode(String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
