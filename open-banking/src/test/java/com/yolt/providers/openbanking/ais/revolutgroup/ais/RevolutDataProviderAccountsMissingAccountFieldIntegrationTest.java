package com.yolt.providers.openbanking.ais.revolutgroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.revolutgroup.RevolutSampleAuthenticationMeans;
import com.yolt.providers.openbanking.ais.revolutgroup.RevolutTestApp;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * This test contains tests for missing data during fetch data step in Revolut.
 * <p>
 * During live maintenance it was found that Revolut uses to return empty "account" field in /accounts response.
 * <p>
 * Covered flows:
 * - fetching data when some fields are missing
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {RevolutTestApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("revolut")
@AutoConfigureWireMock(stubs = "classpath:/stubs/revolut/ais-3.1.0/sad-flow/accounts-with-missing-account-field", httpsPort = 0, port = 0)
public class RevolutDataProviderAccountsMissingAccountFieldIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID USER_SITE_ID = UUID.randomUUID();

    private RestTemplateManagerMock restTemplateManagerMock;
    private String requestTraceId;

    @Autowired
    @Qualifier("RevolutDataProviderV10")
    private GenericBaseDataProvider revolutGbDataProviderV10;

    @Autowired
    @Qualifier("RevolutEuDataProviderV8")
    private GenericBaseDataProvider revolutEuDataProviderV8;

    @Autowired
    ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(revolutEuDataProviderV8, revolutGbDataProviderV10);
    }

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws Exception {
        authenticationMeans = new RevolutSampleAuthenticationMeans().getAuthenticationMeans();
        requestTraceId = UUID.randomUUID().toString();
        restTemplateManagerMock = new RestTemplateManagerMock(externalRestTemplateBuilderFactory);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnCorrectFetchData(UrlDataProvider dataProvider) throws Exception {
        // given
        UrlFetchDataRequest urlFetchData = createUrlFetchDataRequest("908b3693-23bf-41c8-898d-3846d22a6858");

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(2);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        Optional<ProviderAccountDTO> firstAccount = dataProviderResponse.getAccounts()
                .stream().filter(
                        account ->
                                account.getAccountId().equalsIgnoreCase("22ed9418-0510-4b29-9d8a-b30cdb208aa4"))

                .findFirst();
        assertThat(firstAccount).isPresent();
        validateGbpAccount(firstAccount.get());
        validateTransactionsGbpAccount(firstAccount.get().getTransactions());

        Optional<ProviderAccountDTO> secondAccount = dataProviderResponse.getAccounts()
                .stream().filter(
                        account ->
                                account.getAccountId().equalsIgnoreCase("b5cda922-6af1-453e-9b29-10b53807a33d"))
                .findFirst();
        assertThat(secondAccount).isPresent();
        validateEurAccount(secondAccount.get());
        validateTransactionsEurAccount(secondAccount.get().getTransactions());
    }

    private void validateGbpAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getName()).isEqualTo("Revolut GBP Account");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("44.00"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("44.00"));
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
    }

    private void validateTransactionsGbpAccount(List<ProviderTransactionDTO> transactions) {
        ProviderTransactionDTO transactionDebit = transactions.get(0);
        assertThat(transactionDebit.getAmount()).isEqualTo(new BigDecimal("25.00"));
        assertThat(transactionDebit.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transactionDebit.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transactionDebit.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transactionDebit.getMerchant()).isNull();
        assertThat(transactionDebit.getDescription()).isEqualTo("Sad Aws Emea A1T1");
        transactionDebit.validate();

        ProviderTransactionDTO transactionCredit = transactions.get(1);
        assertThat(transactionCredit.getAmount()).isEqualTo(new BigDecimal("5.00"));
        assertThat(transactionCredit.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transactionCredit.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transactionCredit.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transactionDebit.getMerchant()).isNull();
        assertThat(transactionCredit.getDescription()).isEqualTo("Sad Aws Emea A1T2");
        transactionCredit.validate();
    }

    private void validateEurAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getName()).isEqualTo("Revolut EUR Account");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("33.00"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("33.00"));
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
    }

    private void validateTransactionsEurAccount(List<ProviderTransactionDTO> transactions) {
        ProviderTransactionDTO transaction1 = transactions.get(0);
        assertThat(transaction1.getAmount()).isEqualTo(new BigDecimal("75.00"));
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction1.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction1.getMerchant()).isNull();
        assertThat(transaction1.getDescription()).isEqualTo("Sad Emea Aws");
        transaction1.validate();
    }

    private UrlFetchDataRequest createUrlFetchDataRequest(String accessToken) throws JsonProcessingException {
        ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder().build();
        AccessMeans token = new AccessMeans(USER_ID, accessToken, "refreshToken", Date.from(Instant.now().plus(1, ChronoUnit.DAYS)), null, null);
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
}
