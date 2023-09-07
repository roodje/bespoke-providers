package com.yolt.providers.bunq.common.http;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.AuthMeans;
import com.yolt.providers.bunq.RestTemplateManagerMock;
import com.yolt.providers.bunq.common.auth.BunqApiContext;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.model.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.security.KeyPair;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(httpsPort = 0, port = 0)
public class BunqHttpServiceV5Test {

    private Map<String, BasicAuthenticationMean> typedAuthenticationMeans;

    @Autowired
    private BunqProperties properties;

    @Autowired
    @Qualifier("BunqObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    private MeterRegistry meterRegistry;

    private BunqHttpServiceV5 httpService;


    @BeforeEach
    public void beforeEach() {
        typedAuthenticationMeans = AuthMeans.prepareAuthMeansV2();
        RestTemplateManagerMock restTemplateManagerMock = new RestTemplateManagerMock(externalRestTemplateBuilderFactory);
        BunqHttpClientFactory httpClientFactory = new BunqHttpClientFactory(meterRegistry, objectMapper, properties, new BunqHttpHeaderProducer(objectMapper));
        BunqHttpClientV5 httpClient = httpClientFactory.createHttpClient(restTemplateManagerMock, "BUNQ");
        httpService = new BunqHttpServiceV5(properties, httpClient);
    }

    @Test
    public void shouldMapResponseWhenSendingInstallationRequest() throws TokenInvalidException {
        //given
        KeyPair keyPair = SecurityUtils.generateKeyPair();

        // when
        InstallationResponse installationResponse = httpService.createInstallation(keyPair);

        // then
        assertThat(installationResponse).isNotNull();
        assertThat(installationResponse.getToken().getId()).isEqualTo(16469L);
        assertThat(installationResponse.getToken().getTokenString()).isEqualTo("22df07d3afe4f7dc445f023413392e5329c59a4ea73847b3f043af4701251273");
    }

    @Test
    public void shouldMapResponseWhenSendingRegisterPsd2ServiceProviderRequest() throws TokenInvalidException {
        //given
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        String installationToken = "22df07d3afe4f7dc445f023413392e5329c59a4ea73847b3f043af4701251273";
        Psd2ProviderRequest psd2ProviderRequest = new Psd2ProviderRequest("SOME_CERTIFICATE", "SOME-CHAIN", "SOME-SIGNATURE");

        //when
        Psd2ProviderResponse psd2ProviderResponse = httpService.registerProvider(keyPair, psd2ProviderRequest, installationToken);

        //then
        assertThat(psd2ProviderResponse.getCredentialPasswordIp().getTokenValue()).isEqualTo("6fa459de8951067c68605df354172298df0c1f06b4737aa7ac5531b1de9fe2eb");
    }

    @Test
    public void shouldMapResponseWhenSendingPsd2SessionServerRequest() throws TokenInvalidException {
        //given
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        String installationToken = "22df07d3afe4f7dc445f023413392e5329c59a4ea73847b3f043af4701251273";
        String apiToken = "6fa459de8951067c68605df354172298df0c1f06b4737aa7ac5531b1de9fe2eb";

        //when
        Psd2SessionResponse sessionResponse = httpService.createPsd2SessionServer(keyPair, installationToken, apiToken);

        //then
        assertThat(sessionResponse.getToken().getTokenString()).isEqualTo("a3cc2d56b7d30dae471c418b16cd15d674aa06ffa9f45beae39dbb9a8ac749c9");
        assertThat(sessionResponse.getPsd2UserId()).isEqualTo(28196);
    }

    @Test
    public void shouldMapResponseWhenSendingOauthClientRegistrationRequest() throws TokenInvalidException {
        //given
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        String sessionToken = "a3cc2d56b7d30dae471c418b16cd15d674aa06ffa9f45beae39dbb9a8ac749c9";
        long userId = 28196;

        //when
        OauthClientRegistrationResponse response = httpService.registerOAuthClient(keyPair, sessionToken, userId);

        //then
        assertThat(response.getOAuthClientId()).isEqualTo(633);
    }

    @Test
    public void shouldMapResponseWhenSendingOauthClientDetailsRequest() throws TokenInvalidException {
        //given
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        String sessionToken = "a3cc2d56b7d30dae471c418b16cd15d674aa06ffa9f45beae39dbb9a8ac749c9";
        long userId = 28196;
        long oauthUserId = 633;

        //when
        OauthClientDetailsResponse response = httpService.getOAuthClientDetails(keyPair, sessionToken, userId, oauthUserId);

        //then
        assertThat(response.getClientId()).isEqualTo("some-client-id");
        assertThat(response.getClientSecret()).isEqualTo("some-client-secret");
        assertThat(response.getCallbackUrls()).hasSize(1);
        assertThat(response.getCallbackUrls().get(0).getId()).isEqualTo(693);
        assertThat(response.getCallbackUrls().get(0).getUrl()).isEqualTo("https://yolt.com/callback");
    }

    @Test
    public void shouldMapResponseWhenSendingAddCallbackUrl() throws TokenInvalidException {
        //given
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        String sessionToken = "a3cc2d56b7d30dae471c418b16cd15d674aa06ffa9f45beae39dbb9a8ac749c9";
        long userId = 28196;
        long oauthUserId = 633;
        String callbackUrl = "https://yolt.com/callback";

        //when
        OauthAddCallbackUrlResponse response = httpService.addCalbackUrl(keyPair, sessionToken, userId, oauthUserId, callbackUrl);

        //then
        assertThat(response.getCallbackUrlId()).isEqualTo(634);
    }

    @Test
    public void shouldMapResponseWhenSendingDeviceServerRequest() throws TokenInvalidException {
        // given
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        String installationToken = "22df07d3afe4f7dc445f023413392e5329c59a4ea73847b3f043af4701251273";
        String apiToken = "sandbox_34310af2612c882ff38da75d592c0b22e901f2de1ceb3885e6acb67d";

        // when
        DeviceServerResponse response = httpService.createDeviceServer(keyPair, installationToken, apiToken);

        // then
        assertThat(response.getId()).isEqualTo(2523L);
    }

    @Test
    public void shouldMapResponseWhenSendingSessionServerRequest() throws TokenInvalidException {
        // given
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        String installationToken = "22df07d3afe4f7dc445f023413392e5329c59a4ea73847b3f043af4701251273";
        String apiToken = "sandbox_34310af2612c882ff38da75d592c0b22e901f2de1ceb3885e6acb67d";
        SessionServerResponse sessionServerResponse = httpService.createSessionServer(keyPair, installationToken, apiToken);

        // when
        BunqApiContext context = new BunqApiContext(sessionServerResponse.getBunqId(), installationToken, keyPair, apiToken, sessionServerResponse.getToken().getTokenString(), sessionServerResponse.getExpiryTimeInSeconds());

        // then
        assertThat(context.getBunqUserId()).isEqualTo("1");
        assertThat(context.getSessionToken()).isEqualTo("sessionToken");
    }

    @Test
    public void shouldMapListOfAccountsWhenRequestingMonetaryAccounts() throws TokenInvalidException {
        // given
        BunqApiContext context = new BunqApiContext("2459", "serverToken", SecurityUtils.generateKeyPair(), "oAuthToken", "sessionToken", 60L);

        // when
        MonetaryAccountResponse response = httpService.getAccounts(null, context);

        // then
        assertThat(response.getMonetaryAccounts()).hasSize(3);

        MonetaryAccountResponse.MonetaryAccount monetaryAccount = response.getMonetaryAccounts().get(0);
        assertThat(monetaryAccount.getBalance().getValue()).isEqualTo("1");
        assertThat(monetaryAccount.getDescription()).isEqualTo("MonetaryAccountBank");
        assertThat(monetaryAccount.getHolderName()).isNull();

        MonetaryAccountResponse.MonetaryAccount monetaryAccount1 = response.getMonetaryAccounts().get(1);
        assertThat(monetaryAccount1.getBalance().getValue()).isEqualTo("2");
        assertThat(monetaryAccount1.getDescription()).isEqualTo("MonetaryAccountJoint");

        MonetaryAccountResponse.MonetaryAccount monetaryAccount2 = response.getMonetaryAccounts().get(2);
        assertThat(monetaryAccount2.getBalance().getValue()).isEqualTo("3");
        assertThat(monetaryAccount2.getDescription()).isEqualTo("MonetaryAccountLight");
    }

    @Test
    public void shouldMapTokenWhenRequestingOauthToken() throws TokenInvalidException {
        // given
        BunqAuthenticationMeansV2 authenticationMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(typedAuthenticationMeans, "BUNQ");

        // when
        OauthAccessTokenResponse response = httpService.postAccessCodeWithPsd2OauthMeans(authenticationMeans, "someCode", "https://www.yolt.com/callback");

        // then
        assertThat(response.getAccessToken()).isEqualTo("a1cd891ef7d76369a48df8201ccdd2d29e8911fe5d5d62fb99be705d61b15b69");
    }

    @Test
    public void shouldMapListOfTransactionsWhenRequestingTransactionsForAMonetaryAccount() throws TokenInvalidException {
        // given
        BunqApiContext context = new BunqApiContext("2459", "serverToken", SecurityUtils.generateKeyPair(), "oAuthToken", "sessionToken", 60L);

        // when
        TransactionsResponse response = httpService.getTransactions(null, context, "1");

        // then
        TransactionsResponse.Transaction transaction = response.getTransactions().get(0);
        assertThat(transaction.getDescription()).isEqualTo("testPayment");
        assertThat(response.getTransactions()).hasSize(3);
        assertThat(transaction.getAmount().getValue()).isEqualTo("-20.00");
        assertThat(transaction.getCreated()).isNotNull();
    }

    @Test
    public void shouldMapListOfTransactionsWhenRequestingTransactionsForAMonetaryAccountWithFilledOlderIdParameter() throws TokenInvalidException {
        // given
        BunqApiContext context = new BunqApiContext("2459", "serverToken", SecurityUtils.generateKeyPair(), "oAuthToken", "sessionToken", 60L);

        // when
        TransactionsResponse response = httpService.getTransactions("229830", context, "1");

        // then
        assertThat(response.getTransactions()).hasSize(1);
        TransactionsResponse.Transaction transaction = response.getTransactions().get(0);
        assertThat(transaction.getDescription()).isEqualTo("testPayment123");
        assertThat(transaction.getAmount().getValue()).isEqualTo("30.00");
        assertThat(transaction.getCreated()).isNotNull();
    }

    @Test
    public void shouldThrowTokenInvalidExceptionAfterUsingIncorrectOauthApiToken() throws TokenInvalidException {
        // given
        KeyPair keyPair = SecurityUtils.generateKeyPair();
        String installationToken = "22df07d3afe4f7dc445f023413392e5329c59a4ea73847b3f043af4701251273";
        String apiToken = "invalidApiToken";

        // when
        ThrowableAssert.ThrowingCallable handler = () -> httpService.createSessionServer(keyPair, installationToken, apiToken);

        // then
        assertThatThrownBy(handler)
                .isExactlyInstanceOf(TokenInvalidException.class)
                .hasMessage("Token invalid, received status 400 BAD_REQUEST.");
    }
}
