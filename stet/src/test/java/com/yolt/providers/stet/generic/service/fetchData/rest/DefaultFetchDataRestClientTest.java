package com.yolt.providers.stet.generic.service.fetchData.rest;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.dto.*;
import com.yolt.providers.stet.generic.dto.account.StetAccountsResponseDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalancesResponseDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import com.yolt.providers.stet.generic.http.error.ExecutionSupplier;
import com.yolt.providers.stet.generic.http.error.HttpErrorHandler;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.DefaultFetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataHttpHeadersFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;

@ExtendWith(MockitoExtension.class)
class DefaultFetchDataRestClientTest {

    private static final String ACCESS_TOKEN = "24521c70-a947-47f7-b45a-7d5495588c3e";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    @Mock
    private FetchDataHttpHeadersFactory headersFactory;

    @Mock
    private HttpErrorHandler errorHandler;

    @Mock
    private HttpClient httpClient;

    @Mock
    private Signer signer;

    @Captor
    private ArgumentCaptor<ExecutionSupplier<?>> executionSupplierArgumentCaptor;

    @Captor
    private ArgumentCaptor<ExecutionInfo> executionInfoArgumentCaptor;

    @InjectMocks
    private DefaultFetchDataRestClient restClient;

    @Test
    void shouldReturnCorrectResponseForGettingAccounts() throws TokenInvalidException {
        // given
        String endpoint = "/accounts";
        DataRequest dataRequest = createDataRequest();
        HttpHeaders expectedHeaders = createHttpHeaders();
        StetAccountsResponseDTO expectedAccountsResponseDTO = createStetAccountsResponseDTO();
        ExecutionInfo expectedExecutionInfo = new ExecutionInfo(endpoint, GET, expectedHeaders, GET_ACCOUNTS);

        when(headersFactory.createFetchDataHeaders(anyString(), any(DataRequest.class), any(HttpMethod.class)))
                .thenReturn(createHttpHeaders());
        when(errorHandler.executeAndHandle(ArgumentMatchers.<ExecutionSupplier<StetAccountsResponseDTO>>any(), any(ExecutionInfo.class)))
                .thenReturn(expectedAccountsResponseDTO);

        // when
        StetAccountsResponseDTO accountsResponseDTO = restClient.getAccounts(httpClient, endpoint, dataRequest);

        // then
        assertThat(accountsResponseDTO).isEqualTo(expectedAccountsResponseDTO);
        verify(headersFactory).createFetchDataHeaders(endpoint, dataRequest, GET);
        verify(errorHandler).executeAndHandle(executionSupplierArgumentCaptor.capture(), executionInfoArgumentCaptor.capture());
        assertThat(executionSupplierArgumentCaptor.getValue()).isNotNull();
        assertThat(executionInfoArgumentCaptor.getValue()).isEqualToComparingFieldByField(expectedExecutionInfo);
    }

    @Test
    void shouldReturnCorrectResponseForGettingBalances() throws TokenInvalidException {
        // given
        String endpoint = "/account/1/balances";
        DataRequest dataRequest = createDataRequest();
        HttpHeaders expectedHeaders = createHttpHeaders();
        StetBalancesResponseDTO expectedBalancesResponseDTO = createStetBalancesResponseDTO();
        ExecutionInfo expectedExecutionInfo = new ExecutionInfo(endpoint, GET, expectedHeaders, GET_BALANCES_BY_ACCOUNT_ID);

        when(headersFactory.createFetchDataHeaders(anyString(), any(DataRequest.class), any(HttpMethod.class)))
                .thenReturn(expectedHeaders);
        when(errorHandler.executeAndHandle(ArgumentMatchers.<ExecutionSupplier<StetBalancesResponseDTO>>any(), any(ExecutionInfo.class)))
                .thenReturn(expectedBalancesResponseDTO);

        // when
        StetBalancesResponseDTO accountsResponseDTO = restClient.getBalances(httpClient, endpoint, dataRequest);

        // then
        assertThat(accountsResponseDTO).isEqualTo(expectedBalancesResponseDTO);
        verify(headersFactory).createFetchDataHeaders(endpoint, dataRequest, GET);
        verify(errorHandler).executeAndHandle(executionSupplierArgumentCaptor.capture(), executionInfoArgumentCaptor.capture());
        assertThat(executionSupplierArgumentCaptor.getValue()).isNotNull();
        assertThat(executionInfoArgumentCaptor.getValue()).isEqualToComparingFieldByField(expectedExecutionInfo);
    }

