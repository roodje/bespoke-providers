package com.yolt.providers.kbcgroup.common.mapper;

import com.yolt.providers.kbcgroup.dto.Balance1;
import com.yolt.providers.kbcgroup.dto.InlineResponse200Accounts;
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

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class KbcGroupAccountMapper {

    private final Clock clock;

    public ProviderAccountDTO toProviderAccountDTO(InlineResponse200Accounts kbcAccount,
                                                   Balance1 kbcBalance,
                                                   List<ProviderTransactionDTO> transactions) {

        BigDecimal balance = toBalanceAmount(kbcBalance);

        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(balance)
                .currentBalance(balance)
                .accountId(kbcAccount.getResourceId())
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, kbcAccount.getIban()))
                .name(kbcAccount.getName())
                .currency(toCurrencyCode(kbcAccount.getCurrency()))
                .transactions(transactions)
                .extendedAccount(toExtendedAccountDTO(kbcAccount, kbcBalance))
                .build();
    }

    private ExtendedAccountDTO toExtendedAccountDTO(InlineResponse200Accounts kbcAccount,
                                                    Balance1 kbcBalance) {
        return ExtendedAccountDTO.builder()
                .resourceId(kbcAccount.getResourceId())
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, kbcAccount.getIban())))
                .balances(mapToBalances(kbcBalance))
                .currency(toCurrencyCode(kbcAccount.getCurrency()))
                .name(kbcAccount.getName())
                .product(kbcAccount.getProduct())
                .build();
    }

    private List<BalanceDTO> mapToBalances(final Balance1 kbcBalance) {
        if (kbcBalance == null || kbcBalance.getBalanceAmount() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(BalanceDTO.builder()
                .balanceAmount(new BalanceAmountDTO(toCurrencyCode(kbcBalance.getBalanceAmount().getCurrency()), toBalanceAmount(kbcBalance)))
                .balanceType(BalanceType.fromName(kbcBalance.getBalanceType()))
                .build());
    }

    private CurrencyCode toCurrencyCode(final String currencyCode) {
        try {
            return CurrencyCode.valueOf(currencyCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private BigDecimal toBalanceAmount(Balance1 kbcBalance) {
        if (kbcBalance == null || kbcBalance.getBalanceAmount() == null) {
            return null;
        }
        return new BigDecimal(kbcBalance.getBalanceAmount().getAmount()).abs();
    }
}
