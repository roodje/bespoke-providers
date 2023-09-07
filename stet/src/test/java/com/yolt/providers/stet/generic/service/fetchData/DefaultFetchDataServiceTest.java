package com.yolt.providers.stet.generic.service.fetchData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.dto.*;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountsResponseDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.AccountMapper;
import com.yolt.providers.stet.generic.mapper.providerstate.DefaultProviderStateMapper;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.mapper.transaction.TransactionMapper;
import com.yolt.providers.stet.generic.service.fetchdata.DefaultFetchDataService;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.request.FetchDataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.FetchTransactionsStrategy;
import lombok.SneakyThrows;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultFetchDataServiceTest {

    private static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String ACCESS_TOKEN = "b6b4ffe8-5e60-4ef9-858f-78b48c5d7b11";

    private DefaultFetchDataService fetchDataService;

    @Mock
    private Signer signer;

    @Mock
    private FetchDataRestClient restClient;

    @Mock
    private FetchAccountsStrategy fetchAccountsStrategy;

    @Mock
    private FetchTransactionsStrategy fetchTransactionsStrategy;

    @Mock
    private FetchBalancesStrategy fetchBalancesStrategy;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private HttpClient httpClient;

    @Captor
    private ArgumentCaptor<DataRequest> dataRequestArgumentCaptor;

    @BeforeEach
    void initialize() {
        DefaultProperties properties = new DefaultProperties();
        properties.setPaginationLimit(2);

        ProviderStateMapper providerStateMapper = new DefaultProviderStateMapper(new ObjectMapper());
        fetchDataService = new DefaultFetchDataService(
                restClient,
                providerStateMapper,
                fetchAccountsStrategy,
                fetchTransactionsStrategy,
                fetchBalancesStrategy,
                new DateTimeSupplier(Clock.systemUTC()),
                accountMapper,
                transactionMapper,
                properties);
    }

    @Test
    void shouldFetchData() throws TokenInvalidException, ProviderFetchDataException {
        // given
        StetAccountDTO accountResource = createAccountResourceDTO();
        List<StetBalanceDTO> balanceResources = createBalanceResourceDTOs();
        StetTransactionsResponseDTO halTransactions = createHalTransactionsDTO();
        ProviderAccountDTO providerAccountDTO = createProviderAccountDTO();
        List<ProviderTransactionDTO> providerTransactionDTOs = createProviderTransactionDTOs();
        List<ProviderAccountNumberDTO> providerAccountNumberDTOs = createProviderAccountNumberDTOs();
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();
        Instant transactionsFetchStartTime = Instant.parse("2021-01-01T10:00:00.0Z");
        FetchDataRequest fetchDataRequest = createFetchDataRequest(authMeans, transactionsFetchStartTime);

        when(fetchAccountsStrategy.fetchAccounts(any(HttpClient.class), anyString(), anyString(), any(DataRequest.class)))
                .thenReturn((List<StetAccountDTO>) createHalAccountsDTO(accountResource).getAccounts());
        when(fetchBalancesStrategy.fetchBalances(any(HttpClient.class), anyString(), any(DataRequest.class), any(StetAccountDTO.class)))
                .thenReturn(balanceResources);
        when(fetchTransactionsStrategy.fetchTransactions(any(HttpClient.class), anyString(), any(DataRequest.class), any(Instant.class)))
                .thenReturn((List<StetTransactionDTO>) halTransactions.getTransactions());
        when(transactionMapper.mapToProviderTransactionDTOs(anyList()))
                .thenReturn(providerTransactionDTOs);
        when(accountMapper.mapToProviderAccountDTO(any(StetAccountDTO.class), anyList(), anyList()))
                .thenReturn(providerAccountDTO);

        // when
        DataProviderResponse response = fetchDataService.getAccountsAndTransactions(httpClient, fetchDataRequest);

        // then
        assertThat(response.getAccounts()).containsExactly(providerAccountDTO);

        verify(fetchAccountsStrategy).fetchAccounts(eq(httpClient), eq("/accounts"), eq("/consents"), dataRequestArgumentCaptor.capture());
        DataRequest capturedDataRequest = dataRequestArgumentCaptor.getValue();
        assertThat(capturedDataRequest).extracting(
                DataRequest::getAccessToken,
                DataRequest::getAuthMeans,
                DataRequest::getBaseUrl,
                DataRequest::getPsuIpAddress,
                DataRequest::getSigner
        ).contains(ACCESS_TOKEN, authMeans, "http://localhost", PSU_IP_ADDRESS, signer);
        verify(fetchBalancesStrategy)
                .fetchBalances(eq(httpClient), eq("/accounts/1/balances"), dataRequestArgumentCaptor.capture(), eq(accountResource));
        verify(fetchTransactionsStrategy).fetchTransactions(eq(httpClient), eq("/accounts/1/transactions"), dataRequestArgumentCaptor.capture(), eq(transactionsFetchStartTime));
        verify(transactionMapper)
                .mapToProviderTransactionDTOs((List<StetTransactionDTO>) halTransactions.getTransactions());
        verify(accountMapper)
                .mapToProviderAccountDTO(accountResource, balanceResources, providerTransactionDTOs);
    }

    @Test
    void shouldThrowProviderFetchDataExceptionWhileFetchingAccounts() throws TokenInvalidException {
        // given
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();
        FetchDataRequest fetchDataRequest = createFetchDataRequest(authMeans);

        when(fetchAccountsStrategy.fetchAccounts(any(HttpClient.class), anyString(), anyString(), any(DataRequest.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // when
        ThrowingCallable throwingCallable = () -> fetchDataService.getAccountsAndTransactions(httpClient, fetchDataRequest);

        // then
        assertThatThrownBy(throwingCallable)
                .isInstanceOf(ProviderFetchDataException.class);

        verify(fetchAccountsStrategy)
                .fetchAccounts(eq(httpClient), eq("/accounts"), eq("/consents"), dataRequestArgumentCaptor.capture());

        DataRequest capturedDataRequest = dataRequestArgumentCaptor.getValue();
        assertThat(capturedDataRequest).extracting(
                DataRequest::getAccessToken,
                DataRequest::getAuthMeans,
                DataRequest::getBaseUrl,
                DataRequest::getPsuIpAddress,
                DataRequest::getSigner
        ).contains(ACCESS_TOKEN, authMeans, "http://localhost", PSU_IP_ADDRESS, signer);
    }

    @Test
    void shouldThrowProviderFetchDataExceptionWhileFetchingBalances() throws TokenInvalidException {
        // given
        StetAccountDTO accountResource = createAccountResourceDTO();
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();
        FetchDataRequest fetchDataRequest = createFetchDataRequest(authMeans);

        when(fetchAccountsStrategy.fetchAccounts(any(HttpClient.class), anyString(), anyString(), any(DataRequest.class)))
                .thenReturn((List<StetAccountDTO>) createHalAccountsDTO(accountResource).getAccounts());
        when(fetchBalancesStrategy.fetchBalances(any(HttpClient.class), anyString(), any(DataRequest.class), any(StetAccountDTO.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // when
        ThrowingCallable throwingCallableFetchBalances = () -> fetchDataService.getAccountsAndTransactions(httpClient, fetchDataRequest);

        // then
        assertThatThrownBy(throwingCallableFetchBalances)
                .isInstanceOf(ProviderFetchDataException.class);

        verify(fetchAccountsStrategy)
                .fetchAccounts(eq(httpClient), eq("/accounts"), eq("/consents"), dataRequestArgumentCaptor.capture());
        DataRequest capturedDataRequest = dataRequestArgumentCaptor.getValue();
        assertThat(capturedDataRequest).extracting(
                DataRequest::getAccessToken,
                DataRequest::getAuthMeans,
                DataRequest::getBaseUrl,
                DataRequest::getPsuIpAddress,
                DataRequest::getSigner
        ).contains(ACCESS_TOKEN, authMeans, "http://localhost", PSU_IP_ADDRESS, signer);
        verify(fetchBalancesStrategy)
                .fetchBalances(eq(httpClient), eq("/accounts/1/balances"), dataRequestArgumentCaptor.capture(), eq(accountResource));
    }

    @Test
    void shouldThrowProviderFetchDataExceptionWhileFetchingTransactions() throws TokenInvalidException {
        // given
        StetAccountDTO accountResource = createAccountResourceDTO();
        List<StetBalanceDTO> balanceResources = createBalanceResourceDTOs();
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();
        Instant transactionsFetchStartTime = Instant.parse("2021-01-01T10:00:00.0Z");
        FetchDataRequest fetchDataRequest = createFetchDataRequest(authMeans, transactionsFetchStartTime);

        when(fetchAccountsStrategy.fetchAccounts(any(HttpClient.class), anyString(), anyString(), any(DataRequest.class)))
                .thenReturn((List<StetAccountDTO>) createHalAccountsDTO(accountResource).getAccounts());
        when(fetchBalancesStrategy.fetchBalances(any(HttpClient.class), anyString(), any(DataRequest.class), any(StetAccountDTO.class)))
                .thenReturn(balanceResources);
        when(fetchTransactionsStrategy.fetchTransactions(any(HttpClient.class), anyString(), any(DataRequest.class), any(Instant.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // when
        ThrowingCallable throwingCallableFetchTransactions = () ->
                fetchDataService.getAccountsAndTransactions(httpClient, fetchDataRequest);

        // then
        assertThatThrownBy(throwingCallableFetchTransactions)
                .isInstanceOf(ProviderFetchDataException.class);

        verify(fetchAccountsStrategy)
                .fetchAccounts(eq(httpClient), eq("/accounts"), eq("/consents"), dataRequestArgumentCaptor.capture());
        DataRequest capturedDataRequest = dataRequestArgumentCaptor.getValue();
        assertThat(capturedDataRequest).extracting(
                DataRequest::getAccessToken,
                DataRequest::getAuthMeans,
                DataRequest::getBaseUrl,
                DataRequest::getPsuIpAddress,
                DataRequest::getSigner
        ).contains(ACCESS_TOKEN, authMeans, "http://localhost", PSU_IP_ADDRESS, signer);
        verify(fetchBalancesStrategy)
                .fetchBalances(eq(httpClient), eq("/accounts/1/balances"), dataRequestArgumentCaptor.capture(), eq(accountResource));
        verify(fetchTransactionsStrategy)
                .fetchTransactions(eq(httpClient), eq("/accounts/1/transactions"), dataRequestArgumentCaptor.capture(), eq(transactionsFetchStartTime));
    }

    private DefaultAuthenticationMeans createAuthenticationMeans() {
        return DefaultAuthenticationMeans.builder()
                .signingKeyIdHeader("HeaderKeyId")
                .clientSigningKeyId(UUID.randomUUID())
                .clientSigningCertificate(readCertificate())
                .build();
    }

    @SneakyThrows
    private X509Certificate readCertificate() {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        String certificatePem = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
        return KeyUtil.createCertificateFromPemFormat(certificatePem);
    }

    private StetAccountsResponseDTO createHalAccountsDTO(StetAccountDTO account) {
        return TestStetAccountsResponseDTO.builder()
                .accounts(Collections.singletonList(account))
                .build();
    }

    private StetAccountDTO createAccountResourceDTO() {
        return TestStetAccountDTO.builder()
                .resourceId("1")
                .build();
    }

    private List<StetBalanceDTO> createBalanceResourceDTOs() {
        return Collections.singletonList(TestStetBalanceDTO.builder()
                .type(StetBalanceType.XPCD)
                .build());


    }

    private StetTransactionsResponseDTO createHalTransactionsDTO() {
        return TestStetTransactionsResponseDTO.builder()
                .transactions(Collections.singletonList(TestStetTransactionDTO.builder()
                        .resourceId("2")
                        .build()))
                .links(TestPaginationDTO.builder()
                        .next("/accounts/1/transactions/next")
                        .build())
                .build();
    }

    private ProviderAccountDTO createProviderAccountDTO() {
        return ProviderAccountDTO.builder()
                .accountId("1")
                .build();
    }

    private List<ProviderTransactionDTO> createProviderTransactionDTOs() {
        return Collections.singletonList(ProviderTransactionDTO.builder()
                .externalId("2")
                .build());
    }

    private List<ProviderAccountNumberDTO> createProviderAccountNumberDTOs() {
        return Collections.singletonList(new ProviderAccountNumberDTO(IBAN, "FR5512739000305286995875D46"));
    }

    private Region prepareRegion() {
        Region region = new Region();
        region.setBaseUrl("http://localhost");
        region.setAuthUrl("/auth");
        region.setTokenUrl("/token");
        region.setName("Region 1");
        region.setCode("REGION1");
        return region;
    }

    private FetchDataRequest createFetchDataRequest(DefaultAuthenticationMeans authMeans) {
        return createFetchDataRequest(authMeans, Instant.parse("2021-01-01T10:00:00.0Z"));
    }

    private FetchDataRequest createFetchDataRequest(DefaultAuthenticationMeans authMeans, Instant fetchStartTime) {
        return new FetchDataRequest(
                DataProviderState.authorizedProviderState(prepareRegion(), ACCESS_TOKEN, null),
                fetchStartTime,
                signer,
                authMeans,
                PSU_IP_ADDRESS);
    }
}
