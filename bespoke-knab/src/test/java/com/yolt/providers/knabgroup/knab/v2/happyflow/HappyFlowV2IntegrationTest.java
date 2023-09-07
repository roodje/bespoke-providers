package com.yolt.providers.knabgroup.knab.v2.happyflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.knabgroup.TestApp;
import com.yolt.providers.knabgroup.TestRestTemplateManager;
import com.yolt.providers.knabgroup.TestSigner;
import com.yolt.providers.knabgroup.common.KnabGroupDataProviderV2;
import com.yolt.providers.knabgroup.common.data.TransactionBankSpecific;
import com.yolt.providers.knabgroup.common.dto.internal.KnabAccessMeans;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExchangeRateDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.YoltCategory;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

import static nl.ing.lovebird.extendeddata.common.CurrencyCode.EUR;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.AccountType.SAVINGS_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/happyflowv2", httpsPort = 0, port = 0)
class HappyFlowV2IntegrationTest {

    private static final String PSU_IP_ADDRESS = "0.0.0.0";

    private static final UUID USER_ID = UUID.fromString("fdbc609b-ec60-4ddf-a19a-5223c8b5b100");
    public static final String BASE_CLIENT_REDIRECT_URL = "https://www.yolt.com/callback-dev";

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    private Clock clock;

    @Autowired
    @Qualifier("KnabDataProviderV2")
    private KnabGroupDataProviderV2 provider;

    @Autowired
    private ObjectMapper mapper;

    private RestTemplateManager restTemplateManager;
    private Map<String, BasicAuthenticationMean> authenticationMeans = SampleAuthenticationMeans.getSampleAuthenticationMeans();
    private Signer signer = new TestSigner();

    @BeforeEach
    public void beforeEach() {
        restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @Test
        // getLoginInfo
    void shouldReturnRedirectUrlWithAuthorizeUrlToSiteManagement() {
        // given
        String state = UUID.randomUUID().toString();
        ;
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setAuthenticationMeans(authenticationMeans)
                .setState(state)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        RedirectStep loginInfo = provider.getLoginInfo(urlGetLoginRequest);

        // then
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(loginInfo.getRedirectUrl()).build();

        assertThat(uri.getQueryParams().toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "response_type", "code",
                "client_id", "d3de0198-6738-4784-92d0-a3e5e0894413",
                "redirect_uri", BASE_CLIENT_REDIRECT_URL,
                "scope", "psd2 offline_access AIS:some-consent-id",
                "state", state
        ));

        assertThat(loginInfo.getExternalConsentId()).isEqualTo("some-consent-id");
        assertThat(loginInfo.getProviderState()).isNull();
    }

