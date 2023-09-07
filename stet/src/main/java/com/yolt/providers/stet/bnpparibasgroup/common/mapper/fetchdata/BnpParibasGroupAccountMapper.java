package com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata;

import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.DefaultAccountMapper;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.Status;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.Collections;
import java.util.List;
public class BnpParibasGroupAccountMapper extends DefaultAccountMapper {

    private final BnpParibasGroupBalanceMapper balanceMapper;

    public BnpParibasGroupAccountMapper(DateTimeSupplier dateTimeSupplier, BnpParibasGroupBalanceMapper balanceMapper) {
        super(dateTimeSupplier);
        this.balanceMapper = balanceMapper;
    }

    @Override
    public ProviderAccountDTO mapToProviderAccountDTO(StetAccountDTO account, List<StetBalanceDTO> balances, List<ProviderTransactionDTO> providerTransactions) {
        CurrencyCode supportedCurrency = account.getCurrency();

        return ProviderAccountDTO.builder()
                .name(account.getName())
                .accountNumber(mapToProviderAccountNumberDTO(account, mapToHolderName(account)))
                .yoltAccountType(mapToAccountType(account.getType()))
                .lastRefreshed(dateTimeSupplier.getDefaultZonedDateTime())
                .currency(mapToCurrencyCode(account, balances))
                .currentBalance(balanceMapper
                        .extractBalanceAmount(balances, supportedCurrency, getPreferredCurrentBalanceTypes()))
                .availableBalance(balanceMapper
                        .extractBalanceAmount(balances, supportedCurrency, getPreferredAvailableBalanceTypes()))
                .accountId(account.getResourceId())
                .transactions(providerTransactions)
                .extendedAccount(mapToExtendedAccountDTO(account, balances))
                .build();
    }

    @Override
    public ExtendedAccountDTO mapToExtendedAccountDTO(StetAccountDTO account, List<StetBalanceDTO> balances) {
        CurrencyCode supportedCurrency = account.getCurrency();

        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .name(account.getName())
                .accountReferences(mapToAccountReferenceDTOs(account))
                .status(Status.ENABLED)
                .product(account.getProduct())
                .details(account.getDetails())
                .usage(mapToUsageType(account.getUsage()))
                .balances(balanceMapper.mapToBalanceDTOs(balances, supportedCurrency))
                .currency(mapToCurrencyCode(account, balances))
                .cashAccountType(mapToExternalCashAccountType(account.getType()))
                .build();
    }

    @Override
    protected List<AccountReferenceDTO> mapToAccountReferenceDTOs(StetAccountDTO account) {
        return Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban()));
    }

    @Override
    public List<StetBalanceType> getPreferredCurrentBalanceTypes() {
        return List.of(StetBalanceType.CLBD, StetBalanceType.OTHR);
    }

    @Override
    public List<StetBalanceType> getPreferredAvailableBalanceTypes() {
        return List.of(StetBalanceType.OTHR, StetBalanceType.CLBD);
    }
}
