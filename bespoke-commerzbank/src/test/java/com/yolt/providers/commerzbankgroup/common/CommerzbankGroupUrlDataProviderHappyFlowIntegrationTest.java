package com.yolt.providers.commerzbankgroup.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.commerzbankgroup.TestApp;
import com.yolt.providers.commerzbankgroup.TestRestTemplateManager;
import com.yolt.providers.commerzbankgroup.common.authentication.CommerzbankGroupAccessMeans;
import com.yolt.providers.commerzbankgroup.common.authentication.CommerzbankGroupProviderState;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/happy-flow", httpsPort = 0, port = 0)
class CommerzbankGroupUrlDataProviderHappyFlowIntegrationTest {

    public static final String SERIALIZED_ACCESS_MEANS = "{\"accessToken\":\"SOME_ACCESS_TOKEN\",\"refreshToken\":\"SOME_REFRESH_TOKEN\",\"consentId\":\"1234-wertiq-983\"}";
    public static final String SERIALIZED_PROVIDER_STATE = "{\"codeVerifier\":\"7eOZvM0GkQuWos4KG1N89I7Ie4nPf6cFcZk6Pr6ZAmt1\",\"consentId\":\"1234-wertiq-983\"}";

    @Autowired
    @Qualifier("CommerzbankProvider")
    private UrlDataProvider commerzbankGroupUrlDataProvider;

    @Autowired
    private Clock clock;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private TestRestTemplateManager restTemplateManager;

