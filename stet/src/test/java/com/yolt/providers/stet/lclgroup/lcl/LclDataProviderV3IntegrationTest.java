package com.yolt.providers.stet.lclgroup.lcl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.autoonboarding.RegistrationOperation;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.lclgroup.LclGroupTestConfig;
import com.yolt.providers.stet.lclgroup.lcl.configuration.LclStetProperties;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.stet.lclgroup.common.auth.LclGroupClientConfiguration.CLIENT_ID_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = LclGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("lclgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/lcl", httpsPort = 0, port = 0)
public class LclDataProviderV3IntegrationTest {

    private static final UUID USER_ID = UUID.fromString("07a540a2-7b91-11e9-8f9e-2a86e4085a59");
    private static final String ACCESS_TOKEN = "access-token";
    private static final String ACCESS_TOKEN_FORBIDDEN = "access-token-forbidden";
    private static final String ACCESS_TOKEN_UNAUTHORIZED = "access-token-unauthorized";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String REDIRECT_URL = "http://yolt.com/redirect/lcl";
    private static final String STATE = "state";
    private static final String ACCOUNT_ID = "1";
    private static final String SECOND_ACCOUNT_ID = "2";
    private static final Instant TRANSACTIONS_FETCH_START_TIME = Instant.parse("2020-03-10T10:13:39.283Z");

    private Map<String, BasicAuthenticationMean> clientConfiguration;
    private LclGroupSampleAuthenticationMeans sampleAuthenticationMeans;

    @Autowired
    @Qualifier("LclDataProviderV3")
    private LclDataProviderV3 dataProvider;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("LclStetProperties")
    private LclStetProperties lclProperties;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private Signer signer;

    @BeforeEach
    void setup() {
        sampleAuthenticationMeans = new LclGroupSampleAuthenticationMeans();
        clientConfiguration = sampleAuthenticationMeans.getSampleAuthMeans();
    }

    @Test
    void shouldReturnAuthenticationMeansAfterAutoConfiguration() {
        // given
        Map<String, BasicAuthenticationMean> authenticationMeans = sampleAuthenticationMeans.getAutoOnBoardingAuthMeans();

        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setBaseClientRedirectUrl("https://www.example.com")
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = dataProvider.autoConfigureMeans(request);

        // then
        assertThat(configuredAuthMeans).hasSize(8);
        assertThat(configuredAuthMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("PSDNL-DNB-B0163_1");
    }

    @Test
    void shouldReturnTypedAuthenticationMeansThatWillBeAutoConfigured() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getAutoConfiguredMeans();

        // then
        assertThat(typedAuthMeans).hasSize(1);
        assertThat(typedAuthMeans.get(CLIENT_ID_NAME)).isEqualTo(CLIENT_ID_STRING);
    }

