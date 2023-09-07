package com.yolt.providers.monorepogroup.qontogroup.common.service;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Organization;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transaction;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transactions;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoFetchDataResult;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoGroupProviderState;
import com.yolt.providers.monorepogroup.qontogroup.common.filter.DefaultQontoGroupAccountFilter;
import com.yolt.providers.monorepogroup.qontogroup.common.filter.DefaultQontoGroupTransactionFilter;
import com.yolt.providers.monorepogroup.qontogroup.common.http.QontoGroupHttpClient;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultQontoFetchDataServiceTest {

    @Mock
    private QontoGroupHttpClient httpClient;
    @Mock
    private Signer signer;

    private QontoGroupProviderState providerState;
    private QontoGroupAuthenticationMeans authenticationMeans;
    private QontoGroupAuthenticationMeans.SigningData signingData;
    private DefaultQontoFetchDataService fetchDataService;

    @BeforeEach
    void setUp() {
        providerState = new QontoGroupProviderState("accessToken", null, null);
        signingData = new QontoGroupAuthenticationMeans.SigningData("https://s3baseurl.com", UUID.randomUUID());
        authenticationMeans = new QontoGroupAuthenticationMeans(
                signingData,
                null,
                null
        );
        fetchDataService = new DefaultQontoFetchDataService(10, new DefaultQontoGroupAccountFilter(), new DefaultQontoGroupTransactionFilter());
    }

    @Test
    void shouldFetchData() throws TokenInvalidException {
        //given
        Instant transactionFetchStartDate = Instant.now();
        Account account1 = mock(Account.class);
        given(account1.getIban()).willReturn("IBAN1");
        Account account2 = mock(Account.class);
        given(account2.getIban()).willReturn("IBAN2");
        Organization organization = mock(Organization.class);
        given(organization.getAccounts()).willReturn(List.of(account1, account2));
        given(httpClient.fetchOrganization("accessToken", "127.0.0.1", signer, signingData))
                .willReturn(organization);
        Transaction transactionOnFirstPageForAccount1 = mock(Transaction.class);
        Transactions transactionResponseFirstPageForAccount1 = mock(Transactions.class);
        given(transactionResponseFirstPageForAccount1.getTransactions())
                .willReturn(List.of(transactionOnFirstPageForAccount1));
        given(transactionResponseFirstPageForAccount1.getNextPage())
                .willReturn("2");
        given(httpClient.fetchTransactions("accessToken", "127.0.0.1", signer, signingData, "IBAN1", transactionFetchStartDate, "1"))
                .willReturn(transactionResponseFirstPageForAccount1);
        Transaction transactionOnSecondPageForAccount1 = mock(Transaction.class);
        Transactions transactionResponseSecondPageForAccount1 = mock(Transactions.class);
        given(transactionResponseSecondPageForAccount1.getTransactions())
                .willReturn(List.of(transactionOnSecondPageForAccount1));
        given(httpClient.fetchTransactions("accessToken", "127.0.0.1", signer, signingData, "IBAN1", transactionFetchStartDate, "2"))
                .willReturn(transactionResponseSecondPageForAccount1);
        Transaction transactionOnFirstPageForAccount2 = mock(Transaction.class);
        Transactions transactionsResponseFirstPageForAccount2 = mock(Transactions.class);
        given(transactionsResponseFirstPageForAccount2.getTransactions())
                .willReturn(List.of(transactionOnFirstPageForAccount2));
        given(httpClient.fetchTransactions("accessToken", "127.0.0.1", signer, signingData, "IBAN2", transactionFetchStartDate, "1"))
                .willReturn(transactionsResponseFirstPageForAccount2);
        var expectedFetchDataResult = new QontoFetchDataResult();
        expectedFetchDataResult.addResources(account1, List.of(transactionOnFirstPageForAccount1, transactionOnSecondPageForAccount1));
        expectedFetchDataResult.addResources(account2, List.of(transactionOnFirstPageForAccount2));

        //when
        var result = fetchDataService.fetchAccount(authenticationMeans, httpClient, providerState, transactionFetchStartDate, "127.0.0.1", signer);

        //then
        assertThat(result).isEqualTo(expectedFetchDataResult);
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenItIsThrownByHttpClient() throws TokenInvalidException {
        //given
        Instant transactionFetchStartDate = Instant.now();
        var tokenInvalidException = new TokenInvalidException("Thrown by http client");
        given(httpClient.fetchOrganization("accessToken", "127.0.0.1", signer, signingData))
                .willThrow(tokenInvalidException);

        //when
        ThrowableAssert.ThrowingCallable call = () -> fetchDataService.fetchAccount(authenticationMeans, httpClient, providerState, transactionFetchStartDate, "127.0.0.1", signer);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call)
                .withMessage("Thrown by http client");
    }
}