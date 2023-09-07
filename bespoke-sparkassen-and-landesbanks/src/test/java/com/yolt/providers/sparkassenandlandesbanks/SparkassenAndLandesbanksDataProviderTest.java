package com.yolt.providers.sparkassenandlandesbanks;

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
import com.yolt.providers.sparkassenandlandesbanks.common.Department;
import com.yolt.providers.sparkassenandlandesbanks.common.LandesbanksDataProvider;
import com.yolt.providers.sparkassenandlandesbanks.common.SparkassenAndLandesbanksAccessMeans;
import com.yolt.providers.sparkassenandlandesbanks.common.SparkassenAndLandesbanksDataProvider;
import com.yolt.providers.sparkassenandlandesbanks.common.dto.SparkassenAndLandesbanksLoginFormDTO;
import com.yolt.providers.sparkassenandlandesbanks.common.dto.SparkassenAndLandesbanksProviderState;
import com.yolt.providers.sparkassenandlandesbanks.sparkassen.SparkassenDataProviderV1;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.SelectField;
import nl.ing.lovebird.providershared.form.SelectOptionValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("sparkassenandlandesbanks")
@AutoConfigureWireMock(stubs = "classpath:/mappings/sparkassenandlandesbanks", httpsPort = 0, port = 0)
class SparkassenAndLandesbanksDataProviderTest {

    private static final String TEST_CONSENT_ID = "SOME_CONSENT_ID";
    private static final String TEST_ACCESS_TOKEN = "SOME_ACCESS_TOKEN";
    private static final String TEST_REFRESH_TOKEN = "SOME_REFRESH_TOKEN";
    private static final String TEST_CODE_VERIFIER = "TEST_CODE_VERIFIER";
    private static final String TEST_DATE_TIME = "2020-10-25T00:00+02:00[Europe/Berlin]";
    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";
    private static final String TEST_WELL_KNOWN_ENDPOINT = "/scaOauthLinks";
    private static final Department TEST_DEPARTMENT = new Department(null, null, "10050000");

    @Autowired
    private SparkassenDataProviderV1 sparkassenDataProviderV1;

    @Qualifier("LbbwDataProviderV1")
    @Autowired
    private LandesbanksDataProvider lbbwDataProviderV1;

    @Qualifier("NordLbDataProviderV1")
    @Autowired
    private LandesbanksDataProvider nordLbDataProviderV1;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("SparkassenAndLandesbanksObjectMapper")
    private ObjectMapper objectMapper;

    private static final Map<String, BasicAuthenticationMean> TEST_AUTHENTICATION_MEANS = SparkassenAndLandesbanksSampleTypedAuthenticationMeans.createTestAuthenticationMeans();
    private static final String BASE_CLIENT_REDIRECT_URL = "https://www.yolt.com/callback";
    private String testProviderState;

    private Stream<SparkassenAndLandesbanksDataProvider> getAllSparkassenAndLandesbanksProviders() {
        return Stream.of(sparkassenDataProviderV1,
                lbbwDataProviderV1,
                nordLbDataProviderV1);
    }

    private Stream<SparkassenAndLandesbanksDataProvider> getLandesbankProviders() {
        return Stream.of(
                lbbwDataProviderV1,
                nordLbDataProviderV1
        );
    }

    @BeforeEach
    public void setup() throws IOException {
        testProviderState = objectMapper.writeValueAsString(new SparkassenAndLandesbanksProviderState(TEST_CODE_VERIFIER, TEST_DEPARTMENT, TEST_WELL_KNOWN_ENDPOINT, TEST_CONSENT_ID));
    }

    @Test
    void shouldReturnFormWithCorrectlySelectedDepartment() {
        // given
        String loginState = UUID.randomUUID().toString();

        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setState(loginState)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        //When
        FormStep formStep = (FormStep) sparkassenDataProviderV1.getLoginInfo(urlGetLogin);

        //Then
        assertThat(formStep.getForm().getFormComponents()).hasSize(1);
        assertThat(formStep.getForm().getFormComponents().get(0).getDisplayName()).isEqualTo("Bank");

        List<SelectOptionValue> selectOptionValues = ((SelectField) formStep.getForm().getFormComponents().get(0)).getSelectOptionValues();
        assertThat(selectOptionValues).hasSize(2);

        SelectOptionValue firstSelectOptionValue = selectOptionValues.get(0);

        assertThat(firstSelectOptionValue.getValue()).isEqualTo("BERLINER_SPARKASSE");
        assertThat(firstSelectOptionValue.getDisplayName()).isEqualTo("Berliner Sparkasse");
    }

