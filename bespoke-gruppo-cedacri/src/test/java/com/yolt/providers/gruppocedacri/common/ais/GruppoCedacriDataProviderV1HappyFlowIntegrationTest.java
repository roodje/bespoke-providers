package com.yolt.providers.gruppocedacri.common.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.gruppocedacri.FakeRestTemplateManager;
import com.yolt.providers.gruppocedacri.GruppoCedacriSampleTypedAuthenticationMeans;
import com.yolt.providers.gruppocedacri.GruppoCedacriTestApp;
import com.yolt.providers.gruppocedacri.common.GruppoCedacriAccessMeans;
import com.yolt.providers.gruppocedacri.common.GruppoCedacriDataProviderV1;
import com.yolt.providers.gruppocedacri.common.dto.token.TokenResponse;
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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.yolt.providers.gruppocedacri.common.GruppoCedacriAuthenticationMeans.*;
import static java.time.temporal.ChronoUnit.SECONDS;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.*;

/**
 * This test suite contains all happy flows occurring in Gruppo Cedacri providers.
 * Tests are parametrized and run for all {@link GruppoCedacriDataProviderV1} providers in group.
 * Covered flows:
 * - getting typed authentication means
 * - getting typed authentication means configured during autoonboarding process
 * - registering on bank side
 * - acquiring consent page
 * - creating access means
 * - refreshing access means
 * - deleting consent on bank side
 * - fetching accounts, balances, transactions
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = GruppoCedacriTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/gruppocedacri/ais/happy-flow", port = 0, httpsPort = 0)
@ActiveProfiles("gruppocedacri")
public class GruppoCedacriDataProviderV1HappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String REDIRECT_URL = "https://yolt.com/callback";
    private static final String STATE = "d6290c4a-3ae9-415e-99cd-e572d0fca3f7";
    private static final String CONSENT_ID = "8c929c62-53f3-4543-97c0-0aed02b1d9bc";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String ACCESS_TOKEN = "o9xcq8V2zUg893gm6ROpO7XDUhaBkIOyilSHG0M11XCXFgjMPP7U6R";

    @Autowired
    @Qualifier("BancaMediolanumDataProviderV1")
    private GruppoCedacriDataProviderV1 bancaMediolanumDataProviderV1;

    Stream<UrlDataProvider> getGruppoCedacriProviders() {
        return Stream.of(bancaMediolanumDataProviderV1);
    }

    @Autowired
    @Qualifier("GruppoCedacri")
    private ObjectMapper objectMapper;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private RestTemplateManager restTemplateManager;

    @BeforeEach
    public void beforeEach() {
        authenticationMeans = new GruppoCedacriSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @ParameterizedTest
    @MethodSource("getGruppoCedacriProviders")
    void shouldReturnListOfAuthenticationMeans(UrlDataProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                CLIENT_ID_NAME,
                CLIENT_SECRET_NAME,
                EMAIL_NAME,
                CANCEL_LINK_NAME,
                CLIENT_TRANSPORT_CERTIFICATE_NAME,
                CLIENT_TRANSPORT_KEY_ID_NAME
        );
    }

    @ParameterizedTest
    @MethodSource("getGruppoCedacriProviders")
    void shouldReturnListOfAuthenticationMeansToBeSetDuringAutoOnboarding(AutoOnboardingProvider autoOnboardingProvider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = autoOnboardingProvider.getAutoConfiguredMeans();

        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                CLIENT_ID_NAME,
                CLIENT_SECRET_NAME
        );
    }

    @ParameterizedTest
    @MethodSource("getGruppoCedacriProviders")
    void shouldCreateNewRegistration(AutoOnboardingProvider autoOnboardingProvider) {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);
        registerMeans.remove(CLIENT_ID_NAME);
        registerMeans.remove(CLIENT_SECRET_NAME);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(registerMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setRedirectUrls(Collections.singletonList(REDIRECT_URL))
                .setScopes(Set.of(TokenScope.ACCOUNTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configureMeans = autoOnboardingProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans.containsKey(CLIENT_ID_NAME)).isTrue();
        assertThat(configureMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("8843e662-c45c-63c8-342e-123456f6bcef");
        assertThat(configureMeans.containsKey(CLIENT_SECRET_NAME)).isTrue();
        assertThat(configureMeans.get(CLIENT_SECRET_NAME).getValue()).isEqualTo("cb31234f-1b8a-3b45-a132-ad4be4efb4dc");
    }

    @ParameterizedTest
    @MethodSource("getGruppoCedacriProviders")
    void shouldCallConsentEndpointWithInvalidTokenAndReturnConsentPageUrl(UrlDataProvider dataProvider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(STATE)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        String loginUrl = redirectStep.getRedirectUrl();
        assertThat(loginUrl).contains("https://www.apimediolanum.it/ecm/login");

        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(loginUrl).build().getQueryParams().toSingleValueMap();
        assertThat(queryParams)
                .containsEntry("abi", "03062")
                .containsEntry("lang", "IT")
                .containsEntry("cancel_link", "https://tpp.psd2.cedacrigroup.it/")
                .containsEntry("d", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ")
                .containsEntry("client_id", "someClientId")
                .containsEntry("redirect_uri", "https://yolt.com/callback")
                .containsEntry("scope", "aisp.base")
                .containsEntry("state", STATE);
    }

    @ParameterizedTest
    @MethodSource("getGruppoCedacriProviders")
    void shouldCreateNewAccessMeansAndReturnScaRedirectUrl(UrlDataProvider dataProvider) throws IOException {
        // given
        String redirectUrl = "https://yolt.com/callback?code=someCode&state=" + STATE;

        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setState(STATE)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(request);

        // then
        // Validate access means
        assertThat(result.getAccessMeans()).isNull();

        // Validate step
        RedirectStep redirectStep = (RedirectStep) result.getStep();
        assertThat(redirectStep.getRedirectUrl()).isEqualTo("https://api.mediolanum.it:9090/consent/init?consent_id=8c929c62-53f3-4543-97c0-0aed02b1d9bc&d=eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBLU9BRVAtMjU2In0.A5WAzn0nw3g2t8yBD6k0_J9gwhhaOpJBVtm53TgWv4Goo1wkWoe4MWPlmzZeysle9sTiG3y3CbViuAqgpvH_pY-WKic2ZQgoTtJtgSexp3FN78FHrxuThrQDvzX8hC3Q2W4cJjL9n70rPwTycZaJI-GsHwGIN8Bi95AsgQk0lXMAgU2a-Zlb1lxTMHl_VXewppjhw_-Xe7jcn1V6cd3UHsfVj6oLXTM4FkhVItDO73ueFpdqWm8oTykrnCifhdt4mTGhgtSdBqDjJlyDHMzt7EtheVPXbPFcw84Y-ESXjSS1ubTZYxHNI87B0idEXXpZOIKghtN0GG4h5sjtAEO_cw.ZRkTBQ1u2GoIaWxIYiU-Bg.hKKNAvLi1_hWnCgXAsXoYGZrPpQaGw1bRPrQWMF9dXFJf_DO8cz-E3CejjqBZSkSDibT2kBfafZJkPONaPxmQTtTc6aUTfERxMqX-ImID57fOEZkiSoJz_n7ANg-tkx7BP13eW5nTyNAYryVyyaEoELwRBeTeuUbOpADsWuZV4cXXOKsdSfde0cphMj7euWtmaYFuthEzELuXAoGZDqqKu3ENhVotbzdH0n_vKUs35Y.BHls0M-oxoStLfFTPUb4lQ");

        // Validate provider state
        AccessMeansDTO accessMeansDTO = objectMapper.readValue(redirectStep.getProviderState(), AccessMeansDTO.class);
        GruppoCedacriAccessMeans accessMeans = objectMapper.readValue(accessMeansDTO.getAccessMeans(), GruppoCedacriAccessMeans.class);
        assertThat(accessMeans.getTokenResponse().getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(accessMeans.getTokenResponse().getTokenType()).isEqualTo("Bearer");
        assertThat(accessMeans.getTokenResponse().getExpiresIn()).isEqualTo(7776000);
        assertThat(accessMeans.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @ParameterizedTest
    @MethodSource("getGruppoCedacriProviders")
    void shouldReturnAccessMeansFromProviderState(UrlDataProvider dataProvider) throws IOException {
        // given
        AccessMeansDTO providerState = createAccessMeansDTO();

        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setProviderState(objectMapper.writeValueAsString(providerState))
                .setState(STATE)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(request);

        // then
        // Validate step
        assertThat(result.getStep()).isNull();

        // Validate access means
        AccessMeansDTO accessMeansDTO = result.getAccessMeans();
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);

        // Validate provider state
        GruppoCedacriAccessMeans accessMeans = objectMapper.readValue(accessMeansDTO.getAccessMeans(), GruppoCedacriAccessMeans.class);
        assertThat(accessMeans.getTokenResponse().getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(accessMeans.getTokenResponse().getTokenType()).isEqualTo("Bearer");
        assertThat(accessMeans.getTokenResponse().getExpiresIn()).isEqualTo(3920);
        assertThat(accessMeans.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @ParameterizedTest
    @MethodSource("getGruppoCedacriProviders")
    void shouldThrowTokenInvalidExceptionWhenAccessTokenIsExpired(UrlDataProvider dataProvider) throws IOException {
        // given
        AccessMeansDTO accessMeansDTO = createAccessMeansDTO();

        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.refreshAccessMeans(request);

        // then
        assertThatExceptionOfType(TokenInvalidException.class).isThrownBy(throwingCallable).withMessageContaining("Invalid token");
    }

    @ParameterizedTest
    @MethodSource("getGruppoCedacriProviders")
    void shouldSuccessfullyRemoveUserConsentOnUserSiteDelete(UrlDataProvider dataProvider) throws JsonProcessingException {
        // given
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(CONSENT_ID)
                .setAccessMeans(createAccessMeansDTO())
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> dataProvider.onUserSiteDelete(urlGetLogin);

        // then
        assertThatCode(onUserSiteDeleteCallable).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getGruppoCedacriProviders")
    void shouldReturnAccountsAndTransactions(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(createAccessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        List<ProviderAccountDTO> accounts = response.getAccounts();
        assertThat(accounts).hasSize(2);

        ProviderAccountDTO accountWithTransactions = accounts.get(0);
        assertThat(accountWithTransactions).satisfies(validateProviderAccountDTO("IT42Z0608500120000000616474_EUR", "3183.06", "1200.05", CurrencyCode.EUR));

        List<ProviderTransactionDTO> transactions = accountWithTransactions.getTransactions();
        assertThat(transactions).hasSize(3);
        assertThat(transactions.get(0)).satisfies(validateProviderTransactionDTO("1.00", BOOKED, CREDIT, "TEST",
                "2020-06-24T00:00+02:00[Europe/Rome]", CurrencyCode.EUR));
        assertThat(transactions.get(2)).satisfies(validateProviderTransactionDTO("2.00", BOOKED, DEBIT, "example",
                "2019-02-19T00:00+01:00[Europe/Rome]", CurrencyCode.EUR));

        ProviderAccountDTO accountWithEmptyTransactionsList = accounts.get(1);
        assertThat(accountWithEmptyTransactionsList).satisfies(validateProviderAccountDTO("IT42Z0608500120000000862916_USD", "3.00", "3.00", CurrencyCode.USD));

        List<ProviderTransactionDTO> emptyTransactionsList = accountWithEmptyTransactionsList.getTransactions();
        assertThat(emptyTransactionsList).isEmpty();
    }

    private Consumer<ProviderAccountDTO> validateProviderAccountDTO(String accountId,
                                                                    String availableBalance,
                                                                    String currentBalance,
                                                                    CurrencyCode currencyCode) {
        return providerAccountDTO -> {
            providerAccountDTO.validate();

            assertThat(providerAccountDTO.getAccountId()).isEqualTo(accountId);
            assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
            assertThat(providerAccountDTO.getLastRefreshed()).isCloseTo(ZonedDateTime.now(), within(32, SECONDS));
            assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal(availableBalance));
            assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal(currentBalance));
            assertThat(providerAccountDTO.getName()).isEqualTo("Banca Mediolanum Current Account - " + currencyCode);
            assertThat(providerAccountDTO.getCurrency()).isEqualTo(currencyCode);

            ProviderAccountNumberDTO accountNumberDTO = providerAccountDTO.getAccountNumber();
            assertThat(accountNumberDTO.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
            assertThat(accountNumberDTO.getIdentification()).isNotEmpty();

            ExtendedAccountDTO extendedAccountDTO = providerAccountDTO.getExtendedAccount();
            assertThat(extendedAccountDTO.getResourceId()).isEqualTo(accountId);
            assertThat(extendedAccountDTO.getName()).isEqualTo(providerAccountDTO.getName());
            assertThat(extendedAccountDTO.getCurrency()).isEqualTo(currencyCode);

            AccountReferenceDTO accountReferenceDTO = extendedAccountDTO.getAccountReferences().get(0);
            assertThat(accountReferenceDTO.getType()).isEqualTo(AccountReferenceType.IBAN);
            assertThat(accountReferenceDTO.getValue()).isEqualTo(accountNumberDTO.getIdentification());

            List<BalanceDTO> balances = extendedAccountDTO.getBalances();
            assertThat(balances).isNotEmpty();
        };
    }

    private Consumer<ProviderTransactionDTO> validateProviderTransactionDTO(String amount,
                                                                            TransactionStatus status,
                                                                            ProviderTransactionType type,
                                                                            String remittanceInformationUnstructured,
                                                                            String bookingDateTime,
                                                                            CurrencyCode currency) {
        return providerTransactionDTO -> {
            assertThat(providerTransactionDTO.getExternalId()).isNull();
            if (bookingDateTime != null) {
                assertThat(providerTransactionDTO.getDateTime()).isEqualTo(bookingDateTime);
            }
            assertThat(providerTransactionDTO.getAmount()).isEqualTo(new BigDecimal(amount));
            assertThat(providerTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(providerTransactionDTO.getType()).isEqualTo(type);
            assertThat(providerTransactionDTO.getDescription()).isNotEmpty();
            assertThat(providerTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);

            ExtendedTransactionDTO extendedTransactionDTO = providerTransactionDTO.getExtendedTransaction();
            assertThat(extendedTransactionDTO.getStatus()).isEqualTo(status);
            if (bookingDateTime != null) {
                assertThat(extendedTransactionDTO.getBookingDate()).isEqualTo(bookingDateTime);
            }
            assertThat(extendedTransactionDTO.getValueDate()).isNull();
            assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isEqualTo(remittanceInformationUnstructured);

            BalanceAmountDTO balanceAmountDTO = extendedTransactionDTO.getTransactionAmount();
            if (DEBIT.equals(type)) {
                assertThat(balanceAmountDTO.getAmount()).isEqualTo(new BigDecimal(amount).negate());
            }
            if (CREDIT.equals(type)) {
                assertThat(balanceAmountDTO.getAmount()).isEqualTo(new BigDecimal(amount));
            }
            assertThat(balanceAmountDTO.getCurrency()).isEqualTo(currency);
        };
    }

    private AccessMeansDTO createAccessMeansDTO() throws JsonProcessingException {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(ACCESS_TOKEN);
        tokenResponse.setExpiresIn(3920);
        tokenResponse.setTokenType("Bearer");

        GruppoCedacriAccessMeans accessMeans = new GruppoCedacriAccessMeans(tokenResponse, CONSENT_ID);

        return new AccessMeansDTO(
                USER_ID,
                objectMapper.writeValueAsString(accessMeans),
                new Date(),
                Date.from(Instant.now().plusSeconds(600)));
    }
}