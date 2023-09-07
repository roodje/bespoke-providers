package com.yolt.providers.openbanking.ais.rbsgroup.ais.v11;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsApp;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;
import com.yolt.providers.openbanking.ais.rbsgroup.common.RbsGroupDataProviderV5;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains fetch data happy flow in all RBS group providers for some specifis cases, except Coutts, since it cannot handle pagination on localhost due to port removal.
 * <p>
 * Covered flows:
 * - fetching accounts, balances (without credit lines), transactions (with debtor and creditor information), direct debits and standing orders
 * <p>
 * Providers: ALL RBS Group except Coutts
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = RbsApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("rbsgroup-v5")
@AutoConfigureWireMock(stubs = "classpath:/stubs/rbsgroup/ob_3.1.6/ais/fetchdata_specific/common", port = 0, httpsPort = 0)
class RbsGroupDataProviderFetchDataSpecificIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private Signer signer;

    @Autowired
    @Qualifier("NatWestDataProviderV11")
    private RbsGroupDataProviderV5 natwestDataProvider;
    @Autowired
    @Qualifier("NatWestCorporateDataProviderV10")
    private RbsGroupDataProviderV5 natwestCorpoDataProvider;
    @Autowired
    @Qualifier("RoyalBankOfScotlandDataProviderV11")
    private RbsGroupDataProviderV5 rbsDataProvider;
    @Autowired
    @Qualifier("RoyalBankOfScotlandCorporateDataProviderV10")
    private RbsGroupDataProviderV5 rbsCorpoDataProvider;
    @Autowired
    @Qualifier("UlsterBankDataProviderV10")
    private RbsGroupDataProviderV5 ulsterDataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Stream<RbsGroupDataProviderV5> getProviders() {
        return Stream.of(
                natwestDataProvider,
                natwestCorpoDataProvider,
                rbsDataProvider,
                rbsCorpoDataProvider,
                ulsterDataProvider);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldFetchDataWithSpecificChanges(RbsGroupDataProviderV5 dataProvider) throws Exception {
        // given
        AccessMeans token = new AccessMeans(Instant.ofEpochMilli(0L),
                null,
                "accessToken456V2",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                null,
                null);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, objectMapper.writeValueAsString(token), new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        RestTemplateManagerMock restTemplateManagerMock = new RestTemplateManagerMock(() -> "12345");
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(RbsSampleAuthenticationMeansV4.getRbsSampleAuthenticationMeansForAis())
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
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("2.00");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("3.00");
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
        assertThat(transaction1.getAmount()).isEqualTo("0.02");
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

        ProviderTransactionDTO transaction2 = transactions.get(1);
        assertThat(transaction2.getAmount()).isEqualTo("0.01");
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

        ProviderTransactionDTO transaction3 = transactions.get(2);
        assertThat(transaction3.getAmount()).isEqualTo("0.01");
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
    }
}
