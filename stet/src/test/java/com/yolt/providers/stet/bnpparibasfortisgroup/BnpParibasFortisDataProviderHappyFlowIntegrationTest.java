package com.yolt.providers.stet.bnpparibasfortisgroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import com.yolt.providers.stet.SignerMock;
import com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.BnpParibasFortisDataProviderV2;
import com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.config.BnpParibasFortisProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.auth.BnpParibasFortisAuthenticationMeansSupplier.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * This test contains all happy flows occurring in BNP Paribas Fortis group providers.
 * <p>
 * Disclaimer: as all providers in BNP Paribas Fortis group are the same from code and stubs perspective (then only difference is configuration)
 * we are using {@link BnpParibasFortisDataProviderV2} for testing, but this covers all providers from BNP Paribas Fortis group
 * <p>
 * Covered flows:
 * - updating authenticaton means using autoonboarding
 * - acquiring consent page
 * - fetching accounts, balances, transactions
 * - creating access means
 * - refreshing access means
 * <p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/bnpparibasfortis-2.0.1/happy-flow/", httpsPort = 0, port = 0)
@Import(BnpParibasFortisTestConfig.class)
@ActiveProfiles("bnpparibasfortis")
class BnpParibasFortisDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final String ACCESS_TOKEN = "b343aa01-ea3c-4fe5-9658-944c82cb7683";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String REMITTANCE_INFORMATION = "transaction-information-1001";
    private static final String EXPECTED_DATE_TIME = "2019-02-27T00:00+01:00[Europe/Brussels]";
    private static final String BASE_CLIENT_REDIRECT_URL = "https://yolt.com/callback";

    @Autowired
    private BnpParibasFortisDataProviderV2 dataProvider;

    @Autowired
    @Qualifier("BnpParibasFortisStetProperties")
    private BnpParibasFortisProperties properties;

    private final RestTemplateManager restTemplateManagerMock = new SimpleRestTemplateManagerMock();
    private final SignerMock signerMock = new SignerMock();
    private final BnpParibasFortisGroupSampleMeans sampleAuthenticationMeans = new BnpParibasFortisGroupSampleMeans();

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @BeforeEach
    void initialize() throws IOException, URISyntaxException {
        authenticationMeans = sampleAuthenticationMeans.getConfiguredAuthMeans();
    }

    @Test
    void shouldReturnTypedAuthenticationMeansThatWillBeAutoConfigured() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getAutoConfiguredMeans();

        // then
        assertThat(typedAuthMeans)
                .hasSize(2)
                .containsEntry(CLIENT_ID_STRING_NAME, CLIENT_ID_STRING)
                .containsEntry(CLIENT_SECRET_STRING_NAME, CLIENT_SECRET_STRING);
    }

    @Test
    void shouldReturnAuthenticationMeansAfterAutoConfiguration() throws IOException, URISyntaxException {
        // given
        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(sampleAuthenticationMeans.getPreconfiguredAuthMeans())
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = dataProvider.autoConfigureMeans(request);

        // then
        assertThat(configuredAuthMeans.get(CLIENT_NAME).getValue()).isEqualTo("Yolt Application");
        assertThat(configuredAuthMeans.get(CLIENT_DESCRIPTION).getValue()).isEqualTo("Third Party Provider");
        assertThat(configuredAuthMeans.get(CLIENT_WEBSITE_URI).getValue()).isEqualTo("https://www.yolt.com/");
        assertThat(configuredAuthMeans.get(CONTACT_FIRST_NAME).getValue()).isEqualTo("John");
        assertThat(configuredAuthMeans.get(CONTACT_LAST_NAME).getValue()).isEqualTo("Smith");
        assertThat(configuredAuthMeans.get(CONTACT_EMAIL).getValue()).isEqualTo("example@yolt.com");
        assertThat(configuredAuthMeans.get(SIGNING_KEY_ID_NAME).getValue()).isEqualTo("5391cac7-b840-4628-8036-d4998dfb8959");
        assertThat(configuredAuthMeans.get(SIGNING_CERTIFICATE_NAME).getValue()).isEqualTo(sampleAuthenticationMeans.readFakeCertificatePem());
        assertThat(configuredAuthMeans.get(TRANSPORT_KEY_ID_NAME).getValue()).isEqualTo("2be4d475-f240-42c7-a22c-882566ac0f95");
        assertThat(configuredAuthMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()).isEqualTo(sampleAuthenticationMeans.readFakeCertificatePem());
        assertThat(configuredAuthMeans.get(CLIENT_ID_STRING_NAME).getValue()).isEqualTo("registered-client-id");
        assertThat(configuredAuthMeans.get(CLIENT_SECRET_STRING_NAME).getValue()).isEqualTo("registered-client-secret");
    }

    @Test
    void shouldReturnTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans)
                .hasSize(13)
                .containsEntry(CLIENT_NAME, CLIENT_NAME_TYPE)
                .containsEntry(CLIENT_DESCRIPTION, CLIENT_DESCRIPTION_TYPE)
                .containsEntry(CLIENT_WEBSITE_URI, CLIENT_WEBSITE_URI_TYPE)
                .containsEntry(CONTACT_FIRST_NAME, CONTACT_FIRST_NAME_TYPE)
                .containsEntry(CONTACT_LAST_NAME, CONTACT_LAST_NAME_TYPE)
                .containsEntry(CONTACT_EMAIL, CONTACT_EMAIL_TYPE)
                .containsEntry(CONTACT_PHONE, CONTACT_PHONE_TYPE)
                .containsEntry(SIGNING_KEY_ID_NAME, KEY_ID)
                .containsEntry(SIGNING_CERTIFICATE_NAME, CERTIFICATE_PEM)
                .containsEntry(TRANSPORT_KEY_ID_NAME, KEY_ID)
                .containsEntry(TRANSPORT_CERTIFICATE_NAME, CERTIFICATE_PEM)
                .containsEntry(CLIENT_ID_STRING_NAME, CLIENT_ID_STRING)
                .containsEntry(CLIENT_SECRET_STRING_NAME, CLIENT_SECRET_STRING);
    }

    @Test
    void shouldReturnRedirectStepWithConsentUrl() {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .setState("f736527c-c13a-4441-af18-31dd1634e0e3")
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .build();

        // when
        Step result = dataProvider.getLoginInfo(request);

        // then
        assertThat(result).isInstanceOf(RedirectStep.class);
        RedirectStep step = (RedirectStep) result;
        String loginUrl = step.getRedirectUrl();
        assertThat(loginUrl).contains("https://sandbox.auth.bnpparibasfortis.com/authorize");

        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(loginUrl).build().getQueryParams().toSingleValueMap();
        assertThat(queryParams)
                .containsEntry("response_type", "code")
                .containsEntry("client_id", "client-id")
                .containsEntry("redirect_uri", BASE_CLIENT_REDIRECT_URL)
                .containsEntry("scope", "aisp")
                .containsEntry("state", "f736527c-c13a-4441-af18-31dd1634e0e3");
    }

    @Test
    void shouldFetchData() throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest fetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(USER_ID, BnpParibasFortisGroupSampleMeans.createAuthorizedJsonProviderState(objectMapper, properties, ACCESS_TOKEN), new Date(), new Date())
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(fetchDataRequest);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(3);

        // Verify Current Account 1
        ProviderAccountDTO currentAccount1 = getCurrentAccountById(dataProviderResponse, "1");
        assertThat(currentAccount1.getAccountId()).isEqualTo("1");
        assertThat(currentAccount1.getName()).isEqualTo("Account One");
        assertThat(currentAccount1.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(currentAccount1.getAvailableBalance()).isEqualTo("330.30");
        assertThat(currentAccount1.getCurrentBalance()).isEqualTo("-220.20");
        assertThat(currentAccount1.getClosed()).isNull();
        assertThat(currentAccount1.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(currentAccount1.getExtendedAccount().getAccountReferences().get(0).getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(currentAccount1.getExtendedAccount().getAccountReferences().get(0).getValue()).isEqualTo("BE54470111111100");
        assertThat(currentAccount1.getExtendedAccount().getBalances()).hasSize(3);
        assertThat(currentAccount1.getExtendedAccount().getBalances())
                .allMatch(balanceDTO -> balanceDTO.getBalanceType() != null)
                .allMatch(balanceDTO -> balanceDTO.getBalanceAmount() != null);
        assertThat(currentAccount1.getTransactions()).hasSize(2);

        validateCurrentAccountTransactions(currentAccount1.getTransactions());

        // Verify Current Account 2
        ProviderAccountDTO currentAccount2 = getCurrentAccountById(dataProviderResponse, "2");
        assertThat(currentAccount2.getAccountId()).isEqualTo("2");
        assertThat(currentAccount2.getName()).isEqualTo("Account Two");
        assertThat(currentAccount2.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(currentAccount2.getAvailableBalance()).isEqualTo("-50.50");
        assertThat(currentAccount2.getCurrentBalance()).isEqualTo("-50.50");
        assertThat(currentAccount2.getClosed()).isNull();
        assertThat(currentAccount2.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(currentAccount2.getExtendedAccount().getBalances()).hasSize(1);
        assertThat(currentAccount2.getExtendedAccount().getBalances())
                .allMatch(balanceDTO -> balanceDTO.getBalanceType() != null)
                .allMatch(balanceDTO -> balanceDTO.getBalanceAmount() != null);
        assertThat(currentAccount2.getTransactions()).hasSize(1);

        // Verify Current Account 3
        ProviderAccountDTO currentAccount3 = getCurrentAccountById(dataProviderResponse, "3");
        assertThat(currentAccount3.getAccountId()).isEqualTo("3");
        assertThat(currentAccount3.getName()).isEqualTo("Account Three");
        assertThat(currentAccount3.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(currentAccount3.getAvailableBalance()).isEqualTo("-40.40");
        assertThat(currentAccount3.getCurrentBalance()).isEqualTo("-20.20");
        assertThat(currentAccount3.getClosed()).isNull();
        assertThat(currentAccount3.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(currentAccount3.getExtendedAccount().getBalances()).hasSize(4);
        assertThat(currentAccount3.getExtendedAccount().getBalances())
                .allMatch(balanceDTO -> balanceDTO.getBalanceType() != null)
                .allMatch(balanceDTO -> balanceDTO.getBalanceAmount() != null);
        assertThat(currentAccount3.getTransactions()).isEmpty();
    }

    @Test
    void shouldReturnNewAccessMeans() {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .setState("29dbba15-1e67-4ac0-ab0f-2487dc0c960b")
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setRedirectUrlPostedBackFromSite(BASE_CLIENT_REDIRECT_URL + "?state=29dbba15-1e67-4ac0-ab0f-2487dc0c960b&code=authorization-code")
                .setUserId(USER_ID)
                .setProviderState(serializeProviderState(DataProviderState.authorizedProviderState(createRegion(), "token", null)))
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(accessMeansOrStepDTO.getStep()).isNull();

        AccessMeansDTO accessMeansDTO = accessMeansOrStepDTO.getAccessMeans();
        assertThat(accessMeansDTO.getUserId()).isEqualTo(request.getUserId());
        assertThat(accessMeansDTO.getUpdated()).isBeforeOrEqualTo(Date.from(Instant.now()));
        assertThat(accessMeansDTO.getExpireTime()).isBeforeOrEqualTo(Date.from(Instant.now().plusSeconds(7775999)));

        DataProviderState providerState = BnpParibasFortisGroupSampleMeans.deserializeProviderState(objectMapper, accessMeansDTO.getAccessMeans());
        assertThat(providerState.getAccessToken()).isEqualTo("d5010e19-6e93-4720-af4c-98b9466c126f");
    }

    @Test
    void shouldReturnRefreshedAccessMeans() {
        // given
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .setAccessMeans(
                        UUID.randomUUID(),
                        serializeProviderState(
                                DataProviderState.authorizedProviderState(
                                        createRegion(),
                                        "token",
                                        null)),
                        new Date(),
                        new Date())
                .build();

        // when-then
        assertThatThrownBy(() -> dataProvider.refreshAccessMeans(request)).isInstanceOf(TokenInvalidException.class);
    }

    private ProviderAccountDTO getCurrentAccountById(DataProviderResponse response, String accountId) {
        return response.getAccounts().stream()
                .filter(account -> account.getAccountId().equals(accountId))
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }

    private void validateCurrentAccountTransactions(final List<ProviderTransactionDTO> transactions) {
        // Verify transaction 1
        ProviderTransactionDTO transaction1 = transactions.get(0);
        assertThat(transaction1.getExternalId()).isEqualTo("1001");
        assertThat(transaction1.getDateTime()).isEqualTo("2019-02-26T00:00+01:00[Europe/Brussels]");
        assertThat(transaction1.getAmount()).isEqualTo("12.25");
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction1.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction1.getDescription()).isEqualTo(REMITTANCE_INFORMATION);

        ExtendedTransactionDTO extendedTransaction = transaction1.getExtendedTransaction();
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo(REMITTANCE_INFORMATION);
        assertThat(extendedTransaction.getBookingDate()).isEqualTo(EXPECTED_DATE_TIME);
        assertThat(extendedTransaction.getValueDate()).isEqualTo("2019-02-28T00:00+01:00[Europe/Brussels]");
        assertThat(extendedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-12.25");
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransaction.getEntryReference()).isEqualTo("1001");

        // Verify transaction 2
        ProviderTransactionDTO transaction2 = transactions.get(1);
        assertThat(transaction2.getExternalId()).isEqualTo("1002");
        assertThat(transaction2.getDateTime()).isEqualTo(EXPECTED_DATE_TIME);
        assertThat(transaction2.getAmount()).isEqualTo("245.50");
        assertThat(transaction2.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction2.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction2.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction2.getDescription()).isEqualTo("N/A");

    }

    private String serializeProviderState(DataProviderState providerState) {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Region createRegion() {
        return properties.getRegions().get(0);
    }
}
