package com.yolt.providers.monorepogroup.cecgroup.common.mapper;

import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data.Account;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data.Balance;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static nl.ing.lovebird.extendeddata.account.BalanceType.CLOSING_BOOKED;
import static nl.ing.lovebird.extendeddata.account.BalanceType.INTERIM_AVAILABLE;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;

@RequiredArgsConstructor
public class CecGroupAccountMapperV1 implements CecGroupAccountMapper {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Bucharest");

    private static final String BALANCE_CLOSING_BOOKED = "closingBooked";
    private static final String BALANCE_EXPECTED = "expected";
    private static final String BALANCE_AUTHORIZED = "authorised";
    private static final String BALANCE_OPENING_BOOKED = "openingBooked";
    private static final String BALANCE_INTERIM_AVAILABLE = "interimAvailable";
    private static final String BALANCE_INTERIM_BOOKED = "interimBooked";
    private static final String BALANCE_FORWARD_AVAILABLE = "forwardAvailable";
    private static final String BALANCE_NON_INVOICED = "nonInvoiced";

    private final Clock clock;

    @Override
    public ProviderAccountDTO mapToProviderAccount(Account account,
                                                   List<ProviderTransactionDTO> transactions,
                                                   String providerDisplayName) {

        BigDecimal preferredBalance = getPreferredBalance(account.getBalances(), List.of(CLOSING_BOOKED));
        ProviderAccountNumberDTO accountNumber = null;
        if (StringUtils.hasText(account.getIban())) {
            accountNumber = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban());
        }
        return ProviderAccountDTO.builder()
                .yoltAccountType(CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(preferredBalance)
                .currentBalance(preferredBalance)
                .accountId(account.getResourceId())
                .accountNumber(accountNumber)
                .bic(account.getBic())
                .name(mapToAccountName(account.getName(), providerDisplayName))
                .currency(mapToCurrencyCode(account.getCurrency()))
                .transactions(transactions)
                .extendedAccount(mapExtendedAccountDTO(providerDisplayName, account))
                .build();
    }

    private BigDecimal getPreferredBalance(List<Balance> accountBalances, List<BalanceType> preferredBalances) {
        for (BalanceType bt : preferredBalances) {
            for (Balance balance : accountBalances) {
                if (bt.getName().equals(balance.getBalanceType())) {
                    return balance.getAmount();
                }
            }
        }
        return null;
    }

    private CurrencyCode mapToCurrencyCode(String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private ExtendedAccountDTO mapExtendedAccountDTO(String providerDisplayName, Account account) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .bic(account.getBic())
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .name(mapToAccountName(account.getName(), providerDisplayName))
                .accountReferences(singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .balances(mapToBalancesDTO(account.getBalances()))
                .currency(mapToCurrencyCode(account.getCurrency()))
                .build();
    }


    private String mapToAccountName(String accountName, String providerDisplayName) {
        if (StringUtils.hasText(accountName)) {
            return accountName;
        }
        return providerDisplayName + " Current Account";
    }

    private List<BalanceDTO> mapToBalancesDTO(List<Balance> balances) {
        return balances.stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(balance.getAmount())
                                .currency(mapToCurrencyCode(balance.getCurrency()))
                                .build())
                        .balanceType(mapToBalanceType(balance.getBalanceType()))
                        .referenceDate(balance.getReferenceDate().atStartOfDay(ZONE_ID))
                        .build())
                .collect(Collectors.toList());
    }

    private BalanceType mapToBalanceType(String balanceType) {
        return switch (balanceType) {
            case BALANCE_CLOSING_BOOKED -> BalanceType.CLOSING_BOOKED;
            case BALANCE_EXPECTED -> BalanceType.EXPECTED;
            case BALANCE_AUTHORIZED -> BalanceType.AUTHORISED;
            case BALANCE_OPENING_BOOKED -> BalanceType.OPENING_BOOKED;
            case BALANCE_INTERIM_AVAILABLE -> INTERIM_AVAILABLE;
            case BALANCE_INTERIM_BOOKED -> BalanceType.INTERIM_BOOKED;
            case BALANCE_FORWARD_AVAILABLE -> BalanceType.FORWARD_AVAILABLE;
            case BALANCE_NON_INVOICED -> BalanceType.NON_INVOICED;
            default -> null;
        };
    }
}