    @Test
    void shouldReturnAccessMeans() throws JsonProcessingException {
        // given
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback-dev?code=my-awesome-authorization-code&scope=psd2%20offline_access%20AIS%3Amy-consent-id&state=random42")
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansDTO accessMeansDTO = provider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        //then
        assertThat(accessMeansDTO.getUpdated()).isBefore(accessMeansDTO.getExpireTime());
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);
        String accessMeans = accessMeansDTO.getAccessMeans();
        KnabAccessMeans knabAccessMeans = mapper.readValue(accessMeans, KnabAccessMeans.class);
        assertThat(knabAccessMeans).extracting(
                KnabAccessMeans::getAccessToken,
                KnabAccessMeans::getRefreshToken,
                KnabAccessMeans::getTokenType,
                KnabAccessMeans::getScope
        ).contains("user-access-token", "user-refresh-token", "Bearer", "psd2 offline_access AIS:some-consent-id");
    }

    @Test
    void shouldReturnRefreshedAccessMeans() throws JsonProcessingException, TokenInvalidException {
        // given
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setAccessMeans(createAccessMeansDto())
                .build();

        //when
        AccessMeansDTO refreshedAccessMeans = provider.refreshAccessMeans(urlRefreshAccessMeans);

        //then
        assertThat(refreshedAccessMeans.getUpdated()).isBefore(refreshedAccessMeans.getExpireTime());
        assertThat(refreshedAccessMeans.getUserId()).isEqualTo(USER_ID);
        String accessMeans = refreshedAccessMeans.getAccessMeans();
        KnabAccessMeans knabAccessMeans = mapper.readValue(accessMeans, KnabAccessMeans.class);
        assertThat(knabAccessMeans).extracting(
                KnabAccessMeans::getAccessToken,
                KnabAccessMeans::getRefreshToken,
                KnabAccessMeans::getTokenType,
                KnabAccessMeans::getScope
        ).contains("brand-new-user-access-token", "brand-new-user-refresh-token", "Bearer", "psd2 offline_access AIS:some-consent-id");
    }

    @Test
    void shouldFetchData() throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(createAccessMeansDto())
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();
        DataProviderResponse expectedResult = new DataProviderResponse(getExpectedAccounts());

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchData);

        // then

        //Verify Accounts
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        List<ProviderAccountDTO> expectedAccounts = expectedResult.getAccounts();
        assertThat(accounts).hasSize(2);
        ProviderAccountDTO account_0 = accounts.get(0);
        ProviderAccountDTO expectedExtendedAccount_0 = expectedAccounts.get(0);
        assertThat(account_0)
                .usingRecursiveComparison()
                .ignoringFields("lastRefreshed", "transactions", "extendedAccount")
                .isEqualTo(expectedExtendedAccount_0);
        assertThat(account_0.getExtendedAccount())
                .usingRecursiveComparison()
                .isEqualTo(expectedExtendedAccount_0.getExtendedAccount());
        assertThat(account_0.getLastRefreshed()).isNotNull();
        ProviderAccountDTO account_1 = accounts.get(1);
        ProviderAccountDTO expectedExtendedAccount_1 = expectedAccounts.get(1);
        assertThat(account_1)
                .usingRecursiveComparison()
                .ignoringFields("lastRefreshed", "transactions", "extendedAccount")
                .isEqualTo(expectedExtendedAccount_1);
        assertThat(account_1.getExtendedAccount())
                .usingRecursiveComparison()
                .isEqualTo(expectedExtendedAccount_1.getExtendedAccount());
        assertThat(account_1.getLastRefreshed()).isNotNull();

        List<ProviderTransactionDTO> transactions_0 = account_0.getTransactions();
        List<ProviderTransactionDTO> expectedTransactions_0 = expectedExtendedAccount_0.getTransactions();
        assertThat(transactions_0).hasSize(1);
        ProviderTransactionDTO transaction_0 = transactions_0.get(0);
        ProviderTransactionDTO expectedExtendedTransaction_0 = expectedTransactions_0.get(0);
        assertThat(transaction_0)
                .usingRecursiveComparison()
                .ignoringFields("extendedTransaction")
                .isEqualTo(expectedExtendedTransaction_0);
        assertThat(transaction_0.getExtendedTransaction())
                .usingRecursiveComparison()
                .isEqualTo(expectedExtendedTransaction_0.getExtendedTransaction());

        List<ProviderTransactionDTO> transactions_1 = account_1.getTransactions();
        List<ProviderTransactionDTO> expectedTransactions_1 = expectedExtendedAccount_1.getTransactions();
        assertThat(transactions_1.size()).isEqualTo(expectedTransactions_1.size());
        assertThat(transactions_1).hasSize(1);
        ProviderTransactionDTO transaction_1 = transactions_1.get(0);
        ProviderTransactionDTO expectedExtendedTransaction_1 = expectedTransactions_1.get(0);
        assertThat(transaction_1)
                .usingRecursiveComparison()
                .ignoringFields("extendedTransaction")
                .isEqualTo(expectedExtendedTransaction_1);
        assertThat(transaction_1.getExtendedTransaction())
                .usingRecursiveComparison()
                .isEqualTo(expectedExtendedTransaction_1.getExtendedTransaction());
    }

    private AccessMeansDTO createAccessMeansDto() {
        String SERIALIZED_ACCESS_MEANS = "{\"accessToken\":\"userAccessToken\",\"refreshToken\":\"userRefreshToken\",\"tokenType\":\"Bearer\",\"expiryTimestamp\":1595848039000,\"scope\":\"psd2 offline_access AIS:userConsentId\"}";
        Date UNUSED_DATE_IN_PROVIDERS_SERVICE = new Date();
        return new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, UNUSED_DATE_IN_PROVIDERS_SERVICE, UNUSED_DATE_IN_PROVIDERS_SERVICE);
    }

    private List<ProviderAccountDTO> getExpectedAccounts() {
        return Arrays.asList(ProviderAccountDTO.builder()
                .accountId("f1b1066d0209-4945-ba0e-7c1b9671c544")
                .accountNumber(createProviderAccountNumberDTO("NL57KNAB9990461305", "OrganizationSampleName1"))
                .currency(CurrencyCode.EUR)
                .name("Current Account")
                .yoltAccountType(CURRENT_ACCOUNT)
                .availableBalance(new BigDecimal("9391.63"))
                .currentBalance(new BigDecimal("8592.89"))
                .transactions(getProviderTransactions1())
                .extendedAccount(getExtendedAccount1())
                .build(), ProviderAccountDTO.builder()
                .accountId("cfd7b2f8-85d1-4ac8-8476-fd51a286cd8c")
                .currency(CurrencyCode.EUR)
                .name("Knab Flexibel Sparen Zakelijk")
                .yoltAccountType(SAVINGS_ACCOUNT)
                .availableBalance(new BigDecimal("100.63"))
                .currentBalance(new BigDecimal("200.89"))
                .transactions(getProviderTransactions2())
                .extendedAccount(getExtendedAccount2())
                .build());
    }

    private List<ProviderTransactionDTO> getProviderTransactions1() {
        return Collections.singletonList(ProviderTransactionDTO.builder()
                .externalId("C0F10SRA002A00HK000001")
                .extendedTransaction(getExtendedTransaction1())
                .type(DEBIT)
                .amount(new BigDecimal("153.90"))
                .status(BOOKED)
                .dateTime(getDateTime("2020-06-09T00:00:00+02:00[Europe/Amsterdam]"))
                .description("A gift from Grandpa. - Periodic transfer")
                .category(YoltCategory.GENERAL)
                .merchant("Jonas Snow")
                .bankSpecific(TransactionBankSpecific.builder()
                        .dayStartBalanceCurrency("EUR")
                        .dayStartBalanceAmount("90.10")
                        .build().toMap())
                .build());
    }

    private List<ProviderTransactionDTO> getProviderTransactions2() {
        return Collections.singletonList(ProviderTransactionDTO.builder()
                .externalId("C0F10SRA002A00HK000002")
                .extendedTransaction(getExtendedTransaction2())
                .type(CREDIT)
                .amount(new BigDecimal("2.90"))
                .status(BOOKED)
                .dateTime(getDateTime("2020-06-19T00:00:00+02:00[Europe/Amsterdam]"))
                .description("One time transfer")
                .category(YoltCategory.GENERAL)
                .build());
    }

    private ExtendedTransactionDTO getExtendedTransaction1() {
        return ExtendedTransactionDTO.builder()
                .bookingDate(getDateTime("2020-06-10T00:00:00+02:00[Europe/Amsterdam]"))
                .valueDate(getDateTime("2020-06-11T00:00:00+02:00[Europe/Amsterdam]"))
                .creditorName("Jonas Snow")
                .transactionIdGenerated(false)
                .proprietaryBankTransactionCode("Periodic transfer")
                .entryReference("0006551")
                .remittanceInformationUnstructured("A gift from Grandpa.")
                .remittanceInformationStructured("A gift from Grandpa. Structured")
                .endToEndId("C0F10SRBMT80UM0E")
                .mandateId("mandateId1")
                .creditorId("creditorId1")
                .status(BOOKED)
                .exchangeRate(Collections.singletonList(new ExchangeRateDTO(CurrencyCode.EUR, null, CurrencyCode.PLN, "rate1", getDateTime("2020-06-12T00:00:00+02:00[Europe/Amsterdam]"), "rateContract1")))
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("-153.90")))
                .creditorAccount(new AccountReferenceDTO(AccountReferenceType.BBAN, "53964486"))
                .build();
    }

    private ExtendedTransactionDTO getExtendedTransaction2() {
        return ExtendedTransactionDTO.builder()
                .bookingDate(getDateTime("2020-06-20T00:00:00+02:00[Europe/Amsterdam]"))
                .valueDate(getDateTime("2020-06-21T00:00:00+02:00[Europe/Amsterdam]"))
                .debtorName("Jonas Malcolm Snow")
                .transactionIdGenerated(false)
                .proprietaryBankTransactionCode("One time transfer")
                .entryReference("0006552")
                .remittanceInformationStructured("A gift from Rich Grandpa. Structured")
                .endToEndId("C0F10SRBMT80UM0F")
                .mandateId("mandateId2")
                .creditorId("creditorId2")
                .status(BOOKED)
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("2.90")))
                .debtorAccount(new AccountReferenceDTO(AccountReferenceType.IBAN, "NL57KNAB9990461307"))
                .build();
    }

    private ExtendedAccountDTO getExtendedAccount2() {
        return ExtendedAccountDTO.builder()
                .resourceId("cfd7b2f8-85d1-4ac8-8476-fd51a286cd8c")
                .accountReferences(Collections.singletonList(AccountReferenceDTO.builder().type(AccountReferenceType.BBAN).value("53964486").build()))
                .name("OrganizationSampleName2")
                .product("Knab Flexibel Sparen Zakelijk")
                .currency(EUR)
                .balances(getBalances2())
                .build();
    }

    private ExtendedAccountDTO getExtendedAccount1() {
        return ExtendedAccountDTO.builder()
                .resourceId("f1b1066d0209-4945-ba0e-7c1b9671c544")
                .accountReferences(Collections.singletonList(AccountReferenceDTO.builder().type(AccountReferenceType.IBAN).value("NL57KNAB9990461305").build()))
                .name("OrganizationSampleName1")
                .product("Current Account")
                .currency(EUR)
                .balances(getBalances1())
                .build();
    }

    private List<BalanceDTO> getBalances1() {
        return Arrays.asList(
                BalanceDTO.builder()
                        .balanceType(BalanceType.INTERIM_BOOKED)
                        .balanceAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("8592.89")))
                        .build(),

                BalanceDTO.builder()
                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                        .balanceAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("9391.63")))
                        .lastCommittedTransaction("entryReference1")
                        .lastChangeDateTime(getDateTime("2018-07-01T11:16:54.991+02:00[Europe/Amsterdam]"))
                        .build());
    }

    private List<BalanceDTO> getBalances2() {
        return Arrays.asList(
                BalanceDTO.builder()
                        .balanceType(BalanceType.INTERIM_BOOKED)
                        .balanceAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("200.89")))
                        .build(),

                BalanceDTO.builder()
                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                        .balanceAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("100.63")))
                        .lastCommittedTransaction("entryReference2")
                        .lastChangeDateTime(getDateTime("2018-07-02T11:16:54.991+02:00[Europe/Amsterdam]"))
                        .build());
    }

    private ZonedDateTime getDateTime(final String dateTime) {
        return ZonedDateTime.parse(dateTime);
    }

    private ProviderAccountNumberDTO createProviderAccountNumberDTO(final String iban, final String holderName) {
        ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(IBAN, iban);
        providerAccountNumberDTO.setHolderName(holderName);
        return providerAccountNumberDTO;
    }
}