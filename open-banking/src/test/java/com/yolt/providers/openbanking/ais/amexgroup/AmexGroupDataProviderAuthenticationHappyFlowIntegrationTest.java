package com.yolt.providers.openbanking.ais.amexgroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.openbanking.ais.amexgroup.common.domain.AmexLoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
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

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * This test contains all authentication happy flows occurring in AMEX group providers.
 * <p>
 * Covered flows:
 * - acquiring consent page
 * - creating access means
 * - refreshing authentication means
 * - deletion of consent
 * <p>
 * Providers: ALL AMEX Group
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = AmexApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("amexgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/amexgroup/ais-3.1.8/happy_flow/authentication", port = 0, httpsPort = 0)
class AmexGroupDataProviderAuthenticationHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private static RestTemplateManagerMock restTemplateManagerMock;
    private static Map<String, BasicAuthenticationMean> authenticationMeans;
    private static String requestTraceId;

    private Signer signer;

    @Autowired
    @Qualifier("AmexDataProviderV7")
    private GenericBaseDataProviderV2 amexDataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Stream<GenericBaseDataProviderV2> getProviders() {
        return Stream.of(
                amexDataProvider);
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = AmexSampleAuthenticationMeans.getAmexSampleAuthenticationMeans();
    }

    @BeforeEach
    void beforeEach() {
        requestTraceId = "12345";
        signer = new SignerMock();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldGetLoginInfo(GenericBaseDataProviderV2 subject) throws JsonProcessingException {
        // given
        String loginState = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier")
                .setState(loginState)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) subject.getLoginInfo(urlGetLogin);

        // then
        UriComponents receivedRedirectUrl = UriComponentsBuilder.fromHttpUrl(loginInfo.getRedirectUrl()).build();
        assertThat(receivedRedirectUrl.getQueryParams().toSingleValueMap()).containsAllEntriesOf(
                Map.of("client_id", "THE-CLIENT-ID",
                        "redirect_uri", "http%3A%2F%2Fyolt.com%2Fidentifier",
                        "scope", "accounts",
                        "code_challenge_method", "S256",
                        "state", loginState,
                        "ConsentId", "urn-amex-intent-88379")
        ).containsKeys("code_challenge");
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("urn-amex-intent-88379");
        AmexLoginInfoState providerState = objectMapper.readValue(loginInfo.getProviderState(), AmexLoginInfoState.class);
        assertThat(providerState.getPermissions()).containsExactlyInAnyOrder(
                "ReadAccountsBasic", "ReadAccountsDetail", "ReadBalances", "ReadProducts", "ReadStatementsBasic", "ReadStatementsDetail", "ReadTransactionsBasic", "ReadTransactionsCredits", "ReadTransactionsDebits", "ReadTransactionsDetail"
        );
        assertThat(providerState.getCodeVerifier()).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldCreateNewAccessMeans(GenericBaseDataProviderV2 subject) throws JsonProcessingException {
        // given
        String redirectUrl = "https://www.yolt.com/callback?authtoken=received-authorization-code&state=secret-state";
        String providerState = """
                {"permissions":["ReadAccountsBasic","ReadAccountsDetail","ReadBalances","ReadProducts","ReadStatementsBasic","ReadStatementsDetail","ReadTransactionsBasic","ReadTransactionsCredits","ReadTransactionsDebits","ReadTransactionsDetail"],"codeVerifier":"hfetMftkPquoW3zXKA0JYph8PepHXRpJppIaWp75xEYW"}""";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(USER_ID)
                .setProviderState(providerState)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .build();

        // when
        AccessMeansDTO accessMeansDTO = subject.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);

        AccessMeansState<AccessMeans> accessMeans = objectMapper.readValue(accessMeansDTO.getAccessMeans(), AccessMeansState.class);
        assertThat(accessMeans.getAccessMeans().getAccessToken()).isEqualTo("19697e0a-c9f1-11ec-9d64-0242ac120002");
        assertThat(accessMeans.getAccessMeans().getRefreshToken()).isEqualTo("ae45eabc-c9f2-11ec-9d64-0242ac120002");
        assertThat(accessMeans.getAccessMeans().getExpireTime()).isAfter(Instant.now().plusSeconds(86000));
        assertThat(accessMeans.getAccessMeans().getUpdated()).isCloseTo(new Date(), 5000);
        assertThat(accessMeans.getAccessMeans().getRedirectUri()).isEqualTo("https://www.yolt.com/callback");
        assertThat(accessMeans.getPermissions()).containsExactlyInAnyOrder("ReadAccountsBasic", "ReadAccountsDetail", "ReadBalances", "ReadProducts", "ReadStatementsBasic", "ReadStatementsDetail", "ReadTransactionsBasic", "ReadTransactionsCredits", "ReadTransactionsDebits", "ReadTransactionsDetail");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldRefreshAccessMeans(GenericBaseDataProviderV2 subject) throws Exception {
        // given
        AccessMeans accessMeans = new AccessMeans(Instant.ofEpochMilli(0L),
                USER_ID,
                "test-accounts",
                "THE-REFRESH-TOKEN",
                new Date(),
                null,
                "https://www.yolt.com/callback");
        AccessMeansState<AccessMeans> accessMeansState = new AccessMeansState(accessMeans, List.of("ReadAccountsBasic", "ReadAccountsDetail", "ReadBalances", "ReadProducts", "ReadStatementsBasic", "ReadStatementsDetail", "ReadTransactionsBasic", "ReadTransactionsCredits", "ReadTransactionsDebits", "ReadTransactionsDetail"));
        String serializedAccessMeansState = objectMapper.writeValueAsString(accessMeansState);
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(new AccessMeansDTO(USER_ID, serializedAccessMeansState, new Date(), new Date()))
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        AccessMeansDTO accessMeansDTO = subject.refreshAccessMeans(urlRefreshAccessMeans);

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);

        AccessMeansState receivedAccessMeansState = objectMapper.readValue(accessMeansDTO.getAccessMeans(), AccessMeansState.class);
        assertThat(receivedAccessMeansState.getAccessMeans().getAccessToken()).isEqualTo("fd39c068-c9f6-11ec-9d64-0242ac120002");
        assertThat(receivedAccessMeansState.getAccessMeans().getRefreshToken()).isEqualTo("0f423ca4-c9f7-11ec-9d64-0242ac120002");
        assertThat(receivedAccessMeansState.getAccessMeans().getExpireTime()).isAfter(Instant.now().plusSeconds(86000));
        assertThat(receivedAccessMeansState.getAccessMeans().getUpdated()).isCloseTo(new Date(), 5000);
        assertThat(receivedAccessMeansState.getAccessMeans().getRedirectUri()).isEqualTo("https://www.yolt.com/callback");
        assertThat(receivedAccessMeansState.getPermissions()).containsExactlyInAnyOrder("ReadAccountsBasic", "ReadAccountsDetail", "ReadBalances", "ReadProducts", "ReadStatementsBasic", "ReadStatementsDetail", "ReadTransactionsBasic", "ReadTransactionsCredits", "ReadTransactionsDebits", "ReadTransactionsDetail");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldOnUserSiteDelete(GenericBaseDataProviderV2 subject) {
        // given
        UrlOnUserSiteDeleteRequest urlOnUserSiteDelete = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("urn-alphabank-intent-88379")
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDelete = () -> subject.onUserSiteDelete(urlOnUserSiteDelete);

        // then
        assertThatCode(onUserSiteDelete)
                .doesNotThrowAnyException();
    }
}
