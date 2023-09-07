package com.yolt.providers.belfius.belfius.service;

import com.yolt.providers.belfius.common.http.client.BelfiusGroupHttpClient;
import com.yolt.providers.belfius.common.model.BelfiusGroupAccessMeans;
import com.yolt.providers.belfius.common.model.BelfiusGroupAccessToken;
import com.yolt.providers.belfius.common.model.ais.Account;
import com.yolt.providers.belfius.common.model.ais.TransactionResponse;
import com.yolt.providers.belfius.common.service.mapper.BelfiusGroupMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.apache.commons.collections.ListUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class BelfiusFetchDataServiceTest {

    @Mock
    private BelfiusGroupMapper mapper;

    @Mock
    private BelfiusGroupHttpClient httpClient;

    private BelfiusFetchService service;

    @BeforeEach
    public void setup() {
        service = new BelfiusFetchService(mapper);
    }

    @Test
    public void shouldCorrectlyFetchDataForAccountWithOnePageTransaction() throws TokenInvalidException, ProviderFetchDataException {
        //given
        BelfiusGroupAccessToken token = new BelfiusGroupAccessToken();
        token.setAccessToken("BELFIUS_ACCESS_TOKEN");
        token.setRefreshToken("BELFIUS_REFRESH_TOKEN");
        token.setExpiresIn(3600L);
        token.setLogicalId("SOME_LOGICAL_ID");

        BelfiusGroupAccessMeans accessMeans = new BelfiusGroupAccessMeans(
                token,
                "en",
                "SOME_REDIRECT"
        );

        Instant from = LocalDate.parse("2016-04-17").atStartOfDay().toInstant(ZoneOffset.UTC);

        Account account = mock(Account.class);
        TransactionResponse firstTransactionResponse = mock(TransactionResponse.class);
        List<TransactionResponse.Transaction> transactions = Arrays.asList(mock(TransactionResponse.Transaction.class));
        ProviderAccountDTO expectedResult = mock(ProviderAccountDTO.class);

        when(firstTransactionResponse.getNextPageUrl()).thenReturn(null);
        when(firstTransactionResponse.getTransactions()).thenReturn(transactions);
        when(httpClient.getAccountForGivenLogicalId("SOME_LOGICAL_ID", "BELFIUS_ACCESS_TOKEN")).thenReturn(account);
        when(httpClient.getTransactionsForGivenLogicalId("SOME_LOGICAL_ID", "BELFIUS_ACCESS_TOKEN", from)).thenReturn(firstTransactionResponse);
        when(mapper.mapToProviderAccountDTO(account, transactions, "SOME_LOGICAL_ID")).thenReturn(expectedResult);

        //when
        DataProviderResponse result = service.fetchData(httpClient, accessMeans, from);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getAccounts()).hasSize(1);
        verify(mapper).mapToProviderAccountDTO(account, transactions, "SOME_LOGICAL_ID");

    }

    @Test
    public void shouldCorrectlyFetchDataForAccountWithTwoPageTransaction() throws TokenInvalidException, ProviderFetchDataException {
        //given
        BelfiusGroupAccessToken token = new BelfiusGroupAccessToken();
        token.setAccessToken("BELFIUS_ACCESS_TOKEN");
        token.setRefreshToken("BELFIUS_REFRESH_TOKEN");
        token.setExpiresIn(3600L);
        token.setLogicalId("SOME_LOGICAL_ID");

        BelfiusGroupAccessMeans accessMeans = new BelfiusGroupAccessMeans(
                token,
                "en",
                "SOME_REDIRECT"
        );

        Instant from = LocalDate.parse("2016-04-17").atStartOfDay().toInstant(ZoneOffset.UTC);

        Account account = mock(Account.class);
        TransactionResponse firstTransactionResponse = mock(TransactionResponse.class);
        List<TransactionResponse.Transaction> firstPageTransactions = Arrays.asList(mock(TransactionResponse.Transaction.class));
        List<TransactionResponse.Transaction> secondPageTransactions = Arrays.asList(mock(TransactionResponse.Transaction.class));
        List<TransactionResponse.Transaction> allTransactions = ListUtils.union(firstPageTransactions, secondPageTransactions);
        TransactionResponse secondTransactionResponse = mock(TransactionResponse.class);
        ProviderAccountDTO firstMappedPageTransactions = mock(ProviderAccountDTO.class);

        when(firstTransactionResponse.getNextPageUrl()).thenReturn("NEXT_PAGE");
        when(firstTransactionResponse.getTransactions()).thenReturn(firstPageTransactions);
        when(secondTransactionResponse.getNextPageUrl()).thenReturn("");
        when(secondTransactionResponse.getTransactions()).thenReturn(secondPageTransactions);
        when(httpClient.getAccountForGivenLogicalId("SOME_LOGICAL_ID", "BELFIUS_ACCESS_TOKEN")).thenReturn(account);
        when(httpClient.getTransactionsForGivenLogicalId("SOME_LOGICAL_ID", "BELFIUS_ACCESS_TOKEN", from)).thenReturn(firstTransactionResponse);
        when(httpClient.getTransactionsNextPageForGivenLogicalId("SOME_LOGICAL_ID", "BELFIUS_ACCESS_TOKEN", "NEXT_PAGE")).thenReturn(secondTransactionResponse);
        when(mapper.mapToProviderAccountDTO(account, allTransactions, "SOME_LOGICAL_ID")).thenReturn(firstMappedPageTransactions);

        //when
        DataProviderResponse result = service.fetchData(httpClient, accessMeans, from);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getAccounts()).hasSize(1);
        verify(mapper).mapToProviderAccountDTO(account, allTransactions, "SOME_LOGICAL_ID");
    }

    @Test
    public void shouldThrowProviderFetchDataExceptionWithFailAccountIfRuntimeErrorWillOccurDuringHttpCall() throws TokenInvalidException {
        //given
        BelfiusGroupAccessToken token = new BelfiusGroupAccessToken();
        token.setAccessToken("BELFIUS_ACCESS_TOKEN");
        token.setRefreshToken("BELFIUS_REFRESH_TOKEN");
        token.setExpiresIn(3600L);
        token.setLogicalId("SOME_LOGICAL_ID");

        BelfiusGroupAccessMeans accessMeans = new BelfiusGroupAccessMeans(token, "en", "SOME_REDIRECT");

        Instant from = LocalDate.parse("2016-04-17").atStartOfDay().toInstant(ZoneOffset.UTC);

        when(httpClient.getAccountForGivenLogicalId("SOME_LOGICAL_ID", "BELFIUS_ACCESS_TOKEN")).thenThrow(RestClientException.class);

        // when
        Throwable thrown = catchThrowable(() -> service.fetchData(httpClient, accessMeans, from));

        // then
        assertThat(thrown).isInstanceOf(ProviderFetchDataException.class);
    }

    @Test
    public void shouldThrowTokenInvalidExceptionWhenItOccursDuringHttpCall() throws TokenInvalidException {
        //given
        BelfiusGroupAccessToken token = new BelfiusGroupAccessToken();
        token.setAccessToken("BELFIUS_ACCESS_TOKEN");
        token.setRefreshToken("BELFIUS_REFRESH_TOKEN");
        token.setExpiresIn(3600L);
        token.setLogicalId("SOME_LOGICAL_ID");

        BelfiusGroupAccessMeans accessMeans = new BelfiusGroupAccessMeans(token, "en", "SOME_REDIRECT");

        Instant from = LocalDate.parse("2016-04-17").atStartOfDay().toInstant(ZoneOffset.UTC);

        when(httpClient.getAccountForGivenLogicalId("SOME_LOGICAL_ID", "BELFIUS_ACCESS_TOKEN")).thenThrow(TokenInvalidException.class);

        // when
        Throwable thrown = catchThrowable(() -> service.fetchData(httpClient, accessMeans, from));

        // then
        assertThat(thrown).isInstanceOf(TokenInvalidException.class);
    }
}
