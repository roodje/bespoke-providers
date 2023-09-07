package com.yolt.providers.stet.cicgroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.autoonboarding.RegistrationOperation;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import com.yolt.providers.stet.cicgroup.beobank.BeobankDataProviderV1;
import com.yolt.providers.stet.cicgroup.cic.CicDataProviderV5;
import com.yolt.providers.stet.cicgroup.cic.config.CicProperties;
import com.yolt.providers.stet.cicgroup.creditmutuel.CreditMutuelDataProviderV7;
import com.yolt.providers.stet.generic.GenericOnboardingDataProvider;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.stet.cicgroup.common.auth.CicGroupAuthenticationMeansSupplier.*;
import static nl.ing.lovebird.extendeddata.common.CurrencyCode.EUR;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CicGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/cic/ais/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("cic")
public class CicGroupDataProviderHappyFlowIntegrationTest {

    // getTypedAuthenticationMeans expectations
    private static final String EXPECTED_TYPED_CLIENT_NAME_DISPLAY_NAME = "Client Name (shown during OAuth2 flow).";
    private static final TypedAuthenticationMeans EXPECTED_TYPED_EMAIL_ADDRESS = TypedAuthenticationMeans.CLIENT_EMAIL;
    private static final TypedAuthenticationMeans EXPECTED_TYPED_SIGNING_PRIVATE_KEY_ID = TypedAuthenticationMeans.KEY_ID;
    private static final TypedAuthenticationMeans EXPECTED_TYPED_SIGNING_CERTIFICATE = TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM;
    private static final TypedAuthenticationMeans EXPECTED_TYPED_TRANSPORT_PRIVATE_KEY_ID = TypedAuthenticationMeans.KEY_ID;
    private static final TypedAuthenticationMeans EXPECTED_TYPED_TRANSPORT_CERTIFICATE = TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM;
    private static final TypedAuthenticationMeans EXPECTED_TYPED_CLIENT_ID = TypedAuthenticationMeans.CLIENT_ID_STRING;
    private static final TypedAuthenticationMeans EXPECTED_TYPED_CLIENT_KEY_ID = TypedAuthenticationMeans.SIGNING_KEY_ID_STRING;
    private static final int EXPECTED_TYPED_AUTHENTICATION_MEANS_SIZE = 8;

    // getAutoConfiguredMeans expectations
    private static final int EXPECTED_AUTO_CONFIGURED_MEANS_MEANS_SIZE = 2;

    // autoConfigureMeans expectations
    private static final String EXPECTED_CLIENT_ID_FROM_REGISTRATION = "4824821a73bcdc59e0ffede950ef046e1cb68aa8";
    private static final String EXPECTED_CLIENT_KEY_ID_FROM_REGISTRATION = "a771a1923bd403603831a2425df818a4";
    private static final String EXPECTED_CLIENT_KEY_ID_FROM_UPDATED_REGISTRATION = "a771a1923bd403603831a2425df818a4_next";

    // getLoginInfo() expectations
    private static final int EXPECTED_CODE_VERIFIER_LENGTH = 42;
    private static final String EXPECTED_CODE = "code";
    private static final String EXPECTED_CODE_CHALLENGE_METHOD = "S256";
    private static final String EXPECTED_CLIENT_ID = "clientId";
    private static final String EXPECTED_SCOPE = "aisp%20extended_transaction_history";
    private static final String EXPECTED_REDIRECT_URI = "https://www.yolt.com/callback";
    private static final String EXPECTED_STATE = "8b6dee15-ea2a-49b2-b100-f5f96d31cd90";

    // createNewAccessMeans() expectations
    private static final String EXPECTED_ACCESS_TOKEN = "{\"access_token\":\"ACCESS_TOKEN\",\"token_type\":\"bearer\",\"expires_in\":10800,\"refresh_token\":\"REFRESH_TOKEN\",\"scope\":\"aisp\"}";

    // refreshAccessMeans() expectations
    private static final String EXPECTED_REFRESH_TOKEN = "{\"access_token\":\"ACCESS_TOKEN\",\"token_type\":\"bearer\",\"expires_in\":10700,\"refresh_token\":\"REFRESH_TOKEN\",\"scope\":\"aisp\"}";

    // Auxiliary constants
    private static final String YOLT_REDIRECT_URI = "https://www.yolt.com/callback";
    private static final String EXTERNAL_REDIRECT_URI = "https://redirect.example.com/cb1";
    private static final String YOLT_REDIRECT_URI_WITH_PARAMS = "http://yolt.com?code=authorization_code&state=uniquestring";
    private static final String STATE = "8b6dee15-ea2a-49b2-b100-f5f96d31cd90";
    private static final String CODE_VERIFIER = "6965646a-e758-4478-8b22-312a96472b856965646a-e758-4478-8b22-312a96472b85";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final Date DATE = new Date();

