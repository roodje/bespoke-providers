package com.yolt.providers.volksbank.common.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.volksbank.FakeRestTemplateManager;
import com.yolt.providers.volksbank.VolksbankSampleTypedAuthenticationMeans;
import com.yolt.providers.volksbank.VolksbankTestApp;
import com.yolt.providers.volksbank.common.model.VolksbankAccessMeans;
import com.yolt.providers.volksbank.common.model.VolksbankAccessTokenResponse;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * This test suite contains all happy flows occurring in Volksbank group providers.
 * Tests are parametrized and run for all {@link VolksbankDataProviderV4} providers in group.
 * Covered flows:
 * - acquiring consent page
 * - creating access means
 * - refreshing access means
 * - fetching accounts, balances, transactions
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = VolksbankTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/volksbank/api_1.1/ais/happy_flow", port = 0, httpsPort = 0)
@ActiveProfiles("volksbank")
public class VolksbankDataProviderV4HappyFlowIntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback/";
    private static final String LOGIN_STATE_RANDOM_UUID = "04ec2916-d42d-4240-95cf-6db789e0c4e4";
    private static final String VOLKSBANK_CONSENT_ID = "CONSENT_ID";
    private static final String ACCESS_TOKEN = "3fb45310-e2bb-11ea-87d0-0242ac130003";
    private static final String REFRESH_TOKEN = "4a33007a-e2bb-11ea-87d0-0242ac130003";
    private static final String NEW_ACCESS_TOKEN = "e8f72628-e2bb-11ea-87d0-0242ac130003";
    private static final String NEW_REFRESH_TOKEN = "f23a5796-e2bb-11ea-87d0-0242ac130003";

    @Autowired
    @Qualifier("RegioDataProviderV5")
    private VolksbankDataProviderV4 regioProviderV5;

    @Autowired
    @Qualifier("SNSDataProviderV5")
    private VolksbankDataProviderV4 snsProviderV5;

    @Autowired
    @Qualifier("ASNDataProviderV5")
    private VolksbankDataProviderV4 asnProviderV5;

    Stream<UrlDataProvider> getVolksbankProviders() {
        return Stream.of(regioProviderV5, snsProviderV5, asnProviderV5);
    }

    @Autowired
    @Qualifier("Volksbank")
    private ObjectMapper mapper;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private RestTemplateManager restTemplateManager;
    private List<StubMapping> stubMappings = new ArrayList<>();

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new VolksbankSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @AfterEach
    public void afterEach() {
        stubMappings.forEach(WireMock::removeStub);
        stubMappings.clear();
    }

    @ParameterizedTest
    @MethodSource("getVolksbankProviders")
    public void shouldReturnConsentPageUrl(UrlDataProvider dataProviderUnderTest) {
        //given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(LOGIN_STATE_RANDOM_UUID)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        //when
        RedirectStep loginInfo = (RedirectStep) dataProviderUnderTest.getLoginInfo(request);

        //then
        assertThat(loginInfo.getExternalConsentId()).isEqualTo(VOLKSBANK_CONSENT_ID);

        Map<String, String> redirectUrlQueryParameters = UriComponentsBuilder
                .fromUriString(loginInfo.getRedirectUrl())
                .build()
                .getQueryParams()
                .toSingleValueMap();
        assertThat(redirectUrlQueryParameters.get("response_type")).isEqualTo("code");
        assertThat(redirectUrlQueryParameters.get("consentId")).isEqualTo(VOLKSBANK_CONSENT_ID);
        assertThat(redirectUrlQueryParameters.get("scope")).isEqualTo("AIS");
        assertThat(redirectUrlQueryParameters.get("state")).isEqualTo(LOGIN_STATE_RANDOM_UUID);
        assertThat(redirectUrlQueryParameters.get("client_id")).isEqualTo("someClientId");
    }

    @ParameterizedTest
    @MethodSource("getVolksbankProviders")
    public void shouldCreateNewAccessMeans(UrlDataProvider dataProviderUnderTest) throws IOException {
        //given
        String redirectUrl = "https://www.yolt.com/callback/?code=SOME_AUTH_CODE&state=" + LOGIN_STATE_RANDOM_UUID;

        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(UUID.randomUUID())
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setProviderState(VOLKSBANK_CONSENT_ID)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        //when
        AccessMeansOrStepDTO result = dataProviderUnderTest.createNewAccessMeans(request);

        //then
        VolksbankAccessMeans accessMeans = mapper.readValue(result.getAccessMeans().getAccessMeans(), VolksbankAccessMeans.class);
        assertThat(accessMeans.getConsentId()).isEqualTo(VOLKSBANK_CONSENT_ID);
        assertThat(accessMeans.getRedirectUrl()).isEqualTo(REDIRECT_URL);
        assertThat(accessMeans.getResponse()).isEqualTo(expectedAccessMeansToken(ACCESS_TOKEN, REFRESH_TOKEN));
    }

    @ParameterizedTest
    @MethodSource("getVolksbankProviders")
    public void shouldReturnCorrectlyRefreshAccessMeans(UrlDataProvider dataProviderUnderTest) throws IOException, TokenInvalidException {
        //given
        AccessMeansDTO accessMeansDTO = createAccessMeansDTO();

        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        //when
        AccessMeansDTO result = dataProviderUnderTest.refreshAccessMeans(request);

        //then
        VolksbankAccessMeans accessMeans = mapper.readValue(result.getAccessMeans(), VolksbankAccessMeans.class);
        assertThat(accessMeans.getConsentId()).isEqualTo(VOLKSBANK_CONSENT_ID);
        assertThat(accessMeans.getRedirectUrl()).isEqualTo(REDIRECT_URL);
        assertThat(accessMeans.getResponse()).isEqualTo(expectedAccessMeansToken(NEW_ACCESS_TOKEN, NEW_REFRESH_TOKEN));
    }

    @ParameterizedTest
    @MethodSource("getVolksbankProviders")
    public void shouldReturnCorrectlyFetchData(UrlDataProvider dataProviderUnderTest) throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        //given
        AccessMeansDTO accessMeansDTO = createAccessMeansDTO();

        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeansDTO)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        //when
        DataProviderResponse dataProviderResponse = dataProviderUnderTest.fetchData(urlFetchData);

        //then
        ProviderAccountDTO expectedAccount = dataProviderResponse.getAccounts().get(0);
        verifyAccount(expectedAccount);
        verifyExtendedAccount(expectedAccount.getExtendedAccount());
        expectedAccount.getTransactions().forEach(ProviderTransactionDTO::validate);
        verifyTransactions(expectedAccount.getTransactions());
        verifyOneGivenExtendedTransaction(expectedAccount.getTransactions().get(0).getExtendedTransaction());
    }

    private VolksbankAccessTokenResponse expectedAccessMeansToken(String accessToken, String refreshToken) {
        VolksbankAccessTokenResponse token = new VolksbankAccessTokenResponse();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setTokenType("Bearer");
        token.setExpiresIn(600);
        token.setScope("AIS");
        return token;
    }

    private void verifyAccount(ProviderAccountDTO expectedAccount) {
        assertThat(expectedAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(expectedAccount.getAvailableBalance()).isEqualTo("500.00");
        assertThat(expectedAccount.getAccountId()).isEqualTo("3dc3d5b3-7023-4848-9853-f5400a64e80f");
        assertThat(expectedAccount.getAccountNumber())
                .extracting(ProviderAccountNumberDTO::getScheme, ProviderAccountNumberDTO::getIdentification)
                .contains(ProviderAccountNumberDTO.Scheme.IBAN, "NL79RBRB0230400868");
        assertThat(expectedAccount.getName()).isEqualTo("BETALEN ZELFSTANDIGEN");
        assertThat(expectedAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(expectedAccount.getBic()).isEqualTo("RBRBNL21");
        assertThat(expectedAccount.getAccountNumber().getHolderName()).isEqualTo("Yatra BV");
    }

    private void verifyExtendedAccount(ExtendedAccountDTO extendedAccount) {
        assertThat(extendedAccount.getResourceId()).isEqualTo("3dc3d5b3-7023-4848-9853-f5400a64e80f");
        assertThat(extendedAccount.getBic()).isEqualTo("RBRBNL21");
        assertThat(extendedAccount.getAccountReferences())
                .extracting(AccountReferenceDTO::getType, AccountReferenceDTO::getValue)
                .contains(tuple(AccountReferenceType.IBAN, "NL79RBRB0230400868"));
        assertThat(extendedAccount.getBalances()).isEqualTo(expectedBalanceDtoList());
        assertThat(extendedAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedAccount.getName()).isEqualTo("BETALEN ZELFSTANDIGEN");
        assertThat(extendedAccount.getProduct()).isEqualTo("MKB REKENING");
    }

    private void verifyTransactions(List<ProviderTransactionDTO> expectedTransactions) {
        assertThat(expectedTransactions).hasSize(4);

        ProviderTransactionDTO transaction1 = expectedTransactions.get(0);
        assertThat(transaction1.getDateTime()).isEqualTo("2017-10-25T00:00+02:00[Europe/Paris]");
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction1.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction1.getAmount()).isEqualTo("256.67");
        assertThat(transaction1.getDescription()).isEqualTo("Uw toelage");
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.BOOKED);

        ProviderTransactionDTO transaction2 = expectedTransactions.get(1);
        assertThat(transaction2.getDateTime()).isEqualTo("2017-10-25T00:00+02:00[Europe/Paris]");
        assertThat(transaction2.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction2.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction2.getAmount()).isEqualTo("999.99");
        assertThat(transaction2.getDescription()).isEqualTo("CUR 1234-1234-1234-1234");
        assertThat(transaction2.getStatus()).isEqualTo(TransactionStatus.BOOKED);

        ProviderTransactionDTO transaction3 = expectedTransactions.get(2);
        assertThat(transaction3.getDateTime()).isEqualTo("2017-10-26T00:00+02:00[Europe/Paris]");
        assertThat(transaction3.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction3.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction3.getAmount()).isEqualTo("200.00");
        assertThat(transaction3.getDescription()).isEqualTo(" ");
        assertThat(transaction3.getStatus()).isEqualTo(TransactionStatus.BOOKED);
    }

    private void verifyOneGivenExtendedTransaction(ExtendedTransactionDTO extendedTransaction) {
        assertThat(extendedTransaction.getEndToEndId()).isEqualTo("12345678901234567890123456789012345");
        assertThat(extendedTransaction.getMandateId()).isEmpty();
        assertThat(extendedTransaction.getPurposeCode()).isEmpty();
        assertThat(extendedTransaction.getProprietaryBankTransactionCode()).isEqualTo("FNGI");
        assertThat(extendedTransaction.getEntryReference()).isEqualTo("20190101-33263746");
        assertThat(extendedTransaction.getBankTransactionCode()).isEqualTo("3723");
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2017-10-25T00:00+02:00[Europe/Paris]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2017-10-25T00:00+02:00[Europe/Paris]");
        assertThat(extendedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-256.67");
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("Uw toelage");
        assertThat(extendedTransaction.getRemittanceInformationStructured()).isEqualTo("CUR 1345-1231-1232-6456");
        assertThat(extendedTransaction.getCreditorName()).isEqualTo("Constant Kaanen");
        assertThat(extendedTransaction.getCreditorAccount()).isEqualTo(new AccountReferenceDTO(AccountReferenceType.IBAN, "NL64SNSB0123456789"));
        assertThat(extendedTransaction.getUltimateCreditor()).isNull();
        assertThat(extendedTransaction.getDebtorName()).isEmpty();
        assertThat(extendedTransaction.getDebtorAccount()).isNull();
        assertThat(extendedTransaction.isTransactionIdGenerated()).isTrue();

    }

    private List<BalanceDTO> expectedBalanceDtoList() {
        return Collections.singletonList(BalanceDTO.builder()
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.valueOf("EUR"), new BigDecimal("500.00")))
                .balanceType(BalanceType.fromName("interimAvailable"))
                .lastChangeDateTime(ZonedDateTime.parse("2017-10-25T15:30:35.035Z"))
                .build());
    }

    private AccessMeansDTO createAccessMeansDTO() throws JsonProcessingException {
        VolksbankAccessTokenResponse accessTokenResponseDTO = new VolksbankAccessTokenResponse();
        accessTokenResponseDTO.setAccessToken(ACCESS_TOKEN);
        accessTokenResponseDTO.setRefreshToken(REFRESH_TOKEN);
        accessTokenResponseDTO.setTokenType("bearer");
        accessTokenResponseDTO.setExpiresIn(600);
        accessTokenResponseDTO.setScope("AIS");

        VolksbankAccessMeans accessMeansDTO = new VolksbankAccessMeans(
                accessTokenResponseDTO,
                REDIRECT_URL,
                VOLKSBANK_CONSENT_ID

        );

        return new AccessMeansDTO(
                UUID.randomUUID(),
                mapper.writeValueAsString(accessMeansDTO),
                new Date(),
                Date.from(Instant.now().plusSeconds(600)));
    }
}