package com.yolt.providers.bancatransilvania.common.mapper;

import com.yolt.providers.bancatransilvania.common.domain.model.fetchdata.Account;
import com.yolt.providers.bancatransilvania.common.domain.model.fetchdata.Balance;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.util.Collections.singletonList;
import static nl.ing.lovebird.extendeddata.account.BalanceType.EXPECTED;

@AllArgsConstructor
public class BancaTransilvaniaGroupAccountMapper {

    private static final String DATE_FORMAT = "yyyy-mm-dd";
    private static final String CURRENT_ACCOUNT = "CurrentAccount";

    private final Clock clock;

    public ProviderAccountDTO mapProviderAccountDTO(Account account,
                                                    List<Balance> balances,
                                                    List<ProviderTransactionDTO> transactions) {
        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .name(account.getName())
                .availableBalance(findBalanceAmount(balances, account.getCurrency()))
                .currentBalance(findBalanceAmount(balances, account.getCurrency()))
                .yoltAccountType(toAccountType(account))
                .lastRefreshed(ZonedDateTime.now(clock))
                .accountNumber(new ProviderAccountNumberDTO(Scheme.IBAN, account.getIban()))
                .currency(toCurrencyCode(account.getCurrency()))
                .transactions(transactions)
                .extendedAccount(mapExtendedAccountDTO(account, balances))
                .build();
    }

    private BigDecimal findBalanceAmount(List<Balance> balances, String currency) {
        if (!balances.isEmpty()) {
            List<Balance> balancesWithSameCurrency = balances.stream()
                    .filter(balance -> currency.equals(balance.getCurrency()))
                    .filter(balance -> balance.getCreditLimitIncluded() == FALSE)
                    .collect(Collectors.toList());

            if (balancesWithSameCurrency.size() == 1) {
                return balancesWithSameCurrency.get(0).getAmount();
            }
            for (Balance balance : balancesWithSameCurrency) {
                if (EXPECTED.equals(toBalanceType(balance.getBalanceType()))) {
                    return balance.getAmount();
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
                .name(account.getName())
                .build();
    }

    private List<BalanceDTO> mapToBalancesDTO(List<Balance> balances) {
        return balances.stream()
                .filter(balance -> balance.getCreditLimitIncluded() == FALSE)
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(balance.getAmount())
                                .currency(toCurrencyCode(balance.getCurrency()))
                                .build())
                        .balanceType(toBalanceType(balance.getBalanceType()))
                        .lastChangeDateTime(parseToZonedDateTime(balance.getReferenceDate()))
                        .build())
                .collect(Collectors.toList());
    }

    private CurrencyCode toCurrencyCode(String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private BalanceType toBalanceType(String balanceType) {
        if ("expected".equals(balanceType)) {
            return EXPECTED;
        }
        return null;
    }

    @SneakyThrows(ParseException.class)
    private static ZonedDateTime parseToZonedDateTime(String date) {
        if (StringUtils.isNotEmpty(date)) {
            Instant instant = new SimpleDateFormat(DATE_FORMAT).parse(date).toInstant();
            return ZonedDateTime.ofInstant(instant, ZoneId.of("Europe/Bucharest"));
        }
        return null;
    }
}
