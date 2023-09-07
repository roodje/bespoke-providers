package com.yolt.providers.monorepogroup.chebancagroup.common.mapper;

import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.Balances;
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
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;

@RequiredArgsConstructor
public class DefaultChaBancaGroupAccountMapper implements CheBancaGroupAccountMapper {

    private final String providerDisplayName;
    private final CheBancaGroupDateMapper dateMapper;

    @Override
    public ProviderAccountDTO.ProviderAccountDTOBuilder map(final Account account, final Balances balance) throws ProviderFetchDataException {
        ProviderAccountNumberDTO accountNumber = null;
        if (StringUtils.hasText(account.getIban())) {
            accountNumber = new ProviderAccountNumberDTO(IBAN, account.getIban());
        }

        return ProviderAccountDTO.builder()
                .accountId(account.getAccountId())
                .name(mapToAccountName(providerDisplayName, account))
                .yoltAccountType(mapToAccountType(account.getProductCode()))
                .availableBalance(balance.getAvailableBalanceAmount())
                .currentBalance(balance.getAccountAmount())
                .lastRefreshed(dateMapper.getZonedDateTime())
                .accountNumber(accountNumber)
                .currency(mapToCurrencyCode(account.getCurrency()))
                .extendedAccount(mapExtendedAccountDTO(providerDisplayName, account, balance)
                );
    }

    public AccountType mapToAccountType(final String type) throws ProviderFetchDataException {
        switch (type) {
            case "AZIM", // Current Account
                    "0600", // Current Account
                    "0601", // Ordinary Basic Account
                    "0603", // Basic Account for Disadvantaged Groups
                    "0604", // Retired Basic Account
                    "0608", // Current Account
                    "0631", // Current Account
                    "0632", // Current Account
                    "0641", // Current Account
                    "0642", // Current Account
                    "0643", // Current Account
                    "0644", // Current Account
                    "0646", // Current Account
                    "0647", // Current Account
                    "0648", // Current Account
                    "0649", // Current Account
                    "0650", // Current Account
                    "0651", // Current Account
                    "0652", // Current Account
                    "0653", // Current Account
                    "0654", // Current Account
                    "0656", // Current Account
                    "0657", // Current Account
                    "0658", // Current Account
                    "0694", // Current Account
                    "0695" // Current account
                    -> {
                return AccountType.CURRENT_ACCOUNT;
            }

            case "CAPC", // Credit Card
                    "CA30", // Credit Card
                    "SI12", // Credit Card
                    "SI13", // Credit Card
                    "SI14", // Credit Card
                    "SI15", // Credit Card
                    "SI16", // Card Credit Card
                    "SI17", // Credit Card
                    "SI18", // Credit Card
                    "SI19", // Credit Card
                    "SI20" // Credit Card
                    -> {
                return CREDIT_CARD;
            }

            case "0633", // Yellow Account
                    "0645", // Business Current Account
                    "IMPB", // Account Business Deposit
                    "MPAC", // Pocket Account
                    "MPAZ", // Pocket Account
                    "MPBC", // Pocket Account
                    "MPBL", // Pocket Account
                    "MPFC", // Pocket Account
                    "MPFU", // Pocket Account
                    "MPGC", // Pocket Account
                    "MPGI", // Pocket Account
                    "MPGR", // Pocket Account
                    "MPRC", // Pocket Account
                    "MPRO", // Pocket Account
                    "MPVC", // Pocket Account
                    "MPVE", // Pocket Account
                    "MPYC", // Pocket Account
                    "MP0C", // Pocket Account
                    "MP00", // Pocket Account
                    "0692", // Peg account no
                    "0697", // Pledge Account
                    "0698", // Pledge Account
                    "CDEP", // Deposito Account
                    "0665", // Business Scudo Account
                    "0666", // Scudo Account
                    "0681", // Premier Account
                    "0655" // Business Current Account
                    -> throw new ProviderFetchDataException("Not supported accountType: " + type);
            default -> throw new ProviderFetchDataException("Not known accountType: " + type);
        }
    }


    private String mapToAccountName(final String providerDisplayName, final Account account) {
        if (StringUtils.hasText(account.getName())) {
            return account.getName();
        }
        return providerDisplayName + " Current Account";
    }

    private CurrencyCode mapToCurrencyCode(final String currency) {
        try {
            return CurrencyCode.valueOf(currency);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private ExtendedAccountDTO mapExtendedAccountDTO(final String providerDisplayName, final Account account, final Balances balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getAccountId())
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .name(mapToAccountName(providerDisplayName, account))
                .accountReferences(singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .balances(mapToBalancesDTO(balances))
                .product(account.getProductCode())
                .currency(mapToCurrencyCode(account.getCurrency()))
                .build();
    }

    private List<BalanceDTO> mapToBalancesDTO(final Balances balances) {

        return Arrays.asList(
                BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(balances.getAccountAmount())
                                .currency(mapToCurrencyCode(balances.getAccountCurrency()))
                                .build())
                        .balanceType(BalanceType.CLOSING_BOOKED)
                        .referenceDate(dateMapper.getZonedDateTime(balances.getReferenceDate()))
                        .build(),
                BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(balances.getAvailableBalanceAmount())
                                .currency(mapToCurrencyCode(balances.getAvailableBalanceCurrency()))
                                .build())
                        .balanceType(BalanceType.FORWARD_AVAILABLE)
                        .referenceDate(dateMapper.getZonedDateTime(balances.getReferenceDate()))
                        .build());
    }
}
