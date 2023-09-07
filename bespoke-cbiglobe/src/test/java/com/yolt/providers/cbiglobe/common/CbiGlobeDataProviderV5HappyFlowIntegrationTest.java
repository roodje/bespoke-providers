package com.yolt.providers.cbiglobe.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.CbiGlobeFixtureProvider;
import com.yolt.providers.cbiglobe.CbiGlobeTestApp;
import com.yolt.providers.cbiglobe.bancawidiba.WidibaDataProviderV2;
import com.yolt.providers.cbiglobe.bcc.BccDataProviderV6;
import com.yolt.providers.cbiglobe.bcc.BccProperties;
import com.yolt.providers.cbiglobe.bnl.BnlDataProviderV5;
import com.yolt.providers.cbiglobe.bpm.BpmDataProviderV2;
import com.yolt.providers.cbiglobe.common.model.CbiGlobeAccessMeansDTO;
import com.yolt.providers.cbiglobe.intesasanpaolo.IntesaSanpaoloDataProviderV5;
import com.yolt.providers.cbiglobe.montepaschisiena.MontePaschiSienaDataProviderV5;
import com.yolt.providers.cbiglobe.posteitaliane.PosteItalianeDataProviderV5;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.FormComponent;
import nl.ing.lovebird.providershared.form.SelectField;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Period;
import java.util.*;

import static com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CbiGlobeTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/ais/3.0/happy_flow", httpsPort = 0, port = 0)
@ActiveProfiles("cbiglobe")
class CbiGlobeDataProviderV5HappyFlowIntegrationTest {

    private static final String STATE_FOR_GLOBAL_CONSENT_WITH_SCA = "11111111-1111-1111-1111-111111111111";
    private static final String STATE_FOR_GLOBAL_CONSENT_WITHOUT_SCA = "22222222-2222-2222-2222-222222222222";
    private static final String STATE_FOR_GLOBAL_CONSENT_WITHOUT_SCA_AND_SMS_OTP = "22222222-2222-3333-3333-222222222222";
    private static final String STATE_FOR_DETAILED_CONSENT_FOR_FIRST_ACCOUNT_WITH_SCA = "33333333-3333-3333-3333-333333333333";
    private static final String STATE_FOR_DETAILED_CONSENT_FOR_SECOND_ACCOUNT_WITH_SCA = "44444444-4444-4444-4444-444444444444";

    private static final String FIRST_CONSENT_WITH_SCA_ID = "1";
    private static final String SECOND_CONSENT_WITH_SCA_ID = "3";

    private static final String ACCOUNT_ID_1 = "1";
    private static final String ACCOUNT_ID_2 = "2";

    private static final String LOGIN_URL = "https://cbiglobe.it/clientlogin?id=some-id";
    private static final String LOGIN_URL_FOR_ACCOUNT_2_DETAILED_CONSENT = "https://cbiglobe.it/clientlogin?id=some-id2";
    private static final String TRANSPORT_KEY_ID = "2be4d475-f240-42c7-a22c-882566ac0f95";
    private static final String SIGNING_KEY_ID = "2e9ecac7-b840-4628-8036-d4998dfb8959";
    private static final String ACCESS_TOKEN = "00000000-0000-0000-0000-000000000000";
    private static final UUID USER_ID = UUID.fromString("7bb49dfa-5d54-43b0-9a39-e4b2b0e9ae10");
    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private BccProperties properties;

    @Autowired
    private BccDataProviderV6 bccDataProvider;

    @Autowired
    private BnlDataProviderV5 bnlDataProvider;

    @Autowired
    private IntesaSanpaoloDataProviderV5 intesaSanpaoloDataProvider;

    @Autowired
    private MontePaschiSienaDataProviderV5 montePaschiSienaDataProvider;

    @Autowired
    private PosteItalianeDataProviderV5 posteItalianeDataProvider;

    @Autowired
    private WidibaDataProviderV2 widibaDataProvider;

    @Autowired
    private BpmDataProviderV2 bpmDataProvider;

