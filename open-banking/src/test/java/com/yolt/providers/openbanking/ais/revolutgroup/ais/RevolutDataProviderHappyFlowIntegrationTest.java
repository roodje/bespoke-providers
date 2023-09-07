package com.yolt.providers.openbanking.ais.revolutgroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.openbanking.ais.TestConfiguration;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.revolutgroup.RevolutSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.revolutgroup.RevolutTestApp;
import com.yolt.providers.openbanking.ais.revolutgroup.common.auth.RevolutEuAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.revolutgroup.common.auth.RevolutGbAuthMeansBuilderV2;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.openbanking.ais.revolutgroup.common.auth.RevolutEuAuthMeansBuilderV2.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * This test contains all happy flows occurring in Revolut provider.
 * <p>
 * Revolut follows OB standard except refresh token flow. Hence refresh token test is expected
 * to led user in re-consent flow by throwing TokenInvalidException
 * <p>
 * Covered flows:
 * - remove old & create new registration on bank side using autoonboarding
 * - acquiring consent page
 * - fetching accounts, balances, transactions
 * - creating access means
 * - refreshing access means
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {RevolutTestApp.class, TestConfiguration.class, OpenbankingConfiguration.class}, webEnvironment = RANDOM_PORT)
@ActiveProfiles("revolut")
@AutoConfigureWireMock(stubs = "classpath:/stubs/revolut/ais-3.1.0/happy-flow", httpsPort = 0, port = 0)
public class RevolutDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID USER_SITE_ID = UUID.randomUUID();

    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("RevolutDataProviderV10")
    private GenericBaseDataProvider revolutGbDataProviderV10;


    @Autowired
    @Qualifier("RevolutEuDataProviderV8")
    private GenericBaseDataProvider revolutEuDataProviderV8;

    @Autowired
    ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private Stream<UrlDataProvider> getDataProvidersWithEidasCerts() {
        return Stream.of(revolutEuDataProviderV8);
    }

    private Stream<UrlDataProvider> getDataProvidersWithNewOBCerts() {
        return Stream.of(revolutGbDataProviderV10);
    }

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(revolutEuDataProviderV8, revolutGbDataProviderV10);
    }

    private Stream<UrlDataProvider> getDataProvidersWithoutClientIdInBodyForClientCredentialsGrant() {
        return Stream.of(revolutEuDataProviderV8, revolutGbDataProviderV10);
    }

    private Stream<Arguments> getDataProvidersWithClientIdInBodyForClientCredentialsGrant() {
        return Stream.of(
                Arguments.of(revolutGbDataProviderV10, "4616835c-f9d1-4db6-bb50-95519cc266cb") // RealTechnologies and YTS Credit Scoring App
        );
    }

    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private Map<String, BasicAuthenticationMean> authenticationMeansForEidas;
    private Map<String, BasicAuthenticationMean> authenticationMeansNewOBCerts;

    @BeforeEach
    public void beforeEach() throws Exception {
        RevolutSampleAuthenticationMeans revolutSampleAuthenticationMeans = new RevolutSampleAuthenticationMeans();
        authenticationMeans = revolutSampleAuthenticationMeans.getAuthenticationMeans();
        authenticationMeansForEidas = revolutSampleAuthenticationMeans.getAuthenticationMeansForEidasRegistration();
        authenticationMeansNewOBCerts = revolutSampleAuthenticationMeans.getAuthenticationMeansNewOBCerts();
        restTemplateManagerMock = new RestTemplateManagerMock(externalRestTemplateBuilderFactory);
        signer = new SignerMock();
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithoutClientIdInBodyForClientCredentialsGrant")
    public void shouldReturnConsentPageUrlWithoutAuthorizationHeaderAndWithoutClientIdInBodyForClientCredentialsGrant(UrlDataProvider dataProvider) {
        // given
        final String loginState = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) dataProvider.getLoginInfo(urlGetLogin);

        // then
        final String clientId = "06951f38-6aed-4005-a54e-f039f978f119";
        final String scope = "openid+accounts";
        assertThat(loginInfo.getRedirectUrl()).contains("?response_type=code+id_token&client_id=" + clientId + "&state=" + loginState + "&scope=" + scope + "&nonce=" + loginState + "&redirect_uri=http%3A%2F%2Fyolt.com%2Fidentifier&request=");
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("50ca5ed5-317c-451c-8438-3b3fb91466e1");
        verify(postRequestedFor(urlEqualTo("/token"))
                .withoutHeader("Authorization")
                .withHeader("x-fapi-financial-id", containing("0032400006451CCVVX"))
                .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                .withRequestBody(equalTo("grant_type=client_credentials&scope=accounts"))
        );
        verify(postRequestedFor(urlEqualTo("/account-access-consents"))
                .withHeader("Authorization", containing("Bearer 978e46f2-b8a5-4f81-ac45-f10d32e6b764"))
                .withHeader("x-fapi-financial-id", containing("0032400006451CCVVX"))
                .withHeader("Content-Type", containing("application/json;charset=UTF-8"))
        );
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithClientIdInBodyForClientCredentialsGrant")
    public void shouldReturnConsentPageUrlWithoutAuthorizationHeaderAndWithClientIdInBodyForClientCredentialsGrant(UrlDataProvider dataProvider, String clientId) {
        // given
        authenticationMeans.remove(RevolutGbAuthMeansBuilderV2.CLIENT_ID_NAME);
        authenticationMeans.put(RevolutGbAuthMeansBuilderV2.CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), clientId));
        final String loginState = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setAuthenticationMeansReference(new AuthenticationMeansReference(UUID.fromString(clientId), null))
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) dataProvider.getLoginInfo(urlGetLogin);

        // then
        final String scope = "openid+accounts";
        assertThat(loginInfo.getRedirectUrl()).contains("?response_type=code+id_token&client_id=" + clientId + "&state=" + loginState + "&scope=" + scope + "&nonce=" + loginState + "&redirect_uri=http%3A%2F%2Fyolt.com%2Fidentifier&request=");
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("50ca5ed5-317c-451c-8438-3b3fb91466e1");
        verify(postRequestedFor(urlEqualTo("/token"))
                .withoutHeader("Authorization")
                .withHeader("x-fapi-financial-id", containing("0032400006451CCVVX"))
                .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                .withRequestBody(equalTo("grant_type=client_credentials&scope=accounts&client_id=" + clientId))
        );
        verify(postRequestedFor(urlEqualTo("/account-access-consents"))
                .withHeader("Authorization", containing("Bearer 978e46f2-b8a5-4f81-ac45-f10d32e6b764"))
                .withHeader("x-fapi-financial-id", containing("0032400006451CCVVX"))
                .withHeader("Content-Type", containing("application/json;charset=UTF-8"))
        );
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnCorrectFetchData(UrlDataProvider dataProvider) throws Exception {
        // given
        UrlFetchDataRequest urlFetchData = createUrlFetchDataRequest("978e46f2-b8a5-4f81-ac45-f10d32e6b764");

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(3);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        Optional<ProviderAccountDTO> firstAccount = dataProviderResponse.getAccounts()
                .stream().filter(
                        account ->
                                account.getAccountId().equalsIgnoreCase("cbba2fa2-2630-4862-a080-872b6735bfb1"))
                .findFirst();
        assertThat(firstAccount).isPresent();
        validateGbpAccount(firstAccount.get());
        validateTransactionsGbpAccount(firstAccount.get().getTransactions());

        Optional<ProviderAccountDTO> secondAccount = dataProviderResponse.getAccounts()
                .stream().filter(
                        account ->
                                account.getAccountId().equalsIgnoreCase("4c8f3ed8-a3d7-4d75-b239-3c9f42ac8685"))
                .findFirst();
        assertThat(secondAccount).isPresent();
        validateEurAccount(secondAccount.get());
        validateTransactionsEurAccount(secondAccount.get().getTransactions());

        Optional<ProviderAccountDTO> accountWithEmptyCurrency = dataProviderResponse.getAccounts()
                .stream()
                .filter(account -> Objects.isNull(account.getCurrency()))
                .findAny();
        assertThat(accountWithEmptyCurrency).isEmpty();

        Optional<ProviderAccountDTO> savingsAccount = dataProviderResponse.getAccounts()
                .stream().filter(
                        account ->
                                account.getAccountId().equalsIgnoreCase("2e7d0eed-c1c1-47a4-a884-b9fc83f51d5f"))
                .findFirst();
        assertThat(savingsAccount).isPresent();
        validateSavingsAccount(savingsAccount.get());
        validateTransactionsSavingsAccount(savingsAccount.get().getTransactions());
    }

    private void validateGbpAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getName()).isEqualTo("John Smith");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("10.20"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("10.20"));
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);;
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("LT222222222222222222");;
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(providerAccountDTO.getBankSpecific()).containsAllEntriesOf(Map.of("allIbanIdentifiers", "LT222222222222222222"));
    }

    private void validateTransactionsGbpAccount(List<ProviderTransactionDTO> transactions) {
        ProviderTransactionDTO transaction1 = transactions.get(0);
        assertThat(transaction1.getAmount()).isEqualTo(new BigDecimal("1.59"));
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction1.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction1.getMerchant()).isEqualTo("Aws Emea");
        assertThat(transaction1.getDescription()).isEqualTo("Aws Emea");
        transaction1.validate();
    }

    private void validateEurAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getName()).isEqualTo("Meghan Fountain");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("50.20"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("50.20"));
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);;
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("RO36BREL0022222222222222");;
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(providerAccountDTO.getBankSpecific()).containsAllEntriesOf(Map.of("allIbanIdentifiers", "RO36BREL0022222222222222, LT222222222222222222"));
    }

    private void validateTransactionsEurAccount(List<ProviderTransactionDTO> transactions) {
        ProviderTransactionDTO transactionDebit = transactions.get(0);
        assertThat(transactionDebit.getAmount()).isEqualTo(new BigDecimal("8.49"));
        assertThat(transactionDebit.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transactionDebit.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transactionDebit.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transactionDebit.getDescription()).isEqualTo("Aws Emea A2T1");
        assertThat(transactionDebit.getMerchant()).isEqualTo("Aws Emea");
        transactionDebit.validate();

        ProviderTransactionDTO transactionCredit = transactions.get(1);
        assertThat(transactionCredit.getAmount()).isEqualTo(new BigDecimal("13.559"));
        assertThat(transactionCredit.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transactionCredit.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transactionCredit.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transactionDebit.getMerchant()).isEqualTo("Aws Emea");
        assertThat(transactionCredit.getDescription()).isEqualTo("Aws Emea A2T2");
        transactionCredit.validate();
    }

    private void validateSavingsAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getName()).isEqualTo("My Savings Account");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("34.45"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("34.45"));
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);
    }

    private void validateTransactionsSavingsAccount(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions).isEmpty();
    }

    private UrlFetchDataRequest createUrlFetchDataRequest(String accessToken) throws JsonProcessingException {
        ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder().build();
        AccessMeans token = new AccessMeans(USER_ID, accessToken, "", Date.from(Instant.now().plus(1, ChronoUnit.DAYS)), null, null);
        String serializedAccessMeans = objectMapper.writeValueAsString(token);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedAccessMeans, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        return new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setUserSiteId(USER_SITE_ID)
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithNewOBCerts")
    public void shouldReturnCorrectAuthMeansAfterAutoConfigurationForNewOBCerts(UrlDataProvider dataProvider) {
        // given
        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(getPreRegistrationAuthMeansForNewOBCerts((AutoOnboardingProvider) dataProvider))
                .setRestTemplateManager(restTemplateManagerMock)
                .setBaseClientRedirectUrl("Doesn't matter")
                .setSigner(signer)
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = ((AutoOnboardingProvider) dataProvider).autoConfigureMeans(request);

        // then
        assertThat(configuredAuthMeans).hasSize(9);
        assertThat(configuredAuthMeans.get(RevolutGbAuthMeansBuilderV2.CLIENT_ID_NAME).getValue()).isEqualTo("06951f38-6aed-4005-a54e-f039f978f119");
        assertThat(configuredAuthMeans.get(RevolutGbAuthMeansBuilderV2.INSTITUTION_ID_NAME).getValue()).isEqualTo("0032400006451CCVVX");
        assertThat(configuredAuthMeans.get(RevolutGbAuthMeansBuilderV2.SIGNING_KEY_HEADER_ID_NAME).getValue()).isEqualTo("signing-key-header-id");
        assertThat(configuredAuthMeans.get(RevolutGbAuthMeansBuilderV2.TRANSPORT_CERTIFICATE_NAME).getValue()).isNotNull();
        assertThat(configuredAuthMeans.get(RevolutGbAuthMeansBuilderV2.SOFTWARE_ID_NAME).getValue()).isEqualTo("testSoftwareId");
        assertThat(configuredAuthMeans.get(RevolutGbAuthMeansBuilderV2.SOFTWARE_STATEMENT_ASSERTION_NAME).getValue()).isEqualTo("testSsa");
        assertThat(configuredAuthMeans.get(RevolutGbAuthMeansBuilderV2.ORGANIZATION_ID_NAME).getValue()).isEqualTo("testOrganizationId");
        assertThat(configuredAuthMeans).containsKeys(RevolutGbAuthMeansBuilderV2.SIGNING_PRIVATE_KEY_ID_NAME, RevolutGbAuthMeansBuilderV2.TRANSPORT_PRIVATE_KEY_ID_NAME);
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithEidasCerts")
    public void shouldReturnCorrectAuthMeansAfterAutoConfigurationForEidasCerts(UrlDataProvider dataProvider) {
        // given
        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(getPreRegistrationAuthMeansForEidasRegistration((AutoOnboardingProvider) dataProvider))
                .setRestTemplateManager(restTemplateManagerMock)
                .setBaseClientRedirectUrl("Doesn't matter")
                .setSigner(signer)
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = ((AutoOnboardingProvider) dataProvider).autoConfigureMeans(request);

        // then
        assertThat(configuredAuthMeans).hasSize(9);
        assertThat(configuredAuthMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("06951f38-6aed-4005-a54e-f039f978f119");
        assertThat(configuredAuthMeans.get(INSTITUTION_ID_NAME).getValue()).isEqualTo("0032400006451CCVVX");
        assertThat(configuredAuthMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue()).isEqualTo("signing-key-header-id");
        assertThat(configuredAuthMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()).isNotNull();
        assertThat(configuredAuthMeans).containsKeys(SIGNING_PRIVATE_KEY_ID_NAME, TRANSPORT_PRIVATE_KEY_ID_NAME);

        assertThat(configuredAuthMeans.get(ORG_JWKS_ENDPOINT_NAME).getValue()).isEqualTo("https://keystore.openbanking.org.uk/organizationId/softwareId.jwks");
        assertThat(configuredAuthMeans.get(ORG_NAME_NAME).getValue()).isEqualTo("TPP Org Name");
        assertThat(configuredAuthMeans.get(SOFTWARE_CLIENT_NAME_NAME).getValue()).isEqualTo("TPP Name");
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithNewOBCerts")
    public void shouldReturnCorrectAuthMeansAfterRegistrationUpdateForNewOBCerts(UrlDataProvider dataProvider) {
        // given
        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(getPreRegistrationUpdateAuthMeansForNewOBCerts((AutoOnboardingProvider) dataProvider))
                .setRestTemplateManager(restTemplateManagerMock)
                .setBaseClientRedirectUrl("Doesn't matter")
                .setSigner(signer)
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = ((AutoOnboardingProvider) dataProvider).autoConfigureMeans(request);

        // then
        verify(getRequestedFor(urlEqualTo("/register/06951f38-6aed-4005-a54e-f039f978f119"))
                .withHeader("Authorization", equalTo("Bearer 978e46f2-b8a5-4f81-ac45-f10d32e6b764"))
        );

        verify(putRequestedFor(urlEqualTo("/register/06951f38-6aed-4005-a54e-f039f978f119"))
                .withHeader("Authorization", equalTo("Bearer 978e46f2-b8a5-4f81-ac45-f10d32e6b764"))
                .withHeader("Content-Type", equalTo("application/jwt"))
        );

        assertThat(configuredAuthMeans).hasSize(7);
        assertThat(configuredAuthMeans.get(RevolutGbAuthMeansBuilderV2.CLIENT_ID_NAME).getValue()).isEqualTo("06951f38-6aed-4005-a54e-f039f978f119");
        assertThat(configuredAuthMeans.get(RevolutGbAuthMeansBuilderV2.INSTITUTION_ID_NAME).getValue()).isEqualTo("0032400006451CCVVX");
        assertThat(configuredAuthMeans.get(RevolutGbAuthMeansBuilderV2.SIGNING_KEY_HEADER_ID_NAME).getValue()).isEqualTo("signing-key-header-id");
        assertThat(configuredAuthMeans.get(RevolutGbAuthMeansBuilderV2.TRANSPORT_CERTIFICATE_NAME).getValue()).isNotNull();
        assertThat(configuredAuthMeans).containsKeys(RevolutGbAuthMeansBuilderV2.SIGNING_PRIVATE_KEY_ID_NAME, RevolutGbAuthMeansBuilderV2.TRANSPORT_PRIVATE_KEY_ID_NAME, RevolutGbAuthMeansBuilderV2.SOFTWARE_STATEMENT_ASSERTION_NAME);
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithEidasCerts")
    public void shouldReturnCorrectAuthMeansAfterRegistrationUpdateForEidasCerts(UrlDataProvider dataProvider) {
        // given
        Map authMeans = getPreRegistrationUpdateAuthMeansForEidasCerts((AutoOnboardingProvider) dataProvider);
        authMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "06951f38-6aed-4005-a54e-f039f978f119"));
        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setBaseClientRedirectUrl("Doesn't matter")
                .setSigner(signer)
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = ((AutoOnboardingProvider) dataProvider).autoConfigureMeans(request);

        // then
        verify(getRequestedFor(urlEqualTo("/register/06951f38-6aed-4005-a54e-f039f978f119"))
                .withHeader("Authorization", equalTo("Bearer 978e46f2-b8a5-4f81-ac45-f10d32e6b764"))
        );

        verify(putRequestedFor(urlEqualTo("/register/06951f38-6aed-4005-a54e-f039f978f119"))
                .withHeader("Authorization", equalTo("Bearer 978e46f2-b8a5-4f81-ac45-f10d32e6b764"))
                .withHeader("Content-Type", equalTo("application/jwt"))
        );

        assertThat(configuredAuthMeans).hasSize(9);
        assertThat(configuredAuthMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("06951f38-6aed-4005-a54e-f039f978f119");
        assertThat(configuredAuthMeans.get(INSTITUTION_ID_NAME).getValue()).isEqualTo("0032400006451CCVVX");
        assertThat(configuredAuthMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue()).isEqualTo("signing-key-header-id");
        assertThat(configuredAuthMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()).isNotNull();
        assertThat(configuredAuthMeans.get(ORG_NAME_NAME).getValue()).isEqualTo("TPP Org Name");
        assertThat(configuredAuthMeans.get(SOFTWARE_CLIENT_NAME_NAME).getValue()).isEqualTo("TPP Name");
        assertThat(configuredAuthMeans.get(ORG_JWKS_ENDPOINT_NAME).getValue()).isEqualTo("https://keystore.openbanking.org.uk/organizationId/softwareId.jwks");
        assertThat(configuredAuthMeans).containsKeys(SIGNING_PRIVATE_KEY_ID_NAME, TRANSPORT_PRIVATE_KEY_ID_NAME);
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithNewOBCerts")
    public void shouldReturnCorrectTypedAuthenticationMeansForNewOBRegistration(UrlDataProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans).hasSize(9)
                .containsEntry(RevolutGbAuthMeansBuilderV2.CLIENT_ID_NAME, CLIENT_ID_STRING)
                .containsEntry(RevolutGbAuthMeansBuilderV2.INSTITUTION_ID_NAME, INSTITUTION_ID_STRING)
                .containsEntry(RevolutGbAuthMeansBuilderV2.SIGNING_KEY_HEADER_ID_NAME, SIGNING_KEY_ID_STRING)
                .containsEntry(RevolutGbAuthMeansBuilderV2.SOFTWARE_ID_NAME, SOFTWARE_ID_STRING)
                .containsEntry(RevolutGbAuthMeansBuilderV2.SOFTWARE_STATEMENT_ASSERTION_NAME, SOFTWARE_STATEMENT_ASSERTION_STRING)
                .containsEntry(RevolutGbAuthMeansBuilderV2.TRANSPORT_CERTIFICATE_NAME, CERTIFICATE_PEM)
                .containsEntry(RevolutGbAuthMeansBuilderV2.ORGANIZATION_ID_NAME, ORGANIZATION_ID_STRING)
                .containsEntry(RevolutGbAuthMeansBuilderV2.SIGNING_PRIVATE_KEY_ID_NAME, KEY_ID)
                .containsEntry(RevolutGbAuthMeansBuilderV2.TRANSPORT_PRIVATE_KEY_ID_NAME, KEY_ID);
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithEidasCerts")
    public void shouldReturnCorrectTypedAuthenticationMeansForEidasRegistration(UrlDataProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans).hasSize(9)
                .containsEntry(CLIENT_ID_NAME, CLIENT_ID_STRING)
                .containsEntry(INSTITUTION_ID_NAME, INSTITUTION_ID_STRING)
                .containsEntry(SIGNING_KEY_HEADER_ID_NAME, SIGNING_KEY_ID_STRING)
                .containsEntry(TRANSPORT_CERTIFICATE_NAME, CERTIFICATE_PEM)
                .containsEntry(SIGNING_PRIVATE_KEY_ID_NAME, KEY_ID)
                .containsEntry(TRANSPORT_PRIVATE_KEY_ID_NAME, KEY_ID)
                .containsEntry(ORG_JWKS_ENDPOINT_NAME, ORG_JWKS_ENDPOINT_TYPE)
                .containsEntry(ORG_NAME_NAME, ORG_NAME_TYPE)
                .containsEntry(SOFTWARE_CLIENT_NAME_NAME, SOFTWARE_CLIENT_NAME_TYPE);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void refreshAccessMeans_not_supported_throws_exception(UrlDataProvider dataProvider) {
        // given
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .build();
        // when
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);
        // then
        assertThatExceptionOfType(TokenInvalidException.class).isThrownBy(throwingCallable).withMessage("Operation refresh access means is not supported by REVOLUT");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldCreateNewAccessMeans(UrlDataProvider dataProvider) throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "737f1248-3c75-4a47-a750-0384817c4b83";
        final String redirectUrl = "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0?code=" + authorizationCode + "&state=secretState";
        Date fourMinutesFromNow = Date.from(Instant.now().plus(4, ChronoUnit.MINUTES));
        UrlCreateAccessMeansRequest urlCreateAccessMeans = createUrlCreateAccessMeansRequest(redirectUrl, userId);

        // when
        AccessMeansDTO newAccessMeans = dataProvider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(fourMinutesFromNow).isBefore(newAccessMeans.getExpireTime());
        assertThat(newAccessMeans.getUserId()).isEqualTo(userId);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnOkWhileDeletingUserSiteAndReceiveUnprocessableEntity(UrlDataProvider dataProvider) {
        // given
        String externalConsentId = "35d62f61-3e33-4660-a1db-19f9bd044bf8";
        UrlOnUserSiteDeleteRequest request = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(externalConsentId)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signer)
                .build();
        StubMapping failedConsentStub = stubFor(delete(urlEqualTo("/account-access-consents/" + externalConsentId)).willReturn(noContent().withStatus(422)));
        // when -> then
        try {
            dataProvider.onUserSiteDelete(request);
        } catch (Exception e) {
            fail("something went wrong during deleting account", e);
        } finally {
            removeStub(failedConsentStub);
        }
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithNewOBCerts")
    public void shouldReturnTransportKeyRequirementsForNewOBCerts(UrlDataProvider dataProvider) {
        // when
        Optional<KeyRequirements> transportKeyRequirements = dataProvider.getTransportKeyRequirements();

        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(RevolutGbAuthMeansBuilderV2.TRANSPORT_PRIVATE_KEY_ID_NAME, RevolutGbAuthMeansBuilderV2.TRANSPORT_CERTIFICATE_NAME));
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithEidasCerts")
    public void shouldReturnSigningKeyRequirementsForEidasProvider(UrlDataProvider dataProvider) {
        // when
        Optional<KeyRequirements> signingKeyRequirements = dataProvider.getSigningKeyRequirements();

        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME));
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithNewOBCerts")
    public void shouldReturnSigningKeyRequirementsForNewOBProvider(UrlDataProvider dataProvider) {
        // when
        Optional<KeyRequirements> signingKeyRequirements = dataProvider.getSigningKeyRequirements();

        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmUtils.getKeyRequirements(RevolutGbAuthMeansBuilderV2.SIGNING_PRIVATE_KEY_ID_NAME));
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithNewOBCerts")
    public void shouldReturnTypedAuthenticationMeansForNewOBRegistration(UrlDataProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                RevolutGbAuthMeansBuilderV2.INSTITUTION_ID_NAME,
                RevolutGbAuthMeansBuilderV2.CLIENT_ID_NAME,
                RevolutGbAuthMeansBuilderV2.SIGNING_KEY_HEADER_ID_NAME,
                RevolutGbAuthMeansBuilderV2.SIGNING_PRIVATE_KEY_ID_NAME,
                RevolutGbAuthMeansBuilderV2.TRANSPORT_CERTIFICATE_NAME,
                RevolutGbAuthMeansBuilderV2.TRANSPORT_PRIVATE_KEY_ID_NAME,
                RevolutGbAuthMeansBuilderV2.SOFTWARE_ID_NAME,
                RevolutGbAuthMeansBuilderV2.ORGANIZATION_ID_NAME,
                RevolutGbAuthMeansBuilderV2.SOFTWARE_STATEMENT_ASSERTION_NAME
        );
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithEidasCerts")
    public void shouldReturnTypedAuthenticationMeansForEidasRegistration(UrlDataProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                INSTITUTION_ID_NAME,
                CLIENT_ID_NAME,
                SIGNING_KEY_HEADER_ID_NAME,
                SIGNING_PRIVATE_KEY_ID_NAME,
                TRANSPORT_CERTIFICATE_NAME,
                TRANSPORT_PRIVATE_KEY_ID_NAME,
                ORG_JWKS_ENDPOINT_NAME,
                ORG_NAME_NAME,
                SOFTWARE_CLIENT_NAME_NAME
        );
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeansRequest(String redirectUrl, UUID userId) {
        return new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();
    }

    private Map<String, BasicAuthenticationMean> getPreRegistrationAuthMeansForEidasRegistration(AutoOnboardingProvider dataProvider) {
        return authenticationMeansForEidas.entrySet()
                .stream()
                .filter(entry -> !dataProvider.getAutoConfiguredMeans().containsKey(entry.getKey()))
                .filter(entry -> !RevolutEuAuthMeansBuilderV2.CLIENT_ID_NAME.equals(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, BasicAuthenticationMean> getPreRegistrationUpdateAuthMeansForEidasCerts(AutoOnboardingProvider dataProvider) {
        return authenticationMeansForEidas.entrySet()
                .stream()
                .filter(entry -> !dataProvider.getAutoConfiguredMeans().containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, BasicAuthenticationMean> getPreRegistrationAuthMeansForNewOBCerts(AutoOnboardingProvider dataProvider) {
        return authenticationMeansNewOBCerts.entrySet()
                .stream()
                .filter(entry -> !dataProvider.getAutoConfiguredMeans().containsKey(entry.getKey()))
                .filter(entry -> !RevolutGbAuthMeansBuilderV2.CLIENT_ID_NAME.equals(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, BasicAuthenticationMean> getPreRegistrationUpdateAuthMeansForNewOBCerts(AutoOnboardingProvider dataProvider) {
        return authenticationMeansNewOBCerts.entrySet()
                .stream()
                .filter(entry -> !dataProvider.getAutoConfiguredMeans().containsKey(entry.getKey()))
                .filter(entry -> !RevolutGbAuthMeansBuilderV2.SOFTWARE_ID_NAME.equals(entry.getKey()))
                .filter(entry -> !RevolutGbAuthMeansBuilderV2.ORGANIZATION_ID_NAME.equals(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
