package com.yolt.providers.openbanking.ais.nationwide.service.ais.accountmapper;

import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountNameMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountNumberMapperV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapperV3;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.SupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.BalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;

public class NationwideAccountMapperV7 extends DefaultAccountMapperV3 {


    private final Supplier<List<OBBalanceType1Code>> getAvailableBalanceTypeForCreditCard;
    private final Function<OBExternalAccountSubType1Code, AccountType> accountTypeMapper;
    private final BalanceMapper balanceMapper;

    public NationwideAccountMapperV7(Supplier<List<OBBalanceType1Code>> getCurrentBalanceType,
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
                currencyCodeMapper, accountIdMapper, accountTypeMapper, creditCardMapper,
                accountNumberMapper, accountNameMapper, balanceMapper, extendedAccountMapper, supportedSchemeAccountFilter, clock);
        this.getAvailableBalanceTypeForCreditCard = getAvailableBalanceTypeForCreditCard;
        this.accountTypeMapper = accountTypeMapper;
        this.balanceMapper = balanceMapper;
    }

    @Override
    protected ProviderAccountDTO.ProviderAccountDTOBuilder getBuilder(OBAccount6 account, List<ProviderTransactionDTO> transactions, Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType, List<StandingOrderDTO> standingOrders, List<DirectDebitDTO> directDebits, List<PartyDto> parties) {
        ProviderAccountDTO.ProviderAccountDTOBuilder builder = super.getBuilder(account, transactions, balancesByType, standingOrders, directDebits, parties);
        AccountType yoltAccountType = accountTypeMapper.apply(account.getAccountSubType());
        if (CREDIT_CARD.equals(yoltAccountType)) {
            //Nationwide sends to as positive number marked as DEBIT so we need to negate it manually (so it would be positive in the app)
            BigDecimal balance = balanceMapper.getBalance(balancesByType, getAvailableBalanceTypeForCreditCard);
            builder.availableBalance(balance.negate());
            builder.creditCardData(ProviderCreditCardDTO.builder()
                    .availableCreditAmount(balance.negate())
                    .build());
        }
        return builder;
    }
}
