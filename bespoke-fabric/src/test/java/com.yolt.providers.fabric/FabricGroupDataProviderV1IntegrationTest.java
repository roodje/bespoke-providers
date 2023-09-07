package com.yolt.providers.fabric;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.fabric.common.FabricGroupDataProviderV1;
import com.yolt.providers.fabric.common.model.GroupProviderState;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.SECONDS;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = AppConf.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs", httpsPort = 0, port = 0)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("fabric")
public class FabricGroupDataProviderV1IntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback-acc";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final LocalDate LOCAL_DATE_NOW = LocalDate.now(ZONE_ID);
    private static final LocalDate CONSENT_DATE = LocalDate.of(2022, 01, 01);
    private static final Instant FETCH_START_TIME = CONSENT_DATE.minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("BancaSellaDataProviderV1")
    private FabricGroupDataProviderV1 bancaSellaDataProvider;

    @Autowired
    @Qualifier("FabricGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplateManager restTemplateManager;

    private Stream<UrlDataProvider> getAllFabricGroupDataProviders() {
        return Stream.of(bancaSellaDataProvider);
    }

    @BeforeEach
    void setup() {
        authenticationMeans = SampleAuthenticationMeans.getSampleAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getAllFabricGroupDataProviders")
    void shouldReturnAuthenticationMeansAfterAutoConfigurationForAutoOnboarding(FabricGroupDataProviderV1 dataProvider) {
        // given
        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl("https://www.example.com")
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = dataProvider.autoConfigureMeans(request);

        // then
        assertThat(configuredAuthMeans).hasSize(4);
    }

    @ParameterizedTest
    @MethodSource("getAllFabricGroupDataProviders")
    void shouldReturnTypedAuthenticationMeansThatWillBeAutoConfigured(FabricGroupDataProviderV1 dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getAutoConfiguredMeans();

        // then
        assertThat(typedAuthMeans).hasSize(0);
    }

    @ParameterizedTest
    @MethodSource("getAllFabricGroupDataProviders")
    void shouldReturnCorrectLoginUrl(FabricGroupDataProviderV1 dataProvider) throws JsonProcessingException {
        // given
        String state = "11a1aaa1-aa1a-11a1-a111-a1a11a11aa11";
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(state)
                .setAuthenticationMeans(authenticationMeans)
                .setPsuIpAddress("127.0.0.1")
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep step = dataProvider.getLoginInfo(request);

        // then
        assertThat(step.getExternalConsentId()).isEqualTo("11111");

        GroupProviderState newProviderState = objectMapper.readValue(step.getProviderState(), GroupProviderState.class);
        assertThat(newProviderState.getConsentId()).isEqualTo("11111");
        assertThat(getLocalDateFromConsentGenerateAt(newProviderState)).isEqualTo(LOCAL_DATE_NOW);
        assertThat(newProviderState.getConsentValidTo()).isEqualTo(LOCAL_DATE_NOW.plusDays(89));

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(step.getRedirectUrl()).build();
        assertThat(uriComponents.getPath()).isEqualTo("/api/fabrick/psd2/v1/scaRedirect/aaaaaaaa-11a1-1111-aaaa-11111aaaa111");
    }

    @ParameterizedTest
    @MethodSource("getAllFabricGroupDataProviders")
    void shouldCreateAccessMeans(FabricGroupDataProviderV1 dataProvider) throws JsonProcessingException {
        //given
        GroupProviderState providerState = new GroupProviderState("11111", CONSENT_DATE.toEpochSecond(LocalTime.parse("00:00:00"), ZoneOffset.UTC), CONSENT_DATE);

        //given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setRedirectUrlPostedBackFromSite("https:\\baseRedirectUri.com")
                .setAuthenticationMeans(authenticationMeans)
                .setPsuIpAddress("127.0.0.1")
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(objectMapper.writeValueAsString(providerState))
                .build();

        //when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(request);

        //then
        assertThat(accessMeansOrStepDTO.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeansOrStepDTO.getAccessMeans().getExpireTime()).isInstanceOf(Date.class);
        assertThat(accessMeansOrStepDTO.getAccessMeans().getAccessMeans()).contains(
                "consentId", "11111",
                "consentGeneratedAt",
                "consentValidTo", "2022-01-01"
        );
    }


    @ParameterizedTest
    @MethodSource("getAllFabricGroupDataProviders")
    void shouldDeleteConsentSuccessfully(FabricGroupDataProviderV1 dataProvider) {
        //given
        String accessMeans = "{\"consentId\":\"11111\",\"consentGeneratedAt\":1, \"consentValidTo\":\"2022-01-01\"}";
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, accessMeans, new Date(), new Date());
        UrlOnUserSiteDeleteRequest request = new UrlOnUserSiteDeleteRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .build();

        //when
        ThrowableAssert.ThrowingCallable throwable = () -> dataProvider.onUserSiteDelete(request);

        //then
        assertThatCode(throwable).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getAllFabricGroupDataProviders")
    void shouldReturnAccountsAndTransactionsAndBalances(FabricGroupDataProviderV1 dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        String accessMeans = "{\"consentId\":\"11111\",\"consentGeneratedAt\":1, \"consentValidTo\":\"2022-01-01\"}";
        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(USER_ID, accessMeans, new Date(), new Date());

        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(accessMeansDTO)
                .setPsuIpAddress("127.0.0.1")
                .setTransactionsFetchStartTime(FETCH_START_TIME)
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        List<ProviderAccountDTO> accounts = response.getAccounts();
        assertThat(accounts).hasSize(2);

        ProviderAccountDTO accountWithTransactions = accounts.get(0);
        assertThat(accountWithTransactions).satisfies(validateProviderAccountDTO("1234", "IT7612548029981234567890122", "123.02", "123.02",
                BalanceType.CLOSING_BOOKED, "123.02", CurrencyCode.EUR, CurrencyCode.EUR));

        List<ProviderTransactionDTO> transactions = accountWithTransactions.getTransactions();
        assertThat(transactions).hasSize(2);
        assertThat(transactions.get(0)).satisfies(validateProviderTransactionDTO("222", "20.01", PENDING, DEBIT, "CB LIDL DAC",
                "2021-10-28T00:00Z", "2021-10-28T00:00Z", CurrencyCode.EUR, "CB LIDL DAC"));
        assertThat(transactions.get(1)).satisfies(validateProviderTransactionDTO("111", "100.01", BOOKED, CREDIT, "CB CARREFOUR DAC",
                "2021-10-28T00:00Z", "2021-10-28T00:00Z", CurrencyCode.EUR, "CB CARREFOUR DAC"));

    }

    private Consumer<ProviderAccountDTO> validateProviderAccountDTO(final String accountId,
                                                                    final String identification,
                                                                    final String availableBalance,
                                                                    final String currentBalance,
                                                                    final BalanceType balanceType,
                                                                    final String balanceAmount,
                                                                    final CurrencyCode balanceCurrency,
                                                                    final CurrencyCode currencyCode) {
        return providerAccountDTO -> {
            providerAccountDTO.validate();

            assertThat(providerAccountDTO.getAccountId()).isEqualTo(accountId);
            assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
            assertThat(providerAccountDTO.getLastRefreshed()).isCloseTo(ZonedDateTime.now(), within(32, SECONDS));
            assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal(availableBalance));
            assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal(currentBalance));
            assertThat(providerAccountDTO.getName()).isEqualTo("MLLE VALENTINE MABIRE");
            assertThat(providerAccountDTO.getCurrency()).isEqualTo(currencyCode);

            ProviderAccountNumberDTO accountNumberDTO = providerAccountDTO.getAccountNumber();
            assertThat(accountNumberDTO.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
            assertThat(accountNumberDTO.getIdentification()).isEqualTo(identification);

            ExtendedAccountDTO extendedAccountDTO = providerAccountDTO.getExtendedAccount();
            assertThat(extendedAccountDTO.getResourceId()).isEqualTo(accountId);
            assertThat(extendedAccountDTO.getName()).isEqualTo(providerAccountDTO.getName());
            assertThat(extendedAccountDTO.getCurrency()).isEqualTo(currencyCode);

            AccountReferenceDTO accountReferenceDTO = extendedAccountDTO.getAccountReferences().get(0);
            assertThat(accountReferenceDTO.getType()).isEqualTo(AccountReferenceType.IBAN);
            assertThat(accountReferenceDTO.getValue()).isEqualTo(accountNumberDTO.getIdentification());

            List<BalanceDTO> balances = extendedAccountDTO.getBalances();
            assertThat(balances.get(0).getBalanceType()).isEqualTo(balanceType);
            assertThat(balances.get(0).getBalanceAmount().getAmount()).isEqualTo(balanceAmount);
            assertThat(balances.get(0).getBalanceAmount().getCurrency()).isEqualTo(balanceCurrency);
        };
    }

    private Consumer<ProviderTransactionDTO> validateProviderTransactionDTO(final String transactionId,
                                                                            final String amount,
                                                                            final TransactionStatus status,
                                                                            final ProviderTransactionType type,
                                                                            final String remittanceInformationUnstructured,
                                                                            final String bookingDateTime,
                                                                            final String valueDateTime,
                                                                            final CurrencyCode currency,
                                                                            final String description) {
        return providerTransactionDTO -> {
            assertThat(providerTransactionDTO.getExternalId()).isEqualTo(transactionId);
            if (bookingDateTime != null) {
                assertThat(providerTransactionDTO.getDateTime()).isEqualTo(bookingDateTime);
            }
            assertThat(providerTransactionDTO.getAmount()).isEqualTo(new BigDecimal(amount));
            assertThat(providerTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(providerTransactionDTO.getType()).isEqualTo(type);
            assertThat(providerTransactionDTO.getDescription()).isEqualTo(description);
            assertThat(providerTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);

            ExtendedTransactionDTO extendedTransactionDTO = providerTransactionDTO.getExtendedTransaction();
            assertThat(extendedTransactionDTO.getStatus()).isEqualTo(status);
            if (bookingDateTime != null) {
                assertThat(extendedTransactionDTO.getBookingDate()).isEqualTo(bookingDateTime);
            }
            if (valueDateTime != null) {
                assertThat(extendedTransactionDTO.getValueDate()).isEqualTo(valueDateTime);
            }
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


    private LocalDate getLocalDateFromConsentGenerateAt(GroupProviderState newProviderState) {
        return Instant.ofEpochSecond(newProviderState.getConsentGeneratedAt() / 1000).atZone(ZONE_ID).toLocalDate();
    }
}

