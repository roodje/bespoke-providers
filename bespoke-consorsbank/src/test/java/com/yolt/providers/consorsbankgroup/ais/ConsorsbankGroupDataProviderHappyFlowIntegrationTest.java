package com.yolt.providers.consorsbankgroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.consorsbankgroup.ConsorsbankGroupSampleAuthMeans;
import com.yolt.providers.consorsbankgroup.ConsorsbankGroupTestApp;
import com.yolt.providers.consorsbankgroup.FakeRestTemplateManager;
import com.yolt.providers.consorsbankgroup.common.ais.ConsorsbankGroupDataProvider;
import com.yolt.providers.consorsbankgroup.common.ais.DefaultAccessMeans;
import com.yolt.providers.consorsbankgroup.consorsbank.ConsorsbankBeanConfig;
import lombok.SneakyThrows;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("consorsbankgroup")
@SpringBootTest(classes = {ConsorsbankGroupTestApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = {"classpath:/stubs/consorsbankgroup/ais-1.3/happy-flow"}, httpsPort = 0, port = 0)
class ConsorsbankGroupDataProviderHappyFlowIntegrationTest {

    private static final String REDIRECT_URL = "https://www.yolt.com/callback";
    private static final String CONSENT_EXTERNAL_ID = "123";
    private static final String REDIRECT_EXTERNAL_ID = "321";
    private static final String PSU_IP_ADDRESS = "TEST_PSU_IP_ADDRESS";

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    private ConsorsbankGroupDataProvider dataProvider;

    @Autowired
    @Qualifier("consorsbankGroupObjectMapper")
    private ObjectMapper objectMapper;

    private RestTemplateManager restTemplateManager;

    @BeforeEach
    public void beforeEach() {
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @Test
    public void shouldReturnConsentPageUrl() throws IOException, URISyntaxException {
        // given
        String state = "randomState";

        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setAuthenticationMeans(ConsorsbankGroupSampleAuthMeans.sampleAuthMeans())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("TEST_PSU_IP_ADDRESS")
                .setState(state)
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        assertThat(loginInfo.getProviderState()).isEqualTo(CONSENT_EXTERNAL_ID);

        Map<String, String> redirectUrlQueryParams = UriComponentsBuilder
                .fromHttpUrl(loginInfo.getRedirectUrl())
                .build()
                .getQueryParams()
                .toSingleValueMap();

        assertThat(redirectUrlQueryParams).containsEntry("redirect_id", REDIRECT_EXTERNAL_ID);
        assertThat(redirectUrlQueryParams).containsEntry("consent_id", CONSENT_EXTERNAL_ID);
    }

    @Test
    public void shouldCreateAccessMeans() throws JsonProcessingException {
        // given
        UUID testUserId = UUID.randomUUID();
        String providerState = "TEST_CONSENT_ID";
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(testUserId)
                .setProviderState(providerState)
                .build();

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(request);
        DefaultAccessMeans deserializedAccessMeans = objectMapper.readValue(result.getAccessMeans().getAccessMeans(), DefaultAccessMeans.class);

        // then
        assertThat(result.getAccessMeans().getUserId()).isEqualTo(testUserId);
        assertThat(result.getAccessMeans().getAccessMeans()).containsSequence(providerState);
        assertThat(deserializedAccessMeans.getConsentId()).isEqualTo(providerState);
    }

    @Test
    public void shouldDeleteConsent() throws TokenInvalidException, IOException, URISyntaxException {
        // given
        UrlOnUserSiteDeleteRequest request = new UrlOnUserSiteDeleteRequestBuilder()
                .setAuthenticationMeans(ConsorsbankGroupSampleAuthMeans.sampleAuthMeans())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setAccessMeans(sampleAccessMeansDTO(CONSENT_EXTERNAL_ID, UUID.randomUUID()))
                .build();

        // when
        dataProvider.onUserSiteDelete(request);
    }

    @Test
    public void shouldReturnCorrectlyFetchData() throws TokenInvalidException, ProviderFetchDataException, IOException, URISyntaxException {
        // given
        UUID testUserId = UUID.randomUUID();
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(sampleAccessMeansDTO(CONSENT_EXTERNAL_ID, testUserId))
                .setAuthenticationMeans(ConsorsbankGroupSampleAuthMeans.sampleAuthMeans())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        DataProviderResponse providerResponse = dataProvider.fetchData(request);

        // then
        assertThat(providerResponse.getAccounts()).hasSize(3);
        providerResponse.getAccounts()
                .forEach(ProviderAccountDTO::validate);
        verifyCurrentAccount(providerResponse.getAccounts().get(0));
        verifySavingsAccount(providerResponse.getAccounts().get(1));
        verifyInvestmentsAccount(providerResponse.getAccounts().get(2));
    }

    private void verifyCurrentAccount(final ProviderAccountDTO acc) {
        assertThat(acc.getAccountId()).isEqualTo("1");
        assertThat(acc.getLastRefreshed()).isNotNull();
        assertThat(acc.getCurrentBalance()).isEqualTo("1501");
        assertThat(acc.getAvailableBalance()).isEqualTo("1500");
        assertThat(acc.getName()).isEqualTo("Consorsbank account");
        assertThat(acc.getClosed()).isFalse();
        assertThat(acc.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(acc.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(acc.getBic()).isEqualTo("");

        ExtendedAccountDTO extAcc = acc.getExtendedAccount();
        assertThat(extAcc.getResourceId()).isEqualTo("1");
        assertThat(extAcc.getBic()).isEqualTo("");
        assertThat(extAcc.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extAcc.getAccountReferences().get(0).getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(extAcc.getAccountReferences().get(0).getValue()).isEqualTo("DE60760300800500123456");

        List<BalanceDTO> balances = extAcc.getBalances();
        BalanceDTO currentBalance = balances.stream()
                .filter(b -> BalanceType.CLOSING_BOOKED.equals(b.getBalanceType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("should not be null"));
        BalanceDTO availableBalance = balances.stream()
                .filter(b -> BalanceType.INTERIM_AVAILABLE.equals(b.getBalanceType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("should not be null"));
        assertThat(currentBalance.getBalanceAmount().getAmount()).isEqualTo("1501");
        assertThat(currentBalance.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(currentBalance.getBalanceType()).isEqualTo(BalanceType.CLOSING_BOOKED);
        assertThat(currentBalance.getLastChangeDateTime()).isNotNull();
        assertThat(availableBalance.getBalanceAmount().getAmount()).isEqualTo("1500");
        assertThat(availableBalance.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(availableBalance.getBalanceType()).isEqualTo(BalanceType.INTERIM_AVAILABLE);
        assertThat(availableBalance.getLastChangeDateTime()).isNotNull();

        assertThat(acc.getTransactions()).hasSize(66);
        long pendingCount = acc.getTransactions()
                .stream()
                .filter(t -> TransactionStatus.PENDING.equals(t.getStatus()))
                .count();
        assertThat(pendingCount).isEqualTo(1);

        long bookedCount = acc.getTransactions()
                .stream()
                .filter(t -> TransactionStatus.BOOKED.equals(t.getStatus()))
                .count();
        assertThat(bookedCount).isEqualTo(65);

        ProviderTransactionDTO firstTransaction = acc.getTransactions()
                .stream()
                .filter(t -> "d2209556-da2a-4d7d-8fd1-110d3ea2833c".equals(t.getExternalId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("should not be null"));

        assertThat(firstTransaction.getAmount()).isEqualTo("56.27");
        assertThat(firstTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(firstTransaction.getDescription())
                .isEqualTo("VISA13016010MARKT MUSTER          56,27EUR0,000000000012.02.        0,00 10355411");
        assertThat(firstTransaction.getDateTime())
                .isEqualTo(ZonedDateTime.of(2019, 2, 14, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(firstTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(firstTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);

        ExtendedTransactionDTO firstExtTrn = firstTransaction.getExtendedTransaction();
        assertThat(firstExtTrn.getBookingDate())
                .isEqualTo(ZonedDateTime.of(2019, 2, 14, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(firstExtTrn.getValueDate())
                .isEqualTo(ZonedDateTime.of(2019, 2, 14, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(firstExtTrn.getCreditorAccount().getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(firstExtTrn.getCreditorAccount().getValue()).isEqualTo("DE00760300800922904916");
        assertThat(firstExtTrn.getCreditorName()).isEqualTo("NORMA");
        assertThat(firstExtTrn.getDebtorAccount().getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(firstExtTrn.getDebtorAccount().getValue()).isEqualTo("DE60760300800500123456");
        assertThat(firstExtTrn.getDebtorName()).isEqualTo("DE21760300800400500502");
        assertThat(firstExtTrn.getEndToEndId()).isEqualTo("12019000687-26: 01/0000170070");
        assertThat(firstExtTrn.getMandateId()).isEqualTo("1800000139");
        assertThat(firstExtTrn.getProprietaryBankTransactionCode()).isEqualTo("5");
        assertThat(firstExtTrn.getStatus()).isEqualTo(TransactionStatus.BOOKED);

        ProviderTransactionDTO secondTransaction = acc.getTransactions()
                .stream()
                .filter(t -> "24e6712e-e1a1-4555-85c6-13c36ec1509f".equals(t.getExternalId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("should not be null"));

        assertThat(secondTransaction.getAmount()).isEqualTo("1527.8");
        assertThat(secondTransaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(secondTransaction.getDescription())
                .isEqualTo("Gehalt");
        assertThat(secondTransaction.getDateTime())
                .isEqualTo(ZonedDateTime.of(2019, 2, 15, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(secondTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(secondTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);

        ExtendedTransactionDTO secondExtTrn = secondTransaction.getExtendedTransaction();
        assertThat(secondExtTrn.getBookingDate())
                .isEqualTo(ZonedDateTime.of(2019, 2, 15, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(secondExtTrn.getValueDate())
                .isEqualTo(ZonedDateTime.of(2019, 2, 15, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(secondExtTrn.getCreditorAccount().getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(secondExtTrn.getCreditorAccount().getValue()).isEqualTo("DE21760300800400500502");
        assertThat(secondExtTrn.getCreditorName()).isEqualTo("Juliana Mustern");
        assertThat(secondExtTrn.getDebtorAccount().getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(secondExtTrn.getDebtorAccount().getValue()).isEqualTo("DE60760300800500123456");
        assertThat(secondExtTrn.getDebtorName()).isEqualTo("DE62700500000401166266");
        assertThat(secondExtTrn.getEndToEndId()).isEqualTo("");
        assertThat(secondExtTrn.getMandateId()).isEqualTo("");
        assertThat(secondExtTrn.getProprietaryBankTransactionCode()).isEqualTo("53");
        assertThat(secondExtTrn.getStatus()).isEqualTo(TransactionStatus.BOOKED);

        ProviderTransactionDTO thirdTransaction = acc.getTransactions()
                .stream()
                .filter(t -> "f9e06185-18c4-42ec-ad86-67e6c713ff53".equals(t.getExternalId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("should not be null"));

        assertThat(thirdTransaction.getAmount()).isEqualTo("25");
        assertThat(thirdTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(thirdTransaction.getDescription())
                .isEqualTo("Katja DWS Invest Top Divi          0828285801 001001 DWS1D8");
        assertThat(thirdTransaction.getDateTime())
                .isEqualTo(ZonedDateTime.of(2019, 2, 1, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(thirdTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(thirdTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);

        ExtendedTransactionDTO thirdExtTrn = thirdTransaction.getExtendedTransaction();
        assertThat(thirdExtTrn.getBookingDate())
                .isEqualTo(ZonedDateTime.of(2019, 2, 1, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(thirdExtTrn.getValueDate())
                .isEqualTo(ZonedDateTime.of(2019, 2, 1, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(thirdExtTrn.getCreditorAccount().getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(thirdExtTrn.getCreditorAccount().getValue()).isEqualTo("DE61760300800820285809");
        assertThat(thirdExtTrn.getCreditorName()).isEqualTo("Katja Mustern");
        assertThat(thirdExtTrn.getDebtorAccount().getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(thirdExtTrn.getDebtorAccount().getValue()).isEqualTo("DE60760300800500123456");
        assertThat(thirdExtTrn.getDebtorName()).isEqualTo("DE21760300800400500502");
        assertThat(thirdExtTrn.getEndToEndId()).isEqualTo("NOTPROVIDED");
        assertThat(thirdExtTrn.getMandateId()).isEqualTo("01-20161118-225313-223882");
        assertThat(thirdExtTrn.getProprietaryBankTransactionCode()).isEqualTo("5");
        assertThat(thirdExtTrn.getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    private void verifySavingsAccount(final ProviderAccountDTO acc) {
        assertThat(acc.getAccountId()).isEqualTo("2");
        assertThat(acc.getLastRefreshed()).isNotNull();
        assertThat(acc.getCurrentBalance()).isEqualTo("2301");
        assertThat(acc.getAvailableBalance()).isEqualTo("2300");
        assertThat(acc.getName()).isEqualTo("Consorsbank account");
        assertThat(acc.getClosed()).isFalse();
        assertThat(acc.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(acc.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);
        assertThat(acc.getBic()).isEqualTo("");

        ExtendedAccountDTO extAcc = acc.getExtendedAccount();
        assertThat(extAcc.getResourceId()).isEqualTo("2");
        assertThat(extAcc.getBic()).isEqualTo("");
        assertThat(extAcc.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extAcc.getAccountReferences().get(0).getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(extAcc.getAccountReferences().get(0).getValue()).isEqualTo("DE98701204008538752000");

        List<BalanceDTO> balances = extAcc.getBalances();
        BalanceDTO currentBalance = balances.stream()
                .filter(b -> BalanceType.CLOSING_BOOKED.equals(b.getBalanceType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("should not be null"));
        BalanceDTO availableBalance = balances.stream()
                .filter(b -> BalanceType.INTERIM_AVAILABLE.equals(b.getBalanceType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("should not be null"));
        assertThat(currentBalance.getBalanceAmount().getAmount()).isEqualTo("2301");
        assertThat(currentBalance.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(currentBalance.getBalanceType()).isEqualTo(BalanceType.CLOSING_BOOKED);
        assertThat(currentBalance.getLastChangeDateTime()).isNotNull();
        assertThat(availableBalance.getBalanceAmount().getAmount()).isEqualTo("2300");
        assertThat(availableBalance.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(availableBalance.getBalanceType()).isEqualTo(BalanceType.INTERIM_AVAILABLE);
        assertThat(availableBalance.getLastChangeDateTime()).isNotNull();

        assertThat(acc.getTransactions()).hasSize(2);

        ProviderTransactionDTO firstTransaction = acc.getTransactions()
                .stream()
                .filter(t -> "8508921e-2cd4-43e8-ba1e-26b143307927".equals(t.getExternalId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("should not be null"));

        assertThat(firstTransaction.getAmount()).isEqualTo("100");
        assertThat(firstTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(firstTransaction.getDescription())
                .isEqualTo("Alles Gute zum Geburstag DATUM 04.02.2018, 21.21 UHR1.TAN 598233");
        assertThat(firstTransaction.getDateTime())
                .isEqualTo(ZonedDateTime.of(2019, 2, 4, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(firstTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(firstTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);

        ExtendedTransactionDTO firstExtTrn = firstTransaction.getExtendedTransaction();
        assertThat(firstExtTrn.getBookingDate())
                .isEqualTo(ZonedDateTime.of(2019, 2, 4, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(firstExtTrn.getValueDate())
                .isEqualTo(ZonedDateTime.of(2019, 2, 4, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(firstExtTrn.getCreditorAccount().getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(firstExtTrn.getCreditorAccount().getValue()).isEqualTo("DE45760365682870018759");
        assertThat(firstExtTrn.getCreditorName()).isEqualTo("Robert Betzel");
        assertThat(firstExtTrn.getDebtorAccount().getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(firstExtTrn.getDebtorAccount().getValue()).isEqualTo("DE98701204008538752000");
        assertThat(firstExtTrn.getDebtorName()).isEqualTo("Isabella Ionescu");
        assertThat(firstExtTrn.getEndToEndId()).isEqualTo("");
        assertThat(firstExtTrn.getMandateId()).isEqualTo("");
        assertThat(firstExtTrn.getProprietaryBankTransactionCode()).isEqualTo("");
        assertThat(firstExtTrn.getStatus()).isEqualTo(TransactionStatus.BOOKED);
    }

    private void verifyInvestmentsAccount(final ProviderAccountDTO acc) {
        assertThat(acc.getAccountId()).isEqualTo("3");
        assertThat(acc.getLastRefreshed()).isNotNull();
        assertThat(acc.getCurrentBalance()).isEqualTo("201");
        assertThat(acc.getAvailableBalance()).isEqualTo("230");
        assertThat(acc.getName()).isEqualTo("Consorsbank account");
        assertThat(acc.getClosed()).isFalse();
        assertThat(acc.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(acc.getYoltAccountType()).isEqualTo(AccountType.INVESTMENT);
        assertThat(acc.getBic()).isEqualTo("CSXXXXXXXXX");

        ExtendedAccountDTO extAcc = acc.getExtendedAccount();
        assertThat(extAcc.getResourceId()).isEqualTo("3");
        assertThat(extAcc.getBic()).isEqualTo("CSXXXXXXXXX");
        assertThat(extAcc.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extAcc.getAccountReferences().get(0).getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(extAcc.getAccountReferences().get(0).getValue()).isEqualTo("DE17236023400834520145647");

        List<BalanceDTO> balances = extAcc.getBalances();
        BalanceDTO currentBalance = balances.stream()
                .filter(b -> BalanceType.CLOSING_BOOKED.equals(b.getBalanceType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("should not be null"));
        BalanceDTO availableBalance = balances.stream()
                .filter(b -> BalanceType.INTERIM_AVAILABLE.equals(b.getBalanceType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("should not be null"));
        assertThat(currentBalance.getBalanceAmount().getAmount()).isEqualTo("201");
        assertThat(currentBalance.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(currentBalance.getBalanceType()).isEqualTo(BalanceType.CLOSING_BOOKED);
        assertThat(currentBalance.getLastChangeDateTime()).isNotNull();
        assertThat(availableBalance.getBalanceAmount().getAmount()).isEqualTo("230");
        assertThat(availableBalance.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(availableBalance.getBalanceType()).isEqualTo(BalanceType.INTERIM_AVAILABLE);
        assertThat(availableBalance.getLastChangeDateTime()).isNotNull();

        assertThat(acc.getTransactions()).hasSize(2);

        ProviderTransactionDTO firstTransaction = acc.getTransactions()
                .stream()
                .filter(t -> "8508921e-2cd4-43e8-ba1e-26b143307927".equals(t.getExternalId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("should not be null"));

        assertThat(firstTransaction.getAmount()).isEqualTo("100");
        assertThat(firstTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(firstTransaction.getDescription())
                .isEqualTo("Alles Gute zum Geburstag DATUM 04.02.2018, 21.21 UHR1.TAN 598233");
        assertThat(firstTransaction.getDateTime())
                .isEqualTo(ZonedDateTime.of(2019, 2, 4, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(firstTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(firstTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);

        ExtendedTransactionDTO firstExtTrn = firstTransaction.getExtendedTransaction();
        assertThat(firstExtTrn.getBookingDate())
                .isEqualTo(ZonedDateTime.of(2019, 2, 4, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(firstExtTrn.getValueDate())
                .isEqualTo(ZonedDateTime.of(2019, 2, 4, 0, 0, 0, 0, ConsorsbankBeanConfig.ZONE_ID));
        assertThat(firstExtTrn.getCreditorAccount().getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(firstExtTrn.getCreditorAccount().getValue()).isEqualTo("DE45760365682870018759");
        assertThat(firstExtTrn.getCreditorName()).isEqualTo("Robert Betzel");
        assertThat(firstExtTrn.getDebtorAccount().getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(firstExtTrn.getDebtorAccount().getValue()).isEqualTo("DE98701204008538752000");
        assertThat(firstExtTrn.getDebtorName()).isEqualTo("Isabella Ionescu");
        assertThat(firstExtTrn.getEndToEndId()).isEqualTo("");
        assertThat(firstExtTrn.getMandateId()).isEqualTo("");
        assertThat(firstExtTrn.getProprietaryBankTransactionCode()).isEqualTo("");
        assertThat(firstExtTrn.getStatus()).isEqualTo(TransactionStatus.BOOKED);
    }

    @SneakyThrows
    private AccessMeansDTO sampleAccessMeansDTO(String consentId, UUID userId) {
        DefaultAccessMeans accessMeans = new DefaultAccessMeans(consentId);
        String serializedAccessMeans = objectMapper.writeValueAsString(accessMeans);
        return new AccessMeansDTO(
                userId,
                serializedAccessMeans,
                new Date(),
                Date.from(Instant.now().plus(89, ChronoUnit.DAYS)));
    }
}