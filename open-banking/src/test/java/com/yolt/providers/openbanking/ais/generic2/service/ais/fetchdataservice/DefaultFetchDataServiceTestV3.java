package com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice;

import com.yolt.providers.common.exception.JsonParseException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeansBuilder;
import com.yolt.providers.openbanking.ais.generic2.configuration.GenericTestProperties;
import com.yolt.providers.openbanking.ais.generic2.configuration.auth.GenericSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.frombookingdatetimeformatter.DefaultFromBookingDateTimeFormatter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.*;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.DefaultSupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.amount.DefaultAmountParser;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.DefaultBalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balancetype.DefaultBalanceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.creditcard.CreditCardMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency.DefaultCurrencyMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedbalance.DefaultExtendedBalancesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.parties.DefaultPartiesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.period.DefaultPeriodMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.DefaultSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultDirectDebitMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultStandingOrderMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.PartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code.CURRENTACCOUNT;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultFetchDataServiceTestV3 {

    private final DefaultProperties properties = GenericTestProperties.generateTestProperties();
    private final Clock clock = Clock.systemUTC();
    @Mock
    PartiesRestClient partiesRestClient;
    @Mock
    DefaultTransactionMapper defaultTransactionMapper;
    private DefaultFetchDataServiceV3 fetchDataService;
    private DefaultAuthMeans authMeans;
    @Mock
    private RestClient restClient;
    @Mock
    private HttpClient httpClient;

    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        authMeans = new DefaultAuthMeansBuilder().createAuthenticationMeans(
                new GenericSampleTypedAuthenticationMeans().getAuthenticationMeans(),
                "TEST_IMPL_OPENBANKING");
        ZoneId zoneId = ZoneId.of("Europe/London");
        DefaultDateTimeMapper dateTimeMapper = new DefaultDateTimeMapper(zoneId);
        DefaultAccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        DefaultCurrencyMapper currencyCodeMapper = new DefaultCurrencyMapper();
        DefaultAmountParser amountParser = new DefaultAmountParser();
        DefaultSchemeMapper schemeMapper = new DefaultSchemeMapper();
        DefaultBalanceMapper balanceMapper = new DefaultBalanceMapper();

        fetchDataService = new DefaultFetchDataServiceV3(restClient, partiesRestClient, properties,
                defaultTransactionMapper,
                new DefaultDirectDebitMapper(zoneId, amountParser),
                new DefaultStandingOrderMapper(new DefaultPeriodMapper(), amountParser, schemeMapper, dateTimeMapper),
                new DefaultPartiesMapper(),
                new DefaultAccountMapperV3(
                        () -> Arrays.asList(INTERIMBOOKED),
                        () -> Arrays.asList(INTERIMAVAILABLE),
                        () -> Arrays.asList(OPENINGCLEARED),
                        () -> Arrays.asList(FORWARDAVAILABLE),
                        currencyCodeMapper,
                        new DefaultAccountIdMapper(),
                        new DefaultAccountTypeMapper(),
                        new CreditCardMapper(),
                        new DefaultAccountNumberMapperV2(schemeMapper),
                        new DefaultAccountNameMapper(account -> "Test implementation Open Banking Account"),
                        balanceMapper,
                        new DefaultExtendedAccountMapper(
                                accountReferenceTypeMapper,
                                currencyCodeMapper,
                                new DefaultExtendedBalancesMapper(new DefaultBalanceAmountMapper(
                                        currencyCodeMapper,
                                        new DefaultBalanceMapper()),
                                        new DefaultBalanceTypeMapper(),
                                        zoneId)),
                        new DefaultSupportedSchemeAccountFilter(),
                        clock),
                new DefaultFromBookingDateTimeFormatter(),
                new DefaultAccountFilter(),
                new DefaultSupportedAccountsSupplier(),
                DefaultConsentWindow.DURATION,
                "",
                clock);
    }

    @Test
    public void shouldThrowProviderFetchDataExceptionWithSingleFailedAccountForGetAccountsAndTransactionsWhenSomethingWentWrong() throws JsonParseException, TokenInvalidException {
        // given
        when(restClient.fetchAccounts(any(), any(), any(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "error-json".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        // when
        ThrowableAssert.ThrowingCallable getAccountsAndTransactionsCallable = () -> fetchDataService.getAccountsAndTransactions(httpClient, authMeans, null, new AccessMeansState(new AccessMeans(), Collections.emptyList()));

        // then
        assertThatThrownBy(getAccountsAndTransactionsCallable)
                .isInstanceOf(ProviderFetchDataException.class)
                .hasMessage("Failed fetching data");
    }

    @Test
    public void shouldReturnAllBalancesByTypeForFetchBalancesWithExpectedBalance() throws Exception {
        // given
        when(restClient.fetchBalances(any(), any(), any(), any(), any()))
                .thenReturn(createBalanceResponse(INTERIMAVAILABLE, INTERIMBOOKED));

        // when
        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType = fetchDataService.fetchBalances(httpClient,
                new AccessMeans(), "extId", authMeans);

        // then
        assertThat(balancesByType.keySet()).containsExactlyInAnyOrder(INTERIMAVAILABLE, INTERIMBOOKED);
    }

    @Test
    public void shouldReturnAllBalancesByTypeWithExpectedBalanceForFetchBalancesForCreditCard() throws Exception {
        // given
        when(restClient.fetchBalances(any(), any(), any(), any(), any()))
                .thenReturn(createBalanceResponse(FORWARDAVAILABLE, OPENINGCLEARED));

        // when
        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType = fetchDataService.fetchBalances(httpClient
                , new AccessMeans(), "extId", authMeans);

        // then
        assertThat(balancesByType.keySet()).containsExactlyInAnyOrder(FORWARDAVAILABLE, OPENINGCLEARED);
    }

    @Test
    public void shouldReturnEmptyMapForFetchBalancesWhenMoreThanOnePageIsLoaded() throws Exception {
        // given
        properties.setPaginationLimit(2);
        OBReadBalance1 response = createBalanceResponse(INTERIMAVAILABLE, INTERIMBOOKED);
        response.setLinks(new Links().self("self").next("next"));
        when(restClient.fetchBalances(any(), any(), any(), any(), any()))
                .thenReturn(response);

        // when
        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balances = fetchDataService.fetchBalances(httpClient, new AccessMeans(), "extId", authMeans);
        // then
        assertThat(balances).isEmpty();
    }

    @Test
    public void shouldReturnMappedBalancesForFetchBalancesWhenExpectedBalanceIsNotThere() throws Exception {
        // given
        when(restClient.fetchBalances(any(), any(), any(), any(), any()))
                .thenReturn(createBalanceResponse(OPENINGBOOKED, EXPECTED));

        // when
        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balances = fetchDataService.fetchBalances(httpClient, new AccessMeans(), "extId", authMeans);

        // then
        assertThat(balances.keySet()).contains(OPENINGBOOKED, EXPECTED);
    }

    @Test
    public void shouldReturnEmptyTransactionsForGetAllTransactionsWhenNoTransactionsInResponse() throws Exception {
        // given
        OBReadTransaction6 response = new OBReadTransaction6()
                .data(new OBReadDataTransaction6());
        when(restClient.fetchTransactions(any(), any(), any(), any(), any()))
                .thenReturn(response, response);
        Instant instant = Instant.now().minus(1, ChronoUnit.DAYS);

        // when
        List<ProviderTransactionDTO> result = fetchDataService.getAllTransactions(CURRENTACCOUNT, httpClient, new AccessMeans(),
                "extId", instant, authMeans);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnTransactionsForGetAllTransactionsWhenLinksIsNullInResponse() throws Exception {
        // given
        ProviderTransactionDTO mappedTransaction = ProviderTransactionDTO.builder().build();

        OBReadTransaction6 transaction = new OBReadTransaction6()
                .data(
                        new OBReadDataTransaction6().addTransactionItem(new OBTransaction6())
                );
        when(restClient.fetchTransactions(any(), any(), any(), any(), any()))
                .thenReturn(transaction, transaction);
        when(defaultTransactionMapper.apply(any()))
                .thenReturn(mappedTransaction, mappedTransaction);

        Instant instant = Instant.now().minus(1, ChronoUnit.DAYS);

        // when
        List<ProviderTransactionDTO> result = fetchDataService.getAllTransactions(CURRENTACCOUNT, httpClient, new AccessMeans(),
                "extId", instant, authMeans);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    public void shouldReturnEmptyMapForFetchBalancesWhenNullInExpectedBalance() throws Exception {
        // given
        when(restClient.fetchBalances(any(), any(), any(), any(), any()))
                .thenReturn(createBalanceResponseWithNull());

        // when
        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balances = fetchDataService.fetchBalances(httpClient, new AccessMeans(), "extId", authMeans);

        // then
        assertThat(balances).isEmpty();
    }

    @Test
    public void shouldReturnEmptyMapForFetchBalancesWhenBalancesResponseIsNull() throws Exception {
        // given
        when(restClient.fetchBalances(any(), any(), any(), any(), any()))
                .thenReturn(null);

        // when
        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balances = fetchDataService.fetchBalances(httpClient, new AccessMeans(), "extId", authMeans);

        // then
        assertThat(balances).isEmpty();
    }

    @Test
    public void shouldReturnEmptyMapForFetchBalancesWhenBalanceDataIsNull() throws Exception {
        // given
        when(restClient.fetchBalances(any(), any(), any(), any(), any()))
                .thenReturn(createBalanceResponseWithNullData());

        // when
        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balances = fetchDataService.fetchBalances(httpClient, new AccessMeans(), "extId", authMeans);

        // then
        assertThat(balances).isEmpty();
    }

    @Test
    public void shouldReturnMappedPartyDto() throws TokenInvalidException {
        //given
        AccessMeans accessMeans = new AccessMeans();
        accessMeans.setCreated(Instant.now());
        AccessMeansState accessMeansState = new AccessMeansState(accessMeans, List.of("ReadParty"));
        when(partiesRestClient.callForParties(httpClient, "/aisp/accounts/extId/parties", accessMeans, authMeans, OBReadParty3.class)).thenReturn(createPartyResponse());
        PartyDto expectedParty = new PartyDto("Some Party Name");

        //when
        List<PartyDto> receivedResponse = fetchDataService.getParties(httpClient, accessMeansState, "extId", authMeans);

        //then
        assertThat(receivedResponse).hasSize(1);
        assertThat(receivedResponse.get(0)).isEqualTo(expectedParty);
    }

    @Test
    public void shouldReturnEmptyListWhenErrorOccurs() throws TokenInvalidException {
        //given
        AccessMeans accessMeans = new AccessMeans();
        accessMeans.setCreated(Instant.now());
        AccessMeansState accessMeansState = new AccessMeansState(accessMeans, List.of("ReadParty"));
        when(partiesRestClient.callForParties(httpClient, "/aisp/accounts/extId/parties", accessMeans, authMeans, OBReadParty3.class))
                .thenThrow(new HttpServerErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized", "error-json".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        //when
        List<PartyDto> receivedResponse = fetchDataService.getParties(httpClient, accessMeansState, "extId", authMeans);

        //then
        assertThat(receivedResponse).hasSize(0);
    }

    @Test
    public void shouldReturnEmptyListWhenThereIsNotPermissionForReadParties() throws TokenInvalidException {
        //given
        AccessMeans accessMeans = new AccessMeans();
        accessMeans.setCreated(Instant.now());
        AccessMeansState accessMeansState = new AccessMeansState(accessMeans, Collections.emptyList());

        //when
        List<PartyDto> receivedResponse = fetchDataService.getParties(httpClient, accessMeansState, "extId", authMeans);

        //then
        assertThat(receivedResponse).hasSize(0);
    }

    @Test
    public void shouldReturnEmptyListWhenIsOutsideOfConsentWindow() throws TokenInvalidException {
        //given
        AccessMeans accessMeans = new AccessMeans();
        accessMeans.setCreated(Instant.now().minus(Duration.ofDays(365001)));
        AccessMeansState accessMeansState = new AccessMeansState(accessMeans, Collections.emptyList());

        //when
        List<PartyDto> receivedResponse = fetchDataService.getParties(httpClient, accessMeansState, "extId", authMeans);

        //then
        assertThat(receivedResponse).hasSize(0);
    }

    private OBReadParty3 createPartyResponse() {
        return new OBReadParty3()
                .data(new OBReadParty3Data()
                        .party(List.of(new OBParty2()
                                .partyId("some party id")
                                .name("Some Party Name")
                                .partyType(OBExternalPartyType1Code.SOLE)
                                .fullLegalName("Some full Legal name"))))
                .links(new Links());
    }

    private OBReadBalance1 createBalanceResponse(OBBalanceType1Code... balanceTypes) {
        return new OBReadBalance1()
                .data(new OBReadBalance1Data()
                        .balance(Arrays.stream(balanceTypes)
                                .map(t -> new OBReadBalance1DataBalance().type(t))
                                .collect(toList())))
                .links(new Links());
    }

    private OBReadBalance1 createBalanceResponseWithNull() {
        return new OBReadBalance1()
                .data(new OBReadBalance1Data()
                        .balance(null))
                .links(new Links());
    }

    private OBReadBalance1 createBalanceResponseWithNullData() {
        return new OBReadBalance1()
                .data(null)
                .links(new Links());
    }
}
