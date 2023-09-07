package com.yolt.providers.openbanking.ais.monzogroup.common.service.ais.fetchdataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.JsonParseException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountMapper;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoSampleTypedAuthMeansV2;
import com.yolt.providers.openbanking.ais.monzogroup.common.auth.MonzoGroupAuthMeansMapper;
import com.yolt.providers.openbanking.ais.monzogroup.common.service.ais.potmapper.MonzoGroupPotMapperV2;
import com.yolt.providers.openbanking.ais.monzogroup.common.service.restclient.MonzoGroupAisRestClientV3;
import com.yolt.providers.openbanking.ais.monzogroup.monzo.configuration.MonzoPropertiesV2;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code.CURRENTACCOUNT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MonzoDataProviderV5ConsentWindowTest {

    private static final int A_YEAR_AGO = 365;
    private static final long CONSENT_WINDOW_THRESHOLD = 5;
    private static final long ALLOWED_TRANSACTIONS_HISTORY_AFTER_CONSENT_WINDOW = 89;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final Duration CONSENT_WINDOW_DURATION = Duration.ofMinutes(5);
    private static final String PROVIDER_KEY = "MONZO";
    private static final Clock clock = Clock.systemUTC();
    private static MonzoGroupFetchDataServiceV4 fetchDataService;
    private static AccessMeans EXCEEDED_CONSENT_WINDOW_MONZO_ACCESS_MEANS;
    private static AccessMeans NOT_EXCEEDED_CONSENT_WINDOW_MONZO_ACCESS_MEANS;
    private static Map<String, BasicAuthenticationMean> authMeans;
    @Mock
    private MonzoGroupAisRestClientV3 restClient;
    @Mock
    private HttpClient httpClient;
    @Mock
    private Function<OBTransaction6, ProviderTransactionDTO> transactionMapper;
    @Mock
    private Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper;
    @Mock
    private Function<OBStandingOrder6, StandingOrderDTO> standingOrdersMapper;
    @Mock
    private AccountMapper accountMapper;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    @Mock
    private MonzoPropertiesV2 properties;

    @BeforeAll
    public static void setup() throws IOException, URISyntaxException {
        EXCEEDED_CONSENT_WINDOW_MONZO_ACCESS_MEANS = new AccessMeans(
                Instant.now().minus(CONSENT_WINDOW_THRESHOLD + 1, ChronoUnit.MINUTES),
                USER_ID,
                "Az90SAOJklae",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);
        NOT_EXCEEDED_CONSENT_WINDOW_MONZO_ACCESS_MEANS = new AccessMeans(
                Instant.now().minus(CONSENT_WINDOW_THRESHOLD - 1, ChronoUnit.MINUTES),
                USER_ID,
                "Az90SAOJklae",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);
        authMeans = new MonzoSampleTypedAuthMeansV2().getAuthenticationMeans();
    }

    @Test
    public void shouldNarrowFetchStartTimeWhenConsentWindowExceeded()
            throws JsonParseException, TokenInvalidException, ProviderFetchDataException {

        // given
        OBReadAccount6 obReadAccountResponse = prepareResponse();
        when(restClient.fetchAccounts(any(), eq("/v3.1/aisp/accounts"), any(), any(), any()))
                .thenReturn(obReadAccountResponse, obReadAccountResponse);

        OBReadTransaction6 obReadTransactionResponse = new OBReadTransaction6().data(new OBReadDataTransaction6());
        String threeMonthsFromNow = DATE_TIME_FORMATTER.format(OffsetDateTime
                .ofInstant(Instant.now().minus(ALLOWED_TRANSACTIONS_HISTORY_AFTER_CONSENT_WINDOW, ChronoUnit.DAYS), ZoneOffset.UTC));
        when(restClient.fetchTransactions(any(), matches(".+fromBookingDateTime=" + threeMonthsFromNow + ".+"),
                any(), any(), any())).thenReturn(obReadTransactionResponse, obReadTransactionResponse);

        DefaultAuthMeans authenticationMeans = (new MonzoGroupAuthMeansMapper())
                .getAuthMeansMapper(PROVIDER_KEY).apply(authMeans);

        ProviderAccountDTO resp = ProviderAccountDTO.builder().build();
        when(accountMapper.mapToProviderAccount(any(), any(), any(), any(), any())).thenReturn(resp, resp);

        fetchDataService = getFetchDataService(properties);

        // when
        DataProviderResponse dataProviderResponse = fetchDataService.getAccountsAndTransactions(
                httpClient,
                authenticationMeans,
                Instant.now().minus(A_YEAR_AGO, ChronoUnit.DAYS),
                EXCEEDED_CONSENT_WINDOW_MONZO_ACCESS_MEANS);

        // then
        assertThat(dataProviderResponse).isNotNull();
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
    }

    @Test
    public void shouldNotNarrowFetchStartTimeWhenConsentWindowNotExceeded()
            throws JsonParseException, TokenInvalidException, ProviderFetchDataException {

        // given
        OBReadAccount6 obReadAccountResponse = prepareResponse();
        when(restClient.fetchAccounts(any(), eq("/v3.1/aisp/accounts"), any(), any(), any()))
                .thenReturn(obReadAccountResponse, obReadAccountResponse);

        OBReadTransaction6 obReadTransactionResponse = new OBReadTransaction6().data(new OBReadDataTransaction6());
        String aYearFromNow = DATE_TIME_FORMATTER.format(OffsetDateTime
                .ofInstant(Instant.now().minus(A_YEAR_AGO, ChronoUnit.DAYS), ZoneOffset.UTC));
        when(restClient.fetchTransactions(any(), matches(".+fromBookingDateTime=" + aYearFromNow + ".+"),
                any(), any(), any())).thenReturn(obReadTransactionResponse, obReadTransactionResponse);

        DefaultAuthMeans authenticationMeans = (new MonzoGroupAuthMeansMapper())
                .getAuthMeansMapper(PROVIDER_KEY).apply(authMeans);

        ProviderAccountDTO resp = ProviderAccountDTO.builder().build();
        when(accountMapper.mapToProviderAccount(any(), any(), any(), any(), any())).thenReturn(resp, resp);

        fetchDataService = getFetchDataService(properties);

        // when
        DataProviderResponse dataProviderResponse = fetchDataService.getAccountsAndTransactions(
                httpClient,
                authenticationMeans,
                Instant.now().minus(A_YEAR_AGO, ChronoUnit.DAYS),
                NOT_EXCEEDED_CONSENT_WINDOW_MONZO_ACCESS_MEANS);

        // then
        assertThat(dataProviderResponse).isNotNull();
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
    }

    private OBReadAccount6 prepareResponse() {
        OBReadAccount6Data obAccountData = new OBReadAccount6Data();
        OBAccount6 obAccount = new OBAccount6();
        obAccount.setAccountSubType(CURRENTACCOUNT);
        obAccountData.setAccount(List.of(obAccount));
        Links linksData = new Links();
        OBReadAccount6 accountGETResponse = new OBReadAccount6()
                .data(obAccountData)
                .links(linksData);

        return accountGETResponse;
    }

    private MonzoGroupFetchDataServiceV4 getFetchDataService(DefaultProperties properties) {
        return new MonzoGroupFetchDataServiceV4(restClient,
                properties,
                transactionMapper,
                directDebitMapper,
                standingOrdersMapper,
                accountMapper,
                new DefaultAccountFilter(),
                new DefaultSupportedAccountsSupplier(),
                CONSENT_WINDOW_DURATION,
                ENDPOINT_VERSION,
                new MonzoGroupPotMapperV2(clock),
                clock);
    }
}
