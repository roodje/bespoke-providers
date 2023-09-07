package com.yolt.providers.argentagroup.common.service.fetchdata;

import com.yolt.providers.argentagroup.common.exception.UnsupportedAccountTypeException;
import com.yolt.providers.argentagroup.dto.GetAccountsResponseAccounts;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class AccountsMapper {

    private final Clock clock;

    public ProviderAccountDTO map(final GetAccountsResponseAccounts accountResponse) {
        return ProviderAccountDTO.builder()
                .accountId(accountResponse.getResourceId())
                .yoltAccountType(mapAccountType(accountResponse.getCashAccountType()))
                .lastRefreshed(ZonedDateTime.now(clock))
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, accountResponse.getIban()))
                .bic(accountResponse.getBic())
                .name(accountResponse.getName())
                .currency(CurrencyCode.valueOf(accountResponse.getCurrency()))
                .closed(Boolean.FALSE)
                .transactions(Collections.emptyList())
                .extendedAccount(mapExtendedAccount(accountResponse))
                .build();
    }

    private ExtendedAccountDTO mapExtendedAccount(final GetAccountsResponseAccounts accountResponse) {
        return ExtendedAccountDTO.builder()
                .resourceId(accountResponse.getResourceId())
                .accountReferences(List.of(
                        new AccountReferenceDTO(AccountReferenceType.IBAN, accountResponse.getIban()),
                        new AccountReferenceDTO(AccountReferenceType.BBAN, accountResponse.getBban())
                ))
                .currency(CurrencyCode.valueOf(accountResponse.getCurrency()))
                .name(accountResponse.getName())
                .product(accountResponse.getProduct())
                .cashAccountType(ExternalCashAccountType.fromCode(accountResponse.getCashAccountType()))
                .bic(accountResponse.getBic())
                .build();
    }

    private AccountType mapAccountType(final String accountType) {
        return switch (accountType) {
            case "CACC" -> AccountType.CURRENT_ACCOUNT;
            case "SVGS" -> AccountType.SAVINGS_ACCOUNT;
            default -> throw new UnsupportedAccountTypeException("Account type:" + accountType + " is not supported");
        };
    }


}
