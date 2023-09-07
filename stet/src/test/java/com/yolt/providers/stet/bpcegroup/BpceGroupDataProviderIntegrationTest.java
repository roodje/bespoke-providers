package com.yolt.providers.stet.bpcegroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.stet.bpcegroup.banquepopulaire.BanquePopulaireDataProviderV6;
import com.yolt.providers.stet.bpcegroup.caissedepargneparticuliers.CaisseDepargneParticuliersDataProviderV6;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providerdomain.YoltCategory;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.stet.bpcegroup.common.auth.BpceAuthenticationMeansSupplier.*;
import static org.assertj.core.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/bpce/ais", httpsPort = 0, port = 0)
@Import(BpceGroupTestConfig.class)
@ActiveProfiles("bpce")
public class BpceGroupDataProviderIntegrationTest {

    private static final String REDIRECT_URL = "https://localhost/redirect/auth";
    private static final String CODE = "THE_CODE";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    public static final String REGION_CODE = "BPBFC";
    private final BpceGroupSampleAuthenticationMeans sampleAuthenticationMeans = new BpceGroupSampleAuthenticationMeans();

    private Signer signer;

    @Value("${wiremock.server.https-port}")
    private int port;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private BanquePopulaireDataProviderV6 banquePopulaireDataProviderV6;

    @Autowired
    private CaisseDepargneParticuliersDataProviderV6 caisseDepargneParticuliersDataProviderV6;

    @Autowired
    @Qualifier("BanquePopulaireStetProperties")
    private DefaultProperties properties;

    private Stream<UrlDataProvider> getBpceGroupProviders() {
        return Stream.of(banquePopulaireDataProviderV6, caisseDepargneParticuliersDataProviderV6);
    }