    @Test
    void shouldReturnCorrectResponseForGettingTransactions() throws TokenInvalidException {
        // given
        String endpoint = "/account/1/transactions";
        DataRequest dataRequest = createDataRequest();
        HttpHeaders expectedHeaders = createHttpHeaders();
        StetTransactionsResponseDTO expectedTransactionsResponseDTO = createStetTransactionsResponseDTO();
        ExecutionInfo expectedExecutionInfo = new ExecutionInfo(endpoint, GET, expectedHeaders, GET_TRANSACTIONS_BY_ACCOUNT_ID);

        when(headersFactory.createFetchDataHeaders(anyString(), any(DataRequest.class), any(HttpMethod.class)))
                .thenReturn(expectedHeaders);
        when(errorHandler.executeAndHandle(ArgumentMatchers.<ExecutionSupplier<StetTransactionsResponseDTO>>any(), any(ExecutionInfo.class)))
                .thenReturn(expectedTransactionsResponseDTO);

        // when
        StetTransactionsResponseDTO transactionsResponseDTO = restClient.getTransactions(httpClient, endpoint, dataRequest);

        // then
        assertThat(transactionsResponseDTO).isEqualTo(expectedTransactionsResponseDTO);
        verify(headersFactory).createFetchDataHeaders(endpoint, dataRequest, GET);
        verify(errorHandler).executeAndHandle(executionSupplierArgumentCaptor.capture(), executionInfoArgumentCaptor.capture());
        assertThat(executionSupplierArgumentCaptor.getValue()).isNotNull();
        assertThat(executionInfoArgumentCaptor.getValue()).isEqualToComparingFieldByField(expectedExecutionInfo);
    }

    @Test
    void shouldReturnCorrectResponseForConsentUpdate() throws TokenInvalidException {
        // given
        String endpoint = "/consents";
        DataRequest dataRequest = createDataRequest();
        Map<String, Object> body = createUpdateConsentRequestBody();
        HttpHeaders expectedHeaders = createHttpHeaders();
        ExecutionInfo expectedExecutionInfo = new ExecutionInfo(endpoint, PUT, expectedHeaders, "update_consent_for_accounts");

        when(headersFactory.createFetchDataHeaders(anyString(), any(DataRequest.class), any(HttpMethod.class)))
                .thenReturn(expectedHeaders);
        when(errorHandler.executeAndHandle(ArgumentMatchers.<ExecutionSupplier<ResponseEntity<Void>>>any(), any(ExecutionInfo.class)))
                .thenReturn(null);

        // when
        ResponseEntity<Void> emptyResponse = restClient.updateConsent(httpClient, endpoint, dataRequest, body);

        // then
        assertThat(emptyResponse).isNull();
        verify(headersFactory).createFetchDataHeaders(endpoint, dataRequest, PUT);
        verify(errorHandler).executeAndHandle(executionSupplierArgumentCaptor.capture(), executionInfoArgumentCaptor.capture());
        assertThat(executionSupplierArgumentCaptor.getValue()).isNotNull();
        assertThat(executionInfoArgumentCaptor.getValue()).isEqualToComparingFieldByField(expectedExecutionInfo);
    }

    private DataRequest createDataRequest() {
        DefaultAuthenticationMeans authMeans = DefaultAuthenticationMeans.builder()
                .clientId("9641371c-5a06-42a8-86b8-5eb1ee0ff3ab")
                .clientSecret("f917c8f4-0d2f-46be-b854-88c2bad103c8")
                .build();

        return new DataRequest("https://test.com", signer, authMeans, ACCESS_TOKEN, PSU_IP_ADDRESS, false);
    }

    private Map<String, Object> createUpdateConsentRequestBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("psuIdentity", "true");
        return body;
    }

    private StetAccountsResponseDTO createStetAccountsResponseDTO() {
        return TestStetAccountsResponseDTO.builder()
                .accounts(Collections.singletonList(TestStetAccountDTO.builder()
                        .resourceId("AccountId")
                        .build()))
                .build();
    }

    private StetBalancesResponseDTO createStetBalancesResponseDTO() {
        return TestStetBalancesResponseDTO.builder()
                .balances(Collections.singletonList(TestStetBalanceDTO.builder()
                        .name("BalanceName")
                        .build()))
                .build();
    }

    private StetTransactionsResponseDTO createStetTransactionsResponseDTO() {
        return TestStetTransactionsResponseDTO.builder()
                .transactions(Collections.singletonList(TestStetTransactionDTO.builder()
                        .entryReference("EntryRereference")
                        .build()))
                .build();
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(ACCESS_TOKEN);
        return headers;
    }
}
