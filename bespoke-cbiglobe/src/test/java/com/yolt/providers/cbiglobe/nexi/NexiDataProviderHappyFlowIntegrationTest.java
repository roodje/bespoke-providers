package com.yolt.providers.cbiglobe.nexi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.CbiGlobeFixtureProvider;
import com.yolt.providers.cbiglobe.CbiGlobeTestApp;
import com.yolt.providers.cbiglobe.common.model.CbiGlobeAccessMeansDTO;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.account.Status;
import nl.ing.lovebird.extendeddata.account.UsageType;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderCreditCardDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;

import static com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans.*;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static java.time.Instant.ofEpochMilli;
import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = CbiGlobeTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(
        stubs = {
                "classpath:/stubs/ais/3.0/happy_flow/consent",
                "classpath:/stubs/ais/3.0/happy_flow/token",
                "classpath:/stubs/ais/3.0/happy_flow/card-accounts/nexi"
        },
        httpsPort = 0, port = 0)
@ActiveProfiles("cbiglobe")
public class NexiDataProviderHappyFlowIntegrationTest {

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("CbiGlobe")
    private ObjectMapper mapper;

    @Mock
    private Signer signer;

    @Autowired
    private NexiDataProviderV1 dataProvider;

    private static final Map<String, BasicAuthenticationMean> TEST_AUTH_MEANS = getSampleAuthMeans();

    @Test
    void shouldReturnRedirectUrl() {
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(TEST_AUTH_MEANS)
                .setSigner(signer)
                .setState("11111111-1111-1111-1111-111111111111")
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl("https://yolt.com/callback-acc")
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) dataProvider.getLoginInfo(urlGetLoginRequest);

        // then
        assertThat(redirectStep.getRedirectUrl()).isEqualTo("https://cbiglobe.it/clientlogin?id=some-id");

