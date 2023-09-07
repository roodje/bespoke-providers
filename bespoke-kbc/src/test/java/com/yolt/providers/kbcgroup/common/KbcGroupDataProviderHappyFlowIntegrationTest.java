package com.yolt.providers.kbcgroup.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.kbcgroup.FakeRestTemplateManager;
import com.yolt.providers.kbcgroup.KbcGroupSampleAuthenticationMeans;
import com.yolt.providers.kbcgroup.KbcGroupTestApp;
import com.yolt.providers.kbcgroup.cbcbank.CbcBankDataProvider;
import com.yolt.providers.kbcgroup.common.dto.KbcGroupAccessMeans;
import com.yolt.providers.kbcgroup.common.dto.KbcGroupLoginFormDTO;
import com.yolt.providers.kbcgroup.common.dto.KbcGroupTokenResponse;
import com.yolt.providers.kbcgroup.kbcbank.KbcBankDataProvider;
import lombok.SneakyThrows;
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
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = KbcGroupTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/mappings/kbcgroup/2.0.6/ais/v1/happy_flow/", httpsPort = 0, port = 0)
@ActiveProfiles("kbcgroup")
class KbcGroupDataProviderHappyFlowIntegrationTest {

    private static final String TEST_STATE = "test-code-verifier";
    private static final String TEST_PSU_IP_ADDRESS = "123.45.67.89";
    private static final UUID TEST_USER_ID = UUID.randomUUID();

    private static final Map<String, BasicAuthenticationMean> TEST_AUTHENTICATION_MEANS = KbcGroupSampleAuthenticationMeans.get();
    private static final String TEST_CONSENT_ID = "test-consent-id";
    private static final String TEST_REDIRECT_URL = "https://example.com/callback";

    @Autowired
    @Qualifier("KbcGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private KbcBankDataProvider kbcBankDataProvider;
    @Autowired
    private CbcBankDataProvider cbcBankDataProvider;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    Stream<KbcGroupDataProvider> kbcGroupDataProviders() {
        return Stream.of(kbcBankDataProvider, cbcBankDataProvider);
    }

    private RestTemplateManager restTemplateManager;

    @BeforeEach
    public void setUp() {
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @ParameterizedTest
    @MethodSource("kbcGroupDataProviders")
    void shouldReturnFormStepOnGetLoginInfo(KbcGroupDataProvider providerUnderTest) {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/callback-acc").setState(UUID.randomUUID().toString())
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();

        // when
        FormStep formStep = (FormStep) providerUnderTest.getLoginInfo(urlGetLogin);

        // then
        TextField textField = (TextField) formStep.getForm().getFormComponents().get(0);
        assertThat(textField.getId()).isEqualTo("Iban");
        assertThat(textField.getDisplayName()).isEqualTo("IBAN");
        assertThat(textField.getLength()).isEqualTo(34);
        assertThat(textField.getMaxLength()).isEqualTo(34);
    }

    @ParameterizedTest
    @MethodSource("kbcGroupDataProviders")
    void shouldReturnRedirectStepIfTriggeredAfterFormStep(KbcGroupDataProvider providerUnderTest) throws JsonProcessingException {
        // given
        String stateId = UUID.randomUUID().toString();
        String redirectUrl = "https://yolt.com/callback-acc";
        KbcGroupLoginFormDTO loginFormDTO = new KbcGroupLoginFormDTO(
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID()),
                redirectUrl);

        String providerState = objectMapper.writeValueAsString(loginFormDTO);
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("Iban", "BE16690375703426");
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setProviderState(providerState)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setRestTemplateManager(restTemplateManager)
                .setState(stateId)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) providerUnderTest.createNewAccessMeans(urlCreateAccessMeansRequest).getStep();

        // then
        String result = redirectStep.getRedirectUrl();
        assertThat(result).contains("/ASK/oauth/authorize/")
                .contains("client_id=PSDNL-ABC-12345")
                .contains("scope=AIS:" + TEST_CONSENT_ID)
                .contains("code_challenge_method=S256")
                .contains("redirect_uri=https://yolt.com/callback-acc")
                .contains("state=" + stateId)
                .matches(".*code_challenge=.+");
    }

    @ParameterizedTest
    @MethodSource("kbcGroupDataProviders")
    void shouldCreateNewAccessMeans(KbcGroupDataProvider providerUnderTest) {
        // given
        UUID testUserId = UUID.randomUUID();
        String redirectUrl = "https://yolt.com/callback-acc?code=test-code";
        String baseUrl = "https://yolt.com/callback-acc";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setUserId(testUserId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setBaseClientRedirectUrl(baseUrl)
                .setProviderState(TEST_STATE)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansOrStepDTO result = providerUnderTest.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThat(result.getAccessMeans().getUserId()).isEqualTo(testUserId);
        assertThat(result.getAccessMeans().getAccessMeans()).containsSequence("test-access-token");
    }

    @ParameterizedTest
    @MethodSource("kbcGroupDataProviders")
    void shouldRefreshTokenSuccessfully(KbcGroupDataProvider providerUnderTest) throws TokenInvalidException {
        // given
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        AccessMeansDTO result = providerUnderTest.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getAccessMeans()).containsSequence("test-access-token-refreshed");
    }