    @BeforeAll
    public void setUp() throws Exception {
        PrivateKey signingKey = KeyUtil.createPrivateKeyFromPemFormat((readCertificates("certificates/bpce/example_client_signing.key")));
        signer = new BpceGroupTestSigner(signingKey);
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    public void shouldReturnAuthenticationMeans(UrlDataProvider dataProvider) {
        //given
        final Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>() {{
            put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
            put(CLIENT_SIGNING_KEY_ID, TypedAuthenticationMeans.KEY_ID);
            put(CLIENT_TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID);
            put(CLIENT_TRANSPORT_CERTIFICATE, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        }};

        // when
        Map<String, TypedAuthenticationMeans> actualTypedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(actualTypedAuthMeans).containsAllEntriesOf(typedAuthenticationMeans);
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    void shouldReturnChangedAuthenticationMeansAfterAutoConfiguration(AutoOnboardingProvider dataProvider) {
        // given
        String redirectUri = "https://sample.redirect.uri/testparam";
        var changedAuthMeans = sampleAuthenticationMeans.getPostRegistrationAuthenticationMeans();
        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(changedAuthMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setBaseClientRedirectUrl(redirectUri)
                .setRedirectUrls(Collections.singletonList(redirectUri))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = dataProvider.autoConfigureMeans(request);

        // then
        assertThat(configuredAuthMeans).containsExactlyEntriesOf(changedAuthMeans);
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    void shouldReturnAuthenticationMeansAfterAutoConfiguration(AutoOnboardingProvider dataProvider) {
        // given
        String redirectUri = "https://sample.redirect.uri/testparam";
        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(sampleAuthenticationMeans.getPreRegistrationAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setBaseClientRedirectUrl(redirectUri)
                .setRedirectUrls(Collections.singletonList(redirectUri))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = dataProvider.autoConfigureMeans(request);

        // then
        assertThat(configuredAuthMeans).containsKeys(CLIENT_ID);
        assertThat(configuredAuthMeans.get(CLIENT_ID).getValue()).isEqualTo("1PSDNL-DNB-33031431-0-RWMH");
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    public void shouldThrowMissingAuthenticationMeansExceptionWhenAuthMeansAreEmpty(UrlDataProvider dataProvider) {
        // given
        FilledInUserSiteFormValues userSiteFormValues = new FilledInUserSiteFormValues();
        userSiteFormValues.setValueMap(Collections.singletonMap("region", REGION_CODE));
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + CODE)
                .setAuthenticationMeans(Collections.emptyMap())
                .setFilledInUserSiteFormValues(userSiteFormValues)
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.createNewAccessMeans(request);

        // then
        assertThatThrownBy(throwingCallable)
                .isInstanceOf(MissingAuthenticationMeansException.class);
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    public void shouldReturnFormStepWithRegion(UrlDataProvider dataProvider) {
        // given
        String loginState = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(loginState)
                .setAuthenticationMeans(sampleAuthenticationMeans.getBasicAuthenticationMeans())
                .build();

        // when
        FormStep formStep = (FormStep) dataProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(formStep.getForm().getFormComponents().get(0).getDisplayName()).isEqualTo("Région");
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    public void shouldReturnLoginUrlWhenCreateNewAccessMeansCalledWithoutRedirectUrlPostedBackFromSite(UrlDataProvider dataProvider) {
        // given
        FilledInUserSiteFormValues userSiteFormValues = new FilledInUserSiteFormValues();
        userSiteFormValues.setValueMap(Collections.singletonMap("region", REGION_CODE));
        String redirectUri = "htpps://sample.redirect.uri/testparam";
        String state = "test-state";
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setAuthenticationMeans(sampleAuthenticationMeans.getBasicAuthenticationMeans())
                .setFilledInUserSiteFormValues(userSiteFormValues)
                .setProviderState(sampleAuthenticationMeans.createEmptyJsonProviderState(objectMapper))
                .setBaseClientRedirectUrl(redirectUri)
                .setState(state)
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

        assertThat(actualQueryParams).hasSize(5);
        assertThat(actualQueryParams.get("response_type")).isEqualTo("code");
        assertThat(actualQueryParams.get("client_id")).isEqualTo("THE_CLIENT_ID");
        assertThat(actualQueryParams.get("scope")).isEqualTo("aisp");
        assertThat(actualQueryParams.get("redirect_uri")).isEqualTo(redirectUri);
        assertThat(actualQueryParams.get("state")).isEqualTo(state);
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    public void shouldCreateNewAccessMeans(UrlDataProvider dataProvider) {
        // given
        Region selectedRegion = properties.getRegionByCode(REGION_CODE);

        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?code=" + CODE)
                .setProviderState(sampleAuthenticationMeans.createPreAuthorizedJsonProviderState(objectMapper, selectedRegion))
                .setAuthenticationMeans(sampleAuthenticationMeans.getBasicAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(request);

        // then
        AccessMeansDTO accessMeansDTO = accessMeansOrStepDTO.getAccessMeans();
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeansDTO.getUpdated()).isCloseTo(new Date(), Duration.ofSeconds(4).toMillis());
        assertThat(accessMeansDTO.getExpireTime()).isAfter(new Date());

        DataProviderState providerState = sampleAuthenticationMeans.createProviderState(objectMapper, accessMeansDTO.getAccessMeans());
        assertThat(providerState.getRegion()).isEqualTo(selectedRegion);
        assertThat(providerState.getAccessToken()).isEqualTo(TOKEN);
        assertThat(providerState.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    public void shouldRefreshAccessMeans(UrlDataProvider dataProvider) throws TokenInvalidException {
        // given
        Region selectedRegion = properties.getRegionByCode(REGION_CODE);
        String jsonProviderState = sampleAuthenticationMeans.createAuthorizedJsonProviderState(objectMapper, selectedRegion, TOKEN, REFRESH_TOKEN);

        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(sampleAuthenticationMeans.getBasicAuthenticationMeans())
                .setAccessMeans(USER_ID, jsonProviderState, new Date(), new Date())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansDTO accessMeans = dataProvider.refreshAccessMeans(request);

        // then
        assertThat(accessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeans.getUpdated()).isCloseTo(new Date(), Duration.ofSeconds(4).toMillis());
        assertThat(accessMeans.getExpireTime()).isAfter(new Date());

        DataProviderState providerState = sampleAuthenticationMeans.createProviderState(objectMapper, accessMeans.getAccessMeans());
        assertThat(providerState.getRegion()).isEqualTo(selectedRegion);
        assertThat(providerState.getAccessToken()).isEqualTo(TOKEN);
        assertThat(providerState.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    public void shouldRefreshAccessMeansWithPreMigrationAccessMeans(UrlDataProvider dataProvider) throws TokenInvalidException {
        // given
        Region selectedRegion = properties.getRegionByBaseUrl("https://localhost:" + port);
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID,
                "{\"expires_in\":7193,\"access_token\":\"old-access-token\",\"refresh_token\":\"refresh-token\",\"token_type\":\"Bearer\",\"scope\":\"aisp offline_access\", \"regionalBaseUrl\":\"https://localhost:" + port + "\"}",
                new Date(),
                new Date());

        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(sampleAuthenticationMeans.getBasicAuthenticationMeans())
                .setAccessMeans(accessMeansDTO)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansDTO accessMeans = dataProvider.refreshAccessMeans(request);

        // then
        assertThat(accessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeans.getUpdated()).isCloseTo(new Date(), Duration.ofSeconds(4).toMillis());
        assertThat(accessMeans.getExpireTime()).isAfter(new Date());

        DataProviderState providerState = sampleAuthenticationMeans.createProviderState(objectMapper, accessMeans.getAccessMeans());
        assertThat(providerState.getRegion()).isEqualTo(selectedRegion);
        assertThat(providerState.getAccessToken()).isEqualTo(TOKEN);
        assertThat(providerState.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    public void shouldThrowTokenInvalidExceptionForInvalidAccessToken(UrlDataProvider dataProvider) {
        // given
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID,
                "{\"expires_in\":7193,\"access_token\":\"old-access-token\",\"refresh_token\":\"refresh-token2\",\"token_type\":\"Bearer\",\"scope\":\"aisp offline_access\", \"regionalBaseUrl\":\"https://localhost:" + port + "\"}",
                new Date(),
                new Date());

        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(sampleAuthenticationMeans.getBasicAuthenticationMeans())
                .setAccessMeans(accessMeansDTO)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.refreshAccessMeans(request);

        // then
        assertThatThrownBy(throwingCallable)
                .isInstanceOf(TokenInvalidException.class)
                .hasMessage("Invalid grant, refresh token is invalid: HTTP 400");
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    public void shouldSuccessfullyFetchData(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        String jsonProviderState = sampleAuthenticationMeans.createAuthorizedJsonProviderState(objectMapper,
                properties.getRegionByCode(REGION_CODE),
                "access-token",
                null);

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, jsonProviderState, new Date(), new Date());
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setTransactionsFetchStartTime(Instant.now())
                .setUserSiteId(UUID.randomUUID())
                .setAccessMeans(accessMeansDTO)
                .setSigner(signer)
                .setAuthenticationMeans(sampleAuthenticationMeans.getBasicAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(request);

        // then
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        assertThat(accounts).hasSize(1);
        ProviderAccountDTO actualAccount = accounts.get(0);
        assertThat(actualAccount.getCurrentBalance().toString()).isEqualTo("-150.00");
        assertThat(actualAccount.getAvailableBalance().toString()).isEqualTo("-140.00");
        assertThat(actualAccount.getName()).isEqualTo("LEA SANDBOXA");
        assertThat(actualAccount.getAccountNumber().getIdentification()).isEqualTo("FR7610907000301234567890125");
        assertThat(actualAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);

        List<ProviderTransactionDTO> transactions = actualAccount.getTransactions();
        assertThat(transactions).hasSize(11);
        ProviderTransactionDTO actualTransaction = transactions.get(0);
        assertThat(actualTransaction.getDateTime()).isEqualTo("2022-08-25T00:00+02:00[Europe/Paris]");
        assertThat(actualTransaction.getAmount()).isEqualTo("3.99");
        assertThat(actualTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(actualTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(actualTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(actualTransaction.getDescription()).isEqualTo("070422 CB****7401 TooGoodToG al4kDK toogoodtogo. 3,99EUR 1 EURO = 1,000000");

        ExtendedTransactionDTO extendedTransaction = actualTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2022-08-25T00:00+02:00[Europe/Paris]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2022-04-08T00:00+02:00[Europe/Paris]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-3.99");
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("070422 CB****7401 TooGoodToG al4kDK toogoodtogo. 3,99EUR 1 EURO = 1,000000");
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    public void shouldThrowBackPressureRequestException(UrlDataProvider dataProvider) {
        // given
        String jsonProviderState = sampleAuthenticationMeans.createAuthorizedJsonProviderState(objectMapper,
                properties.getRegionByCode(REGION_CODE),
                "access-token-429",
                null);
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, jsonProviderState, new Date(), new Date());
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setTransactionsFetchStartTime(Instant.now())
                .setUserSiteId(UUID.randomUUID())
                .setAccessMeans(accessMeansDTO)
                .setSigner(signer)
                .setAuthenticationMeans(sampleAuthenticationMeans.getBasicAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        Throwable call = catchThrowable(() -> dataProvider.fetchData(request));

        //then
        assertThat(call).isInstanceOf(BackPressureRequestException.class).hasMessageContaining("HTTP:429 Too Many Requests");
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    public void shouldReturnEmptyListOfTransactionWhenNoIncomingTransactionsFromApi(UrlDataProvider dataProvider) throws Exception {
        // given
        String jsonProviderState = sampleAuthenticationMeans.createAuthorizedJsonProviderState(objectMapper,
                properties.getRegionByCode(REGION_CODE),
                "empty-transactions-token",
                null);

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, jsonProviderState, new Date(), new Date());
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setTransactionsFetchStartTime(Instant.now())
                .setUserSiteId(UUID.randomUUID())
                .setAccessMeans(accessMeansDTO)
                .setSigner(signer)
                .setAuthenticationMeans(sampleAuthenticationMeans.getBasicAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(request);

        // then
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getTransactions()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("getBpceGroupProviders")
    public void shouldReturnEmptyAccountListWhenHttpStatusIs404AndMessageIsNAAC(UrlDataProvider dataProvider) throws Exception {
        // given
        String jsonProviderState = sampleAuthenticationMeans.createAuthorizedJsonProviderState(objectMapper,
                properties.getRegionByCode(REGION_CODE),
                "access-token-NAAC",
                null);

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, jsonProviderState, new Date(), new Date());
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setTransactionsFetchStartTime(Instant.now())
                .setUserSiteId(UUID.randomUUID())
                .setAccessMeans(accessMeansDTO)
                .setSigner(signer)
                .setAuthenticationMeans(sampleAuthenticationMeans.getBasicAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(request);

        // then
        assertThat(dataProviderResponse.getAccounts()).isEmpty();
    }

    private static String readCertificates(String file) {
        try {
            URI fileURI = BpceGroupDataProviderIntegrationTest.class
                    .getClassLoader()
                    .getResource(file)
                    .toURI();
            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