    public Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(commerzbankGroupUrlDataProvider);
    }

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory);
        authenticationMeans = CommerzbankGroupSampleAuthenticationMeans.getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRedirectStepOnGetLoginInfo(UrlDataProvider dataProvider) throws JsonProcessingException {
        // given
        String redirectUrl = "https://yolt.com/callback-acc";

        String state = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(redirectUrl)
                .setState(state)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        Step step = dataProvider.getLoginInfo(urlGetLogin);

        // then
        var redirectStep = assertInstanceOf(RedirectStep.class, step);

        assertThat(redirectStep.getRedirectUrl())
                .startsWith("https://bogus-authorization.com/berlingroup/authorize/31f68ab6-1ce6-4131-a324-3f37d2ca4b02")
                .contains("client_id=PSDNL-DNB-33031431")
                .contains("scope=AIS%3A1234-wertiq-983")
                .contains("state=")
                .contains("code_challenge=")
                .contains("code_challenge_method=S256")
                .contains("response_type=code");

        var providerState = redirectStep.getProviderState();
        assertThat(providerState)
                .isNotBlank();
        var commerzbankGroupProviderState = objectMapper.readValue(providerState, CommerzbankGroupProviderState.class);
        assertThat(commerzbankGroupProviderState.codeVerifier()).isNotBlank();
        assertThat(commerzbankGroupProviderState.consentId()).isEqualTo("1234-wertiq-983"); // consent id
    }


    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnAccessToken(UrlDataProvider dataProvider) {
        // given
        var redirectUrl = "https://www.yolt.com/callback";
        var redirectUrlPostedBackFromSite = "https://www.yolt.com/callback?code=SOME_CODE&state=SOME_STATE";
        var randomUser = UUID.randomUUID();

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setUserId(randomUser)
                .setBaseClientRedirectUrl(redirectUrl)
                .setRedirectUrlPostedBackFromSite(redirectUrlPostedBackFromSite)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(SERIALIZED_PROVIDER_STATE)
                .setState(UUID.randomUUID().toString())
                .build();

        // when
        var accessMeansOrStepDTO = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        var accessMeans = accessMeansOrStepDTO.getAccessMeans();
        var serializedAccessMeans = accessMeans.getAccessMeans();
        assertThat(serializedAccessMeans).isEqualTo("{\"accessToken\":\"SOME_ACCESS_TOKEN\",\"refreshToken\":\"SOME_REFRESH_TOKEN\",\"consentId\":\"1234-wertiq-983\"}");
        assertThat(accessMeans.getUpdated()).isEqualTo(Instant.now(clock));
        assertThat(accessMeans.getUserId()).isEqualTo(randomUser);
        assertThat(accessMeans.getExpireTime()).isEqualTo(Instant.now(clock).plusSeconds(3600));
    }


    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldRefreshAccessToken(UrlDataProvider dataProvider) throws TokenInvalidException, JsonProcessingException {
        // given
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setAccessMeans(UUID.randomUUID(), SERIALIZED_ACCESS_MEANS, Date.from(Instant.now(clock)), Date.from(Instant.now(clock)))
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        var accessMeansOrStepDTO = dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        var accessMeans = accessMeansOrStepDTO.getAccessMeans();
        assertNotNull(accessMeans);
        var commerzbankGroupAccessMeans = objectMapper.readValue(accessMeans, CommerzbankGroupAccessMeans.class);
        var accessToken = commerzbankGroupAccessMeans.accessToken();
        var refreshToken = commerzbankGroupAccessMeans.refreshToken();
        var consentId = commerzbankGroupAccessMeans.consentId();

        assertThat(accessToken).isEqualTo("SOME_ACCESS_TOKEN");
        assertThat(refreshToken).isEqualTo("SOME_REFRESH_TOKEN");
        assertThat(consentId).isEqualTo("1234-wertiq-983");
    }


    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldFetchAndMapData(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setUserId(UUID.randomUUID())
                .setPsuIpAddress("192.168.16.5")
                .setTransactionsFetchStartTime(Instant.now(clock))
                .setAuthenticationMeans(authenticationMeans)
                .setAccessMeans(UUID.randomUUID(), SERIALIZED_ACCESS_MEANS, Date.from(Instant.now(clock)), Date.from(Instant.now(clock)))
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        var dataProviderResponse = dataProvider.fetchData(urlFetchDataRequest);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(2);
        ProviderAccountDTO account1 = getCurrentAccountById(dataProviderResponse, "3dc3d5b3-7023-4848-9853-f5400a64e80f");
        validateAccount1(account1);
        validateExtendedAccount1(account1.getExtendedAccount());
        validateTransactions1(account1.getTransactions());
        ProviderAccountDTO account2 = getCurrentAccountById(dataProviderResponse, "3dc3d5b3-7023-4848-9853-f5400a64e81e");
        validateAccount2(account2);
        validateExtendedAccount2(account2.getExtendedAccount());
        validateTransactions2(account2.getTransactions());
    }

    private void validateAccount1(ProviderAccountDTO account1) {
        assertThat(account1.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(account1.getName()).isEqualTo("Main Account");
        assertThat(account1.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(account1.getAvailableBalance()).isEqualTo("500.00");
        assertThat(account1.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(account1.getAccountNumber().getIdentification()).isEqualTo("DE2310010010123456789");
    }

    private void validateExtendedAccount1(ExtendedAccountDTO extendedAccount1) {
        assertThat(extendedAccount1.getResourceId()).isEqualTo("3dc3d5b3-7023-4848-9853-f5400a64e80f");
        assertThat(extendedAccount1.getAccountReferences()).hasSize(1);
        assertThat(extendedAccount1.getAccountReferences().get(0).getValue()).isEqualTo("DE2310010010123456789");
        assertThat(extendedAccount1.getAccountReferences().get(0).getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(extendedAccount1.getProduct()).isEqualTo("Girokonto");
        assertThat(extendedAccount1.getCashAccountType()).isEqualTo(ExternalCashAccountType.fromCode("CACC"));
        assertThat(extendedAccount1.getBalances()).hasSize(1);
        assertThat(extendedAccount1.getBalances().get(0).getBalanceType()).isEqualTo(BalanceType.CLOSING_BOOKED);
        assertThat(extendedAccount1.getBalances().get(0).getBalanceAmount().getAmount()).isEqualTo("500.00");
        assertThat(extendedAccount1.getBalances().get(0).getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedAccount1.getBalances().get(0).getReferenceDate()).isEqualTo(ZonedDateTime.of(2017, 10, 25, 0, 0, 0, 0, ZoneId.of("Europe/Berlin")));
    }

    private void validateTransactions1(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions).hasSize(4);
        validateTransaction1(transactions.get(0));
        validateTransaction2(transactions.get(1));
        validateTransaction1(transactions.get(2));
    }

    private void validateTransaction1(ProviderTransactionDTO providerTransactionDTO) {
        assertThat(providerTransactionDTO.getExternalId()).isEqualTo("1234567");
        assertThat(providerTransactionDTO.getAmount()).isEqualTo("256.67");
        assertThat(providerTransactionDTO.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(providerTransactionDTO.getDateTime()).isEqualTo(ZonedDateTime.of(2017, 10, 25, 0, 0, 0, 0, ZoneId.of("Europe/Berlin")));
        validateExtendedTransaction1(providerTransactionDTO.getExtendedTransaction());
    }

    private void validateExtendedTransaction1(ExtendedTransactionDTO extendedTransactionDTO) {
        assertThat(extendedTransactionDTO.getEntryReference()).isEqualTo("1234567");
        assertThat(extendedTransactionDTO.getCreditorName()).isEqualTo("John Miles");
        assertThat(extendedTransactionDTO.getCreditorAccount().getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(extendedTransactionDTO.getCreditorAccount().getValue()).isEqualTo("DE67100100101306118605");
        assertThat(extendedTransactionDTO.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransactionDTO.getTransactionAmount().getAmount()).isEqualTo("256.67");
        assertThat(extendedTransactionDTO.getBookingDate()).isEqualTo(ZonedDateTime.of(2017, 10, 25, 0, 0, 0, 0, ZoneId.of("Europe/Berlin")));
        assertThat(extendedTransactionDTO.getValueDate()).isEqualTo(ZonedDateTime.of(2017, 10, 26, 0, 0, 0, 0, ZoneId.of("Europe/Berlin")));
        assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isEqualTo("Example 1");
    }

    private void validateTransaction2(ProviderTransactionDTO providerTransactionDTO) {
        assertThat(providerTransactionDTO.getExternalId()).isEqualTo("1234568");
        assertThat(providerTransactionDTO.getAmount()).isEqualTo("343.01");
        assertThat(providerTransactionDTO.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(providerTransactionDTO.getDateTime()).isEqualTo(ZonedDateTime.of(2017, 10, 25, 0, 0, 0, 0, ZoneId.of("Europe/Berlin")));
        validateExtendedTransaction2(providerTransactionDTO.getExtendedTransaction());
    }

    private void validateExtendedTransaction2(ExtendedTransactionDTO extendedTransactionDTO) {
        assertThat(extendedTransactionDTO.getEntryReference()).isEqualTo("1234568");
        assertThat(extendedTransactionDTO.getDebtorName()).isEqualTo("Paul Simpson");
        assertThat(extendedTransactionDTO.getDebtorAccount().getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(extendedTransactionDTO.getDebtorAccount().getValue()).isEqualTo("NL76RABO0359400371");
        assertThat(extendedTransactionDTO.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransactionDTO.getTransactionAmount().getAmount()).isEqualTo("343.01");
        assertThat(extendedTransactionDTO.getBookingDate()).isEqualTo(ZonedDateTime.of(2017, 10, 25, 0, 0, 0, 0, ZoneId.of("Europe/Berlin")));
        assertThat(extendedTransactionDTO.getValueDate()).isEqualTo(ZonedDateTime.of(2017, 10, 26, 0, 0, 0, 0, ZoneId.of("Europe/Berlin")));
        assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isEqualTo("Example 2");
    }

    private void validateAccount2(ProviderAccountDTO account2) {
        assertThat(account2.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(account2.getName()).isEqualTo("US Dollar Account");
        assertThat(account2.getCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(account2.getAvailableBalance()).isEqualTo("500.00");
        assertThat(account2.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(account2.getAccountNumber().getIdentification()).isEqualTo("DE2310010010123456788");
    }

    private void validateExtendedAccount2(ExtendedAccountDTO extendedAccount2) {
        assertThat(extendedAccount2.getResourceId()).isEqualTo("3dc3d5b3-7023-4848-9853-f5400a64e81e");
        assertThat(extendedAccount2.getAccountReferences()).hasSize(1);
        assertThat(extendedAccount2.getAccountReferences().get(0).getValue()).isEqualTo("DE2310010010123456788");
        assertThat(extendedAccount2.getAccountReferences().get(0).getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(extendedAccount2.getProduct()).isEqualTo("Fremdwährungskonto");
        assertThat(extendedAccount2.getCashAccountType()).isEqualTo(ExternalCashAccountType.fromCode("CACC"));
        assertThat(extendedAccount2.getBalances()).hasSize(1);
        assertThat(extendedAccount2.getBalances().get(0).getBalanceType()).isEqualTo(BalanceType.CLOSING_BOOKED);
        assertThat(extendedAccount2.getBalances().get(0).getBalanceAmount().getAmount()).isEqualTo("500.00");
        assertThat(extendedAccount2.getBalances().get(0).getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedAccount2.getBalances().get(0).getReferenceDate()).isEqualTo(ZonedDateTime.of(2017, 10, 25, 0, 0, 0, 0, ZoneId.of("Europe/Berlin")));
    }

    private void validateTransactions2(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions).hasSize(0);
    }

    private ProviderAccountDTO getCurrentAccountById(DataProviderResponse response, String accountId) {
        return response.getAccounts().stream()
                .filter(account -> account.getAccountId().equals(accountId))
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }
}
