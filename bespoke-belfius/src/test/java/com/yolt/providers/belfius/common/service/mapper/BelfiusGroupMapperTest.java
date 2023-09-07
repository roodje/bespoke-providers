package com.yolt.providers.belfius.common.service.mapper;

import com.yolt.providers.belfius.common.model.ais.Account;
import com.yolt.providers.belfius.common.model.ais.TransactionResponse;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BelfiusGroupMapperTest {

    @Mock
    private BelfiusGroupTransactionMapper transactionMapper;

    private BelfiusGroupMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new BelfiusGroupMapper(transactionMapper, Clock.systemUTC());
    }

    @Test
    public void shouldMapAccountWithNotValidCurrencyAsNull() {
        // given
        Account account = mock(Account.class);
        List<TransactionResponse.Transaction> transactions = Arrays.asList(mock(TransactionResponse.Transaction.class));

        when(account.getAccountName()).thenReturn("SOME_ACCOUNT_NAME");
        when(account.getCurrency()).thenReturn("SOME_NOT_VALID_CURRENCY");
        when(account.getIban()).thenReturn("SOME_IBAN");
        when(account.getBalance()).thenReturn(new BigDecimal("10.00"));
        when(transactionMapper.mapTransactions(transactions, account)).thenReturn(new ArrayList<>());

        // when
        ProviderAccountDTO result = mapper.mapToProviderAccountDTO(account, transactions, "SOME_LOGICAL_ID");

        // then
        assertThat(result.getName()).isEqualTo("SOME_ACCOUNT_NAME");
        assertThat(result.getCurrency()).isNull();
        assertThat(result.getAccountNumber()).isEqualTo(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "SOME_IBAN"));
        assertThat(result.getCurrentBalance()).isEqualTo("10.00");
        assertThat(result.getAvailableBalance()).isEqualTo("10.00");
        assertThat(result.getAccountId()).isEqualTo("SOME_LOGICAL_ID");
        assertThat(result.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
    }
}