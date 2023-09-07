package com.yolt.providers.direkt1822group.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.direkt1822group.Direkt1822GroupSampleAuthenticationMeans;
import com.yolt.providers.direkt1822group.TestApp;
import com.yolt.providers.direkt1822group.common.dto.Direkt1822GroupLoginFormDTO;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.TextField;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * This test contains all happy flows occurring in 1822Direkt group providers.
 * <p>
 * Covered flows:
 * - acquiring login form
 * - acquiring consent page
 * - access means creation
 * - forcing reconsent on refresh access means
 * - consent deletion
 * <p>
 * Providers: ALL 1822Direkt group providers
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("direkt1822group")
@AutoConfigureWireMock(stubs = "classpath:/mappings/direkt1822group/ais/happy-flow", httpsPort = 0, port = 0)
class Direkt1822GroupDataProviderHappyFlowTest {

    @Autowired
    @Qualifier("Direkt1822DataProvider")
    private Direkt1822GroupDataProvider direkt1822GroupDataProvider;

    @Autowired
    @Qualifier("Direkt1822GroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplateManager restTemplateManager;

    Stream<Direkt1822GroupDataProvider> direkt1822GroupDataProviders() {
        return Stream.of(direkt1822GroupDataProvider);
    }

    private static final Map<String, BasicAuthenticationMean> TEST_AUTHENTICATION_MEANS = Direkt1822GroupSampleAuthenticationMeans.get();
    private static final String TEST_PSU_IP_ADDRESS = "12.34.56.78";
    private static final String TEST_IBAN = "de 9110 0000 0001 2345 6789";

    @ParameterizedTest
    @MethodSource("direkt1822GroupDataProviders")
    public void shouldReturnFormStepOnGetLoginInfo(Direkt1822GroupDataProvider dataProvider) {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://www.yolt.com/callback-test").setState(UUID.randomUUID().toString())
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();

        // when
        FormStep formStep = (FormStep) dataProvider.getLoginInfo(urlGetLogin);

        // then
        TextField textField = (TextField) formStep.getForm().getFormComponents().get(0);
        assertThat(textField.getId()).isEqualTo("Iban");
        assertThat(textField.getDisplayName()).isEqualTo("IBAN");
        assertThat(textField.getLength()).isEqualTo(34);
        assertThat(textField.getMaxLength()).isEqualTo(34);
    }

    @ParameterizedTest
    @MethodSource("direkt1822GroupDataProviders")
    public void shouldReturnRedirectStepIfTriggeredAfterFormStep(Direkt1822GroupDataProvider dataProvider) throws JsonProcessingException {
        // given
        String stateId = "64ed3844-8733-41fc-968e-8e3a5bd75b80";
        String redirectUrl = "https://www.yolt.com/callback-test";
        Direkt1822GroupLoginFormDTO loginFormDTO = new Direkt1822GroupLoginFormDTO(
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID()),
                redirectUrl);

        String providerState = objectMapper.writeValueAsString(loginFormDTO);
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("Iban", TEST_IBAN);
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setProviderState(providerState)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setRestTemplateManager(restTemplateManager)
                .setState(stateId)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest).getStep();