    @Test
    void shouldSuccessfullyUpdateWithNotYetRegisteredRedirectUrl() {
        // given
        Map<String, BasicAuthenticationMean> authenticationMeans = sampleAuthenticationMeans.getSampleAuthMeans();

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                authenticationMeans,
                restTemplateManager,
                signer,
                RegistrationOperation.UPDATE,
                "https://www.yolt.io"
        );
        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configuredAuthMeans).isEqualTo(authenticationMeans);
    }

    @Test
    void shouldReturnThirdVersion() {
        //when
        ProviderVersion version = dataProvider.getVersion();
        //then
        assertThat(version).isEqualTo(ProviderVersion.VERSION_3);
    }

    @Test
    void shouldReturnCorrectConsentPageUrl() {
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(STATE)
                .setAuthenticationMeans(clientConfiguration)
                .build();

        RedirectStep response = (RedirectStep) dataProvider.getLoginInfo(urlGetLoginRequest);
        assertThat(response.getRedirectUrl()).contains("https://lcl.com/authorize?");
        assertThat(response.getRedirectUrl()).contains("response_type=code");
        assertThat(response.getRedirectUrl()).contains("client_id=client-id");
        assertThat(response.getRedirectUrl()).contains("redirect_uri=http://yolt.com/redirect/lcl/");
        assertThat(response.getRedirectUrl()).contains("scope=aisp%20extended_transaction_history");
        assertThat(response.getRedirectUrl()).contains("state=state");
    }

    @Test
    void shouldCreateNewAccessMeans() {
        // given
        UrlCreateAccessMeansRequest createAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?state=state&code=auth-code")
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setAuthenticationMeans(clientConfiguration)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(LclGroupSampleAuthenticationMeans.createPreAuthorizedJsonProviderState(objectMapper, lclProperties))
                .build();

        // when
        AccessMeansOrStepDTO response = dataProvider.createNewAccessMeans(createAccessMeansRequest);

        // then
        assertThat(response.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        assertThat(response.getAccessMeans().getAccessMeans()).contains(ACCESS_TOKEN);
    }

    @Test
    void shouldThrowMissingDataExceptionWhenAuthCodeIsMissing() {
        // given
        UrlCreateAccessMeansRequest createAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL + "?state=state")
                .setAuthenticationMeans(clientConfiguration)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(LclGroupSampleAuthenticationMeans.createPreAuthorizedJsonProviderState(objectMapper, lclProperties))
                .build();

        // when
        assertThatThrownBy(() -> dataProvider.createNewAccessMeans(createAccessMeansRequest)).isExactlyInstanceOf(MissingDataException.class);
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenGetAccountsReturnsForbidden() throws JsonProcessingException {
        // given
        UrlFetchDataRequest fetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(TRANSACTIONS_FETCH_START_TIME)
                .setAccessMeans(createAccessMeans(createDataProviderState(ACCESS_TOKEN_FORBIDDEN)))
                .setAuthenticationMeans(clientConfiguration)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        assertThatThrownBy(() -> dataProvider.fetchData(fetchDataRequest)).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenGetAccountsReturnsUnauthorized() throws JsonProcessingException {
        // given
        UrlFetchDataRequest fetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(TRANSACTIONS_FETCH_START_TIME)
                .setAccessMeans(createAccessMeans(createDataProviderState(ACCESS_TOKEN_UNAUTHORIZED)))
                .setAuthenticationMeans(clientConfiguration)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        assertThatThrownBy(() -> dataProvider.fetchData(fetchDataRequest)).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldRefreshAccessMeans() throws JsonProcessingException, TokenInvalidException {
        // given
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(createAccessMeans(createDataProviderState(ACCESS_TOKEN)))
                .setAuthenticationMeans(clientConfiguration)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansDTO response = dataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getAccessMeans()).contains(ACCESS_TOKEN);
    }

    @Test
    void shouldRefreshAccessMeansWhenOldProviderStateIsAvailable() throws JsonProcessingException, TokenInvalidException {
        // given
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(createAccessMeans("""
                        {"accessToken":"access-token","refreshToken":"refresh-token","tokenType":"Bearer","idToken":"fake.fake.fake","expiresIn":3600}"""))
                .setAuthenticationMeans(clientConfiguration)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansDTO response = dataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getAccessMeans()).contains(ACCESS_TOKEN);
    }

    @Test
    void shouldFetchDataSuccessfully() throws JsonProcessingException, TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest fetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(TRANSACTIONS_FETCH_START_TIME)
                .setAccessMeans(createAccessMeans(createDataProviderState(ACCESS_TOKEN)))
                .setAuthenticationMeans(clientConfiguration)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(fetchDataRequest);

        //then
        assertThat(response.getAccounts()).hasSize(2);

        ProviderAccountDTO firstAccount = getAccountById(response, ACCOUNT_ID);
        assertThat(firstAccount.getTransactions()).hasSize(2);
        assertThat(firstAccount.getTransactions().get(0).getDateTime()).isEqualTo(convertToZonedDateTime(LocalDate.of(2019, 6, 17)));
        assertThat(firstAccount.getTransactions().get(0).getAmount()).isEqualTo(BigDecimal.valueOf(1020, 2));
        assertThat(firstAccount.getTransactions().get(1).getAmount()).isEqualTo(BigDecimal.valueOf(1225, 2));
        assertThat(firstAccount.getTransactions().get(0).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo(BigDecimal.valueOf(1020, 2));
        assertThat(firstAccount.getTransactions().get(1).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo(BigDecimal.valueOf(1225, 2));
        assertThat(firstAccount.getCurrentBalance()).isEqualTo(BigDecimal.valueOf(-100));
        assertThat(firstAccount.getAvailableBalance()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(firstAccount.getExtendedAccount().getBalances()).hasSize(3);

        ExtendedAccountDTO extendedFirstAccount = firstAccount.getExtendedAccount();
        assertThat(extendedFirstAccount.getAccountReferences()).hasSize(1);

        AccountReferenceDTO accountReference = extendedFirstAccount.getAccountReferences().get(0);
        assertThat(accountReference.getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(accountReference.getValue()).isEqualTo("FR5030002002070000025973D36");

        ProviderAccountDTO secondAccount = getAccountById(response, SECOND_ACCOUNT_ID);
        assertThat(secondAccount.getAvailableBalance()).isEqualTo(BigDecimal.valueOf(300));
        assertThat(secondAccount.getCurrentBalance()).isEqualTo(BigDecimal.valueOf(300));

        response.getAccounts().forEach(ProviderAccountDTO::validate);
    }

    private ProviderAccountDTO getAccountById(DataProviderResponse response, String accountId) {
        return response.getAccounts().stream()
                .filter(account -> account.getAccountId().equals(accountId))
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }

    private DataProviderState createDataProviderState(String accessToken) {
        DataProviderState dataProviderState = DataProviderState.authorizedProviderState(
                lclProperties.getRegions().get(0),
                accessToken,
                REFRESH_TOKEN);
        return dataProviderState;
    }

    private AccessMeansDTO createAccessMeans(final DataProviderState dataProviderState) throws JsonProcessingException {
        String serializedDataProviderState = new ObjectMapper().writeValueAsString(dataProviderState);
        return new AccessMeansDTO(USER_ID, serializedDataProviderState, new Date(), new Date());
    }

    private AccessMeansDTO createAccessMeans(final String dataProviderState) {
        return new AccessMeansDTO(USER_ID, dataProviderState, new Date(), new Date());
    }

    private ZonedDateTime convertToZonedDateTime(LocalDate date) {
        return date.atStartOfDay(ZoneId.of("Europe/Paris")).withZoneSameInstant(ZoneId.of("Z"));
    }
}
