package com.yolt.providers.openbanking.ais.capitalonegroup.capitalone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.capitalonegroup.CapitalOneGroupApp;
import com.yolt.providers.openbanking.ais.capitalonegroup.CapitalOneGroupJwsSigningResult;
import com.yolt.providers.openbanking.ais.capitalonegroup.CapitalOneGroupSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.CapitalOneGroupDataProviderV3;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.jose4j.jws.JsonWebSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.openbanking.ais.capitalonegroup.common.CapitalOneGroupDataProviderV3.REGISTRATION_ACCESS_TOKEN_STRING;
import static com.yolt.providers.openbanking.ais.capitalonegroup.common.auth.CapitalOneAuthMeansBuilderV3.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * This test contains all happy flows occurring in Capital One group providers.
 * <p>
 * Covered flows:
 * - updating authentication means using autoonboarding
 * - acquiring consent page
 * - fetching accounts, balances, transactions, standing orders, direct debits
 * - creating access means
 * - refreshing access means
 * - deleting consent on bank side
 * <p>
 */
@SpringBootTest(classes = {CapitalOneGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/capitalonegroup/ais-3.1.2/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("capitalonegroup")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CapitalOneDataProviderV3HappyFlowIntegrationTest {

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://test-redirect-url.com/identifier";

    private static final RestTemplateManagerMock REST_TEMPLATE_MANAGER_MOCK = new RestTemplateManagerMock(() -> "35acdd5c-ddf1-4a70-ac0f-a4322e3bc263");

    private static final Map<String, BasicAuthenticationMean> TEST_AUTHENTICATION_MEANS = CapitalOneGroupSampleAuthenticationMeans.getSampleAuthenticationMeans();
    private static final String TEST_ACCESS_TOKEN = "TEST_ACCESS_TOKEN";
    private static final String TEST_REFRESH_TOKEN = "TEST_REFRESH_TOKEN";
    private static final String TEST_PSU_IP_ADDRESS = "1.1.1.1";
    private static final AccessMeans TEST_ACCESS_MEANS = new AccessMeans(
            Instant.now(),
            TEST_USER_ID,
            TEST_ACCESS_TOKEN,
            TEST_REFRESH_TOKEN,
            Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
            Date.from(Instant.now()),
            TEST_REDIRECT_URL);

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("CapitalOneDataProviderV4")
    private CapitalOneGroupDataProviderV3 capitalOneDataProviderV4;

    private Stream<Arguments> getProviders() {
        return Stream.of(
                Arguments.of(capitalOneDataProviderV4, REGISTRATION_ACCESS_TOKEN_STRING));
    }

    @Mock
    private Signer signer;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        when(signer.sign(ArgumentMatchers.any(JsonWebSignature.class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(new CapitalOneGroupJwsSigningResult());
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnTypedAuthenticationMeansThatWillBeAutoConfigured(AutoOnboardingProvider provider, TypedAuthenticationMeans registrationAccessTokenString) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = provider.getAutoConfiguredMeans();

        // then
        assertThat(typedAuthMeans)
                .hasSize(2)
                .containsEntry(CLIENT_ID_NAME, CLIENT_ID_STRING)
                .containsEntry(REGISTRATION_ACCESS_TOKEN_NAME, registrationAccessTokenString);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnAuthenticationMeansAfterAutoConfiguration(AutoOnboardingProvider provider) {
        // given
        Map<String, BasicAuthenticationMean> forAutoOnboardingAuthenticationMeans = new HashMap<>(TEST_AUTHENTICATION_MEANS);
        forAutoOnboardingAuthenticationMeans.remove(CLIENT_ID_NAME);
        forAutoOnboardingAuthenticationMeans.remove(REGISTRATION_ACCESS_TOKEN_NAME);
        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(forAutoOnboardingAuthenticationMeans)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER_MOCK)
                .setSigner(signer)
                .setRedirectUrls(Collections.singletonList("https://yolt.com/callback-acc"))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();
        UUID expectedTransportPrivateKeyId = UUID.fromString(TEST_AUTHENTICATION_MEANS.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue());

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = provider.autoConfigureMeans(request);

        // then
        assertThat(configuredAuthMeans).hasSize(9);
        assertThat(configuredAuthMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("new-client-id");
        assertThat(configuredAuthMeans.get(REGISTRATION_ACCESS_TOKEN_NAME).getValue()).isEqualTo("new-registration-access-token");
        assertThat(configuredAuthMeans.get(INSTITUTION_ID_NAME).getValue()).isEqualTo("TEST_INSTITUTION_ID");
        assertThat(configuredAuthMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue()).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        assertThat(configuredAuthMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()).isEqualTo("5b626fbf-9761-4dfb-a1d6-132f5ee40123");
        assertThat(configuredAuthMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue()).isEqualTo("TEST_SIGNING_KEY_HEADER_ID");
        assertThat(UUID.fromString(configuredAuthMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue())).isEqualTo(expectedTransportPrivateKeyId);
        assertThat(configuredAuthMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()).isNotBlank();
        assertThat(configuredAuthMeans.get(SOFTWARE_ID_NAME).getValue()).isEqualTo("TEST_SOFTWARE_ID");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnRedirectStepWithConsentUrl(UrlDataProvider provider) {
        // given
        final String loginState = "89290e7a-d29c-42b2-96ba-8e2da9420a1d";

        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(TEST_REDIRECT_URL).setState(loginState)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setSigner(signer)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER_MOCK)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) provider.getLoginInfo(urlGetLogin);

        // then
        String loginUrl = redirectStep.getRedirectUrl();
        assertThat(loginUrl).contains("/authorize");

        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(loginUrl).build().getQueryParams().toSingleValueMap();
        assertThat(queryParams)
                .containsEntry("response_type", "code+id_token")
                .containsEntry("client_id", "TEST_CLIENT_ID")
                .containsEntry("state", "89290e7a-d29c-42b2-96ba-8e2da9420a1d")
                .containsEntry("scope", "openid+accounts")
                .containsEntry("nonce", "89290e7a-d29c-42b2-96ba-8e2da9420a1d")
                .containsEntry("redirect_uri", "https%3A%2F%2Ftest-redirect-url.com%2Fidentifier")
                .containsKey("request");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldCreateNewAccessMeans(UrlDataProvider provider) {
        // given
        String authorizationCode = "qInTtR_YTITHt1xA-2p6j0tbaD4";

        final String redirectUrl = "https://test-redirect-url.com/callback/identifier?code=" + authorizationCode + "&state=secretState";

        UrlCreateAccessMeansRequest urlCreateAccessMeans = createUrlCreateAccessMeansRequest(redirectUrl);

        // when
        AccessMeansDTO accessMeansDTO = provider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        AccessMeans accessMeans = toAccessMeans(accessMeansDTO.getAccessMeans());

        assertThat(accessMeans.getAccessToken()).as("Access token isn't equal to expected").isEqualTo("FIRST_TIME_GIVEN_ACCESS_TOKEN");
        assertThat(accessMeans.getRefreshToken()).as("Refresh token isn't equal to expected").isEqualTo("FIRST_TIME_GIVEN_REFRESH_TOKEN");
        assertThat(accessMeansDTO.getUserId()).as("UserID doesn't match").isEqualTo(TEST_USER_ID);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldRefreshAccessMeans(UrlDataProvider provider) throws TokenInvalidException {
        // given
        AccessMeansDTO accessMeansDTO = provider.refreshAccessMeans(createUrlRefreshAccessMeansRequest(TEST_REFRESH_TOKEN));

        // when
        AccessMeans accessMeans = toAccessMeans(accessMeansDTO.getAccessMeans());

        // then
        assertThat(accessMeans.getAccessToken()).as("Access token isn't equal to expected").isEqualTo("NEW_SHINY_ACCESS_TOKEN");
        assertThat(accessMeans.getRefreshToken()).as("Refresh token isn't equal to expected").isEqualTo(TEST_REFRESH_TOKEN);
        assertThat(accessMeansDTO.getUserId()).as("UserID doesn't match").isEqualTo(TEST_USER_ID);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldRefreshAccessMeansWithBrandNewRefreshToken(UrlDataProvider provider) throws TokenInvalidException {
        // given
        AccessMeansDTO accessMeansDTO = provider.refreshAccessMeans(createUrlRefreshAccessMeansRequest("THIS_REFRESH_TOKEN_SHOULD_ALSO_BE_REFRESHED"));

        // when
        AccessMeans accessMeans = toAccessMeans(accessMeansDTO.getAccessMeans());

        // then
        assertThat(accessMeans.getAccessToken()).as("Access token isn't equal to expected").isEqualTo("NEW_SHINY_ACCESS_TOKEN");
        assertThat(accessMeans.getRefreshToken()).as("Refresh token isn't equal to expected").isEqualTo("REFRESHED_REFRESH_TOKEN");
        assertThat(accessMeansDTO.getUserId()).as("UserID doesn't match").isEqualTo(TEST_USER_ID);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnCorrectlyFetchData(UrlDataProvider provider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest urlFetchDataRequest = createUrlFetchDataRequest();

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchDataRequest);

        // then
        assertThat(dataProviderResponse.getAccounts()).as("Unexpected number of accounts").hasSize(1);

        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        ProviderAccountDTO creditCardAccount = dataProviderResponse.getAccounts().stream()
                .filter(account -> account.getAccountId().equalsIgnoreCase("6b4427c634a1a1d9e1284b57a54b2f1a1721420c"))
                .findFirst().orElseThrow(IllegalStateException::new);

        assertThat(creditCardAccount.getAccountId()).isEqualTo("6b4427c634a1a1d9e1284b57a54b2f1a1721420c");
        assertThat(creditCardAccount.getAvailableBalance()).isEqualTo("0.50");
        assertThat(creditCardAccount.getCurrentBalance()).isEqualTo("0.50");
        assertThat(creditCardAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(creditCardAccount.getAccountNumber()).isNull();
        assertThat(creditCardAccount.getName()).isEqualTo("Robin Hood");
        assertThat(creditCardAccount.getAccountMaskedIdentification()).isEqualTo("xxxx xxxx xxxx 1234");
        assertThat(creditCardAccount.getCreditCardData().getAvailableCreditAmount()).isEqualTo("0.50");
        assertThat(creditCardAccount.getExtendedAccount().getBalances()).hasSize(1);
        validateTransactions(creditCardAccount.getTransactions());
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldDeleteUserSite(UrlDataProvider provider) {
        // given
        UrlOnUserSiteDeleteRequest siteDeleteRequest = createUrlOnUserSiteDeleteRequest("3366f720-26a7-11e8-b65a-bd9397faa378");

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> provider.onUserSiteDelete(siteDeleteRequest);

        // then
        assertThatCode(onUserSiteDeleteCallable).doesNotThrowAnyException();
    }

    private void validateTransactions(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions.size()).as("Unexpected transactions number").isEqualTo(3);

        ProviderTransactionDTO pendingTransaction = transactions.get(0);
        assertThat(pendingTransaction.getAmount()).isEqualTo("4.98");
        assertThat(pendingTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(pendingTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(pendingTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(pendingTransaction.getDateTime()).isEqualTo("2021-01-15T01:30:12Z[Europe/London]");
        assertThat(pendingTransaction.getDescription()).isEqualTo("DIRECT DEBIT PAYMENT - THANK YOU");
        assertThat(pendingTransaction.getMerchant()).isEqualTo("DIRECT DEBIT PAYMENT - THANK YOU");

        pendingTransaction.validate();

        ExtendedTransactionDTO extendedTransaction = pendingTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2021-01-15T01:30:12Z[Europe/London]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2021-01-15T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-4.98");
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("DIRECT DEBIT PAYMENT - THANK YOU");

        ProviderTransactionDTO debitTransaction = transactions.get(1);
        assertThat(debitTransaction.getAmount()).isEqualTo("2.49");
        assertThat(debitTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(debitTransaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(debitTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(debitTransaction.getDateTime()).isEqualTo("2020-12-23T17:06:13Z[Europe/London]");
        assertThat(debitTransaction.getDescription()).isEqualTo("APPLE");
        assertThat(debitTransaction.getMerchant()).isEqualTo("APPLE");

        debitTransaction.validate();
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeansRequest(String redirectUrl) {
        return new UrlCreateAccessMeansRequestBuilder()
                .setUserId(TEST_USER_ID)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setSigner(signer)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER_MOCK)
                .build();
    }

    @SneakyThrows
    private UrlRefreshAccessMeansRequest createUrlRefreshAccessMeansRequest(String refreshToken) {
        AccessMeans oAuthToken = new AccessMeans(Instant.now(), UUID.randomUUID(), TEST_ACCESS_TOKEN, refreshToken, new Date(), new Date(), TEST_REDIRECT_URL);
        String serializedOAuthToken = objectMapper.writeValueAsString(oAuthToken);
        AccessMeansDTO accessMeans = new AccessMeansDTO(TEST_USER_ID, serializedOAuthToken, new Date(), new Date());
        return new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setSigner(signer)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER_MOCK)
                .build();
    }

    private UrlFetchDataRequest createUrlFetchDataRequest() {
        return new UrlFetchDataRequestBuilder()
                .setUserId(TEST_USER_ID)
                .setUserSiteId(null)
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(new AccessMeansDTO(TEST_USER_ID, toSerializedAccessMeans(TEST_ACCESS_MEANS), new Date(), new Date()))
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setSigner(signer)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER_MOCK)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();
    }

    private UrlOnUserSiteDeleteRequest createUrlOnUserSiteDeleteRequest(String externalConsentId) {
        return new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(externalConsentId)
                .setAuthenticationMeans(TEST_AUTHENTICATION_MEANS)
                .setSigner(signer)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER_MOCK)
                .build();
    }

    @SneakyThrows
    private AccessMeans toAccessMeans(String json) {
        return objectMapper.readValue(json, AccessMeans.class);
    }

    @SneakyThrows
    private String toSerializedAccessMeans(AccessMeans accessMeans) {
        return objectMapper.writeValueAsString(accessMeans);
    }
}
