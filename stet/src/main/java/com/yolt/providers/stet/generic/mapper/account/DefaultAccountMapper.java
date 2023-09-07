package com.yolt.providers.stet.generic.mapper.account;

import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountType;
import com.yolt.providers.stet.generic.dto.account.StetAccountUsage;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DefaultAccountMapper implements AccountMapper {

    protected final DateTimeSupplier dateTimeSupplier;

    /**
     * Used to define the priority for preferred balance types for available balance.
     * The priority is maintained according to the elements added (first has the highest priority).
     */
    @Override
    public List<StetBalanceType> getPreferredAvailableBalanceTypes() {
        return Collections.singletonList(StetBalanceType.XPCD);
    }

    /**
     * Used to define the priority for preferred balance types for current balance.
     * The priority is maintained according to the elements added (first has the highest priority).
     */
    @Override
    public List<StetBalanceType> getPreferredCurrentBalanceTypes() {
        return Collections.singletonList(StetBalanceType.CLBD);
    }

    /**
     * Used to define the priority for preferred balance types for card account balance.
     * The priority is maintained according to the elements added (first has the highest priority).
     */
    @Override
    public List<StetBalanceType> getPreferredCardBalanceType() {
        return Arrays.asList(StetBalanceType.OTHR, StetBalanceType.XPCD);
    }

    @Override
    public ProviderAccountDTO mapToProviderAccountDTO(StetAccountDTO account,
                                                      List<StetBalanceDTO> balances,
                                                      List<ProviderTransactionDTO> providerTransactions) {
        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .accountNumber(mapToProviderAccountNumberDTO(account, mapToHolderName(account)))
                .name(account.getName())
                .bic(account.getBicFi())
                .yoltAccountType(mapToAccountType(account.getType()))
                .currency(mapToCurrencyCode(account, balances))
                .lastRefreshed(dateTimeSupplier.getDefaultZonedDateTime())
                .linkedAccount(account.getLinkedAccount())
                .creditCardData(mapToProviderCreditCardDTO(account, getBalanceAmount(balances, getPreferredCardBalanceType())))
                .availableBalance(getBalanceAmount(balances, getPreferredAvailableBalanceTypes()))
                .currentBalance(getBalanceAmount(balances, getPreferredCurrentBalanceTypes()))
                .extendedAccount(mapToExtendedAccountDTO(account, balances))
                .transactions(providerTransactions)
                .build();
    }

    protected ProviderAccountNumberDTO mapToProviderAccountNumberDTO(StetAccountDTO account, String holderName) {
        String iban = account.getIban();
        if (StringUtils.isNotEmpty(iban)) {
            ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(Scheme.IBAN, iban);
            providerAccountNumberDTO.setHolderName(holderName);
            return providerAccountNumberDTO;
        }
        return null;
    }

    protected String mapToHolderName(StetAccountDTO account) {
        return account.getName();
    }

    protected AccountType mapToAccountType(StetAccountType purpose) {
        if (Objects.nonNull(purpose)) {
            switch (purpose) {
                case CACC:
                    return AccountType.CURRENT_ACCOUNT;
                case CARD:
                    return AccountType.CREDIT_CARD;
                default:
            }
        }
        return null;
    }

    protected ProviderCreditCardDTO mapToProviderCreditCardDTO(StetAccountDTO account, BigDecimal balanceAmount) {
        if (StetAccountType.CARD.equals(account.getType())) {
            return ProviderCreditCardDTO.builder()
                    .availableCreditAmount(balanceAmount)
                    .build();
        }
        return null;
    }

    protected BigDecimal getBalanceAmount(List<StetBalanceDTO> balances, List<StetBalanceType> preferredBalanceTypes) {
        Map<StetBalanceType, BigDecimal> balanceAmountMap = balances.stream()
                .collect(Collectors.toMap(StetBalanceDTO::getType, StetBalanceDTO::getAmount));

        for (StetBalanceType preferredBalanceType : preferredBalanceTypes) {
            if (balanceAmountMap.containsKey(preferredBalanceType)) {
                return balanceAmountMap.get(preferredBalanceType);
            }
        }
        return null;
    }

    public ExtendedAccountDTO mapToExtendedAccountDTO(StetAccountDTO account,
                                                      List<StetBalanceDTO> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .name(account.getName())
                .accountReferences(mapToAccountReferenceDTOs(account))
                .currency(mapToCurrencyCode(account, balances))
                .product(account.getProduct())
                .cashAccountType(mapToExternalCashAccountType(account.getType()))
                .bic(account.getBicFi())
                .linkedAccounts(account.getLinkedAccount())
                .usage(mapToUsageType(account.getUsage()))
                .details(account.getDetails())
                .balances(mapToBalanceDTOs(balances))
                .status(Status.ENABLED)
                .build();
    }

    protected List<AccountReferenceDTO> mapToAccountReferenceDTOs(StetAccountDTO account) {
        return Optional.ofNullable(account.getIban())
                .map(iban -> iban.replace(" ", ""))
                .map(iban -> Collections.singletonList(AccountReferenceDTO.builder()
                        .type(AccountReferenceType.IBAN)
                        .value(iban)
                        .build()))
                .orElse(Collections.emptyList());
    }

    protected CurrencyCode mapToCurrencyCode(StetAccountDTO account, List<StetBalanceDTO> balances) {
        return Optional.ofNullable(account.getCurrency())
                .orElseGet(() -> {
                    List<CurrencyCode> currenciesFromBalances = balances.stream()
                            .map(StetBalanceDTO::getCurrency)
                            .distinct()
                            .collect(Collectors.toList());

                    if (currenciesFromBalances.size() == 1) {
                        return currenciesFromBalances.get(0);
                    } else {
                        log.warn("Account's main currency is missing. Based on the balances, the account is determined as multi currency");
                        return CurrencyCode.XXX;
                    }
                });
    }

    protected UsageType mapToUsageType(StetAccountUsage usage) {
        if (usage != null) {
            switch (usage) {
                case PRIV:
                    return UsageType.PRIVATE;
                case ORGA:
                    return UsageType.CORPORATE;
                default:
            }
        }
        return null;
    }

    protected ExternalCashAccountType mapToExternalCashAccountType(StetAccountType purpose) {
        if (StetAccountType.CACC.equals(purpose) || StetAccountType.CARD.equals(purpose)) {
            return ExternalCashAccountType.CURRENT;
        }
        return null;
    }

    public List<BalanceDTO> mapToBalanceDTOs(List<StetBalanceDTO> balances) {
        return balances.stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(balance.getAmount())
                                .currency(balance.getCurrency())
                                .build())
                        .balanceType(mapToBalanceType(balance.getType()))
                        .lastChangeDateTime(dateTimeSupplier.convertToZonedDateTime(balance.getLastChangeDateTime()))
                        .lastCommittedTransaction(balance.getLastCommittedTransaction())
                        .referenceDate(dateTimeSupplier.convertToZonedDateTime(balance.getReferenceDate()))
                        .build())
                .collect(Collectors.toList());
    }

    protected BalanceType mapToBalanceType(StetBalanceType balanceType) {
        if (Objects.nonNull(balanceType)) {
            switch (balanceType) {
                case CLBD:
                    return BalanceType.CLOSING_BOOKED;
                case XPCD:
                    return BalanceType.EXPECTED;
                case OTHR:
                    return BalanceType.AUTHORISED;
                case VALU:
                    return BalanceType.FORWARD_AVAILABLE;
                case ITAV:
                    return BalanceType.INTERIM_AVAILABLE;
                default:
            }
        }
        return null;
    }
}
