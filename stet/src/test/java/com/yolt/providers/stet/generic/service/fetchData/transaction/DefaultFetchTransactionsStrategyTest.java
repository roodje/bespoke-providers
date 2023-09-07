package com.yolt.providers.stet.generic.service.fetchData.transaction;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.dto.TestPaginationDTO;
import com.yolt.providers.stet.generic.dto.TestStetTransactionDTO;
import com.yolt.providers.stet.generic.dto.TestStetTransactionsResponseDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionStatus;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.DefaultFetchTransactionsStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultFetchTransactionsStrategyTest {

    private static final String BASE_URL = "http://localhost/";
    private static final String ACCESS_TOKEN = "9ab42e62-247f-4b94-8ae7-7157874033d9";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final Instant FETCH_START_TIME = Instant.now().minus(30, ChronoUnit.DAYS);

    @Mock
    private FetchDataRestClient restClient;

    @Mock
    private DefaultProperties properties;

    @Mock
    private HttpClient httpClient;

    @Mock
    private Signer signer;

    @Captor
    private ArgumentCaptor<DataRequest> dataRequestArgumentCaptor;

    private DateTimeSupplier dateTimeSupplier;
    private DefaultFetchTransactionsStrategy fetchTransactionsStrategy;

    @BeforeEach
    void initialize() {
        dateTimeSupplier = Mockito.spy(new DateTimeSupplier(Clock.systemUTC()));
        fetchTransactionsStrategy = new DefaultFetchTransactionsStrategy(restClient, dateTimeSupplier, properties);
    }

    @Test
    void shouldReturnListOfTransactionsForFetchTransactionsWhenSinglePageOfTransactionsIsReturnedFromAPI() throws TokenInvalidException {
        // given
        DataRequest dataRequest = createDataRequest();
        StetTransactionsResponseDTO expectedTransactions = createHalTransactionsDTO(false);

        when(properties.getPaginationLimit())
                .thenReturn(100);
        when(restClient.getTransactions(any(HttpClient.class), anyString(), any(DataRequest.class)))
                .thenReturn(expectedTransactions);

        // when
        List<StetTransactionDTO> result = fetchTransactionsStrategy.fetchTransactions(httpClient, "/transactions", dataRequest, FETCH_START_TIME);

        // then
        assertThat(result).containsExactlyInAnyOrderElementsOf(expectedTransactions.getTransactions());

        verify(dateTimeSupplier)
                .convertToLocalDate(FETCH_START_TIME);
        verify(properties)
                .getPaginationLimit();
        verify(restClient)
                .getTransactions(eq(httpClient), eq(applyDateQueryParams("/transactions")), dataRequestArgumentCaptor.capture());

        DataRequest capturedDataRequest = dataRequestArgumentCaptor.getValue();
        assertThat(capturedDataRequest).isEqualTo(dataRequest);
    }

    @Test
    void shouldReturnListOfTransactionsForFetchTransactionsWhenMoreThanOnePagesOfTransactionsIsReturnedFromAPI() throws TokenInvalidException {
        // given
        DataRequest dataRequest = createDataRequest();
        StetTransactionsResponseDTO expectedFirstPage = createHalTransactionsDTO(true);
        StetTransactionsResponseDTO expectedSecondPage = createHalTransactionsDTO(false);

        when(properties.getPaginationLimit())
                .thenReturn(100);
        when(restClient.getTransactions(any(HttpClient.class), anyString(), any(DataRequest.class)))
                .thenReturn(expectedFirstPage)
                .thenReturn(expectedSecondPage);

        // when
        List<StetTransactionDTO> result = fetchTransactionsStrategy.fetchTransactions(httpClient, "/accounts/1/transactions", dataRequest, FETCH_START_TIME);

        // then
        assertThat(result)
                .containsAll(expectedFirstPage.getTransactions())
                .containsAll(expectedSecondPage.getTransactions());

        verify(dateTimeSupplier)
                .convertToLocalDate(FETCH_START_TIME);
        verify(properties, times(2))
                .getPaginationLimit();
        verify(restClient)
                .getTransactions(eq(httpClient), eq(applyDateQueryParams("/accounts/1/transactions")), dataRequestArgumentCaptor.capture());
        verify(restClient)
                .getTransactions(eq(httpClient), eq("/accounts/1/transactions/next"), dataRequestArgumentCaptor.capture());

        DataRequest capturedDataRequest = dataRequestArgumentCaptor.getValue();
        assertThat(capturedDataRequest).isEqualTo(dataRequest);
    }

    @Test
    void shouldReturnOnlyLimitedPagesOfTransactionsForFetchTransactionsWhenMoreThanOnePagesOfTransactionsIsReturnedFromAPIAndPaginationLimitIsReached() throws TokenInvalidException {
        // given
        DataRequest dataRequest = createDataRequest();
        StetTransactionsResponseDTO expectedFirstPage = createHalTransactionsDTO(true);
        StetTransactionsResponseDTO expectedSecondPage = createHalTransactionsDTO(false);

        when(properties.getPaginationLimit())
                .thenReturn(1);
        when(restClient.getTransactions(any(HttpClient.class), anyString(), any(DataRequest.class)))
                .thenReturn(expectedFirstPage)
                .thenReturn(expectedSecondPage);

        // when
        List<StetTransactionDTO> result = fetchTransactionsStrategy.fetchTransactions(httpClient, "/accounts/1/transactions", dataRequest, FETCH_START_TIME);

        // then
        assertThat(result)
                .containsAll(expectedFirstPage.getTransactions())
                .doesNotContainAnyElementsOf(expectedSecondPage.getTransactions());

        verify(dateTimeSupplier)
                .convertToLocalDate(FETCH_START_TIME);
        verify(properties, times(2))
                .getPaginationLimit();
        verify(restClient)
                .getTransactions(eq(httpClient), eq(applyDateQueryParams("/accounts/1/transactions")), dataRequestArgumentCaptor.capture());

        DataRequest capturedDataRequest = dataRequestArgumentCaptor.getValue();
        assertThat(capturedDataRequest).isEqualTo(dataRequest);
    }

    @Test
    public void shouldReturnTransactionsWithoutPendingAndInfo() throws TokenInvalidException {
        StetTransactionsResponseDTO variousTransactionsResponse = createVariousTransactionsDTO();
        //given
        DataRequest dataRequest = createDataRequest();

        when(properties.getPaginationLimit())
                .thenReturn(1);
        when(restClient.getTransactions(eq(httpClient), anyString(), eq(dataRequest)))
                .thenReturn(variousTransactionsResponse);

        // when
        List<StetTransactionDTO> result = fetchTransactionsStrategy.fetchTransactions(httpClient, "/accounts/1/transactions", dataRequest, FETCH_START_TIME);

        //then
        List<StetTransactionDTO> pendingTransactions = result.stream()
                .filter(transaction -> StetTransactionStatus.PDNG.equals(transaction.getStatus()))
                .collect(Collectors.toList());
        List<StetTransactionDTO> infoTransactions = result.stream()
                .filter(transaction -> StetTransactionStatus.INFO.equals(transaction.getStatus()))
                .collect(Collectors.toList());

        assertThat(result).hasSize(2);
        assertThat(pendingTransactions).isEmpty();
        assertThat(infoTransactions).isEmpty();
    }

    private StetTransactionsResponseDTO createVariousTransactionsDTO() {
        return TestStetTransactionsResponseDTO.builder()
                .transactions(List.of(TestStetTransactionDTO.builder()
                                .resourceId("1")
                                .status(StetTransactionStatus.BOOK)
                                .build(),
                        TestStetTransactionDTO.builder()
                                .resourceId("2")
                                .status(StetTransactionStatus.INFO)
                                .build(),
                        TestStetTransactionDTO.builder()
                                .resourceId("3")
                                .status(StetTransactionStatus.PDNG)
                                .build(),
                        TestStetTransactionDTO.builder()
                                .resourceId("4")
                                .status(StetTransactionStatus.OTHR)
                                .build()))
                .links(null)
                .build();
    }

    private DataRequest createDataRequest() {
        DefaultAuthenticationMeans authMeans = DefaultAuthenticationMeans.builder().build();
        return new DataRequest(BASE_URL, signer, authMeans, ACCESS_TOKEN, PSU_IP_ADDRESS, false);
    }

    private StetTransactionsResponseDTO createHalTransactionsDTO(boolean containsNextPage) {
        return TestStetTransactionsResponseDTO.builder()
                .transactions(Collections.singletonList(TestStetTransactionDTO.builder()
                        .resourceId(containsNextPage ? "2" : "1")
                        .status(StetTransactionStatus.BOOK)
                        .build()))
                .links(containsNextPage ? TestPaginationDTO.builder()
                        .next("/accounts/1/transactions/next")
                        .build() : null)
                .build();
    }

    private String applyDateQueryParams(String endpoint) {
        LocalDateTime fetchStartTime = LocalDateTime.ofInstant(FETCH_START_TIME, ZoneId.of("Europe/Paris"));
        return UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("dateFrom", fetchStartTime.toLocalDate())
                .queryParam("dateTo", LocalDate.now())
                .toUriString();
    }
}
