package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account;

import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.SupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.BalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.ExtendedAccountMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.*;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;

@RequiredArgsConstructor
public class DefaultAccountMapperV3 implements AccountMapperV2 {
    private static final String PAN_SCHEMA_NAME = "UK.OBIE.PAN";

    private final Supplier<List<OBBalanceType1Code>> getCurrentBalanceType;
    private final Supplier<List<OBBalanceType1Code>> getAvailableBalanceType;
    private final Supplier<List<OBBalanceType1Code>> getCurrentBalanceTypeForCreditCard;
    private final Supplier<List<OBBalanceType1Code>> getAvailableBalanceTypeForCreditCard;
    private final Function<String, CurrencyCode> currencyCodeMapper;
    private final Function<OBAccount6, String> accountIdMapper;
    private final Function<OBExternalAccountSubType1Code, AccountType> accountTypeMapper;
    private final Function<BigDecimal, ProviderCreditCardDTO> creditCardMapper;
    private final AccountNumberMapperV2 accountNumberMapper;
    private final AccountNameMapper accountNameMapper;
    private final BalanceMapper availableBalanceMapper;
    private final BalanceMapper availableCreditCardBalanceMapper;
    private final BalanceMapper currentBalanceMapper;
    private final BalanceMapper currentForCreditCardBalanceMapper;
    private final ExtendedAccountMapper extendedAccountMapper;
    private final SupportedSchemeAccountFilter supportedSchemeAccountFilter;
    private final Clock clock;

    public DefaultAccountMapperV3(Supplier<List<OBBalanceType1Code>> getCurrentBalanceType,
                                  Supplier<List<OBBalanceType1Code>> getAvailableBalanceType,
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
        this.getCurrentBalanceType = getCurrentBalanceType;
        this.getAvailableBalanceType = getAvailableBalanceType;
        this.getCurrentBalanceTypeForCreditCard = getCurrentBalanceType;
        this.getAvailableBalanceTypeForCreditCard = getAvailableBalanceType;
        this.currencyCodeMapper = currencyCodeMapper;
        this.accountIdMapper = accountIdMapper;
        this.accountTypeMapper = accountTypeMapper;
        this.creditCardMapper = creditCardMapper;
        this.accountNumberMapper = accountNumberMapper;
        this.accountNameMapper = accountNameMapper;
        this.availableBalanceMapper = balanceMapper;
        this.availableCreditCardBalanceMapper = balanceMapper;
        this.currentBalanceMapper = balanceMapper;
        this.currentForCreditCardBalanceMapper = balanceMapper;
        this.extendedAccountMapper = extendedAccountMapper;
        this.supportedSchemeAccountFilter = supportedSchemeAccountFilter;
        this.clock = clock;
    }

    public DefaultAccountMapperV3(Supplier<List<OBBalanceType1Code>> getCurrentBalanceType,
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
        this.getCurrentBalanceType = getCurrentBalanceType;
        this.getAvailableBalanceType = getAvailableBalanceType;
        this.getCurrentBalanceTypeForCreditCard = getCurrentBalanceTypeForCreditCard;
        this.getAvailableBalanceTypeForCreditCard = getAvailableBalanceTypeForCreditCard;
        this.currencyCodeMapper = currencyCodeMapper;
        this.accountIdMapper = accountIdMapper;
        this.accountTypeMapper = accountTypeMapper;
        this.creditCardMapper = creditCardMapper;
        this.accountNumberMapper = accountNumberMapper;
        this.accountNameMapper = accountNameMapper;
        this.availableBalanceMapper = balanceMapper;
        this.availableCreditCardBalanceMapper = balanceMapper;
        this.currentBalanceMapper = balanceMapper;
        this.currentForCreditCardBalanceMapper = balanceMapper;
        this.extendedAccountMapper = extendedAccountMapper;
        this.supportedSchemeAccountFilter = supportedSchemeAccountFilter;
        this.clock = clock;
    }

    protected ProviderAccountDTO.ProviderAccountDTOBuilder getBuilder(final OBAccount6 account,
                                                                      final List<ProviderTransactionDTO> transactions,
                                                                      final Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType,
                                                                      final List<StandingOrderDTO> standingOrders,
                                                                      final List<DirectDebitDTO> directDebits,
                                                                      final List<PartyDto> parties) {
        OBAccount4Account accountWithSupportedScheme = supportedSchemeAccountFilter.findFirstAccountWhereSchemeIsSupported(account.getAccount());
        Optional<ProviderAccountNumberDTO> accountReference = Optional.ofNullable(accountWithSupportedScheme)
                .map(ac -> accountNumberMapper.map(ac, parties));

        String extractedPrimaryAccountName = accountNameMapper.getAccountName(account, accountWithSupportedScheme, account.getAccount());

        AccountType yoltAccountType = accountTypeMapper.apply(account.getAccountSubType());
        ProviderAccountDTO.ProviderAccountDTOBuilder providerAccountDTOBuilder = ProviderAccountDTO.builder()
                .yoltAccountType(yoltAccountType)
                .lastRefreshed(ZonedDateTime.now(clock))
                .currency(currencyCodeMapper.apply(account.getCurrency()))
                .accountId(accountIdMapper.apply(account))
                .name(extractedPrimaryAccountName)
                .transactions(transactions)
                .standingOrders(standingOrders)
                .directDebits(directDebits)
                .availableBalance(CREDIT_CARD.equals(yoltAccountType) ?
                        availableCreditCardBalanceMapper.getBalance(balancesByType, getAvailableBalanceTypeForCreditCard) :
                        availableBalanceMapper.getBalance(balancesByType, getAvailableBalanceType))
                .currentBalance(CREDIT_CARD.equals(yoltAccountType) ?
                        currentForCreditCardBalanceMapper.getBalance(balancesByType, getCurrentBalanceTypeForCreditCard) :
                        currentBalanceMapper.getBalance(balancesByType, getCurrentBalanceType))
                .extendedAccount(extendedAccountMapper.mapToExtendedModelAccount(account, extractedPrimaryAccountName, new ArrayList<>(balancesByType.values())));

        accountReference.ifPresent(providerAccountDTOBuilder::accountNumber);

        if (CREDIT_CARD.equals(yoltAccountType)) {
            providerAccountDTOBuilder
                    .creditCardData(creditCardMapper.apply(availableCreditCardBalanceMapper.getBalance(balancesByType, getAvailableBalanceTypeForCreditCard)));
            findPanAccountIdentification(account.getAccount())
                    .ifPresent(providerAccountDTOBuilder::accountMaskedIdentification);
        }
        return providerAccountDTOBuilder;
    }

    @Override
    public final ProviderAccountDTO mapToProviderAccount(final OBAccount6 account,
                                                         final List<ProviderTransactionDTO> transactions,
                                                         final Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType,
                                                         final List<StandingOrderDTO> standingOrders,
                                                         final List<DirectDebitDTO> directDebits,
                                                         final List<PartyDto> parties) {
        return getBuilder(account, transactions, balancesByType, standingOrders, directDebits, parties)
                .build();
    }

    private Optional<String> findPanAccountIdentification(final List<OBAccount4Account> accountList) {
        if (CollectionUtils.isEmpty(accountList)) {
            return Optional.empty();
        }
        for (OBAccount4Account cashAccount : accountList) {
            if (PAN_SCHEMA_NAME.equalsIgnoreCase(cashAccount.getSchemeName())) {
                return Optional.of(cashAccount.getIdentification());
            }
        }
        return Optional.empty();
    }
}
