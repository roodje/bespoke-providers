package com.yolt.providers.triodosbank.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.triodosbank.common.model.domain.TriodosBankProviderState;
import com.yolt.providers.triodosbank.common.util.TriodosBankPKCE;
import com.yolt.providers.triodosbank.nl.TriodosBankNLDataProvider;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.triodosbank.common.auth.TriodosBankAuthenticationMeans.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/mappings/happy-flow/",
        files = "classpath:/mappings/happy-flow/",
        httpsPort = 0,
        port = 0)
@ActiveProfiles("triodosbank")
class TriodosBankDataProviderHappyFlowIntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final Date UPDATED_DATE = parseDate("2020-01-01");
    private static final Date EXPIRATION_DATE = parseDate("2020-01-02");
    private static final String CONSENT_ID = "consent-id";
    private static final String AUTHORISATION_ID = "authorisation-id";

    @Autowired
    @Qualifier("TriodosBankObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private TriodosBankNLDataProvider dataProvider;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private String pemCertificate;

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "2be4d475-f240-42c7-a22c-882566ac0f95"));
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(SIGNING_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "5391cac7-b840-4628-8036-d4998dfb8959"));
        authenticationMeans.put(CLIENT_ID_STRING_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "client-id"));
        authenticationMeans.put(CLIENT_SECRET_STRING_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), "client-secret"));
    }

    @Test
    void shouldReturnAllTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthenticationMeans).hasSize(6);
        assertThat(typedAuthenticationMeans.get(CLIENT_ID_STRING_NAME)).isEqualTo(CLIENT_ID_STRING);
        assertThat(typedAuthenticationMeans.get(CLIENT_SECRET_STRING_NAME)).isEqualTo(CLIENT_SECRET_STRING);
        assertThat(typedAuthenticationMeans.get(SIGNING_KEY_ID_NAME)).isEqualTo(KEY_ID);
        assertThat(typedAuthenticationMeans.get(SIGNING_CERTIFICATE_NAME)).isEqualTo(CERTIFICATE_PEM);
        assertThat(typedAuthenticationMeans.get(TRANSPORT_KEY_ID_NAME)).isEqualTo(KEY_ID);
        assertThat(typedAuthenticationMeans.get(TRANSPORT_CERTIFICATE_NAME)).isEqualTo(CERTIFICATE_PEM);
    }

    @Test
    void shouldReturnTypedAuthenticationMeansForRegistration() {
        // when
        Map<String, TypedAuthenticationMeans> autoConfiguredMeans = dataProvider.getAutoConfiguredMeans();

        // then
        assertThat(autoConfiguredMeans).hasSize(2);
        assertThat(autoConfiguredMeans.get(CLIENT_ID_STRING_NAME)).isEqualTo(CLIENT_ID_STRING);
        assertThat(autoConfiguredMeans.get(CLIENT_SECRET_STRING_NAME)).isEqualTo(CLIENT_SECRET_STRING);
    }

    @Test
    void shouldReturnDefaultAndRegisteredAuthenticationMeans() {
        // given
        Map<String, BasicAuthenticationMean> unregisteredAuthenticationMeans = new HashMap<>(authenticationMeans);
        unregisteredAuthenticationMeans.remove(CLIENT_ID_STRING_NAME);
        unregisteredAuthenticationMeans.remove(CLIENT_SECRET_STRING_NAME);

        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(unregisteredAuthenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setRedirectUrls(List.of("https://yolt.com/callback", "https://yolt.com/dev"))
                .build();

        // when
        Map<String, BasicAuthenticationMean> authMeans = dataProvider.autoConfigureMeans(request);

        // then
        assertThat(authMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()).isEqualTo(pemCertificate);
        assertThat(authMeans.get(TRANSPORT_KEY_ID_NAME).getValue()).isEqualTo("2be4d475-f240-42c7-a22c-882566ac0f95");
        assertThat(authMeans.get(SIGNING_CERTIFICATE_NAME).getValue()).isEqualTo(pemCertificate);
        assertThat(authMeans.get(SIGNING_KEY_ID_NAME).getValue()).isEqualTo("5391cac7-b840-4628-8036-d4998dfb8959");
        assertThat(authMeans.get(CLIENT_ID_STRING_NAME).getValue()).isEqualTo("registered-client-id");
        assertThat(authMeans.get(CLIENT_SECRET_STRING_NAME).getValue()).isEqualTo("registered-client-secret");
    }

    @Test
    void shouldReturnRedirectStepWithConsentUrl() {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setState("29dbba15-1e67-4ac0-ab0f-2487dc0c960b")
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setBaseClientRedirectUrl("https://yolt.com/callback")
                .build();

        // when
        RedirectStep redirectStep = dataProvider.getLoginInfo(request);

        // then
        String loginUrl = redirectStep.getRedirectUrl();
        assertThat(loginUrl).contains("https://xs2a-sandbox.triodos.com/auth/nl/v1/auth");

        Map<String, String> queryParams = getQueryParamsFromUrl(loginUrl);
        assertThat(queryParams.get("response_type")).isEqualTo("code");
        assertThat(queryParams.get("scope")).isEqualTo("openid+offline_access+AIS%3Ac5bcbf1b-5a3a-45d4-86aa-ed17988e3a63");
        assertThat(queryParams.get("client_id")).isEqualTo("client-id");
        assertThat(queryParams.get("state")).isEqualTo("29dbba15-1e67-4ac0-ab0f-2487dc0c960b");
        assertThat(queryParams.get("redirect_uri")).isEqualTo("https://yolt.com/callback");
        assertThat(queryParams).containsKeys("code_challenge", "code_challenge_method");
    }

    @Test
    void shouldReturnNewAccessMeans() {
        // given
        OAuth2ProofKeyCodeExchange codeExchange = TriodosBankPKCE.createRandomS256();

        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setState("29dbba15-1e67-4ac0-ab0f-2487dc0c960b")
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(serializeProviderState(createProviderState(codeExchange)))
                .setRedirectUrlPostedBackFromSite("https://yolt.com/callback?state=29dbba15-1e67-4ac0-ab0f-2487dc0c960b&code=authorization-code")
                .setUserId(USER_ID)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(accessMeansOrStepDTO.getStep()).isNull();

        AccessMeansDTO accessMeans = accessMeansOrStepDTO.getAccessMeans();
        assertThat(accessMeans.getUserId()).isEqualTo(request.getUserId());
        assertThat(accessMeans.getUpdated()).isAfter(UPDATED_DATE);
        assertThat(accessMeans.getExpireTime()).isAfter(EXPIRATION_DATE);

        TriodosBankProviderState providerState = deserializeProviderState(accessMeans.getAccessMeans());
        assertThat(providerState.getCodeVerifier()).isEqualTo(codeExchange.getCodeVerifier());
        assertThat(providerState.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(providerState.getAccessToken()).isEqualTo("access-token");
        assertThat(providerState.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void shouldReturnRefreshedAccessMeans() throws TokenInvalidException {
        // given
        OAuth2ProofKeyCodeExchange codeExchange = TriodosBankPKCE.createRandomS256();

        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(createAccessMeansDTO(codeExchange))
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansDTO accessMeansDTO = dataProvider.refreshAccessMeans(request);

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeansDTO.getUpdated()).isAfter(UPDATED_DATE);
        assertThat(accessMeansDTO.getExpireTime()).isAfter(EXPIRATION_DATE);

        TriodosBankProviderState providerState = deserializeProviderState(accessMeansDTO.getAccessMeans());
        assertThat(providerState.getCodeVerifier()).isEqualTo(codeExchange.getCodeVerifier());
        assertThat(providerState.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(providerState.getAccessToken()).isEqualTo("new-access-token");
        assertThat(providerState.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void shouldReturnAllAccountsBalancesAndTransactions() throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(createAccessMeansDTO(TriodosBankPKCE.createRandomS256()))
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        List<ProviderAccountDTO> accounts = response.getAccounts();
        assertThat(accounts).hasSize(3);

        ProviderAccountDTO account1 = accounts.get(0);
        assertThat(account1).satisfies(validateProviderAccountDTO("1", "100.00", "100.00", "NL22INGB5157915454"));

        List<ProviderTransactionDTO> account1Transactions = account1.getTransactions();
        assertThat(account1Transactions).hasSize(6);
        assertThat(account1Transactions.get(0)).satisfies(validateProviderTransactionDTO("11", "11.25", "2019-02-26T00:00+01:00[Europe/Amsterdam]", BOOKED, CREDIT));
        assertThat(account1Transactions.get(0).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("11.25");

        assertThat(account1Transactions.get(1)).satisfies(validateProviderTransactionDTO("12", "41.12", "2019-02-26T00:00+01:00[Europe/Amsterdam]", BOOKED, DEBIT));
        assertThat(account1Transactions.get(1).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("-41.12");

        assertThat(account1Transactions.get(2)).satisfies(validateProviderTransactionDTO(null, "999.26", "2021-10-03T00:00+02:00[Europe/Amsterdam]", PENDING, CREDIT));
        assertThat(account1Transactions.get(2).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("999.26");

        assertThat(account1Transactions.get(3)).satisfies(validateProviderTransactionDTO(null, "666.85", "2021-10-02T00:00+02:00[Europe/Amsterdam]", PENDING, DEBIT));
        assertThat(account1Transactions.get(3).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("-666.85");

        ProviderAccountDTO account2 = accounts.get(1);
        assertThat(account2).satisfies(validateProviderAccountDTO("2", "590.01", "510.84", "NL37INGB2952671982"));

        List<ProviderTransactionDTO> account2Transactions = account2.getTransactions();
        assertThat(account2Transactions).hasSize(5);
        assertThat(account2Transactions.get(2).getDescription()).isEmpty();
        assertThat(account2Transactions.get(3)).satisfies(validateProviderTransactionDTO(null, "10.36", "2020-10-03T00:00+02:00[Europe/Amsterdam]", PENDING, DEBIT));
        assertThat(account2Transactions.get(4)).satisfies(validateProviderTransactionDTO(null, "78.76", "2020-10-03T00:00+02:00[Europe/Amsterdam]", PENDING, DEBIT));

        ProviderAccountDTO account3 = accounts.get(2);
        assertThat(account3).satisfies(validateProviderAccountDTO("3", "279.33", "279.33", "NL37INGB2952671983"));
    }

    private Consumer<ProviderAccountDTO> validateProviderAccountDTO(String accountId, String availableBalance, String currentBalance, String iban) {
        return providerAccountDTO -> {
            providerAccountDTO.validate();

            assertThat(providerAccountDTO.getAccountId()).isEqualTo(accountId);
            assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
            assertThat(providerAccountDTO.getLastRefreshed()).isCloseTo(ZonedDateTime.now(), within(32, ChronoUnit.SECONDS));
            assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal(availableBalance));
            assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal(currentBalance));
            assertThat(providerAccountDTO.getName()).isNotEmpty();
            assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
            assertThat(providerAccountDTO.getAccountNumber().getHolderName()).isEqualTo("Simon van der Valk");
            assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
            assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo(iban);

            ProviderAccountNumberDTO accountNumberDTO = providerAccountDTO.getAccountNumber();
            assertThat(accountNumberDTO.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
            assertThat(accountNumberDTO.getIdentification()).isEqualTo(iban);

            ExtendedAccountDTO extendedAccountDTO = providerAccountDTO.getExtendedAccount();
            assertThat(extendedAccountDTO.getResourceId()).isEqualTo(accountId);
            assertThat(extendedAccountDTO.getName()).isEqualTo(providerAccountDTO.getName());
            assertThat(extendedAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);

            AccountReferenceDTO accountReferenceDTO = extendedAccountDTO.getAccountReferences().get(0);
            assertThat(accountReferenceDTO.getType()).isEqualTo(AccountReferenceType.IBAN);
            assertThat(accountReferenceDTO.getValue()).isEqualTo(accountNumberDTO.getIdentification());

            List<BalanceDTO> balances = extendedAccountDTO.getBalances();
            assertThat(balances).isNotEmpty();
        };
    }

    private Consumer<ProviderTransactionDTO> validateProviderTransactionDTO(String transactionId,
                                                                            String amount,
                                                                            String dateTime,
                                                                            TransactionStatus status,
                                                                            ProviderTransactionType type) {
        return providerTransactionDTO -> {
            assertThat(providerTransactionDTO.getExternalId()).isEqualTo(transactionId);
            assertThat(providerTransactionDTO.getDateTime()).isEqualTo(dateTime);
            assertThat(providerTransactionDTO.getAmount()).isEqualTo(new BigDecimal(amount));
            assertThat(providerTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(providerTransactionDTO.getType()).isEqualTo(type);
            assertThat(providerTransactionDTO.getDescription()).isNotEmpty();
            assertThat(providerTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);

            ExtendedTransactionDTO extendedTransactionDTO = providerTransactionDTO.getExtendedTransaction();
            assertThat(extendedTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(extendedTransactionDTO.getBookingDate()).isNotNull();
            if (status.equals(BOOKED)) {
                assertThat(extendedTransactionDTO.getValueDate()).isNotNull();
            } else {
                assertThat(extendedTransactionDTO.getValueDate()).isNull();

            }

            assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isNotEmpty();
            assertThat(extendedTransactionDTO.getProprietaryBankTransactionCode()).isNotEmpty();

            BalanceAmountDTO balanceAmountDTO = extendedTransactionDTO.getTransactionAmount();
            if (DEBIT.equals(type)) {
                assertThat(balanceAmountDTO.getAmount()).isEqualTo(new BigDecimal(amount).negate());
            }
            if (CREDIT.equals(type)) {
                assertThat(balanceAmountDTO.getAmount()).isEqualTo(new BigDecimal(amount));
            }
            assertThat(balanceAmountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
        };
    }

    private Map<String, String> getQueryParamsFromUrl(String url) {
        return UriComponentsBuilder.fromUriString(url)
                .build()
                .getQueryParams()
                .toSingleValueMap();
    }

    private AccessMeansDTO createAccessMeansDTO(OAuth2ProofKeyCodeExchange codeExchange) {
        TriodosBankProviderState providerState = createProviderState(codeExchange);
        providerState.setAccessToken("access-token");
        providerState.setRefreshToken("refresh-token");
        return new AccessMeansDTO(USER_ID, serializeProviderState(providerState), UPDATED_DATE, EXPIRATION_DATE);
    }

    private TriodosBankProviderState createProviderState(OAuth2ProofKeyCodeExchange codeExchange) {
        return new TriodosBankProviderState(codeExchange, CONSENT_ID, AUTHORISATION_ID);
    }

    @SneakyThrows
    private String serializeProviderState(TriodosBankProviderState providerState) {
        return objectMapper.writeValueAsString(providerState);
    }

    @SneakyThrows
    private TriodosBankProviderState deserializeProviderState(String providerState) {
        return objectMapper.readValue(providerState, TriodosBankProviderState.class);
    }

    private static Date parseDate(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay().toInstant(UTC));
    }
}
