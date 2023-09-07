package com.yolt.providers.stet.creditagricolegroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.creditagricolegroup.creditagricole.CreditAgricoleDataProviderV10;
import com.yolt.providers.stet.creditagricolegroup.creditagricole.config.CreditAgricoleProperties;
import com.yolt.providers.stet.creditagricolegroup.creditagricole.domain.CreditAgricoleRegion;
import com.yolt.providers.stet.generic.GenericDataProvider;
import com.yolt.providers.stet.generic.GenericOnboardingDataProvider;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.domain.Scope;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.FormComponent;
import nl.ing.lovebird.providershared.form.SelectField;
import nl.ing.lovebird.providershared.form.SelectOptionValue;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonMap;
import static nl.ing.lovebird.extendeddata.common.CurrencyCode.CHF;
import static nl.ing.lovebird.extendeddata.common.CurrencyCode.EUR;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CreditAgricoleGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("creditagricole")
@AutoConfigureWireMock(stubs = {
        "classpath:/stubs/creditagricole/ais/happy-flow",
        "classpath:/stubs/creditagricole/registration"}, httpsPort = 0, port = 0)
class CreditAgricoleGroupDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String CLIENT_ID = "f89a65f5-b4b4-4ef3-8bb3-3697ebb9ce04";
    private static final String STATE = "39db115e-a40d-4d56-abd1-efecfc192d52";
    private static final String BASE_CLIENT_REDIRECT_URL = "http://www.yolt.io/callback";
    private static final String NEW_BASE_CLIENT_REDIRECT_URL = "http://www.yolt.io/new/callback";
    private static final String NEW_CONTACT_EMAIL = "new@example.com";
    private static final String SELECTED_REGION_CODE = "CAM_ALPES_PROVENCE";
    private static final String AUTHORIZATION_CODE = "ff663031-c8c6-48ed-bee9-ccff2e07bee3";
    private static final String ACCESS_TOKEN = "39911f01-4613-461f-9b47-1e4ba35176e6";
    private static final String REFRESH_TOKEN = "8ebcae13-6297-4ab2-8080-2734bba3f311";
    private static final String REFRESHED_ACCESS_TOKEN = "03d9cb6e-7901-46dd-9d90-95dac39a5813";
    private static final String REDIRECT_URL_POSTED_BACK_FROM_SITE;
    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    static {
        REDIRECT_URL_POSTED_BACK_FROM_SITE = UriComponentsBuilder.fromUriString(BASE_CLIENT_REDIRECT_URL)
                .queryParam(OAuth.STATE, STATE)
                .queryParam(OAuth.CODE, AUTHORIZATION_CODE)
                .toUriString();
    }

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private Signer signer;

    @Autowired
    @Qualifier("CreditAgricoleStetProperties")
    private CreditAgricoleProperties creditAgricoleProperties;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("CreditAgricoleDataProviderV10")
    private CreditAgricoleDataProviderV10 creditAgricoleDataProvider;

    private Stream<Arguments> getDataProviders() {
        return Stream.of(Arguments.of(creditAgricoleDataProvider, creditAgricoleProperties));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldSuccessfullyRegister(GenericOnboardingDataProvider dataProvider) {
        // given
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(CreditAgricoleGroupSampleMeans.getPreconfiguredBasicAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configuredAuthMeans).containsKey("client-id");
        assertThat(configuredAuthMeans.get("client-id").getValue()).isEqualTo(CLIENT_ID);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldSuccessfullyUpdateRegistration(GenericOnboardingDataProvider dataProvider) {
        // given
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(CreditAgricoleGroupSampleMeans.getConfiguredAuthenticationMeans(NEW_CONTACT_EMAIL))
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setBaseClientRedirectUrl(NEW_BASE_CLIENT_REDIRECT_URL)
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configuredAuthMeans).containsKey("client-id");
        assertThat(configuredAuthMeans.get("client-id").getValue()).isEqualTo(CLIENT_ID);
        assertThat(configuredAuthMeans).containsKey("client-contact-email");
        assertThat(configuredAuthMeans.get("client-contact-email").getValue()).isEqualTo(NEW_CONTACT_EMAIL);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnFormStepWithAvailableRegions(GenericDataProvider dataProvider, DefaultProperties properties) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setAuthenticationMeans(CreditAgricoleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        Step step = dataProvider.getLoginInfo(request);

        // then
        List<FormComponent> components = ((FormStep) step).getForm().getFormComponents();
        assertThat(components).hasSize(1);

        FormComponent component = components.get(0);
        assertThat(component).isInstanceOf(SelectField.class);
        assertThat(component.isOptional()).isFalse();

        SelectField selectField = (SelectField) component;
        assertThat(selectField.getId()).isEqualTo("region");
        assertThat(selectField.getSelectOptionValues()).allSatisfy(validateSelectOptionValue(properties));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRedirectStepForSelectedRegion(GenericDataProvider dataProvider, DefaultProperties properties) {
        // given
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(singletonMap("region", SELECTED_REGION_CODE));

        Region expectedRegion = properties.getRegionByCode(SELECTED_REGION_CODE);
        String expectedAuthorizationUrl = UriComponentsBuilder.fromUriString(expectedRegion.getAuthUrl())
                .queryParam(OAuth.CLIENT_ID, CLIENT_ID)
                .queryParam(OAuth.RESPONSE_TYPE, OAuth.CODE)
                .queryParam(OAuth.SCOPE, Scope.AISP_EXTENDED_TRANSACTION_HISTORY.getValue())
                .queryParam(OAuth.STATE, STATE)
                .queryParam(OAuth.REDIRECT_URI, BASE_CLIENT_REDIRECT_URL)
                .toUriString();

        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setState(STATE)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setAuthenticationMeans(CreditAgricoleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setProviderState(CreditAgricoleGroupSampleMeans.createEmptyJsonProviderState(objectMapper))
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(accessMeansOrStepDTO.getAccessMeans()).isNull();
        assertThat(accessMeansOrStepDTO.getStep()).isInstanceOf(RedirectStep.class);

        RedirectStep redirectStep = (RedirectStep) accessMeansOrStepDTO.getStep();
        assertThat(redirectStep.getExternalConsentId()).isNull();
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(expectedAuthorizationUrl);

        DataProviderState providerState = CreditAgricoleGroupSampleMeans.createProviderState(objectMapper, redirectStep.getProviderState());
        assertThat(providerState.getRegion()).isEqualTo(expectedRegion);
        assertThat(providerState.getCodeVerifier()).isNull();
        assertThat(providerState.getAccessToken()).isNull();
        assertThat(providerState.getRefreshToken()).isNull();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldCreateNewAccessMeans(GenericDataProvider dataProvider, DefaultProperties properties) {
        // given
        Region selectedRegion = properties.getRegionByCode(SELECTED_REGION_CODE);

        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setUserId(USER_ID)
                .setAuthenticationMeans(CreditAgricoleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setState(STATE)
                .setProviderState(CreditAgricoleGroupSampleMeans.createPreAuthorizedJsonProviderState(objectMapper, selectedRegion))
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL_POSTED_BACK_FROM_SITE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(request);

        // then
        AccessMeansDTO accessMeansDTO = accessMeansOrStepDTO.getAccessMeans();
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeansDTO.getUpdated()).isCloseTo(new Date(), Duration.ofSeconds(2).toMillis());
        assertThat(accessMeansDTO.getExpireTime()).isAfter(new Date());

        DataProviderState providerState = CreditAgricoleGroupSampleMeans.createProviderState(objectMapper, accessMeansDTO.getAccessMeans());
        assertThat(providerState.getRegion()).isEqualTo(selectedRegion);
        assertThat(providerState.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(providerState.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldRefreshAccessMeans(GenericDataProvider dataProvider, DefaultProperties properties) throws TokenInvalidException {
        // given
        Region selectedRegion = properties.getRegionByCode(SELECTED_REGION_CODE);
        String jsonProviderState = CreditAgricoleGroupSampleMeans.createAuthorizedJsonProviderState(objectMapper, selectedRegion, ACCESS_TOKEN, REFRESH_TOKEN);

        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(USER_ID, jsonProviderState, new Date(), new Date())
                .setAuthenticationMeans(CreditAgricoleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansDTO accessMeansDTO = dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeansDTO.getUpdated()).isCloseTo(new Date(), Duration.ofSeconds(2).toMillis());
        assertThat(accessMeansDTO.getExpireTime()).isAfter(new Date());

        DataProviderState providerState = CreditAgricoleGroupSampleMeans.createProviderState(objectMapper, accessMeansDTO.getAccessMeans());
        assertThat(providerState.getRegion()).isEqualTo(selectedRegion);
        assertThat(providerState.getAccessToken()).isEqualTo(REFRESHED_ACCESS_TOKEN);
        assertThat(providerState.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldRefreshAccessMeansWithOldProviderState(GenericDataProvider dataProvider, DefaultProperties properties) throws TokenInvalidException {
        // given
        Region expectedRegion = properties.getRegionByCode(SELECTED_REGION_CODE);
        CreditAgricoleRegion selectedRegion = CreditAgricoleRegion.valueOf(SELECTED_REGION_CODE);
        String jsonProviderState = CreditAgricoleGroupSampleMeans.createOldJsonProviderState(objectMapper, selectedRegion, ACCESS_TOKEN, REFRESH_TOKEN);

        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(USER_ID, jsonProviderState, new Date(), new Date())
                .setAuthenticationMeans(CreditAgricoleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansDTO accessMeansDTO = dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeansDTO.getUpdated()).isCloseTo(new Date(), Duration.ofSeconds(2).toMillis());
        assertThat(accessMeansDTO.getExpireTime()).isAfter(new Date());

        DataProviderState providerState = CreditAgricoleGroupSampleMeans.createProviderState(objectMapper, accessMeansDTO.getAccessMeans());
        assertThat(providerState.getRegion()).isEqualTo(expectedRegion);
        assertThat(providerState.getAccessToken()).isEqualTo(REFRESHED_ACCESS_TOKEN);
        assertThat(providerState.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldSuccessfullyFetchData(GenericDataProvider dataProvider, DefaultProperties properties) throws TokenInvalidException, ProviderFetchDataException {
        // given
        Region selectedRegion = properties.getRegionByCode(SELECTED_REGION_CODE);
        String jsonProviderState = CreditAgricoleGroupSampleMeans.createAuthorizedJsonProviderState(objectMapper, selectedRegion, ACCESS_TOKEN, REFRESH_TOKEN);

        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setAccessMeans(USER_ID, jsonProviderState, new Date(), new Date())
                .setAuthenticationMeans(CreditAgricoleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(LocalDate.parse("2019-12-31").atStartOfDay(ZoneId.of("Europe/Paris")).toInstant())
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchDataRequest);

        // then
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        assertThat(accounts).hasSize(3);

        assertThat(accounts.get(0)).satisfies(validateProviderAccountDTO("1", "FR7617806004800000010000149", "89530.92", EUR));
        assertThat(accounts.get(1)).satisfies(validateProviderAccountDTO("2", "FR7617806004800000010000252", "2343.63", EUR));
        assertThat(accounts.get(2)).satisfies(validateProviderAccountDTO("4", "FR7617806004800000010000458", "733.63", CHF));

        List<ProviderTransactionDTO> account1Transactions = accounts.get(0).getTransactions();
        assertThat(account1Transactions).hasSize(3);
        assertThat(account1Transactions.get(0)).satisfies(validateProviderTransactionDTO("5503800000499", "-453.36", DEBIT));
        assertThat(account1Transactions.get(1)).satisfies(validateProviderTransactionDTO("5503800000498", "2465.23", CREDIT));
        assertThat(account1Transactions.get(2)).satisfies(validateProviderTransactionDTO("5503800000387", "-33.15", DEBIT));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldSuccessfullyFetchDataWithOldProviderState(GenericDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        CreditAgricoleRegion selectedRegion = CreditAgricoleRegion.valueOf(SELECTED_REGION_CODE);
        String jsonProviderState = CreditAgricoleGroupSampleMeans.createOldJsonProviderState(objectMapper, selectedRegion, ACCESS_TOKEN, REFRESH_TOKEN);

        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setAccessMeans(USER_ID, jsonProviderState, new Date(), new Date())
                .setAuthenticationMeans(CreditAgricoleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(LocalDate.parse("2019-12-31").atStartOfDay(ZoneId.of("Europe/Paris")).toInstant())
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchDataRequest);

        // then
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        assertThat(accounts).hasSize(3);

        assertThat(accounts.get(0)).satisfies(validateProviderAccountDTO("1", "FR7617806004800000010000149", "89530.92", EUR));
        assertThat(accounts.get(1)).satisfies(validateProviderAccountDTO("2", "FR7617806004800000010000252", "2343.63", EUR));
        assertThat(accounts.get(2)).satisfies(validateProviderAccountDTO("4", "FR7617806004800000010000458", "733.63", CHF));

        List<ProviderTransactionDTO> account1Transactions = accounts.get(0).getTransactions();
        assertThat(account1Transactions).hasSize(3);
        assertThat(account1Transactions.get(0)).satisfies(validateProviderTransactionDTO("5503800000499", "-453.36", DEBIT));
        assertThat(account1Transactions.get(1)).satisfies(validateProviderTransactionDTO("5503800000498", "2465.23", CREDIT));
        assertThat(account1Transactions.get(2)).satisfies(validateProviderTransactionDTO("5503800000387", "-33.15", DEBIT));
    }

    private Consumer<SelectOptionValue> validateSelectOptionValue(DefaultProperties properties) {
        return selectOptionValue -> {
            Predicate<Region> predicate = (region) ->
                    StringUtils.equals(region.getCode(), selectOptionValue.getValue()) &&
                            StringUtils.equals(region.getName(), selectOptionValue.getDisplayName());

            List<Region> correspondingRegions = properties.getRegions().stream()
                    .filter(predicate)
                    .collect(Collectors.toList());

            assertThat(correspondingRegions).hasSize(1);
        };
    }

    Consumer<ProviderAccountDTO> validateProviderAccountDTO(String accountId,
                                                            String iban,
                                                            String amount,
                                                            CurrencyCode currency) {
        return (providerAccountDTO) -> {
            providerAccountDTO.validate();

            assertThat(providerAccountDTO.getAccountId()).isEqualTo(accountId);
            assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
            assertThat(providerAccountDTO.getAvailableBalance()).isNull();
            assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(amount);
            assertThat(providerAccountDTO.getCurrency()).isEqualTo(currency);
            assertThat(providerAccountDTO.getLastRefreshed()).isNotNull();
            assertThat(providerAccountDTO.getName()).isNotEmpty();

            ProviderAccountNumberDTO providerAccountNumberDTO = providerAccountDTO.getAccountNumber();
            assertThat(providerAccountNumberDTO.getHolderName()).isEqualTo(providerAccountDTO.getName());
            assertThat(providerAccountNumberDTO.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
            assertThat(providerAccountNumberDTO.getIdentification()).isEqualTo(iban);

            ExtendedAccountDTO extendedAccountDTO = providerAccountDTO.getExtendedAccount();
            assertThat(extendedAccountDTO.getResourceId()).isEqualTo(accountId);
            assertThat(extendedAccountDTO.getName()).isEqualTo(providerAccountDTO.getName());
            assertThat(extendedAccountDTO.getCashAccountType()).isEqualTo(ExternalCashAccountType.CURRENT);

            AccountReferenceDTO accountReferenceDTO = extendedAccountDTO.getAccountReferences().get(0);
            assertThat(accountReferenceDTO.getType()).isEqualTo(AccountReferenceType.IBAN);
            assertThat(accountReferenceDTO.getValue()).isEqualTo(iban);

            List<BalanceDTO> balanceDTOs = extendedAccountDTO.getBalances();
            assertThat(balanceDTOs).hasSize(1);
            assertThat(balanceDTOs.get(0)).satisfies(validateBalanceDTO(amount, currency));
        };
    }

    Consumer<BalanceDTO> validateBalanceDTO(String amount, CurrencyCode currency) {
        return (balanceDTO) -> {
            assertThat(balanceDTO.getBalanceType()).isEqualTo(BalanceType.CLOSING_BOOKED);
            assertThat(balanceDTO.getReferenceDate()).isEqualTo("2019-01-30T00:00+01:00[Europe/Paris]");

            BalanceAmountDTO balanceAmountDTO = balanceDTO.getBalanceAmount();
            assertThat(balanceAmountDTO.getAmount()).isEqualTo(amount);
            assertThat(balanceAmountDTO.getCurrency()).isEqualTo(currency);
        };
    }

    Consumer<ProviderTransactionDTO> validateProviderTransactionDTO(String entryReference,
                                                                    String amount,
                                                                    ProviderTransactionType transactionType) {
        return (providerTransactionDTO) -> {
            providerTransactionDTO.validate();

            assertThat(providerTransactionDTO.getExternalId()).isEqualTo(entryReference);
            assertThat(providerTransactionDTO.getDateTime()).isEqualTo("2019-01-28T00:00+01:00[Europe/Paris]");
            assertThat(providerTransactionDTO.getAmount()).isEqualTo(new BigDecimal(amount).abs());
            assertThat(providerTransactionDTO.getDescription()).isNotEmpty();
            assertThat(providerTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);
            assertThat(providerTransactionDTO.getType()).isEqualTo(transactionType);
            assertThat(providerTransactionDTO.getStatus()).isEqualTo(TransactionStatus.BOOKED);

            ExtendedTransactionDTO extendedTransactionDTO = providerTransactionDTO.getExtendedTransaction();
            assertThat(extendedTransactionDTO.getEntryReference()).isEqualTo(entryReference);
            assertThat(extendedTransactionDTO.getBookingDate()).isEqualTo("2019-01-28T00:00+01:00[Europe/Paris]");
            assertThat(extendedTransactionDTO.getValueDate()).isEqualTo("2019-01-29T00:00+01:00[Europe/Paris]");
            assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isNotEmpty();

            BalanceAmountDTO amountDTO = extendedTransactionDTO.getTransactionAmount();
            assertThat(amountDTO.getAmount()).isEqualTo(amount);
            assertThat(amountDTO.getCurrency()).isEqualTo(EUR);
        };
    }

    Consumer<ProviderAccountNumberDTO> validateProviderAccountNumberDTO(String iban, String holderName) {
        return (providerAccountNumberDTO) -> {
            assertThat(providerAccountNumberDTO.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
            assertThat(providerAccountNumberDTO.getIdentification()).isEqualTo(iban);
            assertThat(providerAccountNumberDTO.getHolderName()).isEqualTo(holderName);
        };
    }
}