    @SneakyThrows
    @Test
    void shouldReturnLoginUrlForTheSparkassenUser() {
        //Given
        String stateId = UUID.randomUUID().toString();
        String redirectUrl = BASE_CLIENT_REDIRECT_URL;
        SparkassenAndLandesbanksLoginFormDTO loginFormDTO = new SparkassenAndLandesbanksLoginFormDTO(
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID()),
                redirectUrl);

        String providerState = objectMapper.writeValueAsString(loginFormDTO);

        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("bank", "BERLINER_SPARKASSE");
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setBaseClientRedirectUrl(redirectUrl)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setRestTemplateManager(restTemplateManager)
                .setState(stateId)
                .setProviderState(providerState)
                .build();

        //When
        RedirectStep redirectStep = (RedirectStep) sparkassenDataProviderV1.createNewAccessMeans(urlCreateAccessMeansRequest).getStep();

        //Then
        assertThat(redirectStep.getRedirectUrl())
                .contains("responseType=code")
                .contains("clientId=PSDNL-ABC-12345")
                .contains("scope=AIS%3A%20SOME_CONSENT_ID")
                .contains("code_challenge_method=S256")
                .contains("&state=" + stateId)
                .contains("code_challenge=");
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("getLandesbankProviders")
    void shouldReturnLoginUrlForTheLandesbankUser(SparkassenAndLandesbanksDataProvider landesbankProvider) {
        //Given
        String loginState = UUID.randomUUID().toString(

        );

        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setState(loginState)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        //When
        RedirectStep redirectStep = (RedirectStep) landesbankProvider.getLoginInfo(urlGetLoginRequest);

        //Then
        assertThat(redirectStep.getRedirectUrl())
                .contains("responseType=code")
                .contains("clientId=PSDNL-ABC-12345")
                .contains("scope=AIS%3A%20SOME_CONSENT_ID")
                .contains("code_challenge_method=S256")
                .contains("&state=" + loginState)
                .contains("code_challenge=");
    }

