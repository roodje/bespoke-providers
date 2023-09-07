package com.yolt.providers.redsys;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.redsys.common.model.RedsysAccessMeans;
import com.yolt.providers.redsys.common.model.Token;
import com.yolt.providers.redsys.mock.RestTemplateManagerMock;
import com.yolt.providers.redsys.mock.SignerMock;
import com.yolt.providers.redsys.unicaja.UnicajaDataProvider;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains fetch data happy flow occurring in Unicaja provider.
 * <p>
 * Disclaimer: There is only one provider {@link com.yolt.providers.redsys.unicaja.UnicajaDataProvider} for this bank, so tests are not parametrized.
 * <p>
 * Covered flows:
 * - fetching accounts, balances, transactions
 * <p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/", httpsPort = 0, port = 0)
@Import(TestConfiguration.class)
@ActiveProfiles("redsys")
class UnicajaDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final String PSU_IP_ADDRESS = "192.168.16.5";
    private static final String REDIRECT_URL = "https://www.yolt.com/callback";
    private static final String ACCESS_TOKEN = "4ebf95f7-02f5-11eb-8309-411ae25e53e3";
    private static final String REFRESH_TOKEN = "1d3aa3a4-02f5-12eb-8309-711ae25e53e6";
    private static final String CONSENT_ID = "7eea2874-04f5-21ec-9cd5-fbg83ac17655";

    @Autowired
    private UnicajaDataProvider dataProvider;

    @Autowired
    @Qualifier("Redsys")
    private ObjectMapper objectMapper;

    @Autowired
    private Clock clock;

    private RestTemplateManagerMock restTemplateManagerMock = new RestTemplateManagerMock();
    private SignerMock signerMock = new SignerMock();

    private RedsysSampleAuthenticationMeans sampleAuthenticationMeans = new RedsysSampleAuthenticationMeans();
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    void initialize() throws IOException, URISyntaxException {
        authenticationMeans = sampleAuthenticationMeans.getRedsysSampleAuthenticationMeans();
    }

    @Test
    void shouldFetchData() throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        AccessMeansDTO accessMeansDTO = createAccessMeansDTO();
        UrlFetchDataRequest fetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now(clock))
                .setAccessMeans(accessMeansDTO)
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
        ProviderAccountDTO currentAccount1 = getCurrentAccountById(dataProviderResponse, "ES018202000000000000000000578044871");
        assertThat(currentAccount1.getAccountId()).isEqualTo("ES018202000000000000000000578044871");
        assertThat(currentAccount1.getName()).isEqualTo("CUENTA EXPANSIÓN");
        assertThat(currentAccount1.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(currentAccount1.getAvailableBalance()).isEqualTo("-100.50");
        assertThat(currentAccount1.getCurrentBalance()).isEqualTo("1230.00");
        assertThat(currentAccount1.getClosed()).isNull();
        assertThat(currentAccount1.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(currentAccount1.getExtendedAccount().getAccountReferences().get(0).getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(currentAccount1.getExtendedAccount().getAccountReferences().get(0).getValue()).isEqualTo("ES2420802795447427789873");
        assertThat(currentAccount1.getExtendedAccount().getBalances()).hasSize(2);
        assertThat(currentAccount1.getTransactions()).hasSize(5);

        validateCurrentAccountTransactions(currentAccount1.getTransactions());

        // Verify Current Account without transactions
        ProviderAccountDTO currentAccount2 = getCurrentAccountById(dataProviderResponse, "ES018202000000000000000000578044872");
        assertThat(currentAccount2.getAccountId()).isEqualTo("ES018202000000000000000000578044872");
        assertThat(currentAccount2.getName()).isEqualTo("CUENTA ONLINE");
        assertThat(currentAccount2.getCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(currentAccount2.getAvailableBalance()).isEqualTo("900.00");
        assertThat(currentAccount2.getCurrentBalance()).isEqualTo("500.00");
        assertThat(currentAccount2.getClosed()).isNull();
        assertThat(currentAccount2.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(currentAccount2.getExtendedAccount().getBalances()).hasSize(2);
        assertThat(currentAccount2.getTransactions()).isEmpty();
    }

    private ProviderAccountDTO getCurrentAccountById(DataProviderResponse response, String accountId) {
        return response.getAccounts().stream()
                .filter(account -> account.getAccountId().equals(accountId))
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }

    private void validateCurrentAccountTransactions(List<ProviderTransactionDTO> transactions) {
        // Verify transaction 1
        ProviderTransactionDTO transaction1 = transactions.get(0);
        assertThat(transaction1.getDateTime()).isEqualTo("2017-10-26T00:00+02:00[Europe/Madrid]");
        assertThat(transaction1.getAmount()).isEqualTo("12.89");
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction1.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction1.getDescription()).isEqualTo("Example for Remittance Information");

        ExtendedTransactionDTO extendedTransaction = transaction1.getExtendedTransaction();
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("Example for Remittance Information");
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2017-10-26T00:00+02:00[Europe/Madrid]");
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2017-10-27T00:00+02:00[Europe/Madrid]");
        assertThat(extendedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-12.89");
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("Example for Remittance Information");

        // Verify transaction 2
        ProviderTransactionDTO transaction2 = transactions.get(1);
        assertThat(transaction2.getDateTime()).isEqualTo("2017-10-25T00:00+02:00[Europe/Madrid]");
        assertThat(transaction2.getAmount()).isEqualTo("16.20");
        assertThat(transaction2.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction2.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction2.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction2.getDescription()).isEqualTo("Example for Remittance Information");
        assertThat(transaction2.getExtendedTransaction().getRemittanceInformationUnstructured()).isEqualTo("Example for Remittance Information");

        //Verify transaction 3
        ProviderTransactionDTO transaction3 = transactions.get(2);
        assertThat(transaction3.getDateTime()).isEqualTo("2017-10-26T00:00+02:00[Europe/Madrid]");
        assertThat(transaction3.getAmount()).isEqualTo("157.30");
        assertThat(transaction3.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction3.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction3.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction3.getDescription()).isEqualTo("Description not available");
        assertThat(transaction3.getExtendedTransaction().getRemittanceInformationUnstructured()).isNull();
        assertThat(transaction3.getExtendedTransaction().getRemittanceInformationStructured()).isNull();

        // Verify transaction 4
        ProviderTransactionDTO transaction4 = transactions.get(3);
        assertThat(transaction4.getDateTime()).isEqualTo("2018-10-25T00:00+02:00[Europe/Madrid]");
        assertThat(transaction4.getAmount()).isEqualTo("50.21");
        assertThat(transaction4.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction4.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction4.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction4.getDescription()).isEqualTo("Example for Remittance Information");
        assertThat(transaction4.getExtendedTransaction().getRemittanceInformationUnstructured()).isEqualTo("Example for Remittance Information");
    }

    private AccessMeansDTO createAccessMeansDTO() throws JsonProcessingException {
        return new AccessMeansDTO(
                USER_ID,
                objectMapper.writeValueAsString(new RedsysAccessMeans(createAccessMeansToken(), REDIRECT_URL, CONSENT_ID, null, Instant.MIN, new FilledInUserSiteFormValues())),
                new Date(),
                Date.from(Instant.now(clock).plusSeconds(3600)));
    }

    private static Token createAccessMeansToken() {
        Token token = new Token();
        token.setAccessToken(ACCESS_TOKEN);
        token.setRefreshToken(REFRESH_TOKEN);
        token.setTokenType("Bearer");
        token.setExpiresIn(3600);
        return token;
    }
}
