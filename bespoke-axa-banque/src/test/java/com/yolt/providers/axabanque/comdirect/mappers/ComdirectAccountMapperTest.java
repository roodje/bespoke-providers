package com.yolt.providers.axabanque.comdirect.mappers;

import com.yolt.providers.axabanque.comdirect.mapper.ComdirectAccountTypeMapper;
import com.yolt.providers.axabanque.common.model.external.Account;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.providerdomain.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComdirectAccountMapperTest {

    private ComdirectAccountTypeMapper accountTypeMapper;

    @BeforeEach
    void setUp() {
        accountTypeMapper = new ComdirectAccountTypeMapper();
    }

    @Test
    void shouldReturnCurrentAccountType() {
        //given
        Account account = mock(Account.class);
        when(account.getCashAccountType()).thenReturn("CACC");
        //when
        AccountType accountType = accountTypeMapper.mapToAccountType(account);
        //then
        assertThat(accountType).isEqualTo(AccountType.CURRENT_ACCOUNT);
    }

    @Test
    void shouldReturnSavingAccountType() {
        //given
        Account account = mock(Account.class);
        when(account.getCashAccountType()).thenReturn("SVGS");
        //when
        AccountType accountType = accountTypeMapper.mapToAccountType(account);
        //then
        assertThat(accountType).isEqualTo(AccountType.SAVINGS_ACCOUNT);
    }

    @Test
    void shouldReturnCreditCardAccountType() {
        //given
        Account account = mock(Account.class);
        when(account.getMaskedPan()).thenReturn("426354XXXXXX7716");
        //when
        AccountType accountType = accountTypeMapper.mapToAccountType(account);
        //then
        assertThat(accountType).isEqualTo(AccountType.CREDIT_CARD);
    }

    @Test
    void shouldReturnCurrentExternalCashAccountType() {
        //given
        Account account = mock(Account.class);
        when(account.getCashAccountType()).thenReturn("CACC");
        //when
        ExternalCashAccountType accountType = accountTypeMapper.mapToExternalCashAccountType(account);
        //then
        assertThat(accountType).isEqualTo(ExternalCashAccountType.CURRENT);
    }

    @Test
    void shouldReturnSavingsExternalCashAccountType() {
        //given
        Account account = mock(Account.class);
        when(account.getCashAccountType()).thenReturn("SVGS");
        //when
        ExternalCashAccountType accountType = accountTypeMapper.mapToExternalCashAccountType(account);
        //then
        assertThat(accountType).isEqualTo(ExternalCashAccountType.SAVINGS);
    }
}