    @ParameterizedTest
    @MethodSource("getAllSparkassenAndLandesbanksProviders")
    void createNewAccessMeans(SparkassenAndLandesbanksDataProvider dataProvider) {
        //Given
        UUID someUserId = UUID.randomUUID();
        String redirectUrl = "https://www.yolt.com/callback?code=SOME_CODE&state=SOME_STATE";
        String baseUrl = BASE_CLIENT_REDIRECT_URL;
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setUserId(someUserId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setBaseClientRedirectUrl(baseUrl)
                .setProviderState(testProviderState)
                .setRestTemplateManager(restTemplateManager)
                .build();

        //When
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        //Then
        assertThat(result.getAccessMeans().getUserId()).isEqualTo(someUserId);
        assertThat(result.getAccessMeans().getAccessMeans()).contains(TEST_ACCESS_TOKEN);
        assertThat(result.getAccessMeans().getAccessMeans()).contains(TEST_REFRESH_TOKEN);
        assertThat(result.getAccessMeans().getAccessMeans()).contains(TEST_CONSENT_ID);
    }

    @ParameterizedTest
    @MethodSource("getAllSparkassenAndLandesbanksProviders")
    void refreshAccessMeans(SparkassenAndLandesbanksDataProvider dataProvider) throws JsonProcessingException, TokenInvalidException {
        //Given
        UUID someUserId = UUID.randomUUID();
        SparkassenAndLandesbanksAccessMeans accessMeans = new SparkassenAndLandesbanksAccessMeans(
                TEST_ACCESS_TOKEN,
                TEST_REFRESH_TOKEN,
                TEST_CONSENT_ID,
                TEST_DEPARTMENT,
                TEST_WELL_KNOWN_ENDPOINT
        );
        String serializedAccessMeans = objectMapper.writeValueAsString(accessMeans);
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(someUserId, serializedAccessMeans, new Date(), new Date());

        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();


        //When
        AccessMeansDTO result = dataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        //Then
        assertThat(result.getUserId()).isEqualTo(someUserId);
        assertThat(result.getAccessMeans()).contains(TEST_ACCESS_TOKEN);
        assertThat(result.getAccessMeans()).contains(TEST_REFRESH_TOKEN);
        assertThat(result.getAccessMeans()).contains(TEST_CONSENT_ID);
    }

    @ParameterizedTest
    @MethodSource("getAllSparkassenAndLandesbanksProviders")
    void shouldCorrectlyFetchAllAccountsAndTransactions(SparkassenAndLandesbanksDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        UUID someUserId = UUID.randomUUID();
        SparkassenAndLandesbanksAccessMeans accessMeans = new SparkassenAndLandesbanksAccessMeans(
                TEST_ACCESS_TOKEN,
                TEST_REFRESH_TOKEN,
                TEST_CONSENT_ID,
                TEST_DEPARTMENT,
                TEST_WELL_KNOWN_ENDPOINT
        );
        String serializedAccessMeans = objectMapper.writeValueAsString(accessMeans);
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(someUserId, serializedAccessMeans, new Date(), new Date());

        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        verifyAccount(dataProviderResponse.getAccounts());
        verifyTransactions(dataProviderResponse.getAccounts().get(0).getTransactions());
    }

    private void verifyAccount(List<ProviderAccountDTO> expectedAccounts) {
        assertThat(expectedAccounts).hasSize(2);
        ProviderAccountDTO firstExpectedAccount = expectedAccounts.get(0);
        firstExpectedAccount.validate();

        assertThat(firstExpectedAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(firstExpectedAccount.getCurrentBalance()).isEqualTo("500.00");
        assertThat(firstExpectedAccount.getAvailableBalance()).isEqualTo("499.00");
        assertThat(firstExpectedAccount.getAccountId()).isEqualTo("3dc3d5b3-7023-4848-9853-f5400a64e80f");
        assertThat(firstExpectedAccount.getName()).isEqualTo("Sparkasse Account");
        assertThat(firstExpectedAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(firstExpectedAccount.getAccountNumber().getIdentification()).isEqualTo("DE2310010010123456789");

        ProviderAccountDTO secondExpectedAccount = expectedAccounts.get(1);
        firstExpectedAccount.validate();

        assertThat(secondExpectedAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(secondExpectedAccount.getCurrentBalance()).isEqualTo("0.00");
        assertThat(secondExpectedAccount.getAvailableBalance()).isNull();
        assertThat(secondExpectedAccount.getAccountId()).isEqualTo("3dc3d5b3-7023-4848-9853-f5400a64e81g");
        assertThat(secondExpectedAccount.getName()).isEqualTo("US Dollar Account");
        assertThat(secondExpectedAccount.getCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(secondExpectedAccount.getAccountNumber().getIdentification()).isEqualTo("DE2310010010123456788");
    }


    private void verifyTransactions(List<ProviderTransactionDTO> expectedTransactions) {
        assertThat(expectedTransactions).hasSize(2);

        ProviderTransactionDTO firstProviderTransactionDTO = expectedTransactions.get(0);
        firstProviderTransactionDTO.validate();
        assertThat(firstProviderTransactionDTO.getExternalId()).isEqualTo("1234567");
        assertThat(firstProviderTransactionDTO.getDateTime()).isEqualTo(TEST_DATE_TIME);
        assertThat(firstProviderTransactionDTO.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(firstProviderTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(firstProviderTransactionDTO.getAmount()).isEqualTo("256.67");
        assertThat(firstProviderTransactionDTO.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(firstProviderTransactionDTO.getDescription()).isEqualTo("Example 1");

        ExtendedTransactionDTO extendedTransactionForFirstTransaction = firstProviderTransactionDTO.getExtendedTransaction();
        assertThat(extendedTransactionForFirstTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extendedTransactionForFirstTransaction.getBookingDate()).isEqualTo(TEST_DATE_TIME);
        assertThat(extendedTransactionForFirstTransaction.getValueDate()).isEqualTo("2020-10-26T00:00+01:00[Europe/Berlin]");
        assertThat(extendedTransactionForFirstTransaction.getTransactionAmount().getAmount()).isEqualTo("256.67");
        assertThat(extendedTransactionForFirstTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransactionForFirstTransaction.getCreditorName()).isEqualTo("John Miles");
        assertThat(extendedTransactionForFirstTransaction.getCreditorAccount().getValue()).isEqualTo("DE67100100101306118605");
        assertThat(extendedTransactionForFirstTransaction.getRemittanceInformationUnstructured()).isEqualTo("Example 1");

        ProviderTransactionDTO secondProviderTransactionDTO = expectedTransactions.get(1);
        secondProviderTransactionDTO.validate();
        assertThat(secondProviderTransactionDTO.getExternalId()).isNull();
        assertThat(secondProviderTransactionDTO.getDateTime()).isEqualTo(TEST_DATE_TIME);
        assertThat(secondProviderTransactionDTO.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(secondProviderTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(secondProviderTransactionDTO.getAmount()).isEqualTo("343.01");
        assertThat(secondProviderTransactionDTO.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(secondProviderTransactionDTO.getDescription()).isEqualTo("Example 2");

        assertThat(secondProviderTransactionDTO.getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("-343.01");

    }
}