package com.yolt.providers.openbanking.ais.cybgroup.ais.v3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.cybgroup.CybgGroupApp;
import com.yolt.providers.openbanking.ais.cybgroup.CybgGroupSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.CybgGroupDataProviderV3;
import com.yolt.providers.openbanking.ais.cybgroup.common.model.CybgGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountType1Code;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains tests for missing data during fetch data step in all CYBG group banks.
 * <p>
 * Disclaimer: as all providers in CYBG group are the same from code and stubs perspective (the only difference is configuration)
 * we are running parametrized tests for testing, so we'll cover all providers in group
 * <p>
 * Covered flows:
 * - creating access means when some fields are missing
 * - fetching data when some fields are missing
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {CybgGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("cybgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/cybgroup/ais-3.1/missing-data/", httpsPort = 0, port = 0)
public class CybgGroupDataProviderV3MissingDataIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID USER_SITE_ID = UUID.randomUUID();
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REDIRECT_URL = "http://yolt.com/identifier";

    private String requestTraceId;
    private final RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> requestTraceId);
    @Autowired
    @Qualifier("ClydesdaleDataProvider")
    private CybgGroupDataProviderV3 clydesdaleDataProvider;

    @Autowired
    @Qualifier("YorkshireDataProvider")
    private CybgGroupDataProviderV3 yorkshireDataProviderV3;

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(clydesdaleDataProvider, yorkshireDataProviderV3);
    }

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private static final Signer SIGNER = new SignerMock();

    private final CybgGroupSampleAuthenticationMeansV2 sampleAuthenticationMeans = new CybgGroupSampleAuthenticationMeansV2();
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        requestTraceId = "4bf28754-9c17-41e6-bc46-6cf98fff679";
        authenticationMeans = sampleAuthenticationMeans.getCybgGroupSampleAuthenticationMeansForAis();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldCreateNewAccessMeansWhenSomeDataInBanksResponseIsMissing(UrlDataProvider dataProvider) throws JsonProcessingException {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "bd941a87-116c-46b5-915e-47ea05711734";

        final String redirectUrl = "https://www.yolt.com/callback/aff01911-7e22-4b9e-8b86-eae36cf7b732?code=" + authorizationCode + "&state=secretState";

        UrlCreateAccessMeansRequest urlCreateAccessMeans = createUrlCreateAccessMeansRequest(redirectUrl, userId);

        // when
        AccessMeansDTO newAccessMeans = dataProvider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        CybgGroupAccessMeansV2 accessMeans = objectMapper.readValue(newAccessMeans.getAccessMeans(), CybgGroupAccessMeansV2.class);
        List<OBAccount6> accountsList = accessMeans.getCachedAccounts();
        assertThat(accountsList).hasSize(1);
        //account
        OBAccount6 account = accountsList.get(0);
        assertThat(account.getCurrency()).isNull();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldCorrectlyFetchDataWhenSomeDataAreMissingIsBankResponse(UrlDataProvider dataProvider) throws JsonProcessingException, TokenInvalidException, ProviderFetchDataException {
        // given
        List<OBAccount6> accounts = Collections.singletonList(createProviderAccountDTO("a134fcf0-3f3a-3dd0-9398-109313b2c6b3", OBExternalAccountSubType1Code.CREDITCARD));
        AccessMeansDTO accessMeansDTO = createAccessMeansDTO(ACCESS_TOKEN, accounts);

        UrlFetchDataRequest urlFetchData = createUrlFetchDataRequest(accessMeansDTO);

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
        ProviderAccountDTO account = dataProviderResponse.getAccounts().get(0);
        //balances
        assertThat(account.getCurrentBalance()).isNull();
        assertThat(account.getAvailableBalance()).isEqualTo("1200.00");
        assertThat(account.getExtendedAccount().getBalances()).hasSize(2);
        BalanceDTO balance0 = getBalanceDtoWithSpecificAmountOrNull(account.getExtendedAccount().getBalances(), new BigDecimal("1200.00"));
        assertThat(balance0.getBalanceAmount().getCurrency()).isNull();
        BalanceDTO balance1 = getBalanceDtoWithSpecificAmountOrNull(account.getExtendedAccount().getBalances(), null);
        assertThat(balance1.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        //transactions
        assertThat(account.getTransactions()).hasSize(3);
        ProviderTransactionDTO transaction0 = getTransactionWithSpecificAmountOrNull(account.getTransactions(), null);
        assertThat(transaction0.getAmount()).isNull();
        assertThat(transaction0.getType()).isNull();
        assertThat(transaction0.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction0.getExtendedTransaction().getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(transaction0.getExtendedTransaction().getTransactionAmount().getAmount()).isNull();
        ProviderTransactionDTO transaction1 = getTransactionWithSpecificAmountOrNull(account.getTransactions(), new BigDecimal("85.00"));
        assertThat(transaction1.getAmount()).isEqualTo("85.00");
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction1.getStatus()).isNull();
        assertThat(transaction1.getExtendedTransaction().getTransactionAmount().getCurrency()).isNull();
        assertThat(transaction1.getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("-85.00");
        ProviderTransactionDTO transaction2 = getTransactionWithSpecificAmountOrNull(account.getTransactions(), new BigDecimal("12.52"));
        assertThat(transaction2.getAmount()).isEqualTo("12.52");
        assertThat(transaction2.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction2.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction2.getExtendedTransaction().getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(transaction2.getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("-12.52");

    }

    private ProviderTransactionDTO getTransactionWithSpecificAmountOrNull(List<ProviderTransactionDTO> transactionList, BigDecimal amount) {
        if (ObjectUtils.isEmpty(amount)) {
            return transactionList.stream()
                    .filter(transaction -> ObjectUtils.isEmpty(transaction.getAmount()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("There is no transaction in list with null amount"));
        }
        return transactionList.stream()
                .filter(transaction -> amount.equals(transaction.getAmount()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format("There is no transaction in list with amount %d", amount)));
    }

    private BalanceDTO getBalanceDtoWithSpecificAmountOrNull(List<BalanceDTO> balanceList, BigDecimal amount) {
        if (ObjectUtils.isEmpty(amount)) {
            return balanceList.stream()
                    .filter(balance -> ObjectUtils.isEmpty(balance.getBalanceAmount().getAmount()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("There is no balance in list with null amount"));
        }
        return balanceList.stream()
                .filter(balance -> amount.equals(balance.getBalanceAmount().getAmount()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format("There is no balance in list with amount %d", amount)));
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeansRequest(String redirectUrl, UUID userId) {
        return new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManager)
                .build();
    }

    private UrlFetchDataRequest createUrlFetchDataRequest(AccessMeansDTO accessMeans) {
        return new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setUserSiteId(USER_SITE_ID)
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManager)
                .build();
    }

    private AccessMeansDTO createAccessMeansDTO(String refreshToken, List<OBAccount6> accounts) throws JsonProcessingException {
        AccessMeans accessMeans = new AccessMeans(Instant.now(), USER_ID, ACCESS_TOKEN, refreshToken, getExpirationDate(), new Date(), REDIRECT_URL);
        String providerState = objectMapper.writeValueAsString(new CybgGroupAccessMeansV2(accessMeans, accounts));
        return new AccessMeansDTO(USER_ID, providerState, new Date(), getExpirationDate());
    }

    private Date getExpirationDate() {
        return Date.from(Instant.now().plus(1, DAYS));
    }

    private OBAccount6 createProviderAccountDTO(String accountId, OBExternalAccountSubType1Code accountType) {
        OBAccount6 account = new OBAccount6();
        account.setCurrency("EUR");
        account.setNickname("Test Account");
        account.setAccountSubType(accountType);
        account.setAccountId(accountId);
        account.setAccountType(OBExternalAccountType1Code.PERSONAL);
        OBAccount4Account accountNumber = new OBAccount4Account();
        accountNumber.setSchemeName("UK.OBIE.IBAN");
        accountNumber.setIdentification("IT35 5000 0000 0549 1000 0003");
        account.setAccount(List.of(accountNumber));
        return account;
    }
}
