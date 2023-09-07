package com.yolt.providers.openbanking.ais.lloydsbankinggroup.ais;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.LloydsGroupApp;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.LloydsSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains tests for missing data during fetch data step in all LBG group banks.
 * <p>
 * Disclaimer: as all providers in LBG group are the same from code and stubs perspective (the only difference is configuration)
 * we are running parametrized tests for testing, so we'll cover all payment providers from LBG group
 * <p>
 * Covered flows:
 * - fetching data when some fields are missing
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {LloydsGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("lloydsgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/lloydsbankinggroup/ais/missing-data/", httpsPort = 0, port = 0)
public class LloydsGroupDataProviderMissingDataIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("BankOfScotlandDataProviderV10")
    private GenericBaseDataProvider bankOfScotlandDataProviderV10;
    @Autowired
    @Qualifier("BankOfScotlandCorpoDataProviderV8")
    private GenericBaseDataProvider bankOfScotlandCorpoDataProviderV8;
    @Autowired
    @Qualifier("HalifaxDataProviderV10")
    private GenericBaseDataProvider halifaxDataProviderV10;
    @Autowired
    @Qualifier("LloydsBankDataProviderV10")
    private GenericBaseDataProvider lloydsBankDataProviderV10;
    @Autowired
    @Qualifier("LloydsBankCorpoDataProviderV8")
    private GenericBaseDataProvider lloydsBankCorpoDataProviderV8;
    @Autowired
    @Qualifier("MbnaCreditCardDataProviderV6")
    private GenericBaseDataProvider mbnaCreditCardDataProviderV6;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private AccessMeans token;
    private AccessMeans postInitialConsentWindowToken;

    private String requestTraceId = "c554a9ef-47c1-4b4e-a77f-2ad770d69748";

    @BeforeAll
    public void beforeAll() throws IOException, URISyntaxException {
        authenticationMeans = new LloydsSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        token = new AccessMeans();
        token.setCreated(Instant.now());
        token.setAccessToken("accessToken");
        token.setExpireTime(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        postInitialConsentWindowToken = new AccessMeans();
        postInitialConsentWindowToken.setCreated(Instant.ofEpochMilli(0L));
        postInitialConsentWindowToken.setAccessToken("accessToken");
        postInitialConsentWindowToken.setExpireTime(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersWithDefaultAccountname")
    public void shouldCorrectlyFetchDataWhenNoStandingOrdersDirectDebits(UrlDataProvider dataProvider, String defaultAccountName) throws Exception {
        // given
        String serializedAccessMeans = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(postInitialConsentWindowToken);

        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedAccessMeans, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setAccessMeans(accessMeans)
                .setTransactionsFetchStartTime(Instant.now())
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(4);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        // Verify CurrentAccount
        ProviderAccountDTO currentAccount = findProviderAccountDTO(dataProviderResponse.getAccounts(), "80496010738761");
        assertThat(currentAccount.getName()).isEqualTo(defaultAccountName);
        assertThat(currentAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(currentAccount.getCurrentBalance()).isEqualTo(new BigDecimal("-1.00"));
        assertThat(currentAccount.getAvailableBalance()).isEqualTo(new BigDecimal("9.00"));
        assertThat(currentAccount.getClosed()).isNull();
        assertThat(currentAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        // Verify Standing Orders for CurrentAccount
        //We want to ignore standing orders, direct debits since Lloyds require too often re-authentication for them
        assertThat(currentAccount.getStandingOrders()).isEmpty();

        // Verify Direct Debit for CurrentAccount
        //We want to ignore standing orders, direct debits since Lloyds require too often re-authentication for them
        assertThat(currentAccount.getDirectDebits()).isEmpty();

        // Verify SavingsAccount
        ProviderAccountDTO savingsAccount = findProviderAccountDTO(dataProviderResponse.getAccounts(), "80496010738762");
        assertThat(savingsAccount.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);
        assertThat(savingsAccount.getAvailableBalance()).isEqualTo(new BigDecimal("9.00"));
        assertThat(savingsAccount.getCurrentBalance()).isEqualTo(new BigDecimal("-1.00"));
        assertThat(savingsAccount.getName()).isEqualTo("Savings Account");
        assertThat(savingsAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(savingsAccount.getCreditCardData()).isNull();
        assertThat(savingsAccount.getAccountNumber()).isNotNull();
        assertThat(savingsAccount.getTransactions()).hasSize(7);

        // Verify CreditCardAccount
        ProviderAccountDTO creditCardAccount = findProviderAccountDTO(dataProviderResponse.getAccounts(), "80496010738763");
        assertThat(creditCardAccount.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(creditCardAccount.getAvailableBalance()).isEqualTo(new BigDecimal("9.00"));
        assertThat(creditCardAccount.getCurrentBalance()).isEqualTo(new BigDecimal("-1.00"));
        assertThat(creditCardAccount.getName()).isEqualTo("Credit Card");
        assertThat(creditCardAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(creditCardAccount.getCreditCardData()).isNotNull();
        assertThat(creditCardAccount.getAccountNumber()).isNotNull();
        assertThat(creditCardAccount.getTransactions()).hasSize(8);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldFetchDataWithMinimalProductionDataWhenEmptyTransactionId(UrlDataProvider dataProvider) throws Exception {
        // given
        requestTraceId = "00000000-8cea-412d-b6ed-04aeb924eace";
        String serializedAccessMeans = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(token);

        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedAccessMeans, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
        assertThat(dataProviderResponse.getAccounts().get(0).getCurrentBalance()).isEqualTo(new BigDecimal("25.88"));
        assertThat(dataProviderResponse.getAccounts().get(0).getAvailableBalance()).isEqualTo(new BigDecimal("1025.88"));
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);
        ProviderAccountDTO providerAccountDTO = dataProviderResponse.getAccounts().get(0);
        assertThat(providerAccountDTO.getTransactions()).hasSize(4);
    }

    private ProviderAccountDTO findProviderAccountDTO(final List<ProviderAccountDTO> accountDTOs, final String accountId) {
        return accountDTOs.stream()
                .filter(providerAccountDTO -> providerAccountDTO.getAccountId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new AssertionError(String.format("ProviderAccountDTO with id %s was not found", accountId)));
    }

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(
                bankOfScotlandDataProviderV10, bankOfScotlandCorpoDataProviderV8,
                halifaxDataProviderV10, lloydsBankDataProviderV10,
                lloydsBankCorpoDataProviderV8, mbnaCreditCardDataProviderV6);
    }

    private Stream<Arguments> getDataProvidersWithDefaultAccountname() {
        return Stream.of(
                Arguments.of(bankOfScotlandDataProviderV10, "Bank of Scotland Account"),
                Arguments.of(bankOfScotlandCorpoDataProviderV8, "Bank of Scotland Corporate Account"),
                Arguments.of(halifaxDataProviderV10, "Halifax Account"),
                Arguments.of(lloydsBankDataProviderV10, "Lloyds Bank Account"),
                Arguments.of(lloydsBankCorpoDataProviderV8, "Lloyds Bank Corporate Account"),
                Arguments.of(mbnaCreditCardDataProviderV6, "MBNA Credit Card Account")
        );
    }
}
