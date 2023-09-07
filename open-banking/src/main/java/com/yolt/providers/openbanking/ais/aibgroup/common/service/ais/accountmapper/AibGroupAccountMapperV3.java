package com.yolt.providers.openbanking.ais.aibgroup.common.service.ais.accountmapper;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountNameMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.SupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.BalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class AibGroupAccountMapperV3 extends DefaultAccountMapper {

    public AibGroupAccountMapperV3(Supplier<List<OBBalanceType1Code>> getCurrentBalanceType,
                                   Supplier<List<OBBalanceType1Code>> getAvailableBalanceType,
                                   Function<String, CurrencyCode> currencyCodeMapper,
                                   Function<OBAccount6, String> accountIdMapper,
                                   Function<OBExternalAccountSubType1Code, AccountType> accountTypeMapper,
                                   Function<BigDecimal, ProviderCreditCardDTO> creditCardMapper,
                                   Function<OBAccount4Account, ProviderAccountNumberDTO> accountNumberMapper,
                                   AccountNameMapper accountNameMapper,
                                   DefaultExtendedAccountMapper extendedAccountMapper,
                                   BalanceMapper balanceMapper,
                                   SupportedSchemeAccountFilter supportedSchemeAccountFilter,
                                   Clock clock) {
        super(getCurrentBalanceType, getAvailableBalanceType, currencyCodeMapper, accountIdMapper, accountTypeMapper, creditCardMapper, accountNumberMapper, accountNameMapper, balanceMapper, extendedAccountMapper, supportedSchemeAccountFilter, clock);
    }

    @Override
    protected ProviderAccountDTO.ProviderAccountDTOBuilder getBuilder(OBAccount6 account, List<ProviderTransactionDTO> transactions, Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType, List<StandingOrderDTO> standingOrders, List<DirectDebitDTO> directDebits) {
        String bic = Optional.ofNullable(account.getServicer())
                .map(OBBranchAndFinancialInstitutionIdentification50::getIdentification)
                .orElse(null);

        return super.getBuilder(account, transactions, balancesByType, standingOrders, directDebits).bic(bic);
    }
}
