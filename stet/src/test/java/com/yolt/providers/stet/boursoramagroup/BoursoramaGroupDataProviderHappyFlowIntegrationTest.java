package com.yolt.providers.stet.boursoramagroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.stet.boursoramagroup.boursorama.BoursoramaDataProviderV4;
import com.yolt.providers.stet.boursoramagroup.boursorama.config.BoursoramaProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.Assertions;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = BoursoramaGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("boursorama")
@AutoConfigureWireMock(stubs = "classpath:/stubs/boursorama/ais/happy-flow", httpsPort = 0, port = 0)
class BoursoramaGroupDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String BASE_CLIENT_REDIRECT_URL = "http://www.yolt.com/callback";
    private static final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImtpZCI6IkhTMjU2In0.eyJqdGkiOiJjZTdiNTVmNzlmY2ZlIiwic3ViIjoiZHNwMiIsImF1ZCI6Ii4qXFwuYm91cnNvcmFtYVxcLmNvbSIsImV4cCI6MTg2NDIyNDY1NCwiaWF0IjoxNTQ4ODY0NjU0LCJuYmYiOjE1NDg4NjQ2NTQsInNlc3Npb24iOnsidXNlcklkIjoiMDAwMDAwMDAiLCJsZXZlbCI6IkNVU1RPTUVSIn0sImlzcyI6IkFkbWluIEpXVCBCb3Vyc29yYW1hIiwidXNlckhhc2giOiI3MDM1MmY0MTA2MWVkYTQiLCJvcmciOiJCMTkiLCJvYXV0aCI6ImM2OTdjOWUxZTUxZjg4Y2U2NWJjOGM4NWNmMjhkMDcyYWNmMDQyNTQifQ.3sewgdSK4OJfcsrVK2eqa8FF2jvDfdpiyBuIOh0CMRI";

    private final Signer signer = mock(Signer.class);

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private BoursoramaProperties boursoramaProperties;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("BoursoramaDataProviderV4")
    private BoursoramaDataProviderV4 boursoramaDataProviderV4;


    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(boursoramaDataProviderV4);
    }

    @BeforeEach
    public void setUp() {
        when(signer.sign(ArgumentMatchers.any(byte[].class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(Base64.toBase64String("TEST-ENCODED-SIGNATURE".getBytes()));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnConsentPageUrl(UrlDataProvider dataProvider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(BoursoramaGroupSampleMeans.getAuthMeans())
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setState(UUID.randomUUID().toString())
                .build();

        // when
        RedirectStep step = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        String expectedUrlRegex = ".*/TEST-AGREEMENT-NUMBER\\?successRedirect=http://www\\.yolt\\.com/callback&state=[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}&errorRedirect=http://www\\.yolt\\.com/callback&scope=aisp%20extended_transaction_history";
        assertThat(step.getRedirectUrl()).matches(expectedUrlRegex);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldCreateNewAccessMeans(UrlDataProvider dataProvider) throws JsonProcessingException {
        // given
        String redirectUrl = "http://www.bogus.com?authorization_code=XXXXXXXXXXXXXXXX&type=code&expires_in=7776000";

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(BoursoramaGroupSampleMeans.getAuthMeans())
                .setUserId(USER_ID)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setState(UUID.randomUUID().toString())
                .setProviderState(BoursoramaGroupSampleMeans.createPreAuthorizedJsonProviderState(objectMapper, boursoramaProperties))
                .build();

        // when
        AccessMeansOrStepDTO newAccessMeans = dataProvider.createNewAccessMeans(urlCreateAccessMeans);
        DataProviderState boursoramaGroupAccessToken = objectMapper.readValue(newAccessMeans.getAccessMeans().getAccessMeans(), DataProviderState.class);

        // then
        assertThat(newAccessMeans.getAccessMeans().getUserId()).isEqualTo(USER_ID);
        assertThat(boursoramaGroupAccessToken.getAccessToken()).isEqualTo("TEST-ACCESS-TOKEN");
        assertThat(boursoramaGroupAccessToken.getRefreshToken()).isEqualTo("TEST-REFRESH-TOKEN");
        assertThat(boursoramaGroupAccessToken.isRefreshed()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldRefreshAccessMeansSuccessfully(UrlDataProvider dataProvider) throws Exception {
        // given
        String accessMeans = "{\"expires_in\":3600,\"access_token\":\"TEST-ACCESS-TOKEN\",\"refresh_token\":\"TEST-REFRESH-TOKEN\", \"refreshed\":\"false\"}";

        AccessMeansDTO accessMeansDTO = new AccessMeansDTO(
                USER_ID,
                accessMeans,
                new Date(),
                new Date());

        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(BoursoramaGroupSampleMeans.getAuthMeans())
                .setAccessMeans(accessMeansDTO)
                .setSigner(signer)
                .build();

        // when
        AccessMeansDTO refreshAccessMeans = dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);
        DataProviderState boursoramaGroupAccessToken = objectMapper.readValue(refreshAccessMeans.getAccessMeans(), DataProviderState.class);

        // then
        assertThat(refreshAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(boursoramaGroupAccessToken.getAccessToken()).isEqualTo("TEST-ACCESS-TOKEN");
        assertThat(boursoramaGroupAccessToken.getRefreshToken()).isEqualTo("TEST-REFRESH-TOKEN");
        assertThat(boursoramaGroupAccessToken.isRefreshed()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldSuccessfullyFetchData(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        Instant transactionsFetchStartTime = Instant.parse("2019-06-27T12:23:25Z");
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAccessMeans(USER_ID,
                        BoursoramaGroupSampleMeans.createAuthorizedJsonProviderState(objectMapper, boursoramaProperties, ACCESS_TOKEN),
                        new Date(),
                        new Date())
                .setAuthenticationMeans(BoursoramaGroupSampleMeans.getAuthMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setTransactionsFetchStartTime(transactionsFetchStartTime)
                .setPsuIpAddress("147.206.96.254")
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(request);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(2);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        validateCashAccount(dataProviderResponse.getAccounts().get(0));
        validateDeferredCardAccount(dataProviderResponse.getAccounts().get(1));
    }

    private void validateDeferredCardAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getName()).isEqualTo("Visa classique MLE Bli Bla Blo");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("-427.57"));
        assertThat(providerAccountDTO.getAvailableBalance()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);

        List<ProviderTransactionDTO> transactions = providerAccountDTO.getTransactions();
        Assertions.assertThat(transactions).hasSize(1);
        transactions.forEach(ProviderTransactionDTO::validate);

        ProviderTransactionDTO transaction = providerAccountDTO.getTransactions().get(0);
        assertThat(transaction.getAmount()).isEqualTo(new BigDecimal("2.85"));
        assertThat(transaction.getDateTime()).isEqualTo("2021-06-01T00:00+02:00[Europe/Paris]");
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction.getDescription()).isEqualTo("CARTE 06 BOULANGERIE BG");
        assertThat(transaction.getCategory()).isEqualTo(YoltCategory.GENERAL);

        ExtendedTransactionDTO extendedTransaction = transaction.getExtendedTransaction();
        assertThat(extendedTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2021-06-01T00:00+02:00[Europe/Paris]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2021-05-11T00:00+02:00[Europe/Paris]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-2.85");
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("CARTE 06 BOULANGERIE BG");
        assertThat(extendedTransaction.isTransactionIdGenerated()).isFalse();
    }

    private void validateCashAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getName()).isEqualTo("Compte factice pour la sandbox DSP2");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("1642.68"));
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("1642.68"));
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        List<ProviderTransactionDTO> transactions = providerAccountDTO.getTransactions();
        Assertions.assertThat(transactions).hasSize(1);
        transactions.forEach(ProviderTransactionDTO::validate);

        ProviderTransactionDTO transaction = providerAccountDTO.getTransactions().get(0);
        assertThat(transaction.getAmount()).isEqualTo(new BigDecimal("12.25"));
        assertThat(transaction.getDateTime()).isEqualTo("2020-01-16T00:00+01:00[Europe/Paris]");
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction.getDescription()).isEqualTo("Account 2 Boursorama Essentiel");
        assertThat(transaction.getCategory()).isEqualTo(YoltCategory.GENERAL);

        ExtendedTransactionDTO extendedTransaction = transaction.getExtendedTransaction();
        assertThat(extendedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-01-16T00:00+01:00[Europe/Paris]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2020-01-16T00:00+01:00[Europe/Paris]");
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-12.25");
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("Account 2 Boursorama Essentiel");
        assertThat(extendedTransaction.isTransactionIdGenerated()).isFalse();
    }
}