    // OAuth constants
    public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";

    // Auxiliary objects
    private static final RestTemplateManager REST_TEMPLATE_MANAGER = new SimpleRestTemplateManagerMock();

    private UrlCreateAccessMeansRequest urlCreateAccessMeans;
    private Map<String, BasicAuthenticationMean> SAMPLE_BASIC_AUTHENTICATION_MEANS;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private CicProperties properties;

    @Autowired
    @Qualifier("CicDataProviderV5")
    private CicDataProviderV5 cicDataProvider;

    @Autowired
    @Qualifier("CreditMutuelDataProviderV7")
    CreditMutuelDataProviderV7 creditMutuelDataProvider;

    @Autowired
    @Qualifier("BeobankDataProviderV1")
    BeobankDataProviderV1 beobankDataProvider;

    private Stream<GenericOnboardingDataProvider> getCicGroupDataProviders() {
        return Stream.of(cicDataProvider, creditMutuelDataProvider);
    }

    private Stream<GenericOnboardingDataProvider> getBeobankDataProvider() {
        return Stream.of(beobankDataProvider);
    }

    @Mock
    private Signer signer;

    // Configuration
    @BeforeEach
    public void setup() {
        SAMPLE_BASIC_AUTHENTICATION_MEANS = new CicGroupSampleAuthenticationMeans().getBasicAuthenticationMeans();
        urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setAuthenticationMeans(SAMPLE_BASIC_AUTHENTICATION_MEANS)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .setRedirectUrlPostedBackFromSite(YOLT_REDIRECT_URI_WITH_PARAMS)
                .setBaseClientRedirectUrl(YOLT_REDIRECT_URI)
                .setProviderState(CODE_VERIFIER)
                .build();
    }