    @ParameterizedTest
    @MethodSource("kbcGroupDataProviders")
    void shouldFetchDataSuccessfully(KbcGroupDataProvider providerUnderTest) throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManager)
                .setTransactionsFetchStartTime(Instant.now())
                .setUserId(TEST_USER_ID)
                .build();

        // when
        DataProviderResponse dataProviderResponse = providerUnderTest.fetchData(urlFetchDataRequest);

        // then
        ProviderAccountDTO expectedAccount = dataProviderResponse.getAccounts().get(0);
        verifyAccount(expectedAccount);
        verifyExtendedAccount(expectedAccount.getExtendedAccount());
        verifyTransactions(expectedAccount.getTransactions());
        verifyOneGivenExtendedTransaction(expectedAccount.getTransactions().get(0).getExtendedTransaction());
    }

    private void verifyAccount(ProviderAccountDTO expectedAccount) {
        assertThat(expectedAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(expectedAccount.getAvailableBalance()).isEqualTo("123.45");
        assertThat(expectedAccount.getAccountId()).isEqualTo("test-resource-id");
        assertThat(expectedAccount.getAccountNumber())
                .extracting(ProviderAccountNumberDTO::getScheme, ProviderAccountNumberDTO::getIdentification)
                .contains(ProviderAccountNumberDTO.Scheme.IBAN, "BE16690375703426");
        assertThat(expectedAccount.getName()).isEqualTo("test-name");
        assertThat(expectedAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
    }

    private void verifyExtendedAccount(ExtendedAccountDTO extendedAccount) {
        assertThat(extendedAccount.getResourceId()).isEqualTo("test-resource-id");
        assertThat(extendedAccount.getAccountReferences())
                .extracting(AccountReferenceDTO::getType, AccountReferenceDTO::getValue)
                .contains(tuple(AccountReferenceType.IBAN, "BE16690375703426"));
        assertThat(extendedAccount.getBalances()).isEqualTo(expectedBalanceDtoList());
        assertThat(extendedAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedAccount.getName()).isEqualTo("test-name");
        assertThat(extendedAccount.getProduct()).isEqualTo("test-product");
    }

    private void verifyTransactions(List<ProviderTransactionDTO> expectedTransactions) {
        assertThat(expectedTransactions).hasSize(2);

        ProviderTransactionDTO transaction1 = expectedTransactions.get(0);
        assertThat(transaction1.getDateTime()).isEqualTo("2020-02-18T00:00+01:00[Europe/Brussels]");
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction1.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction1.getAmount()).isEqualTo("11.22");
        assertThat(transaction1.getDescription()).isEqualTo("test-remittance-info-unstructured");
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.BOOKED);

        ProviderTransactionDTO transaction2 = expectedTransactions.get(1);
        assertThat(transaction2.getDateTime()).isEqualTo("2020-02-19T00:00+01:00[Europe/Brussels]");
        assertThat(transaction2.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction2.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction2.getAmount()).isEqualTo("3.14");
        assertThat(transaction2.getDescription()).isEqualTo("test-remittance-info-unstructured-2");
        assertThat(transaction2.getStatus()).isEqualTo(TransactionStatus.BOOKED);
    }

    private void verifyOneGivenExtendedTransaction(ExtendedTransactionDTO extendedTransaction) {
        assertThat(extendedTransaction.getProprietaryBankTransactionCode()).isEqualTo("test-proprietary-bank-tx-code");
        assertThat(extendedTransaction.getBankTransactionCode()).isEqualTo("PMNT-RCDT-ESCT");
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-02-18T00:00+01:00[Europe/Brussels]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-02-18T00:00+01:00[Europe/Brussels]");
        assertThat(extendedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("11.22");
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("test-remittance-info-unstructured");
        assertThat(extendedTransaction.getCreditorName()).isEqualTo("Test Creditor Name");
        assertThat(extendedTransaction.getCreditorAccount()).isEqualTo(new AccountReferenceDTO(AccountReferenceType.IBAN, "BE16690375703426"));
        assertThat(extendedTransaction.getDebtorName()).isEqualTo("Test Debtor Name");
        assertThat(extendedTransaction.getDebtorAccount()).isEqualTo(new AccountReferenceDTO(AccountReferenceType.IBAN, "BE16690375703427"));
    }

    private List<BalanceDTO> expectedBalanceDtoList() {
        return Collections.singletonList(BalanceDTO.builder()
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.valueOf("EUR"), new BigDecimal("123.45")))
                .balanceType(BalanceType.fromName("closingBooked"))
                .build());
    }

    @SneakyThrows
    private AccessMeansDTO getAccessMeans() {
        KbcGroupTokenResponse token = KbcGroupTokenResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .expiresIn(3600)
                .build();
        KbcGroupAccessMeans kbcGroupAccessMeans = new KbcGroupAccessMeans(token, TEST_REDIRECT_URL, TEST_CONSENT_ID);

        return new AccessMeansDTO(TEST_USER_ID,
                objectMapper.writeValueAsString(kbcGroupAccessMeans),
                new Date(),
                new Date());
    }
}