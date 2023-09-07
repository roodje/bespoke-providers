package com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.accountmapper;

import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountNameMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountNumberMapperV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapperV3;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.SupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.BalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.*;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class HsbcGroupAccountMapperV7 extends DefaultAccountMapperV3 {

    public HsbcGroupAccountMapperV7(Supplier<List<OBBalanceType1Code>> getCurrentBalanceType,
                                    Supplier<List<OBBalanceType1Code>> getAvailableBalanceType,
                                    Supplier<List<OBBalanceType1Code>> getCurrentBalanceTypeForCreditCard,
                                    Supplier<List<OBBalanceType1Code>> getAvailableBalanceTypeForCreditCard,
                                    Function<String, CurrencyCode> currencyCodeMapper,
                                    Function<OBAccount6, String> accountIdMapper,
                                    Function<OBExternalAccountSubType1Code, AccountType> accountTypeMapper,
                                    Function<BigDecimal, ProviderCreditCardDTO> creditCardMapper,
                                    AccountNumberMapperV2 accountNumberMapper,
                                    AccountNameMapper accountNameMapper,
                                    BalanceMapper balanceMapper,
                                    DefaultExtendedAccountMapper extendedAccountMapper,
                                    SupportedSchemeAccountFilter supportedSchemeAccountFilter,
                                    Clock clock) {
        super(getCurrentBalanceType, getAvailableBalanceType, getCurrentBalanceTypeForCreditCard, getAvailableBalanceTypeForCreditCard,
                currencyCodeMapper, accountIdMapper, accountTypeMapper, creditCardMapper, accountNumberMapper, accountNameMapper, balanceMapper,
                extendedAccountMapper, supportedSchemeAccountFilter, clock);
    }


    @Override
    public ProviderAccountDTO.ProviderAccountDTOBuilder getBuilder(final OBAccount6 account,
                                                                   final List<ProviderTransactionDTO> transactions,
                                                                   final Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType,
                                                                   final List<StandingOrderDTO> standingOrders,
                                                                   final List<DirectDebitDTO> directDebits,
                                                                   final List<PartyDto> parties) {
        return super.getBuilder(account, transactions, balancesByType, standingOrders, directDebits, parties)
                .bic(extractBic(account.getServicer()));
    }

    private String extractBic(final OBBranchAndFinancialInstitutionIdentification50 servicer) {
        if (ObjectUtils.isEmpty(servicer)) {
            return null;
        }
        return servicer.getIdentification();
    }
}