        // then
        String result = redirectStep.getRedirectUrl();
        assertThat(result).isEqualTo("https://login.direkt1822.de/authorize/1234?state=64ed3844-8733-41fc-968e-8e3a5bd75b80");
    }

    @ParameterizedTest
    @MethodSource("direkt1822GroupDataProviders")
    public void shouldCreateAccessMeans(Direkt1822GroupDataProvider dataProvider) {
        // given
        UUID testUserId = UUID.randomUUID();
        String providerState = "TEST_CONSENT_ID";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(testUserId)
                .setProviderState(providerState)
                .build();

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThat(result.getAccessMeans().getUserId()).isEqualTo(testUserId);
        assertThat(result.getAccessMeans().getAccessMeans()).containsSequence("TEST_CONSENT_ID");
    }

    @ParameterizedTest
    @MethodSource("direkt1822GroupDataProviders")
    public void shouldThrowTokenInvalidExceptionOnRefreshAccessMeans(Direkt1822GroupDataProvider dataProvider) {
        assertThatThrownBy(() -> dataProvider.refreshAccessMeans(null))
                .isExactlyInstanceOf(TokenInvalidException.class)
                .hasMessage("Refreshing tokens is not supported by bank");
    }

    @ParameterizedTest
    @MethodSource("direkt1822GroupDataProviders")
    public void shouldSuccessfullyDeleteConsent(Direkt1822GroupDataProvider dataProvider) throws TokenInvalidException {
        // given
        UrlOnUserSiteDeleteRequest deleteRequest = new UrlOnUserSiteDeleteRequestBuilder()
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setAccessMeans(getSampleAccessMeans())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        dataProvider.onUserSiteDelete(deleteRequest);
    }

    @ParameterizedTest
    @MethodSource("direkt1822GroupDataProviders")
    public void shouldSuccessfullyFetchData(Direkt1822GroupDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        //given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(getSampleAccessMeans())
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();

        //when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        //then
        List<ProviderAccountDTO> resultAccounts = dataProviderResponse.getAccounts();
        assertThat(resultAccounts).hasSize(2);

        ProviderAccountDTO account1 = resultAccounts.get(0);
        account1.validate();
        assertThat(account1.getCurrentBalance()).isEqualTo("8592.89");
        assertThat(account1.getAvailableBalance()).isEqualTo("9391.63");
        assertThat(account1.getAccountId()).isEqualTo("75647ea0-0d3a-442c-8046-bf415b20a9f6");
        assertThat(account1.getAccountNumber().getHolderName()).isEqualTo("John Smith");
        assertThat(account1.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(account1.getAccountNumber().getIdentification()).isEqualTo("DE72500502011234567897");
        assertThat(account1.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(account1.getName()).isEqualTo("1822 Direkt Account");
        assertThat(account1.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(account1.getExtendedAccount().getProduct()).isEqualTo("Current Account");
        assertThat(account1.getExtendedAccount().getBalances()).hasSize(2);

        List<ProviderTransactionDTO> account1Transactions = account1.getTransactions();
        assertThat(account1Transactions).hasSize(4);
        ProviderTransactionDTO account1Transaction1 = account1Transactions.get(0);
        assertThat(account1Transaction1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(account1Transaction1.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(account1Transaction1.getDescription()).isEqualTo(" ");
        assertThat(account1Transaction1.getAmount()).isEqualTo("254.90");
        assertThat(account1Transaction1.getDateTime()).isEqualTo("2020-06-10T00:00+02:00[Europe/Berlin]");
        assertThat(account1Transaction1.getExtendedTransaction().getValueDate()).isEqualTo("2020-06-11T00:00+02:00[Europe/Berlin]");
        assertThat(account1Transaction1.getExtendedTransaction().getBookingDate()).isEqualTo("2020-06-10T00:00+02:00[Europe/Berlin]");
        assertThat(account1Transaction1.getExtendedTransaction().getCreditorName()).isEqualTo("David Smith");

        ProviderTransactionDTO account1Transaction2 = account1Transactions.get(1);
        assertThat(account1Transaction2.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(account1Transaction2.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(account1Transaction2.getDescription()).isEqualTo(" ");
        assertThat(account1Transaction2.getAmount()).isEqualTo("40.90");
        assertThat(account1Transaction2.getDateTime()).isEqualTo("2020-06-10T00:00+02:00[Europe/Berlin]");
        assertThat(account1Transaction2.getExtendedTransaction().getValueDate()).isEqualTo("2020-06-11T00:00+02:00[Europe/Berlin]");
        assertThat(account1Transaction2.getExtendedTransaction().getBookingDate()).isEqualTo("2020-06-10T00:00+02:00[Europe/Berlin]");
        assertThat(account1Transaction2.getExtendedTransaction().getCreditorName()).isEqualTo("David Smith");

        ProviderTransactionDTO account1Transaction3 = account1Transactions.get(2);
        assertThat(account1Transaction3.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(account1Transaction3.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(account1Transaction3.getDescription()).isEqualTo("**Endsaldo** 111,11HStand31.12.2020 111,11H ");
        assertThat(account1Transaction3.getAmount()).isEqualTo("0.00");
        assertThat(account1Transaction3.getDateTime()).isEqualTo("2020-06-10T00:00+02:00[Europe/Berlin]");
        assertThat(account1Transaction3.getExtendedTransaction().getValueDate()).isEqualTo("2020-06-11T00:00+02:00[Europe/Berlin]");
        assertThat(account1Transaction3.getExtendedTransaction().getBookingDate()).isEqualTo("2020-06-10T00:00+02:00[Europe/Berlin]");
        assertThat(account1Transaction3.getExtendedTransaction().getCreditorName()).isEqualTo("David Smith");

        ProviderTransactionDTO account1Transaction4 = account1Transactions.get(3);
        assertThat(account1Transaction4.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(account1Transaction4.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(account1Transaction4.getDescription()).isEqualTo(" ");
        assertThat(account1Transaction4.getAmount()).isEqualTo("120.90");
        assertThat(account1Transaction4.getDateTime()).isEqualTo("2020-06-12T00:00+02:00[Europe/Berlin]");
        assertThat(account1Transaction4.getExtendedTransaction().getValueDate()).isEqualTo("2020-06-12T00:00+02:00[Europe/Berlin]");
        assertThat(account1Transaction4.getExtendedTransaction().getBookingDate()).isNull();
        assertThat(account1Transaction4.getExtendedTransaction().getCreditorName()).isEqualTo("ABC");

        ProviderAccountDTO account2 = resultAccounts.get(1);
        assertThat(account2.getCurrentBalance()).isEqualTo("592.89");
        assertThat(account2.getAvailableBalance()).isEqualTo("391.63");
        assertThat(account2.getAccountId()).isEqualTo("1c425531-8ef5-484a-a0d7-65788bfe1726");
        assertThat(account2.getAccountNumber().getHolderName()).isEqualTo("John Smith");
        assertThat(account2.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(account2.getAccountNumber().getIdentification()).isEqualTo("DE72500502011234567882");
        assertThat(account2.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(account2.getName()).isEqualTo("1822 Direkt Account");
        assertThat(account2.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(account2.getExtendedAccount().getProduct()).isEqualTo("Current Account");
        assertThat(account2.getExtendedAccount().getBalances()).hasSize(2);

        List<ProviderTransactionDTO> account2Transactions = account2.getTransactions();
        assertThat(account2Transactions).hasSize(2);
    }

    @SneakyThrows
    private AccessMeansDTO getSampleAccessMeans() {
        return new AccessMeansDTO(
                UUID.randomUUID(),
                objectMapper.writeValueAsString(new Direkt1822GroupAccessMeans("12345")),
                new Date(),
                new Date());
    }
}