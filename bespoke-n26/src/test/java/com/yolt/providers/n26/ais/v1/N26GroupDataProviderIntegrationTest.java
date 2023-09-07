package com.yolt.providers.n26.ais.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
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
import com.yolt.providers.n26.TestApp;
import com.yolt.providers.n26.common.auth.N26GroupPKCE;
import com.yolt.providers.n26.common.dto.N26GroupProviderState;
import com.yolt.providers.n26.common.service.mapper.N26GroupProviderStateMapper;
import com.yolt.providers.n26.n26.config.N26DataProviderV1;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
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
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.n26.common.auth.N26GroupAuthenticationMeansProducerV1.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("n26")
@AutoConfigureWireMock(stubs = "classpath:/mappings/n26/happy-flow", httpsPort = 0, port = 0)
public class N26GroupDataProviderIntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final Date UPDATED_DATE = parseDate("2020-01-01");
    private static final Date EXPIRATION_DATE = parseDate("2020-01-02");
    private static final String CONSENT_ID = "16640bfe-9a98-441a-8380-c568976eee4a";
    private static final String REDIRECT_URI = "https://www.yolt.com/callback";
    private static final String REQUEST_ID = "56640bfe-9a98-441a-8380-c568976eee4a";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private N26DataProviderV1 dataProvider;

    @Autowired
    private Clock clock;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private N26GroupProviderStateMapper providerStateMapper;
    private OAuth2ProofKeyCodeExchange codeExchange;

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        String pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        providerStateMapper = new N26GroupProviderStateMapper(new ObjectMapper(), clock);
        codeExchange = new N26GroupPKCE().createRandomS256();

        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "client-id"));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "2be4d475-f240-42c7-a22c-882566ac0f95"));
    }

    @Test
    void shouldReturnTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans).hasSize(3)
                .containsEntry(CLIENT_ID_NAME, CLIENT_ID_STRING)
                .containsEntry(TRANSPORT_KEY_ID_NAME, KEY_ID)
                .containsEntry(TRANSPORT_CERTIFICATE_NAME, CERTIFICATE_PEM);
    }

    @Test
    void shouldReturnRedirectStepWithConsentUrl() {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setState("d79960be-3058-4d8e-9cd3-3753febd4661")
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .build();

        // when
        RedirectStep redirectStep = dataProvider.getLoginInfo(request);

        // then
        String loginUrl = redirectStep.getRedirectUrl();
        assertThat(loginUrl).isEqualTo("https://app.n26.com/login?requestId=0daa152a-651a-4592-8542-47ff60799deb&state=testState&authType=XS2A");
    }

    @Test
    void shouldReturnNewAccessMeans() throws TokenInvalidException {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setSigner(signer)
                .setState("29dbba15-1e67-4ac0-ab0f-2487dc0c960b")
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(providerStateMapper.toJson(createProviderState()))
                .setRedirectUrlPostedBackFromSite("https://yolt.com/callback?state=29dbba15-1e67-4ac0-ab0f-2487dc0c960b&code=authorization-code")
                .setUserId(USER_ID)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(accessMeansOrStepDTO.getStep()).isNull();

        AccessMeansDTO accessMeansDTO = accessMeansOrStepDTO.getAccessMeans();
        assertThat(accessMeansDTO.getUserId()).isEqualTo(request.getUserId());
        assertThat(accessMeansDTO.getUpdated()).isAfter(UPDATED_DATE);
        assertThat(accessMeansDTO.getExpireTime()).isAfter(EXPIRATION_DATE);

        N26GroupProviderState providerState = providerStateMapper.fromJson(accessMeansDTO.getAccessMeans());
        assertThat(providerState.getCodeVerifier()).isEqualTo(codeExchange.getCodeVerifier());
        assertThat(providerState.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(providerState.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(providerState.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    void shouldReturnRefreshedAccessMeans() throws TokenInvalidException {
        // given
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(createAccessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansDTO accessMeansDTO = dataProvider.refreshAccessMeans(request);

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeansDTO.getUpdated()).isAfter(UPDATED_DATE);
        assertThat(accessMeansDTO.getExpireTime()).isAfter(EXPIRATION_DATE);

        N26GroupProviderState providerState = providerStateMapper.fromJson(accessMeansDTO.getAccessMeans());
        assertThat(providerState.getCodeVerifier()).isEqualTo(codeExchange.getCodeVerifier());
        assertThat(providerState.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(providerState.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(providerState.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    void shouldReturnAccountsAndTransactions() throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(createAccessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now(clock))
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        List<ProviderAccountDTO> accounts = response.getAccounts();
        assertThat(accounts).hasSize(2);

        ProviderAccountDTO account1 = accounts.get(0);
        assertThat(account1).satisfies(validateProviderAccountDTO("1", "55.55", "55.55", true));

        List<ProviderTransactionDTO> account1Transactions = account1.getTransactions();
        assertThat(account1Transactions).hasSize(3);
        assertThat(account1Transactions.get(0)).satisfies(validateProviderTransactionDTO("4b856f12-a75c-449f-8e71-69bd72947445", "12.0", BOOKED, DEBIT, null, "sampleCreditorId", null));
        assertThat(account1Transactions.get(1)).satisfies(validateProviderTransactionDTO("b872362d-6476-4f68-88af-99ec85659d13", "14.99", BOOKED, DEBIT, null, null, null));
        assertThat(account1Transactions.get(2)).satisfies(validateProviderTransactionDTO("7f9da399-8c53-4c68-b43c-c7e22a0c70d2", "5.0", BOOKED, CREDIT, "Test, Information", null, "sampleMandateID"));
        ProviderAccountDTO account2 = accounts.get(1);
        assertThat(account2).satisfies(validateProviderAccountDTO("2", "120.00", "120.00", false));

        List<ProviderTransactionDTO> account2Transactions = account2.getTransactions();
        assertThat(account2Transactions).isEmpty();
    }

    @Test
    void shouldSuccessfullyDeleteConsent() {
        // given
        UrlOnUserSiteDeleteRequest request = new UrlOnUserSiteDeleteRequestBuilder()
                .setAccessMeans(createAccessMeansDTO())
                .setAuthenticationMeans(authenticationMeans)
                .setExternalConsentId(UUID.randomUUID().toString())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> dataProvider.onUserSiteDelete(request);

        // then
        assertThatCode(onUserSiteDeleteCallable)
                .doesNotThrowAnyException();
        WireMock.verify(1, WireMock.deleteRequestedFor(WireMock.urlPathEqualTo("/v1/berlin-group/v1/consents/16640bfe-9a98-441a-8380-c568976eee4a")));
    }

    private Consumer<ProviderAccountDTO> validateProviderAccountDTO(String accountId, String availableBalance, String currentBalance, boolean isMainAccount) {
        return providerAccountDTO -> {
            providerAccountDTO.validate();

            assertThat(providerAccountDTO.getAccountId()).isEqualTo(accountId);
            assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
            assertThat(providerAccountDTO.getLastRefreshed()).isCloseTo(ZonedDateTime.now(), within(32, ChronoUnit.SECONDS));
            assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal(availableBalance));
            assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal(currentBalance));
            assertThat(providerAccountDTO.getName()).isNotEmpty();
            assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);

            ExtendedAccountDTO extendedAccountDTO = providerAccountDTO.getExtendedAccount();
            assertThat(extendedAccountDTO.getResourceId()).isEqualTo(accountId);
            assertThat(extendedAccountDTO.getName()).isEqualTo(providerAccountDTO.getName());
            assertThat(extendedAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);

            ProviderAccountNumberDTO accountNumberDTO = providerAccountDTO.getAccountNumber();
            AccountReferenceDTO accountReferenceDTO = extendedAccountDTO.getAccountReferences().get(0);
            if (isMainAccount) {
                assertThat(accountNumberDTO.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
                assertThat(accountNumberDTO.getIdentification()).isNotEmpty();

                assertThat(accountReferenceDTO.getType()).isEqualTo(AccountReferenceType.IBAN);
                assertThat(accountReferenceDTO.getValue()).isEqualTo(accountNumberDTO.getIdentification());
            }

            List<BalanceDTO> balances = extendedAccountDTO.getBalances();
            assertThat(balances).isNotEmpty();
        };
    }

    private Consumer<ProviderTransactionDTO> validateProviderTransactionDTO(String transactionId,
                                                                            String amount,
                                                                            TransactionStatus status,
                                                                            ProviderTransactionType type,
                                                                            String remittanceInformationUnstructured,
                                                                            String creditorId,
                                                                            String mandateId) {
        return providerTransactionDTO -> {
            assertThat(providerTransactionDTO.getExternalId()).isEqualTo(transactionId);
            assertThat(providerTransactionDTO.getDateTime()).isNotNull();
            assertThat(providerTransactionDTO.getDateTime().getZone()).isEqualTo(ZoneId.of("Europe/Berlin"));
            assertThat(providerTransactionDTO.getAmount()).isEqualTo(new BigDecimal(amount));
            assertThat(providerTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(providerTransactionDTO.getType()).isEqualTo(type);
            assertThat(providerTransactionDTO.getDescription()).isNotEmpty();
            assertThat(providerTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);

            ExtendedTransactionDTO extendedTransactionDTO = providerTransactionDTO.getExtendedTransaction();
            assertThat(extendedTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(extendedTransactionDTO.getBookingDate()).isNotNull();
            assertThat(extendedTransactionDTO.getValueDate()).isNotNull();
            assertThat(extendedTransactionDTO.getBookingDate().getZone()).isEqualTo(ZoneId.of("Europe/Berlin"));
            assertThat(extendedTransactionDTO.getValueDate().getZone()).isEqualTo(ZoneId.of("Europe/Berlin"));
            assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isEqualTo(remittanceInformationUnstructured);
            assertThat(extendedTransactionDTO.getCreditorId()).isEqualTo(creditorId);
            assertThat(extendedTransactionDTO.getMandateId()).isEqualTo(mandateId);
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

    private AccessMeansDTO createAccessMeansDTO() {
        N26GroupProviderState providerState = createProviderState();
        providerState.setTokens(ACCESS_TOKEN, REFRESH_TOKEN);
        providerState.setConsentId(CONSENT_ID);
        return new AccessMeansDTO(USER_ID, providerStateMapper.toJson(providerState), UPDATED_DATE, EXPIRATION_DATE);
    }

    private N26GroupProviderState createProviderState() {
        return new N26GroupProviderState(codeExchange, REDIRECT_URI, REQUEST_ID, clock);
    }

    private static Date parseDate(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay().toInstant(UTC));
    }
}
