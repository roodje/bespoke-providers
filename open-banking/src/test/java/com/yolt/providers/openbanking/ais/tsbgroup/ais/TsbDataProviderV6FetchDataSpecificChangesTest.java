package com.yolt.providers.openbanking.ais.tsbgroup.ais;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.tsbgroup.TsbGroupApp;
import com.yolt.providers.openbanking.ais.tsbgroup.TsbGroupSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.tsbgroup.common.TsbGroupBaseDataProvider;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
 * This test contains fetch data scenario with specific changes in banks response applied occurring in TSB provider.
 * <p>
 * Disclaimer: as all providers in TSB group are the same from code and stubs perspective (then only difference is configuration)
 * we are running parametrized tests for testing, but this covers all providers from TSB group.
 * <p>
 * Covered flows:
 * - fetching accounts, balances, transactions
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TsbGroupApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/tsbgroup/ais/ob_3.1.1/specific-changes", httpsPort = 0, port = 0)
@ActiveProfiles("tsbgroup")
public class TsbDataProviderV6FetchDataSpecificChangesTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID USER_SITE_ID = UUID.randomUUID();
    private static String SERIALIZED_ACCESS_MEANS_EXTENDED;
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static RestTemplateManagerMock restTemplateManagerMock;
    private static Map<String, BasicAuthenticationMean> authenticationMeans;
    private static final Signer signer = new SignerMock();

    private final ObjectMapper objectMapper = OpenBankingTestObjectMapper.INSTANCE;
    @Autowired
    @Qualifier("TsbDataProviderV6")
    private TsbGroupBaseDataProvider tsbDataProviderV6;
    @BeforeAll
    public static void setup() throws IOException, URISyntaxException {

        AccessMeans accessToken = new AccessMeans(
                Instant.now(),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);

        SERIALIZED_ACCESS_MEANS_EXTENDED = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(accessToken);
        authenticationMeans = new TsbGroupSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "87da2798-f7e2-4823-80c1-3c03344b8f13");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnFetchDaTaWithSpecificChanges(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setUserSiteId(USER_SITE_ID)
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(USER_ID, SERIALIZED_ACCESS_MEANS_EXTENDED, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);

        ProviderAccountDTO providerAccountDTO = dataProviderResponse.getAccounts().get(0);
        validateAccount(providerAccountDTO);
        validateTransactions(providerAccountDTO.getTransactions());
    }

    private void validateAccount(ProviderAccountDTO providerAccountDTO) {
        ExtendedAccountDTO extendedAccount = providerAccountDTO.getExtendedAccount();
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("456");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("6.00"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("2.00"));
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("GB15AIBK12345678901235");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(providerAccountDTO.getName()).isEqualTo("Current Account");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(extendedAccount.getAccountReferences()).contains(
                AccountReferenceDTO.builder().type(AccountReferenceType.IBAN).value("GB15AIBK12345678901235").build()
        );
    }

    private void validateTransactions(List<ProviderTransactionDTO> transactions) {
        ProviderTransactionDTO transaction1 = transactions.get(0);
        assertThat(transaction1.getAmount()).isEqualTo(new BigDecimal("0.02"));
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction1.getCategory()).isEqualTo(YoltCategory.GENERAL);
        ExtendedTransactionDTO extendedTransaction = transaction1.getExtendedTransaction();
        AccountReferenceDTO creditorAccount = extendedTransaction.getCreditorAccount();
        AccountReferenceDTO debtorAccount = extendedTransaction.getDebtorAccount();
        assertThat(creditorAccount.getType()).isEqualTo(AccountReferenceType.MASKED_PAN);
        assertThat(creditorAccount.getValue()).isEqualTo("123456xxxxxx1234");
        assertThat(debtorAccount.getType()).isEqualTo(AccountReferenceType.PAN);
        assertThat(debtorAccount.getValue()).isEqualTo("5409050000000000");

        transaction1.validate();

        ProviderTransactionDTO transaction2 = transactions.get(1);
        assertThat(transaction2.getAmount()).isEqualTo(new BigDecimal("0.01"));
        assertThat(transaction2.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction2.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction2.getCategory()).isEqualTo(YoltCategory.GENERAL);
        extendedTransaction = transaction2.getExtendedTransaction();
        creditorAccount = extendedTransaction.getCreditorAccount();
        debtorAccount = extendedTransaction.getDebtorAccount();
        assertThat(creditorAccount.getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(creditorAccount.getValue()).isEqualTo("GB29NWBK60161331926819");
        assertThat(debtorAccount.getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(debtorAccount.getValue()).isEqualTo("GB15AIBK12345678901235");

        transaction2.validate();

        ProviderTransactionDTO transaction3 = transactions.get(2);
        assertThat(transaction3.getAmount()).isEqualTo(new BigDecimal("0.01"));
        assertThat(transaction3.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction3.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction3.getCategory()).isEqualTo(YoltCategory.GENERAL);
        extendedTransaction = transaction3.getExtendedTransaction();
        creditorAccount = extendedTransaction.getCreditorAccount();
        debtorAccount = extendedTransaction.getDebtorAccount();
        assertThat(creditorAccount.getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(creditorAccount.getValue()).isEqualTo("GB29NWBK60161331926819");
        assertThat(debtorAccount.getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(debtorAccount.getValue()).isEqualTo("GB15AIBK12345678901235");

        transaction3.validate();
    }

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(tsbDataProviderV6);
    }
}
