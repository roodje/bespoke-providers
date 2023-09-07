package com.yolt.providers.axabanque.common.mappers;

import com.yolt.providers.axabanque.common.fetchdata.mapper.DefaultAccountTypeMapper;
import com.yolt.providers.axabanque.common.model.external.Account;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.providerdomain.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DefaultAccountTypeMapperTest {

    private DefaultAccountTypeMapper accountTypeMapper;

    @BeforeEach
    void setUp() {
        accountTypeMapper = new DefaultAccountTypeMapper();
    }

    @Test
    void shouldReturnCurrentAccountType() {
        //given
        Account account = mock(Account.class);
        //when
        AccountType accountType = accountTypeMapper.mapToAccountType(account);
        //then
        assertThat(accountType).isEqualTo(AccountType.CURRENT_ACCOUNT);
    }

    @Test
    void shouldReturnCurrentExternalCashAccountType() {
        //given
        Account account = mock(Account.class);
        //when
        ExternalCashAccountType accountType = accountTypeMapper.mapToExternalCashAccountType(account);
        //then
        assertThat(accountType).isEqualTo(ExternalCashAccountType.CURRENT);
    }
}