    @Autowired
    @Qualifier("CbiGlobe")
    private ObjectMapper mapper;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    void initialize() throws IOException, URISyntaxException {
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readCertificate()));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID));
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readCertificate()));
        authenticationMeans.put(SIGNING_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID));
        authenticationMeans.put(CLIENT_ID_STRING_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "fakeclientid"));
        authenticationMeans.put(CLIENT_SECRET_STRING_NAME, new BasicAuthenticationMean(TPP_ID.getType(), "fakeclientsecret"));
    }

    CbiGlobeDataProviderV5[] getProviders() {
        return new CbiGlobeDataProviderV5[]{
                bccDataProvider,
                bnlDataProvider,
                intesaSanpaoloDataProvider,
                montePaschiSienaDataProvider,
                posteItalianeDataProvider,
                widibaDataProvider,
                bpmDataProvider
        };
    }

    CbiGlobeDataProviderV5[] getProvidersWithManyASPSPs() {
        return new CbiGlobeDataProviderV5[]{
                bccDataProvider
        };
    }

    CbiGlobeDataProviderV5[] getProvidersWithSingleASPSP() {
        return new CbiGlobeDataProviderV5[]{
                bnlDataProvider,
                intesaSanpaoloDataProvider,
                montePaschiSienaDataProvider,
                posteItalianeDataProvider,
                bpmDataProvider
        };
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithManyASPSPs")
    void shouldReturnFormStepForGetLoginInfoWithFilledFormOfASPSPsForASPSPSelection(CbiGlobeDataProviderV5 dataProvider) {
        // given
        UrlGetLoginRequest urlGetLoginRequest = createUrlGetLoginRequest(STATE_FOR_GLOBAL_CONSENT_WITH_SCA);

        // when
        FormStep formStep = (FormStep) dataProvider.getLoginInfo(urlGetLoginRequest);

        // then
        List<FormComponent> components = formStep.getForm().getFormComponents();
        assertThat(components).hasSize(1);
        assertThat(components.get(0).isOptional()).isFalse();

        SelectField selectField = (SelectField) components.get(0);
        assertThat(selectField.getId()).isEqualTo("bank");
        assertThat(selectField.getSelectOptionValues()).hasSize(properties.getAspsps().size());
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithSingleASPSP")
    void shouldReturnRedirectStepForGetLoginInfoWithCorrectRequestDataForFirstConsentWithSCA(CbiGlobeDataProviderV5 dataProvider) {
        // given
        UrlGetLoginRequest urlGetLoginRequest = createUrlGetLoginRequest(STATE_FOR_GLOBAL_CONSENT_WITH_SCA);

        // when
        RedirectStep redirectStep = (RedirectStep) dataProvider.getLoginInfo(urlGetLoginRequest);

        // then
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(LOGIN_URL);

        CbiGlobeAccessMeansDTO accessMeansDTO = CbiGlobeFixtureProvider.fromProviderState(redirectStep.getProviderState(), mapper);
        assertThat(accessMeansDTO.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(accessMeansDTO.getConsentId()).isEqualTo(FIRST_CONSENT_WITH_SCA_ID);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnRedirectStepWithRedirectUrlForCreateNewAccessMeansWithCorrectRequestDataForFirstConsentWithSCA(CbiGlobeDataProviderV5 dataProvider) {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = createUrlCreateAccessMeansWithFormValues(
                createCbiGlobeAccessMeansDTO(), STATE_FOR_GLOBAL_CONSENT_WITH_SCA);

        // when
        AccessMeansOrStepDTO accessMeansOrStep = dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        RedirectStep redirectStep = (RedirectStep) accessMeansOrStep.getStep();
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(LOGIN_URL);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnRedirectStepWithRedirectUrlForCreateNewAccessMeansWithCorrectRequestDataForFirstConsentWithoutSCA(CbiGlobeDataProviderV5 dataProvider) {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = createUrlCreateAccessMeansWithFormValues(
                createCbiGlobeAccessMeansDTO(), STATE_FOR_GLOBAL_CONSENT_WITHOUT_SCA);

        // when
        AccessMeansOrStepDTO accessMeansOrStep = dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        RedirectStep redirectStep = (RedirectStep) accessMeansOrStep.getStep();
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(LOGIN_URL);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnRedirectStepWithRedirectUrlForCreateNewAccessMeansWithCorrectRequestDataForFirstConsentWithoutSCAAndSmsOtp(CbiGlobeDataProviderV5 dataProvider) {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = createUrlCreateAccessMeansWithFormValues(
                createCbiGlobeAccessMeansDTO(), STATE_FOR_GLOBAL_CONSENT_WITHOUT_SCA_AND_SMS_OTP);

        // when
        AccessMeansOrStepDTO accessMeansOrStep = dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        RedirectStep redirectStep = (RedirectStep) accessMeansOrStep.getStep();
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(LOGIN_URL);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnRedirectStepWithRedirectUrlAnd2CachedAccountsAnd1ConsentedAccountForCreateNewAccessMeansWithCorrectRequestDataForSecondConsentWithSCAWhenCreatingConsentForFirstAccount(CbiGlobeDataProviderV5 dataProvider) {
        // given
        CbiGlobeAccessMeansDTO providerState = createCbiGlobeAccessMeansDTO(FIRST_CONSENT_WITH_SCA_ID);
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = createUrlCreateAccessMeans(providerState, STATE_FOR_DETAILED_CONSENT_FOR_FIRST_ACCOUNT_WITH_SCA);

        // when
        AccessMeansOrStepDTO accessMeansOrStep = dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        // Validate access means
        assertThat(accessMeansOrStep.getAccessMeans()).isNull();

        // Validate step
        RedirectStep redirectStep = (RedirectStep) accessMeansOrStep.getStep();
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(LOGIN_URL);

        // Validate provider state
        CbiGlobeAccessMeansDTO accessMeansDTO = CbiGlobeFixtureProvider.fromProviderState(redirectStep.getProviderState(), mapper);
        assertThat(accessMeansDTO.getConsentId()).isEqualTo(FIRST_CONSENT_WITH_SCA_ID);
        assertThat(accessMeansDTO.getConsentExpiration()).isEqualTo(providerState.getConsentExpiration());
        assertThat(accessMeansDTO.getUpdated()).isEqualTo(providerState.getUpdated());
        assertThat(accessMeansDTO.getAccessTokenExpiration()).isEqualTo(providerState.getAccessTokenExpiration());

        // Validate cached accounts
        List<ProviderAccountDTO> cachedAccounts = accessMeansDTO.getCachedAccounts();
        assertThat(cachedAccounts).hasSize(2);

        // Validate account 1
        ProviderAccountDTO account1 = cachedAccounts.get(0);
        assertThat(account1.getAccountId()).isEqualTo(ACCOUNT_ID_1);
        assertThat(account1.getName()).isEqualTo("Account-1 Name");

        // Validate account 2 (With missing name)
        ProviderAccountDTO account2 = cachedAccounts.get(1);
        assertThat(account2.getAccountId()).isEqualTo(ACCOUNT_ID_2);
        assertThat(account2.getName()).isEqualTo(ACCOUNT_ID_2);

        // Validaty currently proccess account
        assertThat(accessMeansDTO.getConsentedAccounts()).hasSize(1);
        ProviderAccountDTO cachedAccount1 = accessMeansDTO.getConsentedAccounts().get("3");
        assertThat(cachedAccount1.getAccountId()).isEqualTo(ACCOUNT_ID_1);
        assertThat(cachedAccount1.getName()).isEqualTo("Account-1 Name");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnRedirectStepWithRedirectUrlAnd2CachedAccountsAnd2ConsentedAccountForCreateNewAccessMeansWithCorrectRequestDataForSecondConsentWithSCAWhenCreatingConsentForSecondAccount(CbiGlobeDataProviderV5 dataProvider) {
        // given
        CbiGlobeAccessMeansDTO providerState = createCbiGlobeAccessMeansDTOWith2CachedAccountsAnd1ConsentedAccount(FIRST_CONSENT_WITH_SCA_ID);
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = createUrlCreateAccessMeans(providerState, STATE_FOR_DETAILED_CONSENT_FOR_SECOND_ACCOUNT_WITH_SCA);

        // when
        AccessMeansOrStepDTO accessMeansOrStep = dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        // Validate step
        RedirectStep redirectStep = (RedirectStep) accessMeansOrStep.getStep();
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(LOGIN_URL_FOR_ACCOUNT_2_DETAILED_CONSENT);

        // Validate access means
//        AccessMeansDTO accessMeans = accessMeansOrStep.getAccessMeans();
//        assertThat(accessMeans.getUserId()).isEqualTo(USER_ID);
//        assertThat(accessMeans.getUpdated()).isEqualTo(Date.from(ofEpochMilli(1)));
//        assertThat(accessMeans.getExpireTime()).isEqualTo(Date.from(ofEpochMilli(3)));

        // Validate provider state
        CbiGlobeAccessMeansDTO accessMeansDTO = CbiGlobeFixtureProvider.fromProviderState(redirectStep.getProviderState(), mapper);
        assertThat(accessMeansDTO.getConsentId()).isEqualTo(FIRST_CONSENT_WITH_SCA_ID);
        assertThat(accessMeansDTO.getConsentExpiration()).isEqualTo(providerState.getConsentExpiration());
        assertThat(accessMeansDTO.getUpdated()).isEqualTo(providerState.getUpdated());
        assertThat(accessMeansDTO.getAccessTokenExpiration()).isEqualTo(providerState.getAccessTokenExpiration());

        // Validate cached accounts
        List<ProviderAccountDTO> cachedAccounts = accessMeansDTO.getCachedAccounts();
        assertThat(cachedAccounts).hasSize(2);
        assertThat(cachedAccounts.get(0).getAccountId()).isEqualTo(ACCOUNT_ID_1);
        assertThat(cachedAccounts.get(1).getAccountId()).isEqualTo(ACCOUNT_ID_2);

        // Validate consented accounts
        assertThat(accessMeansDTO.getConsentedAccounts()).hasSize(2);
        assertThat(accessMeansDTO.getConsentedAccounts().get("3").getAccountId()).isEqualTo(ACCOUNT_ID_1);
        assertThat(accessMeansDTO.getConsentedAccounts().get("4").getAccountId()).isEqualTo(ACCOUNT_ID_2);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnAccessMeansWith2CachedAccountsAnd1ConsentedAccountForCreateNewAccessMeansWithCorrectRequestWhenAllUserAccountsAreInConsentedListButConsetForSecondAccountIsNotValid(CbiGlobeDataProviderV5 dataProvider) {
        // given
        CbiGlobeAccessMeansDTO providerState = createCbiGlobeAccessMeansDTOWith2CachedAccountsAnd2ConsentedAccount(FIRST_CONSENT_WITH_SCA_ID);
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = createUrlCreateAccessMeans(providerState, STATE_FOR_DETAILED_CONSENT_FOR_SECOND_ACCOUNT_WITH_SCA);

        // when
        AccessMeansOrStepDTO accessMeansOrStep = dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        // Validate step
        accessMeansOrStep.getStep();
        assertThat(accessMeansOrStep.getStep()).isNull();

        // Validate access means
        AccessMeansDTO accessMeans = accessMeansOrStep.getAccessMeans();
        assertThat(accessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeans.getUpdated()).isEqualTo(Date.from(ofEpochMilli(1)));
        assertThat(accessMeans.getExpireTime()).isEqualTo(Date.from(ofEpochMilli(3)));

        // Validate provider state
        CbiGlobeAccessMeansDTO accessMeansDTO = CbiGlobeFixtureProvider.fromProviderState(accessMeans.getAccessMeans(), mapper);
        assertThat(accessMeansDTO.getConsentId()).isEqualTo(FIRST_CONSENT_WITH_SCA_ID);
        assertThat(accessMeansDTO.getConsentExpiration()).isEqualTo(providerState.getConsentExpiration());
        assertThat(accessMeansDTO.getUpdated()).isEqualTo(providerState.getUpdated());
        assertThat(accessMeansDTO.getAccessTokenExpiration()).isEqualTo(providerState.getAccessTokenExpiration());

        // Validate cached accounts
        List<ProviderAccountDTO> cachedAccounts = accessMeansDTO.getCachedAccounts();
        assertThat(cachedAccounts).hasSize(2);
        assertThat(cachedAccounts.get(0).getAccountId()).isEqualTo(ACCOUNT_ID_1);
        assertThat(cachedAccounts.get(1).getAccountId()).isEqualTo(ACCOUNT_ID_2);

        // Validate consented accounts
        assertThat(accessMeansDTO.getConsentedAccounts()).hasSize(1);
        assertThat(accessMeansDTO.getConsentedAccounts().get("3").getAccountId()).isEqualTo(ACCOUNT_ID_1);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldThrowTokenInvalidExceptionForRefreshAccessMeansAsThisFeatureIsUnsupported(CbiGlobeDataProviderV5 dataProvider) {
        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> dataProvider.refreshAccessMeans(null);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isInstanceOf(TokenInvalidException.class);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnDataForFetchDataWithCorrectRequestData(CbiGlobeDataProviderV5 dataProvider) throws ProviderFetchDataException, TokenInvalidException {
        // given
        CbiGlobeAccessMeansDTO givenAccessMeans = createCbiGlobeAccessMeansDTOWith2CachedAccountsAnd2ConsentedAccount(SECOND_CONSENT_WITH_SCA_ID);
        UrlFetchDataRequest urlFetchDataRequest = createUrlFetchDataRequest(givenAccessMeans);

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchDataRequest);

        // then
        // Validate accounts
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        assertThat(accounts).hasSize(2);
        accounts.forEach(ProviderAccountDTO::validate);

        // Validate account 1
        ProviderAccountDTO account1 = accounts.get(0);
        assertThat(account1.getCurrency()).isEqualTo(CurrencyCode.EUR);

        if ("BANCO_BPM".equals(dataProvider.getProviderIdentifier())) {
            assertThat(account1.getAvailableBalance()).isEqualTo("30");
            assertThat(account1.getCurrentBalance()).isEqualTo("60");
        } else {
            assertThat(account1.getAvailableBalance()).isEqualTo("60");
            assertThat(account1.getCurrentBalance()).isEqualTo("70");
        }
        assertThat(account1.getTransactions()).hasSize(2);
        account1.validate();

        // Validate booked transaction for account 1
        ProviderTransactionDTO bookedTransactionForAccount1 = account1.getTransactions().get(0);
        assertThat(bookedTransactionForAccount1.getAmount()).isEqualTo("10");
        assertThat(bookedTransactionForAccount1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(bookedTransactionForAccount1.getDateTime()).isEqualTo("2018-04-06T00:00+02:00[Europe/Rome]");
        assertThat(bookedTransactionForAccount1.getDescription()).isEqualTo("remittance Michele Buongiorno 7 reference consultant");
        bookedTransactionForAccount1.validate();

        // Validate pending transaction for account 1
        ProviderTransactionDTO pendingTransactionForAccount1 = account1.getTransactions().get(1);
        assertThat(pendingTransactionForAccount1.getAmount()).isEqualTo("20");
        assertThat(pendingTransactionForAccount1.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(pendingTransactionForAccount1.getDateTime()).isEqualTo("2018-04-06T00:00+02:00[Europe/Rome]");
        assertThat(pendingTransactionForAccount1.getDescription()).isEqualTo("remittance unstructured remittance structured");
        pendingTransactionForAccount1.validate();

        // Validate extended booked transaction for account 1
        ExtendedTransactionDTO bookedExtendedTransactionForAccount1 = bookedTransactionForAccount1.getExtendedTransaction();
        assertThat(bookedExtendedTransactionForAccount1.getCreditorName()).isEqualTo("Verdi");
        assertThat(bookedExtendedTransactionForAccount1.getBookingDate()).isEqualTo("2018-02-23T00:00+01:00[Europe/Rome]");
        assertThat(bookedExtendedTransactionForAccount1.getValueDate()).isEqualTo("2018-04-06T00:00+02:00[Europe/Rome]");
        assertThat(bookedExtendedTransactionForAccount1.getDebtorName()).isEqualTo("Gialli");
        assertThat(bookedExtendedTransactionForAccount1.getRemittanceInformationUnstructured()).isEqualTo("remittance");
        assertThat(bookedExtendedTransactionForAccount1.getRemittanceInformationStructured()).isEqualTo("Michele Buongiorno 7 reference consultant");

        // Validate extended pending transaction for account 1
        ExtendedTransactionDTO pendingExtendedTransactionForAccount1 = pendingTransactionForAccount1.getExtendedTransaction();
        assertThat(pendingExtendedTransactionForAccount1.getCreditorName()).isEqualTo("Verdi");
        assertThat(pendingExtendedTransactionForAccount1.getBookingDate()).isEqualTo("2018-02-23T00:00+01:00[Europe/Rome]");
        assertThat(pendingExtendedTransactionForAccount1.getValueDate()).isEqualTo("2018-04-06T00:00+02:00[Europe/Rome]");
        assertThat(pendingExtendedTransactionForAccount1.getDebtorName()).isEqualTo("Gialli");
        assertThat(pendingExtendedTransactionForAccount1.getRemittanceInformationUnstructured()).isEqualTo("remittance unstructured");
        assertThat(pendingExtendedTransactionForAccount1.getRemittanceInformationStructured()).isEqualTo("remittance structured");


        // Validate account 2
        ProviderAccountDTO account2 = accounts.get(1);
        assertThat(account2.getCurrentBalance()).isEqualTo("2328.15");
        if ("BANCO_BPM".equals(dataProvider.getProviderIdentifier())) {
            assertThat(account2.getAvailableBalance()).isEqualTo("3328.15");
        } else {
            assertThat(account2.getAvailableBalance()).isEqualTo("328.15");
        }

        // Validate transaction for account 2
        ProviderTransactionDTO transactionForAccount2 = account2.getTransactions().get(0);
        assertThat(transactionForAccount2.getExternalId()).isEqualTo("238423983748973");
    }


    private UrlGetLoginRequest createUrlGetLoginRequest(String state) {
        return new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setState(state)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl("https://yolt.com/callback-acc")
                .build();
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeans(CbiGlobeAccessMeansDTO accessMeansDTO, String state) {
        return createUrlCreateAccessMeans(accessMeansDTO, state, null);
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeansWithFormValues(CbiGlobeAccessMeansDTO accessMeansDTO,
                                                                                 String state) {
        return createUrlCreateAccessMeans(accessMeansDTO, state, getFilledInUserSiteFormValues());
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeans(CbiGlobeAccessMeansDTO accessMeansDTO,
                                                                   String state,
                                                                   FilledInUserSiteFormValues formValues) {
        return new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setProviderState(CbiGlobeFixtureProvider.toProviderState(accessMeansDTO, mapper))
                .setSigner(signer)
                .setState(state)
                .setFilledInUserSiteFormValues(formValues)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setBaseClientRedirectUrl("https://yolt.com/callback-acc")
                .build();
    }

    private FilledInUserSiteFormValues getFilledInUserSiteFormValues() {
        HashMap<String, String> valueMap = new HashMap<>(1);
        valueMap.put("bank", "ASPSP_MM_01");

        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(valueMap);
        return filledInUserSiteFormValues;
    }

    private UrlFetchDataRequest createUrlFetchDataRequest(CbiGlobeAccessMeansDTO accessMeansDTO) {
        return new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now().minus(Period.ofDays(200)))
                .setAccessMeans(CbiGlobeFixtureProvider.createAccessMeansDTO(accessMeansDTO, mapper))
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setRestTemplateManager(restTemplateManager)
                .build();
    }

    private CbiGlobeAccessMeansDTO createCbiGlobeAccessMeansDTO() {
        return createCbiGlobeAccessMeansDTO("", ACCESS_TOKEN);
    }

    private CbiGlobeAccessMeansDTO createCbiGlobeAccessMeansDTO(String consentId) {
        return createCbiGlobeAccessMeansDTO(consentId, ACCESS_TOKEN);
    }

    private CbiGlobeAccessMeansDTO createCbiGlobeAccessMeansDTO(String consentId, String accessToken) {
        return createCbiGlobeAccessMeansDTO(consentId, accessToken, Collections.emptyList(), Collections.emptyMap(),0);
    }

    private CbiGlobeAccessMeansDTO createCbiGlobeAccessMeansDTOWith2CachedAccountsAnd2ConsentedAccount(String consentId) {
        ProviderAccountDTO account1 =  CbiGlobeFixtureProvider.createProviderAccountDTO(ACCOUNT_ID_1);
        ProviderAccountDTO account2 =  CbiGlobeFixtureProvider.createProviderAccountDTO(ACCOUNT_ID_2);
        return createCbiGlobeAccessMeansDTO(consentId, ACCESS_TOKEN, Arrays.asList(
                account1,account2),Map.of("3",account1, "4",account2),1);
    }

    private CbiGlobeAccessMeansDTO createCbiGlobeAccessMeansDTOWith2CachedAccountsAnd1ConsentedAccount(String consentId) {
        ProviderAccountDTO account1 =  CbiGlobeFixtureProvider.createProviderAccountDTO(ACCOUNT_ID_1);
        ProviderAccountDTO account2 =  CbiGlobeFixtureProvider.createProviderAccountDTO(ACCOUNT_ID_2);
        return createCbiGlobeAccessMeansDTO(consentId, ACCESS_TOKEN, Arrays.asList(
                account1,account2),Map.of("3",account1),0);
    }

    private CbiGlobeAccessMeansDTO createCbiGlobeAccessMeansDTO(String consentId, String accessToken, List<ProviderAccountDTO> accountDTOs, Map<String,ProviderAccountDTO> consentedAccount, Integer currentlyProcessAccount) {
        return new CbiGlobeAccessMeansDTO(ofEpochMilli(1), accessToken, ofEpochMilli(2), consentId, ofEpochMilli(3), accountDTOs,consentedAccount,currentlyProcessAccount, "ASPSP_MM_01", null);
    }

    private static String readCertificate() throws IOException, URISyntaxException {
        URL resource = CbiGlobeDataProviderV5HappyFlowIntegrationTest.class
                .getClassLoader().getResource("certificates/fake-certificate.pem");

        Path filePath = new File(Objects.requireNonNull(resource).toURI()).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}