package com.yolt.providers.stet.societegeneralegroup.common.mapper.account;

import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountType;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.mapper.account.DefaultAccountMapper;
import com.yolt.providers.stet.societegeneralegroup.common.mapper.SocieteGeneraleDateTimeSupplier;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderCreditCardDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SocieteGeneraleAccountMapper extends DefaultAccountMapper {

    private final SocieteGeneraleDateTimeSupplier dateTimeSupplier;

    public SocieteGeneraleAccountMapper(SocieteGeneraleDateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
        this.dateTimeSupplier = dateTimeSupplier;
    }

    @Override
    public List<StetBalanceType> getPreferredCurrentBalanceTypes() {
        return Arrays.asList(StetBalanceType.CLBD, StetBalanceType.XPCD, StetBalanceType.OTHR);
    }

    @Override
    public List<StetBalanceType> getPreferredAvailableBalanceTypes() {
        return Arrays.asList(StetBalanceType.XPCD, StetBalanceType.CLBD, StetBalanceType.OTHR);
    }

    @Override
    public List<StetBalanceType> getPreferredCardBalanceType() {
        return Arrays.asList(StetBalanceType.XPCD, StetBalanceType.CLBD, StetBalanceType.OTHR);
    }

    @Override
    public ExtendedAccountDTO mapToExtendedAccountDTO(StetAccountDTO account, List<StetBalanceDTO> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .accountReferences(mapToAccountReferenceDTOs(account))
                .currency(mapToCurrencyCode(account, balances))
                .name(account.getName())
                .cashAccountType(mapToExternalCashAccountType(account.getType()))
                .bic(account.getBicFi())
                .usage(mapToUsageType(account.getUsage()))
                .balances(mapToBalanceDTOs(balances))
                .build();
    }

    @Override
    public ProviderAccountDTO mapToProviderAccountDTO(StetAccountDTO account,
                                                      List<StetBalanceDTO> balances,
                                                      List<ProviderTransactionDTO> providerTransactions) {
        BigDecimal availableBalanceAmount = roundBalance(getBalanceAmount(balances, getPreferredAvailableBalanceTypes()));
        BigDecimal currentBalanceAmount = roundBalance(getBalanceAmount(balances, getPreferredCurrentBalanceTypes()));

        if (StetAccountType.CARD.equals(account.getType())) {
            availableBalanceAmount = negateAmountForCreditCard(availableBalanceAmount);
            currentBalanceAmount = negateAmountForCreditCard(currentBalanceAmount);
        }

        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .accountNumber(mapToProviderAccountNumberDTO(account, mapToHolderName(account)))
                .name(account.getName())
                .bic(account.getBicFi())
                .yoltAccountType(mapToAccountType(account.getType()))
                .currency(mapToCurrencyCode(account, balances))
                .creditCardData(mapToProviderCreditCardDTO(account, roundBalance(getBalanceAmount(balances, getPreferredCardBalanceType()))))
                .availableBalance(availableBalanceAmount)
                .currentBalance(currentBalanceAmount)
                .extendedAccount(mapToExtendedAccountDTO(account, balances))
                .transactions(providerTransactions)
                .lastRefreshed(dateTimeSupplier.getDefaultZonedDateTime())
                .build();
    }

    @Override
    protected BigDecimal getBalanceAmount(List<StetBalanceDTO> balances, List<StetBalanceType> preferredBalanceTypes) {
        Map<StetBalanceType, BigDecimal> balanceAmountMap = new HashMap<>();
        OffsetDateTime othrBalanceDate = null;
        for (StetBalanceDTO balance : balances) {
            if (balance.getType().equals(StetBalanceType.OTHR)) {
                if (othrBalanceDate == null || othrBalanceDate.isBefore(balance.getReferenceDate())) {
                    othrBalanceDate = balance.getReferenceDate();
                    balanceAmountMap.put(balance.getType(), balance.getAmount());
                }
            } else {
                balanceAmountMap.put(balance.getType(), balance.getAmount());
            }
        }

        for (StetBalanceType preferredBalanceType : preferredBalanceTypes) {
            if (balanceAmountMap.containsKey(preferredBalanceType)) {
                return balanceAmountMap.get(preferredBalanceType);
            }
        }
        return null;
    }

    @Override
    public List<BalanceDTO> mapToBalanceDTOs(List<StetBalanceDTO> balances) {
        return balances.stream()
                .map(balance -> BalanceDTO.builder()
                        .balanceAmount(BalanceAmountDTO.builder()
                                .amount(roundBalance(balance.getAmount()))
                                .currency(balance.getCurrency())
                                .build())
                        .balanceType(mapToBalanceType(balance.getType()))
                        .lastChangeDateTime(dateTimeSupplier.convertToZonedDateTime(balance.getLastChangeDateTime()))
                        .lastCommittedTransaction(balance.getLastCommittedTransaction())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    protected BalanceType mapToBalanceType(StetBalanceType balanceType) {
        if (Objects.nonNull(balanceType)) {
            switch (balanceType) {
                case CLBD:
                    return BalanceType.CLOSING_BOOKED;
                case XPCD:
                    return BalanceType.INTERIM_AVAILABLE;
                case OTHR:
                    return BalanceType.NON_INVOICED;
                default:
            }
        }
        return null;
    }

    @Override
    protected ProviderCreditCardDTO mapToProviderCreditCardDTO(StetAccountDTO account, BigDecimal balanceAmount) {
        if (StetAccountType.CARD.equals(account.getType())) {
            return ProviderCreditCardDTO.builder()
                    .availableCreditAmount(balanceAmount.negate())
                    .build();
        }
        return null;
    }

    @Override
    protected ExternalCashAccountType mapToExternalCashAccountType(StetAccountType purpose) {
        switch (purpose) {
            case CARD:
                return ExternalCashAccountType.OTHER;
            case CACC:
                return ExternalCashAccountType.CURRENT;
            default:
                return null;
        }
    }

    private BigDecimal roundBalance(BigDecimal balanceAmount) {
        if (balanceAmount != null) {
            return balanceAmount.setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }

    private BigDecimal negateAmountForCreditCard(BigDecimal amount) {
        if (amount != null) {
            return amount.negate();
        }
        return null;
    }
}
