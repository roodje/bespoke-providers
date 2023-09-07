package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.AccountResponse;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Transaction;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.TransactionResponse;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.FetchDataResult;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.RaiffeisenAtGroupProviderState;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.DefaultRaiffeisenAtGroupHttpClient;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans.DefaultRaiffeisenAtGroupProviderStateMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans.ProviderStateProcessingException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultRaiffeisenAtGroupFetchDataServiceTest {

    @Mock
    DefaultRaiffeisenAtGroupTokenService tokenService;
    @Mock
    DefaultRaiffeisenAtGroupProviderStateMapper providerStateMapper;

    @Mock
    AccountResponse accountResponse;
    @Mock
    Account account1;
    @Mock
    Account account2;
    @Mock
    TransactionResponse transactionResponseForAccount1;
    @Mock
    Transaction bookedTransaction1ForAccount1;
    @Mock
    Transaction bookedTransaction2ForAccount1;
    @Mock
    Transaction pendingTransactionForAccount1;
    @Mock
    TransactionResponse transactionResponseForAccount1NextPage;
    @Mock
    Transaction bookedTransaction3ForAccount1;
    @Mock
    TransactionResponse transactionResponseForAccount2;
    @Mock
    Transaction bookedTransactionForAccount2;

    DefaultRaiffeisenAtGroupFetchDataService fetchDataService;

    @BeforeEach
    void setUp() {
        given(accountResponse.getAccounts())
                .willReturn(List.of(account1, account2));
        given(account1.getResourceId()).willReturn("1");
        given(account2.getResourceId()).willReturn("2");
        given(transactionResponseForAccount1.getBookedTransactions())
                .willReturn(List.of(bookedTransaction1ForAccount1, bookedTransaction2ForAccount1));
        given(transactionResponseForAccount1.getPendingTransactions())
                .willReturn(List.of(pendingTransactionForAccount1));
        given(transactionResponseForAccount1.getNextHref())
                .willReturn("https://nextPage.com");
        given(transactionResponseForAccount1NextPage.getBookedTransactions())
                .willReturn(List.of(bookedTransaction3ForAccount1));
        given(transactionResponseForAccount1NextPage.getPendingTransactions())
                .willReturn(Collections.emptyList());
        given(transactionResponseForAccount2.getBookedTransactions())
                .willReturn(List.of(bookedTransactionForAccount2));
        given(transactionResponseForAccount2.getPendingTransactions())
                .willReturn(Collections.emptyList());

        fetchDataService = new DefaultRaiffeisenAtGroupFetchDataService(providerStateMapper, tokenService, 100);
    }

    @Test
    void shouldFetchData() throws ProviderStateProcessingException, TokenInvalidException, ProviderFetchDataException {
        //given
        var httpClient = mock(DefaultRaiffeisenAtGroupHttpClient.class);
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, UUID.randomUUID().toString());
        var providerState = "THE-PROVIDER-STATE";
        var psuIpAddress = "127.0.0.1";
        var consentId = "THE-CONSENT_ID";
        var clientAccessToken = "THE-CLIENT-ACCESS-TOKEN";
        var transactionsFetchStartTime = Instant.now().minus(365, ChronoUnit.DAYS);
        given(providerStateMapper.deserialize(providerState))
                .willReturn(new RaiffeisenAtGroupProviderState(consentId));
        given(tokenService.createClientCredentialToken(httpClient, authenticationMeans))
                .willReturn(clientAccessToken);
        given(httpClient.fetchAccounts(clientAccessToken, consentId, psuIpAddress))
                .willReturn(accountResponse);
        given(httpClient.fetchTransaction("1", clientAccessToken, consentId, psuIpAddress, transactionsFetchStartTime))
                .willReturn(transactionResponseForAccount1);
        given(httpClient.fetchTransaction("https://nextPage.com", clientAccessToken, consentId, psuIpAddress))
                .willReturn(transactionResponseForAccount1NextPage);
        given(httpClient.fetchTransaction("2", clientAccessToken, consentId, psuIpAddress, transactionsFetchStartTime))
                .willReturn(transactionResponseForAccount2);
        var expectedFetchDataResult = new FetchDataResult();
        expectedFetchDataResult.addResources(account1, List.of(bookedTransaction1ForAccount1, bookedTransaction2ForAccount1, bookedTransaction3ForAccount1), List.of(pendingTransactionForAccount1));
        expectedFetchDataResult.addResources(account2, List.of(bookedTransactionForAccount2), Collections.emptyList());

        //when
        var result = fetchDataService.fetchData(httpClient, authenticationMeans, providerState, psuIpAddress, transactionsFetchStartTime);

        //then
        assertThat(result).isEqualTo(expectedFetchDataResult);
    }

    @Test
    void shouldThrowProviderFetchDataExceptionWhenSomethingGoesWrongDuringDeserializingProviderState() throws ProviderStateProcessingException {
        //given
        var httpClient = mock(DefaultRaiffeisenAtGroupHttpClient.class);
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, UUID.randomUUID().toString());
        var providerState = "THE-PROVIDER-STATE";
        var psuIpAddress = "127.0.0.1";
        var transactionsFetchStartTime = Instant.now().minus(365, ChronoUnit.DAYS);
        var mappingException = new ProviderStateProcessingException("Something goes wrong during mapping", new IOException());
        given(providerStateMapper.deserialize(providerState))
                .willThrow(mappingException);

        //when
        ThrowableAssert.ThrowingCallable call = () -> fetchDataService.fetchData(httpClient, authenticationMeans, providerState, psuIpAddress, transactionsFetchStartTime);

        //then
        assertThatExceptionOfType(ProviderFetchDataException.class)
                .isThrownBy(call)
                .withMessage("Failed fetching data")
                .withCause(mappingException);
    }

    @Test
    void shouldReThrowTokenInvalidExceptionWhenItWillBeThrownByHttpClient() throws ProviderStateProcessingException, TokenInvalidException {
        //given
        var httpClient = mock(DefaultRaiffeisenAtGroupHttpClient.class);
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, UUID.randomUUID().toString());
        var providerState = "THE-PROVIDER-STATE";
        var psuIpAddress = "127.0.0.1";
        var consentId = "THE-CONSENT_ID";
        var clientAccessToken = "THE-CLIENT-ACCESS-TOKEN";
        var transactionsFetchStartTime = Instant.now().minus(365, ChronoUnit.DAYS);
        given(providerStateMapper.deserialize(providerState))
                .willReturn(new RaiffeisenAtGroupProviderState(consentId));
        given(tokenService.createClientCredentialToken(httpClient, authenticationMeans))
                .willReturn(clientAccessToken);
        given(httpClient.fetchAccounts(clientAccessToken, consentId, psuIpAddress))
                .willThrow(TokenInvalidException.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> fetchDataService.fetchData(httpClient, authenticationMeans, providerState, psuIpAddress, transactionsFetchStartTime);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call);
    }
}