package com.yolt.providers.sparkassenandlandesbanks.common.mapper;

import com.yolt.providers.sparkassenandlandesbanks.common.dto.AccountsResponse;
import lombok.RequiredArgsConstructor;
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
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SparkassenAndLandesbanksAccountMapper {

    private static final String AVAILABLE_BALANCE_TYPE = "interimAvailable";
    private static final String CURRENT_BALANCE_TYPE = "closingBooked";
    private static final String DEFAULT_SPARKASSEN_ACCOUNT_NAME = "Sparkasse Account";

    private final Clock clock;

    public ProviderAccountDTO toProviderAccountDTO(AccountsResponse.Account sparkassenAccount,
                                                   List<AccountsResponse.Account.Balance> sparkassenBalances,
                                                   List<ProviderTransactionDTO> transactions) {

        BigDecimal availableBalance = toBalanceAmount(retrieveBalanceByType(sparkassenBalances, AVAILABLE_BALANCE_TYPE));
        BigDecimal currentBalance = toBalanceAmount(retrieveBalanceByType(sparkassenBalances, CURRENT_BALANCE_TYPE));
        String accountName = getAccountName(sparkassenAccount);
        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(Instant.now(clock).atZone(clock.getZone()))
                .availableBalance(availableBalance)
                .currentBalance(currentBalance)
                .accountId(sparkassenAccount.getResourceId())
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, sparkassenAccount.getIban()))
                .name(accountName)
                .currency(toCurrencyCode(sparkassenAccount.getCurrency()))
                .transactions(transactions)
                .extendedAccount(toExtendedAccountDTO(sparkassenAccount, sparkassenBalances))
                .build();
    }

    private ExtendedAccountDTO toExtendedAccountDTO(AccountsResponse.Account sparkassenAccount,
                                                    List<AccountsResponse.Account.Balance> sparkassenBalances) {
        return ExtendedAccountDTO.builder()
                .resourceId(sparkassenAccount.getResourceId())
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, sparkassenAccount.getIban())))
                .balances(mapToBalances(sparkassenBalances))
                .currency(toCurrencyCode(sparkassenAccount.getCurrency()))
                .name(sparkassenAccount.getName())
                .product(sparkassenAccount.getProduct())
                .build();
    }

    private List<BalanceDTO> mapToBalances(final List<AccountsResponse.Account.Balance> sparkassenBalances) {
        return sparkassenBalances.stream()
                .map(sparkassenBalance ->
                        BalanceDTO.builder()
                                .balanceAmount(new BalanceAmountDTO(toCurrencyCode(sparkassenBalance.getBalanceCurrency()), toBalanceAmount(sparkassenBalance)))
                                .balanceType(BalanceType.fromName(sparkassenBalance.getBalanceType()))
                                .build()
                ).collect(Collectors.toList());
    }

    private CurrencyCode toCurrencyCode(final String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private AccountsResponse.Account.Balance retrieveBalanceByType(List<AccountsResponse.Account.Balance> balances, String balanceType) {
        for (AccountsResponse.Account.Balance balance : balances) {
            if (balanceType.equals(balance.getBalanceType()) && balance.getBalanceAmount() != null) {
                return balance;
            }
        }
        return null;
    }

    private BigDecimal toBalanceAmount(AccountsResponse.Account.Balance sparkassenBalance) {
        if (sparkassenBalance == null || sparkassenBalance.getBalanceAmount() == null) {
            return null;
        }
        return sparkassenBalance.getBalanceAmount();
    }

    private String getAccountName(AccountsResponse.Account sparkassenAccount) {
        String accountName = sparkassenAccount.getName();
        if (!StringUtils.isEmpty(accountName)) {
            return accountName;
        } else if (!StringUtils.isEmpty(sparkassenAccount.getOwnerName())) {
            return sparkassenAccount.getOwnerName();
        } else {
            return DEFAULT_SPARKASSEN_ACCOUNT_NAME;
        }
    }
}