    @ParameterizedTest
    @MethodSource({"getCicGroupDataProviders", "getBeobankDataProvider"})
    public void shouldReturnEightTypedMeansWithCorrectType(UrlDataProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthenticationMeans).hasSize(EXPECTED_TYPED_AUTHENTICATION_MEANS_SIZE);
        assertThat(typedAuthenticationMeans.get(CLIENT_ID)).isEqualTo(EXPECTED_TYPED_CLIENT_ID);
        assertThat(typedAuthenticationMeans.get(CLIENT_KEY_ID_MIGRATION)).isEqualTo(EXPECTED_TYPED_CLIENT_KEY_ID);
        assertThat(typedAuthenticationMeans.get(CLIENT_NAME).getDisplayName()).isEqualTo(EXPECTED_TYPED_CLIENT_NAME_DISPLAY_NAME);
        assertThat(typedAuthenticationMeans.get(EMAIL_ADDRESS)).isEqualTo(EXPECTED_TYPED_EMAIL_ADDRESS);
        assertThat(typedAuthenticationMeans.get(SIGNING_CERTIFICATE_MIGRATION)).isEqualTo(EXPECTED_TYPED_SIGNING_CERTIFICATE);
        assertThat(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_MIGRATION)).isEqualTo(EXPECTED_TYPED_SIGNING_PRIVATE_KEY_ID);
        assertThat(typedAuthenticationMeans.get(TRANSPORT_CERTIFICATE)).isEqualTo(EXPECTED_TYPED_TRANSPORT_CERTIFICATE);
        assertThat(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID)).isEqualTo(EXPECTED_TYPED_TRANSPORT_PRIVATE_KEY_ID);
    }

    // AutoOnboarding tests
    @ParameterizedTest
    @MethodSource({"getCicGroupDataProviders", "getBeobankDataProvider"})
    public void shouldReturnClientIdAndClientKeyId(AutoOnboardingProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = dataProvider.getAutoConfiguredMeans();

        // then
        assertThat(typedAuthenticationMeans).hasSize(EXPECTED_AUTO_CONFIGURED_MEANS_MEANS_SIZE);
        assertThat(typedAuthenticationMeans.get(CLIENT_ID)).isEqualTo(EXPECTED_TYPED_CLIENT_ID);
        assertThat(typedAuthenticationMeans.get(CLIENT_KEY_ID_MIGRATION)).isEqualTo(EXPECTED_TYPED_CLIENT_KEY_ID);
    }

    @ParameterizedTest
    @MethodSource({"getCicGroupDataProviders", "getBeobankDataProvider"})
    public void shouldSuccessfullyOnboard(AutoOnboardingProvider dataProvider) {
        // given
        Map<String, BasicAuthenticationMean> authenticationMeans = new CicGroupSampleAuthenticationMeans().getBasicAuthenticationMeans();
        authenticationMeans.remove(CLIENT_ID);
        authenticationMeans.remove(CLIENT_KEY_ID_MIGRATION);
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                authenticationMeans,
                REST_TEMPLATE_MANAGER,
                null,
                RegistrationOperation.CREATE,
                YOLT_REDIRECT_URI
        );

        // when
        Map<String, BasicAuthenticationMean> basicAuthenticationMean = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        BasicAuthenticationMean clientId = basicAuthenticationMean.get(CLIENT_ID);
        BasicAuthenticationMean clientKeyId = basicAuthenticationMean.get(CLIENT_KEY_ID_MIGRATION);

        assertThat(clientId).isNotNull();
        assertThat(clientKeyId).isNotNull();
        assertThat(clientId.getValue()).isEqualTo(EXPECTED_CLIENT_ID_FROM_REGISTRATION);
        assertThat(clientKeyId.getValue()).isEqualTo(EXPECTED_CLIENT_KEY_ID_FROM_REGISTRATION);
    }

    @ParameterizedTest
    @MethodSource({"getCicGroupDataProviders", "getBeobankDataProvider"})
    public void shouldSuccessfullyUpdateWithNotYetRegisteredRedirectUrl(AutoOnboardingProvider dataProvider) {
        // given
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>(SAMPLE_BASIC_AUTHENTICATION_MEANS);
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                authenticationMeans,
                REST_TEMPLATE_MANAGER,
                null,
                RegistrationOperation.UPDATE,
                YOLT_REDIRECT_URI
        );
        // when
        Map<String, BasicAuthenticationMean> basicAuthenticationMean = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        BasicAuthenticationMean clientId = basicAuthenticationMean.get(CLIENT_ID);
        BasicAuthenticationMean clientKeyId = basicAuthenticationMean.get(CLIENT_KEY_ID_MIGRATION);

        assertThat(clientId).isNotNull();
        assertThat(clientKeyId).isNotNull();
        assertThat(clientId.getValue()).isEqualTo(EXPECTED_CLIENT_ID_FROM_REGISTRATION);
        assertThat(clientKeyId.getValue()).isEqualTo(EXPECTED_CLIENT_KEY_ID_FROM_REGISTRATION);
    }

    @ParameterizedTest
    @MethodSource({"getCicGroupDataProviders", "getBeobankDataProvider"})
    public void shouldSuccessfullyUpdateWithNotYetRegisteredSigningCertificate(AutoOnboardingProvider dataProvider) {
        // given
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>(SAMPLE_BASIC_AUTHENTICATION_MEANS);
        authenticationMeans.put(SIGNING_CERTIFICATE_MIGRATION, authenticationMeans.get(TRANSPORT_CERTIFICATE));
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                authenticationMeans,
                REST_TEMPLATE_MANAGER,
                null,
                RegistrationOperation.UPDATE,
                YOLT_REDIRECT_URI
        );
        // when
        Map<String, BasicAuthenticationMean> basicAuthenticationMean = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        BasicAuthenticationMean clientId = basicAuthenticationMean.get(CLIENT_ID);
        BasicAuthenticationMean clientKeyIdMigration = basicAuthenticationMean.get(CLIENT_KEY_ID_MIGRATION);

        assertThat(clientId).isNotNull();
        assertThat(clientKeyIdMigration).isNotNull();
        assertThat(clientId.getValue()).isEqualTo(EXPECTED_CLIENT_ID_FROM_REGISTRATION);
        assertThat(clientKeyIdMigration.getValue()).isEqualTo(EXPECTED_CLIENT_KEY_ID_FROM_UPDATED_REGISTRATION);
    }

    @ParameterizedTest
    @MethodSource({"getCicGroupDataProviders", "getBeobankDataProvider"})
    public void shouldThrowAutoOnboardingExceptionWhenRedirectUrlOrCertificateIsNotChanged(AutoOnboardingProvider dataProvider) {
        // given
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>(SAMPLE_BASIC_AUTHENTICATION_MEANS);
        authenticationMeans.put(CLIENT_ID, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "clientId2"));
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                authenticationMeans,
                REST_TEMPLATE_MANAGER,
                null,
                RegistrationOperation.UPDATE,
                EXTERNAL_REDIRECT_URI
        );
        // when
        ThrowableAssert.ThrowingCallable callable = () -> dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(AutoOnboardingException.class)
                .hasMessageContaining("No change in redirect url or signing certificate, request of updating registration is declined");
    }

    @ParameterizedTest
    @MethodSource({"getCicGroupDataProviders", "getBeobankDataProvider"})
    public void shouldDeleteRegistrationWithClientIdAndOneUri(AutoOnboardingProvider dataProvider) {
        // given
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>(SAMPLE_BASIC_AUTHENTICATION_MEANS);
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                authenticationMeans,
                REST_TEMPLATE_MANAGER,
                null,
                EXTERNAL_REDIRECT_URI
        );

        // when
        Executable callable = () -> dataProvider.removeAutoConfiguration(urlAutoOnboardingRequest);

        // then
        assertDoesNotThrow(callable);
    }

    // Authorization tests
    @ParameterizedTest
    @MethodSource("getCicGroupDataProviders")
    public void shouldReturnProperConsentPageUrl(UrlDataProvider dataProvider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(YOLT_REDIRECT_URI)
                .setState(STATE)
                .setAuthenticationMeans(SAMPLE_BASIC_AUTHENTICATION_MEANS)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        String codeVerifier = redirectStep.getProviderState();
        Map<String, String> redirectUrl = UriComponentsBuilder
                .fromUriString(redirectStep.getRedirectUrl())
                .build(false)
                .getQueryParams()
                .toSingleValueMap();

        assertThat(codeVerifier).hasSizeGreaterThanOrEqualTo(EXPECTED_CODE_VERIFIER_LENGTH);
        assertThat(redirectUrl.get(OAuth.RESPONSE_TYPE)).isEqualTo(EXPECTED_CODE);
        assertThat(redirectUrl.get(CODE_CHALLENGE_METHOD)).isEqualTo(EXPECTED_CODE_CHALLENGE_METHOD);
        assertThat(redirectUrl.get(OAuth.CLIENT_ID)).isEqualTo(EXPECTED_CLIENT_ID);
        assertThat(redirectUrl.get(OAuth.SCOPE)).isEqualTo(EXPECTED_SCOPE);
        assertThat(redirectUrl.get(OAuth.REDIRECT_URI)).isEqualTo(EXPECTED_REDIRECT_URI);
        assertThat(redirectUrl.get(OAuth.STATE)).isEqualTo(EXPECTED_STATE);
    }

    @ParameterizedTest
    @MethodSource("getBeobankDataProvider")
    public void shouldReturnFormStepWithLanguage(UrlDataProvider dataProvider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(YOLT_REDIRECT_URI)
                .setState(STATE)
                .setAuthenticationMeans(SAMPLE_BASIC_AUTHENTICATION_MEANS)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .build();

        // when
        FormStep formStep = (FormStep) dataProvider.getLoginInfo(request);

        // then
        assertThat(formStep.getForm().getFormComponents().get(0).getDisplayName()).isEqualTo("Consent Language");
    }

    @ParameterizedTest
    @MethodSource("getBeobankDataProvider")
    public void shouldReturnLoginUrlWhenCreateNewAccessMeansCalledWithoutRedirectUrlPostedBackFromSite(UrlDataProvider dataProvider) {
        // given
        FilledInUserSiteFormValues userSiteFormValues = new FilledInUserSiteFormValues();
        userSiteFormValues.setValueMap(Collections.singletonMap("ConsentLanguage", "nl"));

        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setAuthenticationMeans(SAMPLE_BASIC_AUTHENTICATION_MEANS)
                .setFilledInUserSiteFormValues(userSiteFormValues)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .setBaseClientRedirectUrl(YOLT_REDIRECT_URI)
                .setProviderState(CicGroupSampleAuthenticationMeans.createPreAuthorizedJsonProviderState(objectMapper, properties, CODE_VERIFIER))
                .setState(STATE)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        assertThat(accessMeansOrStepDTO.getAccessMeans()).isNull();
        assertThat(accessMeansOrStepDTO.getStep()).isInstanceOf(RedirectStep.class);

        RedirectStep redirectStep = (RedirectStep) accessMeansOrStepDTO.getStep();
        assertThat(redirectStep).isNotNull();

        Map<String, String> actualQueryParams = UriComponentsBuilder.fromUriString(redirectStep.getRedirectUrl())
                .build()
                .getQueryParams()
                .toSingleValueMap();

        assertThat(actualQueryParams).hasSize(7);
        assertThat(actualQueryParams.get("response_type")).isEqualTo("code");
        assertThat(actualQueryParams.get("client_id")).isEqualTo("clientId");
        assertThat(actualQueryParams.get("scope")).isEqualTo("aisp extended_transaction_history");
        assertThat(actualQueryParams.get("redirect_uri")).isEqualTo(YOLT_REDIRECT_URI);
        assertThat(actualQueryParams.get("state")).isEqualTo(STATE);
        assertThat(actualQueryParams.get("code_challenge_method")).isEqualTo(EXPECTED_CODE_CHALLENGE_METHOD);
        assertThat(actualQueryParams.get("code_challenge")).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource({"getCicGroupDataProviders", "getBeobankDataProvider"})
    public void shouldCreateNewAccessMeans(UrlDataProvider dataProvider) throws JsonProcessingException {
        // given
        urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setAuthenticationMeans(SAMPLE_BASIC_AUTHENTICATION_MEANS)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .setRedirectUrlPostedBackFromSite(YOLT_REDIRECT_URI_WITH_PARAMS)
                .setBaseClientRedirectUrl(YOLT_REDIRECT_URI)
                .setProviderState(CicGroupSampleAuthenticationMeans.createPreAuthorizedJsonProviderState(objectMapper, properties, CODE_VERIFIER))
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(urlCreateAccessMeans);
        System.out.println(accessMeansOrStepDTO.getAccessMeans().getAccessMeans());
        DataProviderState cicDataProviderState = objectMapper.readValue(accessMeansOrStepDTO.getAccessMeans().getAccessMeans(), DataProviderState.class);

        // then
        assertThat(cicDataProviderState.getAccessToken()).isEqualTo("ACCESS_TOKEN");
        assertThat(cicDataProviderState.getRefreshToken()).isEqualTo("REFRESH_TOKEN");
        assertThat(cicDataProviderState.isRefreshed()).isFalse();
    }

    @ParameterizedTest
    @MethodSource({"getCicGroupDataProviders", "getBeobankDataProvider"})
    public void shouldRefreshAccessMeans(UrlDataProvider dataProvider) throws Exception {
        // given
        String accessMeans = "{\"expires_in\":3600,\"access_token\":\"ACCESS_TOKEN\",\"refresh_token\":\"REFRESH_TOKEN\", \"refreshed\":\"false\"}";

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, accessMeans, DATE, DATE);
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .setAuthenticationMeans(SAMPLE_BASIC_AUTHENTICATION_MEANS)
                .build();

        // when
        AccessMeansDTO refreshedAccessMeans = dataProvider.refreshAccessMeans(urlRefreshAccessMeans);
        DataProviderState refreshedToken = objectMapper.readValue(refreshedAccessMeans.getAccessMeans(), DataProviderState.class);

        System.out.println(refreshedAccessMeans);
        // then
        assertThat(refreshedAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(refreshedToken.getAccessToken()).isEqualTo("ACCESS_TOKEN");
        assertThat(refreshedToken.getRefreshToken()).isEqualTo("REFRESH_TOKEN");
        assertThat(refreshedToken.isRefreshed()).isTrue();
    }

    @ParameterizedTest
    @MethodSource({"getCicGroupDataProviders", "getBeobankDataProvider"})
    public void shouldSuccessfullyFetchData(UrlDataProvider dataProvider) throws ProviderFetchDataException, TokenInvalidException {
        // given
        urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setAuthenticationMeans(SAMPLE_BASIC_AUTHENTICATION_MEANS)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .setRedirectUrlPostedBackFromSite(YOLT_REDIRECT_URI_WITH_PARAMS)
                .setBaseClientRedirectUrl(YOLT_REDIRECT_URI)
                .setProviderState(CicGroupSampleAuthenticationMeans.createPreAuthorizedJsonProviderState(objectMapper, properties, CODE_VERIFIER))
                .build();
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(urlCreateAccessMeans);
        String accessToken = accessMeansOrStepDTO.getAccessMeans().getAccessMeans();

        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, accessToken, DATE, DATE);

        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneId.of("Z")).toInstant())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(SAMPLE_BASIC_AUTHENTICATION_MEANS)
                .setSigner(signer)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .setPsuIpAddress("1.1.1.1")
                .build();
        DataProviderResponse expectedResult = new DataProviderResponse(getAccounts());

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        //then
        //Verify Accounts
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        List<ProviderAccountDTO> expectedAccounts = expectedResult.getAccounts();
        assertThat(accounts).hasSize(expectedAccounts.size());
        for (int i = 0; i < expectedAccounts.size(); i++) {
            ProviderAccountDTO account = accounts.get(i);
            ProviderAccountDTO expectedAccount = expectedAccounts.get(i);
            assertThatObjects(account, expectedAccount).areEqualIgnoringFields("lastRefreshed", "transactions", "extendedAccount");
            assertThatObjects(account.getExtendedAccount(), expectedAccount.getExtendedAccount()).areEqual();
            assertThat(account.getLastRefreshed()).isNotNull();

            //Verify Transactions
            List<ProviderTransactionDTO> transactions = account.getTransactions();
            List<ProviderTransactionDTO> expectedTransactions = expectedAccount.getTransactions();
            assertThat(transactions).hasSize(expectedTransactions.size());
            for (int j = 0; j < expectedTransactions.size(); j++) {
                ProviderTransactionDTO transaction = transactions.get(j);
                ProviderTransactionDTO expectedTransaction = expectedTransactions.get(j);
                assertThatObjects(transaction, expectedTransaction).areEqualIgnoringFields("extendedTransaction");
                assertThatObjects(transaction.getExtendedTransaction(), expectedTransaction.getExtendedTransaction()).areEqual();
            }
        }
    }

    private List<ProviderAccountDTO> getAccounts() {
        ProviderAccountNumberDTO providerAccountNumberDTO1 = new ProviderAccountNumberDTO(IBAN, "FR8912739000404141324742D17");
        providerAccountNumberDTO1.setHolderName("Current Account Name");
        return List.of(ProviderAccountDTO.builder()
                        .yoltAccountType(CURRENT_ACCOUNT)
                        .availableBalance(new BigDecimal("7000.79"))
                        .currentBalance(new BigDecimal("7778.79"))
                        .accountId("123")
                        .accountNumber(providerAccountNumberDTO1)
                        .name("Current Account Name")
                        .currency(CurrencyCode.EUR)
                        .transactions(getProviderTransactions1())
                        .extendedAccount(getExtendedAccount1())
                        .build(),
                ProviderAccountDTO.builder()
                        .yoltAccountType(CREDIT_CARD)
                        .currentBalance(new BigDecimal("123.45"))
                        .accountId("456")
                        .name("Compte de Mr et Mme Dupont2")
                        .currency(CurrencyCode.EUR)
                        .creditCardData(ProviderCreditCardDTO.builder()
                                .availableCreditAmount(new BigDecimal("123.45"))
                                .build())
                        .transactions(getProviderTransactions2())
                        .extendedAccount(getExtendedAccount2())
                        .accountMaskedIdentification("MASK********2222")
                        .build(),
                ProviderAccountDTO.builder()
                        .yoltAccountType(CREDIT_CARD)
                        .currentBalance(new BigDecimal("-736.14"))
                        .accountId("789")
                        .name("Credit Card Account Name")
                        .currency(CurrencyCode.EUR)
                        .creditCardData(ProviderCreditCardDTO.builder()
                                .availableCreditAmount(new BigDecimal("-736.14"))
                                .build())
                        .transactions(getProviderTransactions3())
                        .extendedAccount(getExtendedAccount3())
                        .linkedAccount("123")
                        .accountMaskedIdentification("MASK********3333")
                        .build()
        );
    }

    private ExtendedAccountDTO getExtendedAccount1() {
        return ExtendedAccountDTO.builder()
                .resourceId("123")
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, "FR8912739000404141324742D17")))
                .currency(EUR)
                .name("Current Account Name")
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .status(Status.ENABLED)
                .usage(UsageType.PRIVATE)
                .balances(getBalances1())
                .build();
    }

    private ExtendedAccountDTO getExtendedAccount3() {
        return ExtendedAccountDTO.builder()
                .resourceId("789")
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.MASKED_PAN, "MASK********3333")))
                .currency(EUR)
                .name("Credit Card Account Name")
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .status(Status.ENABLED)
                .usage(UsageType.PRIVATE)
                .balances(Collections.emptyList())
                .linkedAccounts("123")
                .build();
    }

    private List<ProviderTransactionDTO> getProviderTransactions1() {
        return List.of(ProviderTransactionDTO.builder()
                        .externalId("10011202131600001-a9bd3758abc77f5fa4944e6e498281afa55be4921d85f5c1ec226d595b8f066f")
                        .dateTime(getDateTime("2021-11-12T01:00+01:00[Europe/Paris]"))
                        .amount(new BigDecimal("1000"))
                        .status(BOOKED)
                        .type(CREDIT)
                        .description("VIR MR XAVIER LIEFFROY, VIREMENT VERS VICTOR GEORGES PAUL S, VIREMENT DE MR XAVIER LIEFFROY")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(getExtendedTransaction1_1())
                        .build(),
                ProviderTransactionDTO.builder()
                        .externalId("10011202131000001-a9bd3758abc77f5fa4944e6e498281afa55be4921d85f5c1ec226d595b8f066f")
                        .dateTime(getDateTime("2021-11-06T01:00+01:00[Europe/Paris]"))
                        .amount(new BigDecimal("314.91"))
                        .status(BOOKED)
                        .type(DEBIT)
                        .description("ECH PRET CAP+IN 33043 722370 07")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(getExtendedTransaction1_2())
                        .build(),
                ProviderTransactionDTO.builder()
                        .externalId("10011202130900001-a9bd3758abc77f5fa4944e6e498281afa55be4921d85f5c1ec226d595b8f066f")
                        .dateTime(getDateTime("2021-11-05T01:00+01:00[Europe/Paris]"))
                        .amount(new BigDecimal("78.7"))
                        .status(BOOKED)
                        .type(DEBIT)
                        .description("PRLV SEPA MMA IARD SA, 2155782036320211027520959818, LES MUTUELLES DE MANS")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(getExtendedTransaction1_3())
                        .build(),
                ProviderTransactionDTO.builder()
                        .externalId("10011202131400001-a9bd3758abc77f5fa4944e6e498281afa55be4921d85f5c1ec226d595b8f066f")
                        .dateTime(getDateTime("2021-11-10T01:00+01:00[Europe/Paris]"))
                        .amount(new BigDecimal("10.25"))
                        .status(BOOKED)
                        .type(DEBIT)
                        .description(" F COTIS START J. ACTIFS")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(getExtendedTransaction1_4())
                        .build());
    }

    private ExtendedTransactionDTO getExtendedTransaction1_1() {
        return ExtendedTransactionDTO.builder()
                .status(BOOKED)
                .entryReference("10011202131600001-a9bd3758abc77f5fa4944e6e498281afa55be4921d85f5c1ec226d595b8f066f")
                .bookingDate(getDateTime("2021-11-12T01:00+01:00[Europe/Paris]"))
                .valueDate(getDateTime("2021-11-12T01:00+01:00[Europe/Paris]"))
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("1000")))
                .remittanceInformationUnstructured("VIR MR XAVIER LIEFFROY, VIREMENT VERS VICTOR GEORGES PAUL S, VIREMENT DE MR XAVIER LIEFFROY")
                .build();
    }

    private ExtendedTransactionDTO getExtendedTransaction1_2() {
        return ExtendedTransactionDTO.builder()
                .status(BOOKED)
                .entryReference("10011202131000001-a9bd3758abc77f5fa4944e6e498281afa55be4921d85f5c1ec226d595b8f066f")
                .bookingDate(getDateTime("2021-11-06T01:00+01:00[Europe/Paris]"))
                .valueDate(getDateTime("2021-11-05T01:00+01:00[Europe/Paris]"))
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("-314.91")))
                .remittanceInformationUnstructured("ECH PRET CAP+IN 33043 722370 07")
                .build();
    }

    private ExtendedTransactionDTO getExtendedTransaction1_3() {
        return ExtendedTransactionDTO.builder()
                .status(BOOKED)
                .entryReference("10011202130900001-a9bd3758abc77f5fa4944e6e498281afa55be4921d85f5c1ec226d595b8f066f")
                .bookingDate(getDateTime("2021-11-05T01:00+01:00[Europe/Paris]"))
                .valueDate(getDateTime("2021-11-05T01:00+01:00[Europe/Paris]"))
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("-78.7")))
                .remittanceInformationUnstructured("PRLV SEPA MMA IARD SA, 2155782036320211027520959818, LES MUTUELLES DE MANS")
                .build();
    }

    private ExtendedTransactionDTO getExtendedTransaction1_4() {
        return ExtendedTransactionDTO.builder()
                .status(BOOKED)
                .entryReference("10011202131400001-a9bd3758abc77f5fa4944e6e498281afa55be4921d85f5c1ec226d595b8f066f")
                .bookingDate(getDateTime("2021-11-10T01:00+01:00[Europe/Paris]"))
                .valueDate(getDateTime("2021-11-01T01:00+01:00[Europe/Paris]"))
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("-10.25")))
                .remittanceInformationUnstructured(" F COTIS START J. ACTIFS")
                .build();
    }

    private List<BalanceDTO> getBalances1() {
        return List.of(BalanceDTO.builder()
                        .balanceType(BalanceType.CLOSING_BOOKED)
                        .balanceAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("7778.79")))
                        .build(),
                BalanceDTO.builder()
                        .balanceType(BalanceType.EXPECTED)
                        .balanceAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("7000.79")))
                        .build()
        );
    }

    private ExtendedAccountDTO getExtendedAccount2() {
        return ExtendedAccountDTO.builder()
                .resourceId("456")
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.MASKED_PAN, "MASK********2222")))
                .currency(EUR)
                .name("Compte de Mr et Mme Dupont2")
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .status(Status.ENABLED)
                .usage(UsageType.PRIVATE)
                .balances(Collections.emptyList())
                .build();
    }

    private List<ProviderTransactionDTO> getProviderTransactions2() {
        return List.of(ProviderTransactionDTO.builder()
                        .externalId("1587d16aa40834b8e1698718bf566f52c46a63e4b66dbd1dcb524c4f999c6e4a-69d85aa9149898ec5")
                        .dateTime(getDateTime("2018-02-15T01:00:00+01:00[Europe/Paris]"))
                        .amount(new BigDecimal("12.25"))
                        .status(BOOKED)
                        .type(CREDIT)
                        .description("SEPA CREDIT TRANSFER from PSD2Company")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(getExtendedTransaction2_1())
                        .build(),
                ProviderTransactionDTO.builder()
                        .externalId("1587d16aa40834b8e1698718bf566f52c46a63e4b66dbd1dcb524c4f999c6e4a-69d85aa9149898ec6")
                        .dateTime(getDateTime("2018-02-16T01:00:00+01:00[Europe/Paris]"))
                        .amount(new BigDecimal("22.25"))
                        .status(BOOKED)
                        .type(DEBIT)
                        .description("SEPA CREDIT TRANSFER from PSD2Company2")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(getExtendedTransaction2_2())
                        .build(),
                ProviderTransactionDTO.builder()
                        .externalId("1587d16aa40834b8e1698718bf566f52c46a63e4b66dbd1dcb524c4f999c6e4a-69d85aa9149898ec7")
                        .dateTime(getDateTime("2018-02-17T01:00:00+01:00[Europe/Paris]"))
                        .amount(new BigDecimal("25.25"))
                        .status(BOOKED)
                        .type(CREDIT)
                        .description("SEPA CREDIT TRANSFER from PSD2Company2")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(getExtendedTransaction2_3())
                        .build());
    }

    private ExtendedTransactionDTO getExtendedTransaction2_1() {
        return ExtendedTransactionDTO.builder()
                .status(BOOKED)
                .entryReference("1587d16aa40834b8e1698718bf566f52c46a63e4b66dbd1dcb524c4f999c6e4a-69d85aa9149898ec5")
                .bookingDate(getDateTime("2018-02-15T01:00:00+01:00[Europe/Paris]"))
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("12.25")))
                .remittanceInformationUnstructured("SEPA CREDIT TRANSFER from PSD2Company")
                .build();
    }

    private ExtendedTransactionDTO getExtendedTransaction2_2() {
        return ExtendedTransactionDTO.builder()
                .status(BOOKED)
                .entryReference("1587d16aa40834b8e1698718bf566f52c46a63e4b66dbd1dcb524c4f999c6e4a-69d85aa9149898ec6")
                .bookingDate(getDateTime("2018-02-16T01:00:00+01:00[Europe/Paris]"))
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("-22.25")))
                .remittanceInformationUnstructured("SEPA CREDIT TRANSFER from PSD2Company2")
                .build();
    }

    private ExtendedTransactionDTO getExtendedTransaction2_3() {
        return ExtendedTransactionDTO.builder()
                .status(BOOKED)
                .entryReference("1587d16aa40834b8e1698718bf566f52c46a63e4b66dbd1dcb524c4f999c6e4a-69d85aa9149898ec7")
                .bookingDate(getDateTime("2018-02-17T01:00:00+01:00[Europe/Paris]"))
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("25.25")))
                .remittanceInformationUnstructured("SEPA CREDIT TRANSFER from PSD2Company2")
                .build();
    }


    private List<ProviderTransactionDTO> getProviderTransactions3() {
        return Collections.emptyList();
    }


    private ZonedDateTime getDateTime(final String dateTime) {
        return ZonedDateTime.parse(dateTime);
    }

    private ComparedObjects assertThatObjects(Object obj1, Object obj2) {
        return new ComparedObjects(obj1, obj2);
    }

    @RequiredArgsConstructor
    private class ComparedObjects {

        private final Object obj1;
        private final Object obj2;

        void areEqualIgnoringFields(String... ignoringFields) {
            assertThat(obj1)
                    .usingRecursiveComparison()
                    .ignoringFields(ignoringFields)
                    .isEqualTo(obj2);
        }

        void areEqual() {
            areEqualIgnoringFields();
        }
    }
}
