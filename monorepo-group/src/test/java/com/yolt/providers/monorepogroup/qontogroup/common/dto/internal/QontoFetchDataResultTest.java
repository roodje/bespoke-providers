package com.yolt.providers.monorepogroup.qontogroup.common.dto.internal;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class QontoFetchDataResultTest {

    @Mock
    Account account1;
    @Mock
    Transaction transaction1ForAccount1;
    @Mock
    Transaction transaction2ForAccount1;
    @Mock
    Account account2;
    @Mock
    Transaction transaction1ForAccount2;

    QontoFetchDataResult fetchDataResult;

    @BeforeEach
    void setUp() {
        fetchDataResult = new QontoFetchDataResult();
    }

    @Test
    void shouldAddAllResourcesWhenAccountIsntInResources() {
        //when
        fetchDataResult.addResources(account1, List.of(transaction1ForAccount1, transaction2ForAccount1));
        fetchDataResult.addResources(account2, List.of(transaction1ForAccount2));
        Map<Account, List<Transaction>> result = fetchDataResult.getResources();

        //then
        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                account1, List.of(transaction1ForAccount1, transaction2ForAccount1),
                account2, List.of(transaction1ForAccount2)
        ));
    }

    @Test
    void shouldAddResourcesWhenAccountAlreadyExistsInResources() {
        //when
        Transaction newTransaction = mock(Transaction.class);
        fetchDataResult.addResources(account1, List.of(transaction1ForAccount1, transaction2ForAccount1));
        fetchDataResult.addResources(account1, List.of(newTransaction));
        Map<Account, List<Transaction>> result = fetchDataResult.getResources();

        //then
        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                account1, List.of(transaction1ForAccount1, transaction2ForAccount1, newTransaction)
        ));
    }
}