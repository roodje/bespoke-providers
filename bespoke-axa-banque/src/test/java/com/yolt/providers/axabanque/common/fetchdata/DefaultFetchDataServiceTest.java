package com.yolt.providers.axabanque.common.fetchdata;

import com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans;
import com.yolt.providers.axabanque.common.auth.http.clientproducer.DefaultHttpClientProducer;
import com.yolt.providers.axabanque.common.consentwindow.ConsentWindow;
import com.yolt.providers.axabanque.common.fetchdata.errorhandler.DefaultFetchDataHttpErrorHandlerV2;
import com.yolt.providers.axabanque.common.fetchdata.http.client.DefaultFetchDataHttpClientV2;
import com.yolt.providers.axabanque.common.fetchdata.mapper.AccountMapper;
import com.yolt.providers.axabanque.common.fetchdata.service.DefaultFetchDataService;
import com.yolt.providers.axabanque.common.fetchdata.service.FetchDataService;
import com.yolt.providers.axabanque.common.fixtures.AccessMeansFixture;
import com.yolt.providers.axabanque.common.fixtures.AuthMeansFixture;
import com.yolt.providers.axabanque.common.model.external.*;
import com.yolt.providers.axabanque.common.model.external.Transactions.Transaction;
import com.yolt.providers.axabanque.common.model.external.Transactions.TransactionsMetaData;
import com.yolt.providers.axabanque.common.model.internal.GroupAccessMeans;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DefaultFetchDataServiceTest {
    private FetchDataService fetchDataService;
    private DefaultFetchDataHttpClientV2 httpClient;
    private RestTemplateManager restTemplateManager;
    private DefaultFetchDataHttpErrorHandlerV2 errorHandler;
    private AccountMapper accountMapper;
    private ConsentWindow consentWindowUtil;

    @BeforeEach
    public void setup() {
        consentWindowUtil = mock(ConsentWindow.class);
        errorHandler = mock(DefaultFetchDataHttpErrorHandlerV2.class);
        restTemplateManager = mock(RestTemplateManager.class);
        httpClient = mock(DefaultFetchDataHttpClientV2.class);
        when(httpClient.getTransactionsEndpoint()).thenReturn("/transactionsEndpoint");
        DefaultHttpClientProducer restTemplateProducer = mock(DefaultHttpClientProducer.class);
        when(restTemplateProducer.getFetchDataHttpClient(any(), any(), eq(restTemplateManager))).thenReturn(httpClient);
        accountMapper = mock(AccountMapper.class);
        fetchDataService = new DefaultFetchDataService(
                restTemplateProducer,
                accountMapper,
                100,
                consentWindowUtil);
    }

    @Test
    void shouldFetchDataForTimeDependingOnConsentWindow() throws TokenInvalidException, ProviderFetchDataException {
        //given
        Instant dummyDate = Instant.parse("3019-03-14T18:35:24.00Z");
        String dummyPsuIpAddress = "127.0.1.2";
        when(consentWindowUtil.whenFromToFetchData(any(), any())).thenReturn(dummyDate);
        GroupAccessMeans accessMeans = AccessMeansFixture.createAccessMeans("consentId", "accessToken", "xRequestTraceId");
        GroupAuthenticationMeans authMeans = AuthMeansFixture.getAuthMeans("clientId");
        List<Account> accounts = mockAccountResponse();
        List<Balance> balances = mockBalanceResponse();
        List<Transactions> transactions = mockTransactionResponse(dummyDate);
        ProviderAccountDTO accountDTO0 = mock(ProviderAccountDTO.class);
        when(accountMapper.map(accounts.get(0), Collections.singletonList(balances.get(0)), Collections.singletonList(transactions.get(0))))
                .thenReturn(accountDTO0);
        ProviderAccountDTO accountDTO1 = mock(ProviderAccountDTO.class);
        when(accountMapper.map(accounts.get(1), Collections.singletonList(balances.get(1)), Arrays.asList(transactions.get(1), transactions.get(2))))
                .thenReturn(accountDTO1);
        //when
        DataProviderResponse response = fetchDataService.fetchData(accessMeans, authMeans, restTemplateManager, Instant.now(), dummyPsuIpAddress);
        //then
        assertThat(response.getAccounts()).containsExactlyInAnyOrder(accountDTO0, accountDTO1);
    }

    private List<Account> mockAccountResponse() throws TokenInvalidException {
        Accounts accounts = mock(Accounts.class);
        Account account0 = mock(Account.class);
        when(account0.getResourceId()).thenReturn("account0");
        Account account1 = mock(Account.class);
        when(account1.getResourceId()).thenReturn("account1");
        when(accounts.getAccounts()).thenReturn(new ArrayList<Account>() {{
            add(account0);
            add(account1);
        }});
        when(httpClient.getAccounts("accessToken", "consentId", "xRequestTraceId", "127.0.1.2")).thenReturn(accounts);
        return Arrays.asList(account0, account1);
    }

    private List<Balance> mockBalanceResponse() throws TokenInvalidException {
        Balances balances0 = mock(Balances.class);
        Balance balance0 = mock(Balance.class);
        when(balance0.getType()).thenReturn("expected");
        when(balance0.getAmount()).thenReturn(Double.valueOf("1234.44"));
        Balances balances1 = mock(Balances.class);
        Balance balance1 = mock(Balance.class);
        when(balance1.getType()).thenReturn("expected");
        when(balance1.getAmount()).thenReturn(Double.valueOf("556.44"));
        when(balances0.getBalances()).thenReturn(new ArrayList<Balance>() {{
            add(balance0);
        }});
        when(balances1.getBalances()).thenReturn(new ArrayList<Balance>() {{
            add(balance1);
        }});
        when(httpClient.getBalances("accessToken", "consentId", "account0", "xRequestTraceId", "127.0.1.2")).thenReturn(balances0);
        when(httpClient.getBalances("accessToken", "consentId", "account1", "xRequestTraceId", "127.0.1.2")).thenReturn(balances1);
        return Arrays.asList(balance0, balance1);
    }

    private List<Transactions> mockTransactionResponse(Instant fetchDataStartTime) throws TokenInvalidException {
        TransactionsMetaData transactionMetaDataWithoutNextPage = mock(TransactionsMetaData.class);
        when(transactionMetaDataWithoutNextPage.getNext()).thenReturn("");
        Transactions transactions0 = mock(Transactions.class);
        Transaction bookedTransaction = mock(Transaction.class);
        when(transactions0.getBookedTransactions()).thenReturn(Collections.singletonList(bookedTransaction));
        when(transactions0.getTransactionsMetaData()).thenReturn(transactionMetaDataWithoutNextPage);
        Transaction pendingTransactionOnFirstPage = mock(Transaction.class);
        Transaction pendingTransactionOnSecondPage = mock(Transaction.class);
        Transactions transactions1 = mock(Transactions.class);
        when(transactions1.getPendingTransactions()).thenReturn(Collections.singletonList(pendingTransactionOnFirstPage));
        TransactionsMetaData transactionMetaDataWithNextPage = mock(TransactionsMetaData.class);
        when(transactionMetaDataWithNextPage.getNext()).thenReturn("/nextPage");
        when(transactions1.getTransactionsMetaData()).thenReturn(transactionMetaDataWithNextPage);
        Transactions transactions2 = mock(Transactions.class);
        when(transactions2.getPendingTransactions()).thenReturn(Collections.singletonList(pendingTransactionOnSecondPage));
        when(transactions2.getPendingTransactions()).thenReturn(Collections.singletonList(pendingTransactionOnFirstPage));
        when(transactions2.getTransactionsMetaData()).thenReturn(transactionMetaDataWithoutNextPage);
        when(httpClient.getTransactions("account0", fetchDataStartTime, "accessToken", "consentId", "/transactionsEndpoint", "xRequestTraceId", 0, "127.0.1.2")).thenReturn(transactions0);
        when(httpClient.getTransactions("account1", fetchDataStartTime, "accessToken", "consentId", "/transactionsEndpoint", "xRequestTraceId", 0, "127.0.1.2")).thenReturn(transactions1);
        when(httpClient.getTransactions("account1", fetchDataStartTime, "accessToken", "consentId", "/transactionsEndpoint", "xRequestTraceId", 1, "127.0.1.2")).thenReturn(transactions2);
        return Arrays.asList(transactions0, transactions1, transactions2);
    }

    @Test
    void shouldThrowDataProviderFetchDataExceptionWhenHasFailedAccount() throws TokenInvalidException {
        //given
        Instant dummyDate = Instant.parse("3019-03-14T18:35:24.00Z");
        String dummyPsuIpAddress = "127.0.1.2";
        when(consentWindowUtil.whenFromToFetchData(any(), any())).thenReturn(dummyDate);
        GroupAccessMeans accessMeans = AccessMeansFixture.createAccessMeans("consentId", "accessToken", "xRequestTraceId");
        GroupAuthenticationMeans authMeans = AuthMeansFixture.getAuthMeans("clientId");
        List<Account> accounts = mockAccountResponse();
        List<Balance> balances = mockBalanceResponse();
        List<Transactions> transactions = mockTransactionResponse(dummyDate);
        when(accountMapper.map(accounts.get(0), Collections.singletonList(balances.get(0)), Collections.singletonList(transactions.get(0))))
                .thenThrow(new RuntimeException("Failed to map account"));
        //when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> fetchDataService.fetchData(accessMeans, authMeans, restTemplateManager, null, dummyPsuIpAddress);
        //then
        assertThatExceptionOfType(ProviderFetchDataException.class)
                .isThrownBy(throwingCallable);
    }

    @Test
    void shouldHandleHttpExceptions() throws TokenInvalidException, ProviderFetchDataException {
        //given
        Instant dummyDate = Instant.parse("3019-03-14T18:35:24.00Z");
        String dummyPsuIpAddress = "127.0.0.1";
        when(consentWindowUtil.whenFromToFetchData(any(), any())).thenReturn(dummyDate);
        GroupAccessMeans accessMeans = AccessMeansFixture.createAccessMeans("consentId", "accessToken", "xRequestTraceId");
        GroupAuthenticationMeans authMeans = AuthMeansFixture.getAuthMeans("clientId");
        when(httpClient.getAccounts(any(), any(), any(), eq(dummyPsuIpAddress))).thenThrow(new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT, "dummy"));
        doThrow(new RuntimeException("It was handled by handler")).when(errorHandler).handle(new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT), dummyPsuIpAddress);
        //when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> fetchDataService.fetchData(accessMeans, authMeans, restTemplateManager, Instant.now(), dummyPsuIpAddress);
        //then
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(throwingCallable)
                .withMessage("418 dummy");
    }
}
