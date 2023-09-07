package com.yolt.providers.rabobank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.mock.RestTemplateManagerMock;
import com.yolt.providers.mock.SignerMock;
import com.yolt.providers.rabobank.config.RabobankProperties;
import com.yolt.providers.rabobank.dto.AccessTokenResponseDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.rabobank.RabobankAuthenticationMeans.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all happy flows occurring in Rabobank group providers.
 * <p>
 * Disclaimer: The group consists of only one {@link RabobankDataProvider} provider which is used for testing
 * <p>
 * Covered flows:
 * - acquiring consent page
 * - fetching accounts, balances, transactions
 * - creating access means
 * - refreshing access means
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = {"classpath:/stubs/rabobank-ais/happy-flow/", "classpath:/stubs/rabobank-oauth2-2.1.20"}, httpsPort = 0, port = 0)
@Import(TestConfiguration.class)
@ActiveProfiles("rabobank")
class RabobankDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String REDIRECT_URL = "https://yolt.com/callback";
    private static final String ACCESS_TOKEN = "AAIkMGI3ZGY1NWEtOGY1MS00NTRlLTlkZTgtODdiOWNhYzJhYmE0NQ6jyX3MRS4yO9h765MAUlX3hmYQUbk8iIKgQX4leyYpsGimIkxcVirSVoyqz276NWpCj6t6niSkY-jTdOrm3yVgjUR2pm7YPvZZggyNjflRoVk1-IWiOgVRI9c9UIhjeIDKP9JPNdjiJsR-WJC5Zg";
    private static final String REFRESH_TOKEN = "AALIUC2qSZfZr7iF9e7cFJsO9IMXpJbgoYJVtr2gV3u1DX03fkwZnkJiqDyCNxa918pKRP3_GzYnu1WxZRiF8f438szmCeeVRH4r57pZIUV2b0erzQeTKzYJInPd9FAlH6rB_lR0-e4qYY43hBgvHBLjYbXaJBFBXdbXutG3V0onbA";
    private static final String METADATA = "a:consentId b75f5ee4-6b25-41af-bb9c-31128d09151b";

    @Autowired
    private Clock clock;

    @Autowired
    private RabobankProperties properties;

    @Autowired
    private RabobankDataProvider rabobankDataProviderV5;

    private RestTemplateManagerMock restTemplateManagerMock = new RestTemplateManagerMock();
    private SignerMock signerMock = new SignerMock();

    @Autowired
    @Qualifier("RabobankObjectMapper")
    private ObjectMapper objectMapper;

    private RabobankSampleTypedAuthenticationMeans sampleAuthenticationMeans = new RabobankSampleTypedAuthenticationMeans();
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    Stream<UrlDataProvider> getRabobankProviders() {
        return Stream.of(rabobankDataProviderV5);
    }

    @BeforeEach
    void initialize() throws IOException, URISyntaxException {
        authenticationMeans = sampleAuthenticationMeans.getRabobankSampleTypedAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldReturnTypedAuthenticationMeans(UrlDataProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans)
                .hasSize(6)
                .containsEntry(CLIENT_ID_NAME, CLIENT_ID_STRING)
                .containsEntry(CLIENT_SECRET_NAME, CLIENT_SECRET_STRING)
                .containsEntry(CLIENT_SIGNING_KEY_ID, KEY_ID)
                .containsEntry(CLIENT_SIGNING_CERTIFICATE, CLIENT_SIGNING_CERTIFICATE_PEM)
                .containsEntry(CLIENT_TRANSPORT_KEY_ID, KEY_ID)
                .containsEntry(CLIENT_TRANSPORT_CERTIFICATE, CLIENT_TRANSPORT_CERTIFICATE_PEM);
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldReturnRedirectStepWithConsentUrl(UrlDataProvider dataProvider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .setState("8b6dee15-ea2a-49b2-b100-f5f96d31cd90")
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        String loginUrl = redirectStep.getRedirectUrl();
        assertThat(loginUrl).contains(properties.getBaseAuthorizationUrl() + "oauth2/authorize");

        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(loginUrl).build().getQueryParams().toSingleValueMap();
        assertThat(queryParams)
                .containsEntry("response_type", "code")
                .containsEntry("client_id", "0b7df55a-8f51-454e-9de8-87b9cac2aba4")
                .containsEntry("redirect_uri", REDIRECT_URL)
                .containsEntry("scope", "ais.balances.read%20ais.transactions.read-90days%20ais.transactions.read-history")
                .containsEntry("state", "8b6dee15-ea2a-49b2-b100-f5f96d31cd90");
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldFetchData(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        String token = objectMapper.writeValueAsString(new AccessTokenResponseDTO("token",
                "refreshToken", 3600, 3600, "bearer", "AIS-Transactions-v2 AIS-Balance-v2", METADATA));

        UrlFetchDataRequest fetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now(clock))
                .setAccessMeans(new AccessMeansDTO(USER_ID, token, new Date(), new Date()))
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(fetchDataRequest);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(2);

        // Verify Current Account with transactions
        ProviderAccountDTO currentAccount1 = getCurrentAccountById(dataProviderResponse, "ACSeg9Tbre3fIS0yi2NgUE3ggMAM2ztQ_wbXsQ4J2tOrtXdDUn8jkJSt_12Vhqah");
        assertThat(currentAccount1.getAccountId()).isEqualTo("ACSeg9Tbre3fIS0yi2NgUE3ggMAM2ztQ_wbXsQ4J2tOrtXdDUn8jkJSt_12Vhqah");
        assertThat(currentAccount1.getName()).isEqualTo("Rabobank rekening");
        assertThat(currentAccount1.getAccountNumber().getHolderName()).isEqualTo("FREZARKI Z FRYZJI");
        assertThat(currentAccount1.getAccountNumber().getIdentification()).isEqualTo("NL39RABO0320130878");//
        assertThat(currentAccount1.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(currentAccount1.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(currentAccount1.getAvailableBalance()).isNull();
        assertThat(currentAccount1.getCurrentBalance()).isEqualTo("18990.4");
        assertThat(currentAccount1.getClosed()).isFalse();
        assertThat(currentAccount1.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(currentAccount1.getExtendedAccount().getAccountReferences().get(0).getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(currentAccount1.getExtendedAccount().getAccountReferences().get(0).getValue()).isEqualTo("NL39RABO0320130878");
        assertThat(currentAccount1.getExtendedAccount().getBalances()).hasSize(1);
        assertThat(currentAccount1.getTransactions()).hasSize(7);

        validateCurrentAccountTransactions(currentAccount1.getTransactions());

        // Verify Current Account without transactions
        ProviderAccountDTO currentAccount2 = getCurrentAccountById(dataProviderResponse, "PoSeg9Tbre3fIS0yi2NgUE3ggMAM2zqw_pb6sQ4J2tOrtXdDUn8jkJSt_18ahqah");
        assertThat(currentAccount2.getAccountId()).isEqualTo("PoSeg9Tbre3fIS0yi2NgUE3ggMAM2zqw_pb6sQ4J2tOrtXdDUn8jkJSt_18ahqah");
        assertThat(currentAccount2.getName()).isEqualTo("Rabobank rekening");
        assertThat(currentAccount2.getAccountNumber().getHolderName()).isEqualTo("FREZARKI Z FRYZJI");
        assertThat(currentAccount2.getAccountNumber().getIdentification()).isEqualTo("NL14RABO0320130843");//
        assertThat(currentAccount2.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(currentAccount2.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(currentAccount2.getAvailableBalance()).isNull();
        assertThat(currentAccount2.getCurrentBalance()).isEqualTo("123.45");
        assertThat(currentAccount2.getClosed()).isFalse();
        assertThat(currentAccount2.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(currentAccount2.getExtendedAccount().getBalances()).hasSize(1);
        assertThat(currentAccount2.getTransactions()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldReturnNewAccessMeans(UrlDataProvider dataProvider) throws JsonProcessingException {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .setState("29dbba15-1e67-4ac0-ab0f-2487dc0c960b")
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL +"?state=29dbba15-1e67-4ac0-ab0f-2487dc0c960b&code=AAL90J6cIRMa0XcmydI2Ap3tHPdHroyvsM1VwL4Tn97_Nc9Tdcmyg_coyHsIpJxHChm1nTAiZTkaClBRGyMWSOy49B7sgZrTJILvCt8bMk-evHag13lFCyNrkw3lYmuGLPg")
                .setUserId(USER_ID)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(accessMeansOrStepDTO.getStep()).isNull();

        AccessMeansDTO accessMeans = accessMeansOrStepDTO.getAccessMeans();
        assertThat(accessMeans.getUserId()).isEqualTo(request.getUserId());
        assertThat(accessMeans.getUpdated()).isBeforeOrEqualTo(Date.from(Instant.now()));
        assertThat(accessMeans.getExpireTime()).isBeforeOrEqualTo(Date.from(Instant.now(clock).plusSeconds(3600)));

        AccessTokenResponseDTO tokenResponse = objectMapper.readValue(accessMeans.getAccessMeans(), AccessTokenResponseDTO.class);
        assertThat(tokenResponse).extracting(AccessTokenResponseDTO::getAccessToken, AccessTokenResponseDTO::getScope, AccessTokenResponseDTO::getTokenType, AccessTokenResponseDTO::getExpiresIn, AccessTokenResponseDTO::getMetadata)
                .contains(ACCESS_TOKEN, "ais.balances.read ais.transactions.read-90days ais.transactions.read-history oauth2.consents.read", "Bearer", 3600, "a:consentId b75f5ee4-6b25-41af-bb9c-31128d09151b");
    }

    @ParameterizedTest
    @MethodSource("getRabobankProviders")
    void shouldReturnRefreshedAccessMeans(UrlDataProvider dataProvider) throws TokenInvalidException, JsonProcessingException {
        // given
        AccessMeansDTO accessMeansDTO = createAccessMeansDTO();

        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .build();

        // then
        AccessMeansDTO result = dataProvider.refreshAccessMeans(request);

        // then
        assertThat(result.getUserId()).isEqualTo(USER_ID);

        AccessTokenResponseDTO refreshTokenResponse = objectMapper.readValue(result.getAccessMeans(), AccessTokenResponseDTO.class);
        assertThat(refreshTokenResponse.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(refreshTokenResponse.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(refreshTokenResponse.getMetadata()).isEqualTo(METADATA);
    }

    private ProviderAccountDTO getCurrentAccountById(DataProviderResponse response, String accountId) {
        return response.getAccounts().stream()
                .filter(account -> account.getAccountId().equals(accountId))
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }

    private void validateCurrentAccountTransactions(final List<ProviderTransactionDTO> transactions) {
        // Verify transaction 1
        ProviderTransactionDTO transaction1 = transactions.get(0);
        assertThat(transaction1.getExternalId()).isEqualTo("18716");
        assertThat(transaction1.getDateTime()).isEqualTo("2018-09-25T00:00Z[Europe/Amsterdam]");
        assertThat(transaction1.getAmount()).isEqualTo("534.99");
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction1.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction1.getDescription()).isEqualTo("Description Line 1\nDescription Line 2\nLine 3");
        assertThat(transaction1.getBankSpecific()).isNull();

        ExtendedTransactionDTO extendedTransaction = transaction1.getExtendedTransaction();
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("Description Line 1\nDescription Line 2\nLine 3");
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2018-09-25T00:00+02:00[Europe/Amsterdam]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2018-09-26T00:00+02:00[Europe/Amsterdam]");
        assertThat(extendedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-534.99");
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransaction.getEntryReference()).isEqualTo("18716");

        // Verify transaction 2
        ProviderTransactionDTO transaction2 = transactions.get(1);
        assertThat(transaction2.getExternalId()).isEqualTo("18752");
        assertThat(transaction2.getDateTime()).isEqualTo("2018-09-25T00:00Z[Europe/Amsterdam]");
        assertThat(transaction2.getAmount()).isEqualTo("601.21");
        assertThat(transaction2.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction2.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction2.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction2.getDescription()).isEqualTo("8062542234115201\n");
        assertThat(transaction2.getBankSpecific()).isNull();

        // Verify transaction 3
        ProviderTransactionDTO transaction3 = transactions.get(2);
        assertThat(transaction3.getExternalId()).isEqualTo("18702");
        assertThat(transaction3.getDateTime()).isEqualTo("2018-09-25T00:00Z[Europe/Amsterdam]");
        assertThat(transaction3.getAmount()).isEqualTo("242.84");
        assertThat(transaction3.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction3.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction3.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction3.getDescription()).isEqualTo("Description Line 1\nDescription Line 2\nLine 3");
        assertThat(transaction3.getBankSpecific()).isNull();

        // Verify reversal transaction 4
        ProviderTransactionDTO transaction4 = transactions.get(6);
        assertThat(transaction4.getExternalId()).isEqualTo("8606");
        assertThat(transaction4.getDateTime()).isEqualTo("2018-09-26T00:00Z[Europe/Amsterdam]");
        assertThat(transaction4.getAmount()).isEqualTo("322.02");
        assertThat(transaction4.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction4.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction4.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction4.getDescription()).isEmpty();
        assertThat(transaction4.getBankSpecific().get("paymentInformationIdentification")).isEqualTo("JOHN002031DD020056R220330");
    }

    private AccessMeansDTO createAccessMeansDTO() throws JsonProcessingException {
        AccessTokenResponseDTO accessTokenResponseDTO = new AccessTokenResponseDTO();
        accessTokenResponseDTO.setAccessToken(ACCESS_TOKEN);
        accessTokenResponseDTO.setRefreshToken(REFRESH_TOKEN);
        accessTokenResponseDTO.setTokenType("bearer");
        accessTokenResponseDTO.setExpiresIn(3600);
        accessTokenResponseDTO.setScope("AIS-Transactions-v2 AIS-Balance-v2");
        accessTokenResponseDTO.setMetadata(METADATA);

        return new AccessMeansDTO(
                USER_ID,
                objectMapper.writeValueAsString(accessTokenResponseDTO),
                new Date(),
                Date.from(Instant.now().plusSeconds(3600)));
    }

}
