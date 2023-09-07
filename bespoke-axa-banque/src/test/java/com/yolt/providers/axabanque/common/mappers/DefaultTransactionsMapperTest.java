package com.yolt.providers.axabanque.common.mappers;

import com.yolt.providers.axabanque.common.fetchdata.mapper.DefaultTransactionMapper;
import com.yolt.providers.axabanque.common.fetchdata.mapper.TransactionMapper;
import com.yolt.providers.axabanque.common.model.external.Amount;
import com.yolt.providers.axabanque.common.model.external.Transactions;
import com.yolt.providers.axabanque.common.model.external.Transactions.Transaction;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultTransactionsMapperTest {

    private TransactionMapper transactionMapper;

    @BeforeEach
    public void setup() {
        transactionMapper = new DefaultTransactionMapper(ZoneId.of("Europe/Paris"));
    }

    @Test
    public void shouldMapNullValues() {
        //given
        Transactions transactions = mock(Transactions.class);
        when(transactions.getPendingTransactions()).thenReturn(null);
        when(transactions.getBookedTransactions()).thenReturn(null);
        List<ProviderTransactionDTO> expected = Collections.emptyList();
        //when
        List<ProviderTransactionDTO> mappedTransactions = transactionMapper.mapToTransactions(Collections.singletonList(transactions));
        //then
        assertThat(mappedTransactions).isEqualTo(expected);
    }

    @Test
    public void shouldMapEmptyLists() {
        //given
        Transactions transactions = mock(Transactions.class);
        when(transactions.getPendingTransactions()).thenReturn(Collections.emptyList());
        when(transactions.getBookedTransactions()).thenReturn(Collections.emptyList());
        List<ProviderTransactionDTO> expected = Collections.emptyList();
        //when
        List<ProviderTransactionDTO> mappedTransactions = transactionMapper.mapToTransactions(Collections.singletonList(transactions));
        //then
        assertThat(mappedTransactions).isEqualTo(expected);
    }

    @Test
    public void shouldTransactionsWithProperStatus() {
        //given
        Amount amount = mock(Amount.class);
        when(amount.getAmount()).thenReturn(1234.56);
        Transactions transactions = mock(Transactions.class);
        Transaction bookedTransaction = mock(Transaction.class);
        when(bookedTransaction.getTransactionId()).thenReturn("bookedTransactionId");
        when(bookedTransaction.getAmount()).thenReturn(amount);
        Transaction pendingTransaction = mock(Transaction.class);
        when(pendingTransaction.getTransactionId()).thenReturn("pendingTransactionId");
        when(pendingTransaction.getAmount()).thenReturn(amount);
        when(transactions.getPendingTransactions()).thenReturn(Collections.singletonList(pendingTransaction));
        when(transactions.getBookedTransactions()).thenReturn(Collections.singletonList(bookedTransaction));
        //when
        List<ProviderTransactionDTO> mappedTransactions = transactionMapper.mapToTransactions(Collections.singletonList(transactions));
        //then
        ProviderTransactionDTO mappedBookedTransaction = mappedTransactions.stream()
                .filter(t -> t.getExternalId().equals("bookedTransactionId"))
                .findFirst().get();
        ProviderTransactionDTO mappedPendingTransaction = mappedTransactions.stream()
                .filter(t -> t.getExternalId().equals("pendingTransactionId"))
                .findFirst().get();
        assertThat(mappedBookedTransaction).isEqualToComparingFieldByField(getBookedTransactionWithIdAndStatus("bookedTransactionId", TransactionStatus.BOOKED));
        assertThat(mappedPendingTransaction).isEqualToComparingFieldByField(getBookedTransactionWithIdAndStatus("pendingTransactionId", TransactionStatus.PENDING));
    }

    private ProviderTransactionDTO getBookedTransactionWithIdAndStatus(String transactionId, TransactionStatus status) {

        return new ProviderTransactionDTO(transactionId,
                null, new BigDecimal("1234.56"), status, ProviderTransactionType.CREDIT, null, YoltCategory.GENERAL, null,
                ExtendedTransactionDTO.builder().status(status).transactionAmount(BalanceAmountDTO.builder().amount(new BigDecimal("1234.56")).build()).build(), null);
    }
}
