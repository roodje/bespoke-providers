package com.yolt.providers.openbanking.ais.revolutgroup.common.service.ais.mappers;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountNameMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapperV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.SupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.BalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RevolutDefaultAccountMapperV2 extends DefaultAccountMapperV2 {


    public RevolutDefaultAccountMapperV2(Supplier<List<OBBalanceType1Code>> getCurrentBalanceType, Supplier<List<OBBalanceType1Code>> getAvailableBalanceType, Function<String, CurrencyCode> currencyCodeMapper, Function<OBAccount6, String> accountIdMapper, Function<OBExternalAccountSubType1Code, AccountType> accountTypeMapper, Function<BigDecimal, ProviderCreditCardDTO> creditCardMapper, Function<OBAccount4Account, ProviderAccountNumberDTO> accountNumberMapper, AccountNameMapper accountNameMapper, BalanceMapper balanceMapper, DefaultExtendedAccountMapper extendedAccountMapper, SupportedSchemeAccountFilter supportedSchemeAccountFilter, Clock clock) {
        super(getCurrentBalanceType, getAvailableBalanceType, currencyCodeMapper, accountIdMapper, accountTypeMapper, creditCardMapper, accountNumberMapper, accountNameMapper, balanceMapper, extendedAccountMapper, supportedSchemeAccountFilter, clock);
    }

    @Override
    protected ProviderAccountDTO.ProviderAccountDTOBuilder getBuilder(OBAccount6 account, List<ProviderTransactionDTO> transactions, Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType, List<StandingOrderDTO> standingOrders, List<DirectDebitDTO> directDebits) {
        return super.getBuilder(account, transactions, balancesByType, standingOrders, directDebits)
                .bankSpecific(mapAccountsToSpecificFields(account.getAccount()));
    }

    private Map<String, String> mapAccountsToSpecificFields(List<OBAccount4Account> account) {
        Map<String, String> bankSpecificFields = new HashMap<>();
        var ibanScheme = RevolutSupportedSchemeAccountFilter.OB_3_0_0_SCHEME_PREFIX + ProviderAccountNumberDTO.Scheme.IBAN.name();
        var ibansCsv = account.stream()
                .filter((obAccount4Account -> ibanScheme.equalsIgnoreCase(obAccount4Account.getSchemeName())))
                .map(OBAccount4Account::getIdentification)
                .filter((Objects::nonNull))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.joining(", "));

        bankSpecificFields.put("allIbanIdentifiers", ibansCsv);
        return bankSpecificFields;
    }
}