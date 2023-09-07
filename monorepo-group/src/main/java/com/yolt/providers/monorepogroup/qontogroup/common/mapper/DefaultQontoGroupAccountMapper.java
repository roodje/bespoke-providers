package com.yolt.providers.monorepogroup.qontogroup.common.mapper;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Account;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class DefaultQontoGroupAccountMapper implements QontoGroupAccountMapper {

    private final String providerDisplayName;
    private final QontoGroupDateMapper dateMapper;

    @Override
    public ProviderAccountDTO.ProviderAccountDTOBuilder map(Account account) {
        ProviderAccountNumberDTO accountNumber = null;
        if (StringUtils.isNotEmpty(account.getIban())) {
            accountNumber = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, account.getIban());
        }
        return ProviderAccountDTO.builder()
                .accountId(account.getSlug())
                .name(mapToAccountName(providerDisplayName, account.getName()))
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .availableBalance(account.getAuthorizedBalance())
                .currentBalance(account.getBalance())
                .lastRefreshed(dateMapper.getZonedDateTimeWithDefaultClockZoneId())
                .accountNumber(accountNumber)
                .currency(mapToCurrencyCode(account.getCurrency()))
                .bic(account.getBic())
                .extendedAccount(mapToExtendedAccount(providerDisplayName, account));
    }

    private String mapToAccountName(String providerDisplayName, String accountName) {
        if (StringUtils.isNotEmpty(accountName)) {
            return accountName;
        }
        return providerDisplayName + " Current Account";
    }

    private CurrencyCode mapToCurrencyCode(String currency) {
        try {
            return CurrencyCode.valueOf(currency);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private ExtendedAccountDTO mapToExtendedAccount(String providerDisplayName, Account account) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getSlug())
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .name(mapToAccountName(providerDisplayName, account.getName()))
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .bic(account.getBic())
                .balances(mapToBalances(account))
                .currency(mapToCurrencyCode(account.getCurrency()))
                .build();
    }

    private List<BalanceDTO> mapToBalances(Account account) {
        return List.of(
                BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(account.getBalance())
                                .currency(mapToCurrencyCode(account.getCurrency()))
                                .build())
                        .balanceType(BalanceType.INTERIM_BOOKED)
                        .referenceDate(dateMapper.toZonedDateTime(account.getUpdatedAt()))
                        .build(),
                BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(account.getAuthorizedBalance())
                                .currency(mapToCurrencyCode(account.getCurrency()))
                                .build())
                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                        .referenceDate(dateMapper.toZonedDateTime(account.getUpdatedAt()))
                        .build()
        );
    }
}
