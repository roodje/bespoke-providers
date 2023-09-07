package com.yolt.providers.bunq.common;

import com.yolt.providers.bunq.AuthMeans;
import com.yolt.providers.bunq.RestTemplateManagerMock;
import com.yolt.providers.bunq.TestApp;
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
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(httpsPort = 0, port = 0)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BunqDataProviderTest {

    public static final Instant TRANSACTIONS_FETCH_START_TIME = Instant.parse("2017-02-03T10:37:30.00Z");

    private static final Map<String, BasicAuthenticationMean> AUTHENTICATION_MEANS = AuthMeans.prepareAuthMeansV2();

    @Autowired
    private BunqDataProviderV5 bunqDataProviderV5;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private RestTemplateManagerMock restTemplateManagerMock;

    @BeforeEach
    public void beforeEach() {
        restTemplateManagerMock = new RestTemplateManagerMock(externalRestTemplateBuilderFactory);
    }

    private Stream<UrlDataProvider> getBunqUrlProviders() {
        return Stream.of(bunqDataProviderV5);
    }

    private Stream<AutoOnboardingProvider> getBunqAutoonboardingProviders() {
        return Stream.of(bunqDataProviderV5);
    }

    @Mock
    private Signer signer;

    @ParameterizedTest
    @MethodSource("getBunqAutoonboardingProviders")
    public void shouldCorrectlyAutoOnboarding(AutoOnboardingProvider autoOnboardingProvider) {
        // given
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();
        authenticationMeans.put(SIGNING_CERTIFICATE, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM.getType(), AuthMeans.CERTIFICATE_PEM));
        authenticationMeans.put(SIGNING_CERTIFICATE_CHAIN, new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATES_CHAIN_PEM.getType(), AuthMeans.CERTIFICATE_CHAIN));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID, new BasicAuthenticationMean(TypedAuthenticationMeans.KEY_ID.getType(), UUID.randomUUID().toString()));

        // when
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(authenticationMeans, restTemplateManagerMock, signer, "https://yolt.com/callback");

        // then
        Map<String, BasicAuthenticationMean> registeredMeans = autoOnboardingProvider.autoConfigureMeans(urlAutoOnboardingRequest);
        assertThat(registeredMeans.get(CLIENT_ID).getValue()).isEqualTo("some-client-id");
        assertThat(registeredMeans.get(CLIENT_SECRET_STRING).getValue()).isEqualTo("some-client-secret");
        assertThat(registeredMeans.get(PSD2_API_KEY).getValue()).isEqualTo("6fa459de8951067c68605df354172298df0c1f06b4737aa7ac5531b1de9fe2eb");
        assertThat(registeredMeans.get(PSD2_USER_ID).getValue()).isEqualTo("28196");
        assertThat(registeredMeans.get(OAUTH_USER_ID).getValue()).isEqualTo("633");
    }

    @ParameterizedTest
    @MethodSource("getBunqAutoonboardingProviders")
    public void shouldRemoveAutoConfiguration(AutoOnboardingProvider autoOnboardingProvider) {
        // given
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(AUTHENTICATION_MEANS, restTemplateManagerMock, signer, "https://yolt.com/callback");

        // when
        ThrowableAssert.ThrowingCallable removeAutoConfigurationCallable = () -> autoOnboardingProvider.removeAutoConfiguration(urlAutoOnboardingRequest);

        // then
        assertThatCode(removeAutoConfigurationCallable).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getBunqUrlProviders")
    public void shouldReturnConsentPageUrl(UrlDataProvider dataProvider) throws MalformedURLException, UnsupportedEncodingException {
        // given
        HashMap<String, String> queryParams = new HashMap<>();
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("https://www.yolt.com/callback").setState("594f5548-6dfb-4b02-8620-08e03a9469e6")
                .setAuthenticationMeans(AUTHENTICATION_MEANS)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        assertThat(redirectStep.getRedirectUrl()).isEqualTo("https://oauth.bunq.com/auth?response_type=code&client_id=aabb&redirect_uri=https://www.yolt.com/callback&state=594f5548-6dfb-4b02-8620-08e03a9469e6");
        String[] params = new URL(redirectStep.getRedirectUrl()).getQuery().split("&");
        Arrays.stream(params).forEach(param -> queryParams.put(param.split("=")[0], param.split("=")[1]));
        assertThat(URLDecoder.decode(queryParams.get("redirect_uri"), "UTF-8")).isEqualTo("https://www.yolt.com/callback");
    }

    @ParameterizedTest
    @MethodSource("getBunqUrlProviders")
    public void shouldCreateAccessMeans(UrlDataProvider urlDataProvider) {
        // given
        UUID expectedUserId = UUID.randomUUID();
        UrlCreateAccessMeansRequest createAccessMeansRequest = createUrlCreateAccessMeansRequest(expectedUserId);
        Date eightDaysFromNow = Date.from(Instant.now().plus(8, ChronoUnit.DAYS));

        // when
        AccessMeansDTO accessMeans = urlDataProvider.createNewAccessMeans(createAccessMeansRequest).getAccessMeans();

        // then
        assertThat(accessMeans.getUserId()).isEqualTo(expectedUserId);
        assertThat(accessMeans.getExpireTime()).isBefore(eightDaysFromNow);
    }

    @ParameterizedTest
    @MethodSource("getBunqUrlProviders")
    public void shouldCorrectlyFetchData(UrlDataProvider urlDataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlCreateAccessMeansRequest createAccessMeansRequest = createUrlCreateAccessMeansRequest(UUID.randomUUID());
        AccessMeansDTO accessMeans = urlDataProvider.createNewAccessMeans(createAccessMeansRequest).getAccessMeans();
        UrlFetchDataRequest fetchDataRequest = new UrlFetchDataRequestBuilder()
                .setUserId(accessMeans.getUserId())
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setTransactionsFetchStartTime(TRANSACTIONS_FETCH_START_TIME)
                .build();

        // when
        DataProviderResponse accountsAndTransactions = urlDataProvider.fetchData(fetchDataRequest);

        // then
        assertThat(accountsAndTransactions.getAccounts()).hasSize(1);
        assertThat(accountsAndTransactions.getAccounts().get(0).getTransactions()).hasSize(4);
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeansRequest(UUID userID) {
        return new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userID)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback?state=ca7a33be-4441-49ed-91d3-f0232acb6d3c&code=someCode")
                .setAuthenticationMeans(AUTHENTICATION_MEANS)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();
    }
}

