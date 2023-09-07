package com.yolt.providers.monorepogroup.cecgroup;

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
import com.yolt.providers.monorepogroup.TestSigner;
import com.yolt.providers.monorepogroup.cecgroup.common.CecGroupAccessMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.CecGroupDataProvider;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeansProducerV1.*;
import static nl.ing.lovebird.extendeddata.account.ExternalCashAccountType.CURRENT;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CecGroupTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/cecgroup/ais/happy-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("cecgroup")
class CecGroupDataProviderHappyFlowIntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String STATE = "6eff752f-ad5c-4d68-8355-89d291adcedb";
    private static final String REDIRECT_URI = "https://yolt.com/callback";
    private static final UUID USER_ID = UUID.fromString("400ca0ee-d8e7-4e0d-bbac-7fc69c189ad0");
    private static final String CONSENT_ID = "c3e66a4b-d6de-4c18-9195-303397ae13c3";

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("CecDataProviderV1")
    private CecGroupDataProvider dataProvider;

    @Autowired
    @Qualifier("CecGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private Clock clock;

    private static final Signer SIGNER = new TestSigner();

    private static final Map<String, BasicAuthenticationMean> AUTHENTICATION_MEANS = CecGroupSampleAuthenticationMeans.get();

    @Test
    void shouldReturnTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans).containsAllEntriesOf(
                Map.of(TRANSPORT_KEY_ID_NAME, KEY_ID,
                        TRANSPORT_CERTIFICATE_NAME, CERTIFICATE_PEM,
                        SIGNING_KEY_ID_NAME, KEY_ID,
                        SIGNING_CERTIFICATE_NAME, CERTIFICATE_PEM,
                        CLIENT_ID_NAME, CLIENT_ID_STRING,
                        CLIENT_SECRET_NAME, CLIENT_SECRET_STRING));
    }

    @Test
    void shouldReturnLoginUrl() {
        // given
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setSigner(SIGNER)
                .build();

        // when
        RedirectStep result = dataProvider.getLoginInfo(urlGetLoginRequest);

        // then
        assertThat(result.getRedirectUrl()).matches(".*/cec/prod/oauthcec/oauth2/authorize\\?consentId=c3e66a4b-d6de-4c18-9195-303397ae13c3&state=6eff752f-ad5c-4d68-8355-89d291adcedb&response_type=code&scope=AIS:c3e66a4b-d6de-4c18-9195-303397ae13c3&client_id=bb07276b-0a8a-41de-b95a-6c54a67a4d1c");
    }

    @Test
    void shouldCreateNewAccessMeans() throws JsonProcessingException {
        // given
        String redirectUrlWithCode = REDIRECT_URI + "?code=TEST_CODE";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(AUTHENTICATION_MEANS)
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(redirectUrlWithCode)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setProviderState(CONSENT_ID)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        String expectedAccessMeans = objectMapper.writeValueAsString(new CecGroupAccessMeans(CONSENT_ID,
                "TEST_ACCESS_TOKEN",
                "TEST_REFRESH_TOKEN",
                1640991600000L)
        );

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThat(result.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        assertThat(result.getAccessMeans().getAccessMeans()).isEqualTo(expectedAccessMeans);
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws JsonProcessingException, TokenInvalidException {
        // given
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setAuthenticationMeans(AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();
        String expectedAccessMeans = objectMapper.writeValueAsString(new CecGroupAccessMeans(
                        "c3e66a4b-d6de-4c18-9195-303397ae13c3",
                        "NEW_TEST_ACCESS_TOKEN",
                        "NEW_TEST_REFRESH_TOKEN",
                        1640991600000L
                )
        );
        // when
        AccessMeansDTO result = dataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getAccessMeans()).isEqualTo(expectedAccessMeans);
    }

    @Test
    void shouldFetchData() throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(SIGNER)
                .setAccessMeans(getAccessMeans())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .build();
        DataProviderResponse expectedResponse = createExpectedResponse();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        response.getAccounts().forEach(ProviderAccountDTO::validate);
        assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    private AccessMeansDTO getAccessMeans() throws JsonProcessingException {
        CecGroupAccessMeans accessMeans = new CecGroupAccessMeans(
                CONSENT_ID,
                "TEST_ACCESS_TOKEN",
                "TEST_REFRESH_TOKEN",
                3600L
        );

        return new AccessMeansDTO(USER_ID,
                objectMapper.writeValueAsString(accessMeans),
                new Date(clock.millis()),
                new Date(clock.millis()));
    }

    private DataProviderResponse createExpectedResponse() {
        return new DataProviderResponse(
                List.of(createAccount1(), createAccount2())
        );
    }

    private ProviderAccountDTO createAccount1() {
        return ProviderAccountDTO.builder()
                .accountId("436976287219712")
                .bic("AAAADEBBXXX")
                .accountNumber(new ProviderAccountNumberDTO(IBAN, "RO7612345987650123456789014"))
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(new BigDecimal("123"))
                .currentBalance(new BigDecimal("123"))
                .currency(CurrencyCode.EUR)
                .name("Gabriel Howard")
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .transactions(List.of(
                        createAccount1Transaction1(),
                        createAccount1Transaction2(),
                        createAccount1Transaction3(),
                        createAccount1Transaction4(),
                        createAccount1Transaction5()
                ))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("436976287219712")
                        .bic("AAAADEBBXXX")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("RO7612345987650123456789014")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Gabriel Howard")
                        .cashAccountType(CURRENT)
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.CLOSING_BOOKED)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("123"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .referenceDate(toZonedTime(2018, 3, 30))
                                        .build()
                        ))
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount1Transaction1() {
        return ProviderTransactionDTO.builder()
                .externalId("1234567")
                .dateTime(toZonedTime(2017, 10, 25))
                .amount(new BigDecimal("256.67"))
                .status(BOOKED)
                .type(CREDIT)
                .description("Example 1")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .bookingDate(toZonedTime(2017, 10, 25))
                        .valueDate(toZonedTime(2017, 10, 26))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("256.67"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("John Miles")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE67100100101306118605")
                                .build())
                        .transactionIdGenerated(true)
                        .remittanceInformationUnstructured("Example 1")
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount1Transaction2() {
        return ProviderTransactionDTO.builder()
                .externalId("1234568")
                .dateTime(toZonedTime(2017, 10, 27))
                .amount(new BigDecimal("343.01"))
                .status(BOOKED)
                .type(CREDIT)
                .description("Example 2")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .bookingDate(toZonedTime(2017, 10, 27))
                        .valueDate(toZonedTime(2017, 10, 28))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("343.01"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .debtorName("Paul Simpson")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("NL76RABO0359400371")
                                .build())
                        .remittanceInformationUnstructured("Example 2")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount1Transaction3() {
        return ProviderTransactionDTO.builder()
                .externalId("1234569")
                .dateTime(toZonedTime(2017, 10, 26))
                .amount(new BigDecimal("100.03"))
                .status(PENDING)
                .type(DEBIT)
                .description("Example 3")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(PENDING)
                        .bookingDate(toZonedTime(2017, 10, 26))
                        .valueDate(toZonedTime(2017, 10, 26))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("-100.03"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("Claude Renault")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("FR7612345987650123456789014")
                                .build())
                        .remittanceInformationUnstructured("Example 3")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount1Transaction4() {
        return ProviderTransactionDTO.builder()
                .externalId("2234567")
                .dateTime(toZonedTime(2017, 9, 25))
                .amount(new BigDecimal("233.67"))
                .status(BOOKED)
                .type(CREDIT)
                .description("Example 4")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .bookingDate(toZonedTime(2017, 9, 25))
                        .valueDate(toZonedTime(2017, 9, 26))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("233.67"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("John Doe")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE67100100101306118606")
                                .build())
                        .remittanceInformationUnstructured("Example 4")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount1Transaction5() {
        return ProviderTransactionDTO.builder()
                .externalId("22345610")
                .dateTime(toZonedTime(2017, 11, 26))
                .amount(new BigDecimal("65.03"))
                .status(PENDING)
                .type(DEBIT)
                .description("Example 5")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(PENDING)
                        .bookingDate(toZonedTime(2017, 11, 26))
                        .valueDate(toZonedTime(2017, 11, 26))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("-65.03"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("Someone else")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("FR7612345987650123456789015")
                                .build())
                        .remittanceInformationUnstructured("Example 5")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderAccountDTO createAccount2() {
        return ProviderAccountDTO.builder()
                .accountId("436976287219713")
                .bic("AAAADEBBXXX")
                .accountNumber(new ProviderAccountNumberDTO(IBAN, "RO7612345987650123456789015"))
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(new BigDecimal("603"))
                .currentBalance(new BigDecimal("603"))
                .currency(CurrencyCode.EUR)
                .name("CEC Current Account")
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .transactions(Collections.emptyList())
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("436976287219713")
                        .bic("AAAADEBBXXX")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("RO7612345987650123456789015")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("CEC Current Account")
                        .cashAccountType(CURRENT)
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.CLOSING_BOOKED)
                                        .referenceDate(toZonedTime(2018, 3, 30))
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("603"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .build()
                        ))
                        .build())
                .build();
    }

    private ZonedDateTime toZonedTime(int year, int month, int day) {
        return ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.MIN, ZoneId.of(("Europe/Bucharest")));
    }
}