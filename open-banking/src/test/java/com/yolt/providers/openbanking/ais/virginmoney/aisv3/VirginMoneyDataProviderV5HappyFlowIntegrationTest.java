package com.yolt.providers.openbanking.ais.virginmoney.aisv3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import com.yolt.providers.openbanking.ais.virginmoney.VirginMoneyApp;
import com.yolt.providers.openbanking.ais.virginmoney.VirginMoneyDataProviderV5;
import com.yolt.providers.openbanking.ais.virginmoney.VirginMoneyJwsSigningResult;
import com.yolt.providers.openbanking.ais.virginmoney.VirginMoneySampleAuthenticationMeansV3;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.openbanking.ais.virginmoney.auth.VirginMoneyAuthMeansBuilderV4.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This test suite contains all happy flows occurring in Virgin Money provider.
 * Covered flows:
 * - acquiring consent page
 * - creating access means
 * - refreshing access means
 * - fetching accounts, balances, transactions
 * <p>
 */
@SpringBootTest(classes = {VirginMoneyApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("virginmoney")
@AutoConfigureWireMock(files = "classpath:/stubs/virginmoney/ais/v4/happyflow",
        stubs = "classpath:/stubs/virginmoney/ais/v3/happyflow",
        httpsPort = 0,
        port = 0)
public class VirginMoneyDataProviderV5HappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID USER_SITE_ID = UUID.randomUUID();
    private static final String ACCOUNT_ID = "3";
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";

    private RestTemplateManager restTemplateManagerMock;
    private static final SignerMock SIGNER = new SignerMock();
    private String requestTraceId;

    @Autowired
    @Qualifier("VirginMoneyDataProviderV5")
    private VirginMoneyDataProviderV5 dataProvider;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private VirginMoneySampleAuthenticationMeansV3 sampleAuthenticationMeans = new VirginMoneySampleAuthenticationMeansV3();

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        requestTraceId = "6706f0a2-6ae7-46b3-8a12-815497b73c7a";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = sampleAuthenticationMeans.getVirginMoneySampleAuthenticationMeansForAis();

        when(signer.sign(ArgumentMatchers.any(JsonWebSignature.class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(new VirginMoneyJwsSigningResult());
    }

    @Test
    public void shouldReturnTypedAuthenticationMeansThatWillBeAutoConfigured() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getAutoConfiguredMeans();

        // then
        assertThat(typedAuthMeans)
                .hasSize(2)
                .containsEntry(CLIENT_ID_NAME, CLIENT_ID_STRING)
                .containsEntry(CLIENT_SECRET_NAME, CLIENT_SECRET_STRING);

    }

    @Test
    public void shouldReturnAuthenticationMeansAfterAutoConfiguration() throws IOException, URISyntaxException {
        // given
        authenticationMeans.remove(CLIENT_ID_NAME);
        String expectedTransportKeyId = authenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue();
        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .setRedirectUrls(Collections.singletonList("https://yolt.com/callback-acc"))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = dataProvider.autoConfigureMeans(request);

        // then
        assertThat(configuredAuthMeans).hasSize(9);
        assertThat(configuredAuthMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("SOME_FAKE_CLIENT_ID");
        assertThat(configuredAuthMeans.get(CLIENT_SECRET_NAME).getValue()).isEqualTo("SOME_FAKE_CLIENT_SECRET");
        assertThat(configuredAuthMeans.get(INSTITUTION_ID_NAME).getValue()).isEqualTo("0016800001051XVBBZ");
        assertThat(configuredAuthMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue()).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        assertThat(configuredAuthMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()).isEqualTo("5b626fbf-9761-4dfb-a1d6-132f5ee40355");
        assertThat(configuredAuthMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue()).isEqualTo("signing-key-header-id-2");
        assertThat(configuredAuthMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()).isEqualTo(expectedTransportKeyId);
        assertThat(configuredAuthMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()).isEqualTo(sampleAuthenticationMeans.readFakeCertificatePem());
        assertThat(configuredAuthMeans.get(SOFTWARE_ID_NAME).getValue()).isEqualTo("software-id-2");
    }

    @Test
    void shouldReturnTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans)
                .hasSize(9)
                .containsEntry(CLIENT_ID_NAME, CLIENT_ID_STRING)
                .containsEntry(CLIENT_SECRET_NAME, CLIENT_SECRET_STRING)
                .containsEntry(INSTITUTION_ID_NAME, INSTITUTION_ID_STRING)
                .containsEntry(SOFTWARE_STATEMENT_ASSERTION_NAME, SOFTWARE_STATEMENT_ASSERTION_STRING)
                .containsEntry(SIGNING_PRIVATE_KEY_ID_NAME, KEY_ID)
                .containsEntry(SIGNING_KEY_HEADER_ID_NAME, SIGNING_KEY_ID_STRING)
                .containsEntry(TRANSPORT_PRIVATE_KEY_ID_NAME, KEY_ID)
                .containsEntry(TRANSPORT_CERTIFICATE_NAME, CERTIFICATE_PEM)
                .containsEntry(SOFTWARE_ID_NAME, SOFTWARE_ID_STRING);
    }

    @Test
    public void shouldReturnConsentPageUrl() {
        // given
        final String clientId = "someClientId-2";
        final String loginState = UUID.randomUUID().toString();
        String expectedUrlRegex = "?response_type=code+id_token&client_id=" + clientId + "&state=" + loginState + "&scope=openid+accounts&nonce=" + loginState + "&redirect_uri=http%3A%2F%2Fyolt.com%2Fidentifier&request=";
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) dataProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(loginInfo.getRedirectUrl()).contains(expectedUrlRegex);
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("50ca5ed5-317c-451c-8438-3b3fb91466e1");
    }

    @Test
    public void shouldReturnCorrectFetchData() throws Exception {
        // given
        UrlFetchDataRequest urlFetchData = createUrlFetchDataRequest("AAIWNndlNDJvVE1QWFNVM0tiUGVxQnZOVizgg9hirEzwSqcHpl2NfZUdxw6gQ0aaSpsk2uspfv8A5CBnthNaQdyPVfS_Tgps9Wo57i30zj4FBKwjy79b1P6qRq4xq8VCAB0YqgEsiDTV_oxjKUjp9r2Y28hJLnfdrz3b7EehCs4wXuYGL5-vMn-y6Ud0r4bDr986YZhEenqcUph7geB5LDr2MkolAAUN4x3Lpx0pxAYTnirkbVv9_mOuSzeosTeK1NC3LTUz3SpNFzdnPSzk1_uhPJGIyBeR8_zFLL7gFQwhIRc6ljqXaoB3mgu4zJ3M8Adts1bViVPkZX56yq5q-KT2_qvh8JNlPRgM2JX6sSN7lVLGrJwIkQTfOjZuzKMzN-RMn11i5kSCYYBf9KZUy0eUVBqG4JVppfrR85Hiqcbb2her5WeOAgNYm4UwzFIqZaSt6iVXc4gINWj8EV6rIj8xz2X-pbAN0w");
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // when
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        // then
        assertThat(dataProviderResponse.getAccounts().size()).isEqualTo(3);

        ProviderAccountDTO currentAccount = findProviderAccountDTO(dataProviderResponse.getAccounts(), "66c39bb1-74f9-454c-a0df-9996bb50560a");
        assertThat(currentAccount.getName()).isEqualTo("Virgin Money Account");
        assertThat(currentAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(currentAccount.getAvailableBalance()).isEqualTo(new BigDecimal("2904.72"));
        assertThat(currentAccount.getCurrentBalance()).isEqualTo(new BigDecimal("1154.00"));
        assertThat(currentAccount.getClosed()).isNull();
        assertThat(currentAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        validateAccount(currentAccount);
        validateTransactions(currentAccount.getTransactions());

        // Verify Stand Orders
        assertThat(currentAccount.getStandingOrders().size()).isEqualTo(4);
        assertThat(currentAccount.getStandingOrders().get(0).getDescription()).isEqualTo("Robin Hood");
        assertThat(currentAccount.getStandingOrders().get(0).getFrequency()).isEqualTo(Period.ofMonths(1));
        assertThat(currentAccount.getStandingOrders().get(0).getNextPaymentAmount()).isEqualTo(new BigDecimal("150.00"));
        assertThat(currentAccount.getStandingOrders().get(0).getCounterParty().getIdentification()).isEqualTo("50506492837451");

        // Verify Direct Debits
        assertThat(currentAccount.getDirectDebits().size()).isEqualTo(1);
        assertThat(currentAccount.getDirectDebits().get(0).getDescription()).isEqualTo("Sherwood Bow Company");
        assertThat(currentAccount.getDirectDebits().get(0).isDirectDebitStatus()).isTrue();
        assertThat(currentAccount.getDirectDebits().get(0).getPreviousPaymentAmount()).isEqualTo(new BigDecimal("6.57"));

        ProviderAccountDTO savingsAccount = findProviderAccountDTO(dataProviderResponse.getAccounts(), "7cda0cfb-b99e-466a-8a1c-6dd5954ce9b9");
        assertThat(savingsAccount.getName()).isEqualTo("Mr Robin Hood");
        assertThat(savingsAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(savingsAccount.getAvailableBalance()).isEqualTo(new BigDecimal("1001.33"));
        assertThat(savingsAccount.getCurrentBalance()).isEqualTo(new BigDecimal("1001.33"));
        assertThat(savingsAccount.getClosed()).isNull();
        assertThat(savingsAccount.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);

        ProviderAccountDTO creditCardAccount = findProviderAccountDTO(dataProviderResponse.getAccounts(), "65f13d10-dc35-5d75-891d-180dc4bc597b");
        assertThat(creditCardAccount.getName()).isEqualTo("Mr Jones");
        assertThat(creditCardAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(creditCardAccount.getAvailableBalance()).isEqualTo(new BigDecimal("4340.59"));
        assertThat(creditCardAccount.getCurrentBalance()).isEqualTo(new BigDecimal("-1559.41"));
        assertThat(creditCardAccount.getClosed()).isNull();
        assertThat(creditCardAccount.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
    }

    @Test
    public void shouldCorrectRefreshAccessMeans() {
        assertThatThrownBy(() -> dataProvider.refreshAccessMeans(createUrlRefreshAccessMeansRequest(OpenBankingTestObjectMapper.INSTANCE, "refreshToken")))
                .isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldCreateNewAccessMeans() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "AAJ_ozh9tNYZfc3ZSbUjLU624aoU99aHY_iPDwiviCFEth3AVW1JOAwq3QA6PbhGh8RNDhKg8tQmNSYsOOG60zZkcXN2Ik8gdrfDNLFI9ppI93LWebCiPNDSSt-jUyI24F_o57YMNPlahLMQbubaaJW_P3mLdVcaw86HQu63zPYoL_Atta_FrxCdaykIhHM3KXI2pDZpc91Ekh9N9TRKQ7j3AmX5ovvf6qAlnDEZ0HJsa1Vh0gG8jXcBhfkPiFSea4s1nrCj8L_8fJjgGKTOrd-Ymnf4groHo-YHyPz52-OJrFIXZggm9ulg43RA7reiE1MjPjSQF0M4kLFGYD9hpBDO-Fp8y50QJ8J2TykteTyRLtihH_JQ3-MCrK1_LFDCR6iU1oRA9U10mcxHnHctjWwS3bY0Hty8pNMxQnj85mWOgLPigIyg9gjoZ4eF0HmWVC874nXMpjA9mR4g3QprZjPH";
        final String redirectUrl = "https://www.yolt.com/callback/aff01911-7e22-4b9e-8b86-eae36cf7b732?code=" + authorizationCode + "&state=secretState";
        String encodedUrl = URLEncoder.encode("https://www.yolt.com/callback/aff01911-7e22-4b9e-8b86-eae36cf7b732", StandardCharsets.UTF_8);
        UrlCreateAccessMeansRequest urlCreateAccessMeans = createUrlCreateAccessMeansRequest(redirectUrl, userId);
        Date _29MinutesFromNow = Date.from(Instant.now().plus(29, ChronoUnit.MINUTES));

        // when
        AccessMeansDTO newAccessMeans = dataProvider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(_29MinutesFromNow.before(newAccessMeans.getExpireTime())).isTrue();
        assertThat(newAccessMeans.getUserId()).isEqualTo(userId);
    }

    @Test
    public void shouldCorrectOnUserSiteDelete() {
        // given
        UrlOnUserSiteDeleteRequest userSiteDeleteRequest = createUrlOnUserSiteDeleteRequest("3366f720-26a7-11e8-b65a-bd9397faa378");
        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> dataProvider.onUserSiteDelete(userSiteDeleteRequest);

        // then
        assertThatCode(onUserSiteDeleteCallable).doesNotThrowAnyException();
    }

    private void validateAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("66c39bb1-74f9-454c-a0df-9996bb50560a");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("2904.72"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("1154.00"));
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("98765432109876");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER);
        assertThat(providerAccountDTO.getName()).isEqualTo("Virgin Money Account");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
    }

    private UrlFetchDataRequest createUrlFetchDataRequest(String accessToken) throws JsonProcessingException {
        ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder().build();
        AccessMeans token = new AccessMeans(Instant.now(), USER_ID, accessToken, "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)), Date.from(Instant.now()),
                TEST_REDIRECT_URL);
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

    private UrlRefreshAccessMeansRequest createUrlRefreshAccessMeansRequest(ObjectMapper objectMapper, String refreshToken) throws JsonProcessingException {
        AccessMeans oAuthToken = new AccessMeans(UUID.randomUUID(), "accessToken", refreshToken, new Date(), new Date(), TEST_REDIRECT_URL);
        String serializedOAuthToken = objectMapper.writeValueAsString(oAuthToken);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedOAuthToken, new Date(), new Date());
        return new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();
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

    private UrlOnUserSiteDeleteRequest createUrlOnUserSiteDeleteRequest(String externalConsentId) {
        return new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(externalConsentId)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();
    }

    private void validateTransactions(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions).hasSize(8);
        ProviderTransactionDTO creditTransaction = transactions.get(0);
        assertThat(creditTransaction.getAmount()).isEqualTo(new BigDecimal("4.72"));
        assertThat(creditTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(creditTransaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(creditTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        ExtendedTransactionDTO extendedTransaction = creditTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-11-10T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-11-10T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo(new BigDecimal("4.72"));
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("INTEREST EARNED");

        creditTransaction.validate();

        ProviderTransactionDTO debitTransaction = transactions.get(1);
        assertThat(debitTransaction.getAmount()).isEqualTo(new BigDecimal("250.00"));
        assertThat(debitTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(debitTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(debitTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        extendedTransaction = debitTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-11-02T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-11-02T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo(new BigDecimal("-250.00"));
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("Payment from - Hood");

        debitTransaction.validate();
    }

    private ProviderAccountDTO findProviderAccountDTO(final List<ProviderAccountDTO> accountDTOs, final String accountId) {
        return accountDTOs.stream()
                .filter(accountDTO -> accountDTO.getAccountId().equalsIgnoreCase(accountId))
                .findFirst()
                .orElse(null);
    }
}
