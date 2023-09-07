package com.yolt.providers.openbanking.ais.generic2.accountmapper;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.*;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.DefaultSupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.BalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.creditcard.CreditCardMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency.DefaultCurrencyMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.ExtendedAccountMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MappingSeparateBalances {

    @Test
    public void shouldUseNormalBalanceMappersWhenNotMappingCreditCardAccountType() {
        //given
        BalanceMapper availableBalanceMapper = mock(BalanceMapper.class);
        BalanceMapper currentBalanceMapper = mock(BalanceMapper.class);
        BalanceMapper availableCreditCardBalanceMapper = mock(BalanceMapper.class);
        BalanceMapper currentCreditCardBalanceMapper = mock(BalanceMapper.class);
        Function<OBExternalAccountSubType1Code, AccountType> accountTypeMapper = mock(DefaultAccountTypeMapper.class);

        when(accountTypeMapper.apply(any())).thenReturn(AccountType.PREPAID_ACCOUNT);
        when(availableBalanceMapper.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(1L));
        when(currentBalanceMapper.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(2L));
        when(availableCreditCardBalanceMapper.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(3L));
        when(currentCreditCardBalanceMapper.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(4L));


        DefaultAccountMapperV2 accountMapper = getAccountMapper(accountTypeMapper, availableBalanceMapper, currentBalanceMapper, availableCreditCardBalanceMapper, currentCreditCardBalanceMapper);
        //when
        ProviderAccountDTO accountDTO = accountMapper.mapToProviderAccount(getObAccount(), List.of(), Map.of(), List.of(), List.of());
        //then
        assertThat(accountDTO.getAvailableBalance()).isEqualTo("1");
        assertThat(accountDTO.getCurrentBalance()).isEqualTo("2");
    }

    @Test
    public void shouldUseBalanceMappersForCreditCardWhenMappingCreditCardAccountType() {
        //given
        BalanceMapper availableBalanceMapper = mock(BalanceMapper.class);
        BalanceMapper currentBalanceMapper = mock(BalanceMapper.class);
        BalanceMapper availableCreditCardBalanceMapper = mock(BalanceMapper.class);
        BalanceMapper currentCreditCardBalanceMapper = mock(BalanceMapper.class);
        Function<OBExternalAccountSubType1Code, AccountType> accountTypeMapper = mock(DefaultAccountTypeMapper.class);

        when(accountTypeMapper.apply(any())).thenReturn(AccountType.CREDIT_CARD);
        when(availableBalanceMapper.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(1L));
        when(currentBalanceMapper.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(2L));
        when(availableCreditCardBalanceMapper.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(3L));
        when(currentCreditCardBalanceMapper.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(4L));


        DefaultAccountMapperV2 accountMapper = getAccountMapper(accountTypeMapper, availableBalanceMapper, currentBalanceMapper, availableCreditCardBalanceMapper, currentCreditCardBalanceMapper);
        //when
        ProviderAccountDTO accountDTO = accountMapper.mapToProviderAccount(getObAccount(), List.of(), Map.of(), List.of(), List.of());
        //then
        assertThat(accountDTO.getAvailableBalance()).isEqualTo("3");
        assertThat(accountDTO.getCurrentBalance()).isEqualTo("4");
    }

    private OBAccount6 getObAccount() {
        OBAccount6 account = new OBAccount6();
        account.setCurrency("EUR");
        account.setAccountSubType(OBExternalAccountSubType1Code.CURRENTACCOUNT);
        account.setAccount(List.of(new OBAccount4Account()));
        return account;
    }

    private DefaultAccountMapperV2 getAccountMapper(Function<OBExternalAccountSubType1Code, AccountType> accountTypeMapper, BalanceMapper availableBalanceMapper, BalanceMapper currentBalanceMapper, BalanceMapper availableCreditCardBalanceMapper, BalanceMapper currentCreditCardBalanceMapper) {
        return new DefaultAccountMapperV2(
                () -> List.of(INTERIMBOOKED),
                () -> List.of(INTERIMAVAILABLE),
                () -> List.of(OPENINGCLEARED),
                () -> List.of(FORWARDAVAILABLE),
                mock(DefaultCurrencyMapper.class),
                mock(DefaultAccountIdMapper.class),
                accountTypeMapper,
                mock(CreditCardMapper.class),
                mock(AccountNumberMapper.class),
                mock(DefaultAccountNameMapper.class),
                availableBalanceMapper,
                availableCreditCardBalanceMapper,
                currentBalanceMapper,
                currentCreditCardBalanceMapper,
                mock(ExtendedAccountMapper.class),
                mock(DefaultSupportedSchemeAccountFilter.class),
                Clock.systemUTC());
    }
}
