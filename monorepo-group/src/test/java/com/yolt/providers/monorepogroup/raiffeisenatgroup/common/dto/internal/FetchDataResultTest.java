package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal;

import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FetchDataResultTest {

    @Mock
    Transaction bookedTransaction1ForAccount123;
    @Mock
    Transaction bookedTransaction2ForAccount123;
    @Mock
    Transaction pendingTransaction1ForAccount123;
    @Mock
    Transaction bookedTransaction1ForAccount456;

    @Mock
    Account account123;
    @Mock
    Account account456;
    private FetchDataResult fetchDataResult;

    @BeforeEach
    void setUp() {
        given(account123.getResourceId()).willReturn("123");
        given(account456.getResourceId()).willReturn("456");
        fetchDataResult = new FetchDataResult();
        fetchDataResult.addResources(account123, List.of(bookedTransaction1ForAccount123, bookedTransaction2ForAccount123),
                List.of(pendingTransaction1ForAccount123));
        fetchDataResult.addResources(account456, List.of(bookedTransaction1ForAccount456),
                Collections.emptyList());
    }

    @Test
    void shouldReturnProperAccountAndItsBookedAndPendingTransactions() {
        //when
        List<Account> resultAccountList = fetchDataResult.getAccounts();

        List<Transaction> resultBookedTransactions = fetchDataResult.getBookedTransactions("123");
        List<Transaction> resultPendingTransactions = fetchDataResult.getPendingTransactions("123");

        //then
        Account resultAccount = resultAccountList.stream()
                .filter(a -> "123".equals(a.getResourceId()))
                .findFirst().get();
        assertThat(resultAccount).isEqualTo(account123);
        assertThat(resultBookedTransactions).hasSize(2)
                .contains(bookedTransaction1ForAccount123, bookedTransaction2ForAccount123);
        assertThat(resultPendingTransactions).hasSize(1)
                .contains(pendingTransaction1ForAccount123);

    }

    @Test
    void shouldReturnProperAccountAndItsBookedTransactionsOnly() {
        //when
        List<Account> resultAccountList = fetchDataResult.getAccounts();

        List<Transaction> resultBookedTransactions = fetchDataResult.getBookedTransactions("456");
        List<Transaction> resultPendingTransactions = fetchDataResult.getPendingTransactions("456");

        //then
        Account resultAccount = resultAccountList.stream()
                .filter(a -> "456".equals(a.getResourceId()))
                .findFirst().get();
        assertThat(resultAccount).isEqualTo(account456);
        assertThat(resultBookedTransactions).hasSize(1)
                .contains(bookedTransaction1ForAccount456);
        assertThat(resultPendingTransactions).isEmpty();

    }

    @Test
    void shouldAddTransactionsToList() {
        //given
        Transaction newBookedTransactionForAccount123 = mock(Transaction.class);

        //when
        fetchDataResult.addResources(account123, List.of(newBookedTransactionForAccount123), Collections.emptyList());

        //then
        List<Account> resultAccountList = fetchDataResult.getAccounts();
        List<Transaction> resultBookedTransactions = fetchDataResult.getBookedTransactions("123");
        List<Transaction> resultPendingTransactions = fetchDataResult.getPendingTransactions("123");

        assertThat(resultAccountList).hasSize(2)
                .contains(account123, account456);
        assertThat(resultBookedTransactions).hasSize(3)
                .contains(bookedTransaction1ForAccount123, bookedTransaction2ForAccount123, newBookedTransactionForAccount123);
        assertThat(resultPendingTransactions).hasSize(1)
                .contains(pendingTransaction1ForAccount123);
    }
}