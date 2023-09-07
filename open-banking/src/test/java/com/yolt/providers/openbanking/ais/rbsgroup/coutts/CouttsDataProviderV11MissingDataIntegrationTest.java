package com.yolt.providers.openbanking.ais.rbsgroup.coutts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsApp;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;
import com.yolt.providers.openbanking.ais.rbsgroup.common.RbsGroupDataProviderV5;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains missing data flows occurring in Coutts. As a business decision provider implementation should return dto as best effort
 * <p>
 * Covered flows:
 * - fetching accounts, balances, transactions when some data are missing
 * <p>
 */
@SpringBootTest(classes = RbsApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("rbsgroup-v5")
@AutoConfigureWireMock(stubs = "classpath:/stubs/rbsgroup/ob_3.1.6/ais/missing-data/coutts", port = 0, httpsPort = 0)
class CouttsDataProviderV11MissingDataIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static RestTemplateManagerMock restTemplateManagerMock;
    private static Map<String, BasicAuthenticationMean> authenticationMeans;
    private static String requestTraceId;

    @Autowired
    @Qualifier("CouttsDataProviderV3")
    private RbsGroupDataProviderV5 couttsDataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll() throws Exception {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = RbsSampleAuthenticationMeansV4.getRbsSampleAuthenticationMeansForAis();
    }

    @BeforeEach
    void beforeEach() {
        requestTraceId = "12345";
    }

    @Test
    void shouldFetchDataWhenSomeInformationInBankResponseAreMissing() throws Exception {
        // given
        AccessMeans token = new AccessMeans(
                Instant.now(),
                null,
                "test-accounts",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                null,
                null);
        String serializedAccessMeans = objectMapper.writeValueAsString(token);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedAccessMeans, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        DataProviderResponse dataProviderResponse = couttsDataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
        ProviderAccountDTO account = dataProviderResponse.getAccounts().get(0);
        assertThat(account.getName()).isEqualTo("Current account");
        assertThat(account.getAvailableBalance()).isNull();
        assertThat(account.getCurrentBalance()).isEqualTo("1.00");
        assertThat(account.getCurrency()).isNull();
        assertThat(account.getExtendedAccount().getCurrency()).isNull();
        assertThat(account.getExtendedAccount().getBalances()).hasSize(1);
        assertThat(account.getExtendedAccount().getBalances().get(0).getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(account.getExtendedAccount().getBalances().get(0).getBalanceAmount().getAmount()).isEqualTo("1.00");
        assertThat(account.getTransactions()).hasSize(3);
        assertThat(account.getTransactions().get(0).getAmount()).isNull();
        assertThat(account.getTransactions().get(0).getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(account.getTransactions().get(0).getType()).isNull();
        assertThat(account.getTransactions().get(0).getExtendedTransaction().getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(account.getTransactions().get(0).getExtendedTransaction().getTransactionAmount().getAmount()).isNull();
        assertThat(account.getTransactions().get(1).getAmount()).isEqualTo("0.01");
        assertThat(account.getTransactions().get(1).getStatus()).isNull();
        assertThat(account.getTransactions().get(1).getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(account.getTransactions().get(1).getExtendedTransaction().getTransactionAmount().getCurrency()).isNull();
        assertThat(account.getTransactions().get(1).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("0.01");
        assertThat(account.getTransactions().get(2).getAmount()).isEqualTo("15.13");
        assertThat(account.getTransactions().get(2).getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(account.getTransactions().get(2).getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(account.getTransactions().get(2).getExtendedTransaction().getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(account.getTransactions().get(2).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("-15.13");
    }

}
