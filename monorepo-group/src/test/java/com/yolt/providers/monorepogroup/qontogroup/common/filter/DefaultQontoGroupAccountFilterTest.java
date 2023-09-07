package com.yolt.providers.monorepogroup.qontogroup.common.filter;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Account;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class DefaultQontoGroupAccountFilterTest {

    private DefaultQontoGroupAccountFilter accountfilter = new DefaultQontoGroupAccountFilter();

    @Test
    void shouldReturnedFilteredAccountList() {
        //given
        var activeAccount = mock(Account.class);
        given(activeAccount.getStatus()).willReturn("active");
        var closedAccount = mock(Account.class);
        given(closedAccount.getStatus()).willReturn("closed");

        //when
        var result = accountfilter.apply(List.of(activeAccount, closedAccount));

        //then
        assertThat(result).containsExactlyInAnyOrderElementsOf(List.of(
                activeAccount
        ));
    }

    @Test
    void shouldReturnedAllAccounts() {
        //given
        var activeAccount = mock(Account.class);
        given(activeAccount.getStatus()).willReturn("active");
        var activeAccount2 = mock(Account.class);
        given(activeAccount2.getStatus()).willReturn("active");

        //when
        var result = accountfilter.apply(List.of(activeAccount, activeAccount2));

        //then
        assertThat(result).containsExactlyInAnyOrderElementsOf(List.of(
                activeAccount, activeAccount2
        ));
    }

}