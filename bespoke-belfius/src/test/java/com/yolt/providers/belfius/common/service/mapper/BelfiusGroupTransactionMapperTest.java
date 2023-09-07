package com.yolt.providers.belfius.common.service.mapper;

import com.yolt.providers.belfius.common.model.ais.Account;
import com.yolt.providers.belfius.common.model.ais.TransactionResponse;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.YoltCategory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BelfiusGroupTransactionMapperTest {

    private BelfiusGroupTransactionMapper mapper = new BelfiusGroupTransactionMapper();

    @Test
    void shouldCorrectlyMapTransactionForInvalidAmountAndCurrencyAndCounterPartyIban() {
        // given
        Account account = mock(Account.class);
        when(account.getAccountName()).thenReturn("SOME_ACCOUNT_NAME");
        when(account.getIban()).thenReturn("SOME_IBAN");

        TransactionResponse.Transaction transaction = mock(TransactionResponse.Transaction.class);
        List<TransactionResponse.Transaction> transactions = new ArrayList<>();

        transactions.add(transaction);
        when(transaction.getTransactionRef()).thenReturn("SOME_TRANSACTION_REF");
        when(transaction.getAmount()).thenReturn(null);
        when(transaction.getCurrency()).thenReturn(null);
        when(transaction.getExecutionDateTime()).thenReturn("2020-02-02");
        when(transaction.getValueDate()).thenReturn("2020-02-02");
        when(transaction.getCounterPartyAccount()).thenReturn(null);
        when(transaction.getCounterPartyInfo()).thenReturn("COUNTER_PARTY_INFO");
        when(transaction.getRemittanceInfo()).thenReturn("SOME_DESCRIPTION");

        // when
        List<ProviderTransactionDTO> result = mapper.mapTransactions(transactions, account);

        // then
        assertThat(result).hasSize(1);
        ProviderTransactionDTO providerTransactionDTO = result.get(0);
        assertThat(providerTransactionDTO.getExternalId()).isEqualTo("SOME_TRANSACTION_REF");
        assertThat(providerTransactionDTO.getAmount()).isNull();
        assertThat(providerTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(providerTransactionDTO.getType()).isNull();
        assertThat(providerTransactionDTO.getExtendedTransaction().getCreditorAccount()).isNull();
        assertThat(providerTransactionDTO.getExtendedTransaction().getTransactionAmount().getCurrency()).isNull();
    }
}