        CbiGlobeAccessMeansDTO accessMeansDTO = CbiGlobeFixtureProvider.fromProviderState(redirectStep.getProviderState(), mapper);
        assertThat(accessMeansDTO.getAccessToken()).isEqualTo("00000000-0000-0000-0000-000000000000");
        assertThat(accessMeansDTO.getConsentId()).isEqualTo("1");
    }

    @Test
    void shouldCreateAccessMeansSuccessfullyWhenCreatingDetailedConsentForCreditCardAccount() {
        // given
        String consentId = "3";
        CbiGlobeAccessMeansDTO providerState = new CbiGlobeAccessMeansDTO(ofEpochMilli(1), "00000000-0000-0000-0000-000000000000", ofEpochMilli(2), consentId, ofEpochMilli(3), Collections.emptyList(), Collections.emptyMap(), 0, "ASPSP_MM_01", null);
        UUID userId = UUID.fromString("7bb49dfa-5d54-43b0-9a39-e4b2b0e9ae10");
        String state = "33333333-3333-3333-3333-333333333333";
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setProviderState(CbiGlobeFixtureProvider.toProviderState(providerState, mapper))
                .setSigner(signer)
                .setState(state)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(TEST_AUTH_MEANS)
                .setBaseClientRedirectUrl("https://yolt.com/callback-acc")
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStep = dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        // Validate access means
        assertThat(accessMeansOrStep.getAccessMeans()).isNull();

        // Validate access means
        RedirectStep redirectStep = (RedirectStep) accessMeansOrStep.getStep();
        assertThat(redirectStep.getRedirectUrl()).isEqualTo("https://cbiglobe.it/clientlogin?id=some-id");

        // Validate provider state
        CbiGlobeAccessMeansDTO accessMeansDTO = CbiGlobeFixtureProvider.fromProviderState(accessMeansOrStep.getStep().getProviderState(), mapper);
        assertThat(accessMeansDTO.getConsentId()).isEqualTo(consentId);
        assertThat(accessMeansDTO.getConsentExpiration()).isEqualTo(providerState.getConsentExpiration());
        assertThat(accessMeansDTO.getUpdated()).isEqualTo(providerState.getUpdated());
        assertThat(accessMeansDTO.getAccessTokenExpiration()).isEqualTo(providerState.getAccessTokenExpiration());
        ProviderAccountDTO cachedAccount = createdCachedCardAccount();
        assertThat(accessMeansDTO.getCachedAccounts()).usingRecursiveFieldByFieldElementComparatorIgnoringFields("lastRefreshed").containsExactlyInAnyOrderElementsOf(List.of(cachedAccount));
        assertThat(accessMeansDTO.getConsentedAccounts().get("detailedConsentIdForCreditCardAccount")).usingRecursiveComparison().ignoringActualNullFields().ignoringFields("lastRefreshed").isEqualTo(cachedAccount);
    }

    @Test
    void shouldCreateAccessMeansSuccessfullyWhenVerifyingAlreadyCreatedConsents() {
        // given
        String consentId = "3";
        ProviderAccountDTO cachedAccount = createdCachedCardAccount();
        CbiGlobeAccessMeansDTO providerState = new CbiGlobeAccessMeansDTO(ofEpochMilli(1), "00000000-0000-0000-0000-000000000000", ofEpochMilli(2), consentId, ofEpochMilli(3), List.of(cachedAccount), Map.of("detailedConsentIdForCreditCardAccount", cachedAccount), 0, "ASPSP_MM_01", null);
        UUID userId = UUID.fromString("7bb49dfa-5d54-43b0-9a39-e4b2b0e9ae10");
        String state = "33333333-3333-3333-3333-333333333333";
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setProviderState(CbiGlobeFixtureProvider.toProviderState(providerState, mapper))
                .setSigner(signer)
                .setState(state)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(TEST_AUTH_MEANS)
                .setBaseClientRedirectUrl("https://yolt.com/callback-acc")
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStep = dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest);

        // then
        // Validate redirect step
        assertThat(accessMeansOrStep.getStep()).isNull();

        // Validate access means
        AccessMeansDTO accessMeans = accessMeansOrStep.getAccessMeans();
        assertThat(accessMeans.getUserId()).isEqualTo(userId);
        assertThat(accessMeans.getUpdated()).isEqualTo(Date.from(ofEpochMilli(1)));
        assertThat(accessMeans.getExpireTime()).isEqualTo(Date.from(ofEpochMilli(3)));

        // Validate provider state
        CbiGlobeAccessMeansDTO accessMeansDTO = CbiGlobeFixtureProvider.fromProviderState(accessMeans.getAccessMeans(), mapper);
        assertThat(accessMeansDTO.getConsentId()).isEqualTo(consentId);
        assertThat(accessMeansDTO.getConsentExpiration()).isEqualTo(providerState.getConsentExpiration());
        assertThat(accessMeansDTO.getUpdated()).isEqualTo(providerState.getUpdated());
        assertThat(accessMeansDTO.getAccessTokenExpiration()).isEqualTo(providerState.getAccessTokenExpiration());
        assertThat(accessMeansDTO.getCachedAccounts()).usingRecursiveFieldByFieldElementComparatorIgnoringFields("lastRefreshed").containsExactlyInAnyOrderElementsOf(List.of(cachedAccount));
        assertThat(accessMeansDTO.getConsentedAccounts().get("detailedConsentIdForCreditCardAccount")).usingRecursiveComparison().ignoringActualNullFields().ignoringFields("lastRefreshed").isEqualTo(cachedAccount);
    }

    private ProviderAccountDTO createdCachedCardAccount() {
        return ProviderAccountDTO.builder()
                .yoltAccountType(CREDIT_CARD)
                .lastRefreshed(Instant.now().atZone(ZoneId.systemDefault()))
                .accountId("1")
                .accountMaskedIdentification("1111 ******* 1111")
                .name("TestCardAccount")
                .currency(CurrencyCode.EUR)
                .closed(false)
                .creditCardData(ProviderCreditCardDTO.builder()
                        .availableCreditAmount(new BigDecimal("12345.67"))
                        .build())
                .extendedAccount(ExtendedAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .name("TestCardAccount")
                        .cashAccountType(ExternalCashAccountType.CURRENT)
                        .status(Status.ENABLED)
                        .usage(UsageType.PRIVATE)
                        .build())
                .build();
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenTryingToRefreshToken() {
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> dataProvider.refreshAccessMeans(null);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldFetchDataSuccessfully() throws TokenInvalidException, ProviderFetchDataException {
        // given
        String consentId = "3";
        ProviderAccountDTO cachedAccount = createdCachedCardAccount();
        CbiGlobeAccessMeansDTO accessMeansDTO = new CbiGlobeAccessMeansDTO(ofEpochMilli(1), "00000000-0000-0000-0000-000000000000", ofEpochMilli(2), "doesntMatter", ofEpochMilli(3), List.of(cachedAccount), Map.of(consentId, cachedAccount), 0, "ASPSP_MM_01", null);
        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now().minus(Period.ofDays(100)))
                .setAccessMeans(CbiGlobeFixtureProvider.createAccessMeansDTO(accessMeansDTO, mapper))
                .setAuthenticationMeans(TEST_AUTH_MEANS)
                .setSigner(signer)
                .setPsuIpAddress("127.0.0.1")
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchDataRequest);

        // then
        // Validate accounts
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        assertThat(accounts).hasSize(1);
        accounts.forEach(ProviderAccountDTO::validate);

        // Validate account 1
        ProviderAccountDTO account = accounts.get(0);
        assertThat(account.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(account.getYoltAccountType()).isEqualTo(CREDIT_CARD);
        assertThat(account.getName()).isEqualTo("TestCardAccount");
        assertThat(account.getAvailableBalance()).isEqualTo("60");
        assertThat(account.getCurrentBalance()).isEqualTo("70");
        assertThat(account.getTransactions()).hasSize(2);
        account.validate();

        // Validate booked transaction for account 1
        ProviderTransactionDTO bookedTransaction = account.getTransactions().get(0);
        assertThat(bookedTransaction.getAmount()).isEqualTo("10");
        assertThat(bookedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(bookedTransaction.getType()).isEqualTo(CREDIT);
        assertThat(bookedTransaction.getDateTime()).isEqualTo("2018-02-23T00:00+01:00[Europe/Rome]");
        assertThat(bookedTransaction.getDescription()).isEqualTo("test transaction details");
        bookedTransaction.validate();

        // Validate pending transaction for account 1
        ProviderTransactionDTO pendingTransaction = account.getTransactions().get(1);
        assertThat(pendingTransaction.getAmount()).isEqualTo("20");
        assertThat(pendingTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(pendingTransaction.getType()).isEqualTo(CREDIT);
        assertThat(pendingTransaction.getDateTime()).isEqualTo("2018-02-23T00:00+01:00[Europe/Rome]");
        assertThat(pendingTransaction.getDescription()).isEqualTo("test transaction details");
        pendingTransaction.validate();

        // Validate extended booked transaction for account 1
        ExtendedTransactionDTO bookedExtendedTransaction = bookedTransaction.getExtendedTransaction();
        assertThat(bookedExtendedTransaction.getBookingDate()).isEqualTo("2018-02-23T00:00+01:00[Europe/Rome]");
        assertThat(bookedExtendedTransaction.getValueDate()).isEqualTo("2018-02-23T00:00+01:00[Europe/Rome]");
        assertThat(bookedExtendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("test transaction details");
        assertThat(bookedExtendedTransaction.getRemittanceInformationStructured()).isEqualTo("test transaction details");

        // Validate extended pending transaction for account 1
        ExtendedTransactionDTO pendingExtendedTransaction = pendingTransaction.getExtendedTransaction();
        assertThat(pendingExtendedTransaction.getBookingDate()).isEqualTo("2018-02-23T00:00+01:00[Europe/Rome]");
        assertThat(pendingExtendedTransaction.getValueDate()).isEqualTo("2018-02-23T00:00+01:00[Europe/Rome]");
        assertThat(pendingExtendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("test transaction details");
        assertThat(pendingExtendedTransaction.getRemittanceInformationStructured()).isEqualTo("test transaction details");
    }

    @SneakyThrows
    private static Map<String, BasicAuthenticationMean> getSampleAuthMeans() {
        Map<String, BasicAuthenticationMean> authMeans = new HashMap<>();
        authMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readCertificate()));
        authMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "2be4d475-f240-42c7-a22c-882566ac0f95"));
        authMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readCertificate()));
        authMeans.put(SIGNING_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "2e9ecac7-b840-4628-8036-d4998dfb8959"));
        authMeans.put(CLIENT_ID_STRING_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "fakeclientid"));
        authMeans.put(CLIENT_SECRET_STRING_NAME, new BasicAuthenticationMean(TPP_ID.getType(), "fakeclientsecret"));
        return authMeans;
    }

    private static String readCertificate() throws IOException, URISyntaxException {
        URL resource = NexiDataProviderHappyFlowIntegrationTest.class
                .getClassLoader().getResource("certificates/fake-certificate.pem");

        Path filePath = new File(Objects.requireNonNull(resource).toURI()).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }

}
