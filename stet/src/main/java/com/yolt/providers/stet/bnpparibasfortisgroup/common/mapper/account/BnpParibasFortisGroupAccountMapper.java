package com.yolt.providers.stet.bnpparibasfortisgroup.common.mapper.account;

import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.DefaultAccountMapper;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.account.Status;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class BnpParibasFortisGroupAccountMapper extends DefaultAccountMapper {

    public BnpParibasFortisGroupAccountMapper(DateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
    }

    @Override
    public List<StetBalanceType> getPreferredCurrentBalanceTypes() {
        return Arrays.asList(StetBalanceType.CLBD, StetBalanceType.VALU, StetBalanceType.XPCD, StetBalanceType.OTHR);
    }

    @Override
    public List<StetBalanceType> getPreferredAvailableBalanceTypes() {
        return Arrays.asList(StetBalanceType.OTHR, StetBalanceType.XPCD, StetBalanceType.VALU, StetBalanceType.CLBD, StetBalanceType.ITAV);
    }

    @Override
    public ProviderAccountDTO mapToProviderAccountDTO(StetAccountDTO account,
                                                      List<StetBalanceDTO> balances,
                                                      List<ProviderTransactionDTO> transactionDTOs) {
        CurrencyCode supportedCurrency = account.getCurrency();
        return ProviderAccountDTO.builder()
                .accountId(account.getResourceId())
                .name(account.getName())
                .currency(supportedCurrency)
                .yoltAccountType(mapToAccountType(account.getType()))
                .lastRefreshed(dateTimeSupplier.getDefaultZonedDateTime())
                .currentBalance(extractBalanceAmount(balances, supportedCurrency, getPreferredCurrentBalanceTypes()))
                .availableBalance(extractBalanceAmount(balances, supportedCurrency, getPreferredAvailableBalanceTypes()))
                .transactions(transactionDTOs)
                .extendedAccount(mapToExtendedAccountDTO(account, balances))
                .build();
    }

    @Override
    public ExtendedAccountDTO mapToExtendedAccountDTO(StetAccountDTO account, List<StetBalanceDTO> balances) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .name(account.getName())
                .accountReferences(mapToAccountReferenceDTOs(account))
                .status(Status.ENABLED)
                .usage(mapToUsageType(account.getUsage()))
                .balances(mapToBalanceDTOs(balances))
                .currency(account.getCurrency())
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .build();
    }

    private BigDecimal extractBalanceAmount(List<StetBalanceDTO> balanceResources,
                                            CurrencyCode supportedCurrency,
                                            List<StetBalanceType> preferredBalances) {
        if (!ObjectUtils.isEmpty(supportedCurrency)) {
            for (StetBalanceType preferredBalance : preferredBalances) {
                BigDecimal balanceAmount = extractBalanceAmountForPreferredBalance(balanceResources, supportedCurrency, preferredBalance);
                if (balanceAmount != null) {
                    return balanceAmount;
                }
            }
        }
        return null;
    }

    private BigDecimal extractBalanceAmountForPreferredBalance(List<StetBalanceDTO> balanceResources,
                                                               CurrencyCode supportedCurrency,
                                                               StetBalanceType preferredBalance) {
        return balanceResources.stream()
                .filter(balance -> supportedCurrency.equals(balance.getCurrency()))
                .filter(balance -> balance.getType().equals(preferredBalance))
                .findFirst()
                .map(StetBalanceDTO::getAmount)
                .orElse(null);
    }

    /**
     * Includes pending transactions and standing orders
     */
    @Override
    protected BalanceType mapToBalanceType(StetBalanceType balanceType) {
        if (StetBalanceType.OTHR.equals(balanceType)) {
            return BalanceType.AVAILABLE;
        }
        return super.mapToBalanceType(balanceType);
    }
}
