package com.yolt.providers.unicredit.hypovereinsbank;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.FakeRestTemplateManager;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.unicredit.AccessMeansTestMapper;
import com.yolt.providers.unicredit.TestApp;
import com.yolt.providers.unicredit.UnicreditSampleTypedAuthenticationMeans;
import com.yolt.providers.unicredit.common.ais.UniCreditDataProvider;
import com.yolt.providers.unicredit.common.dto.UniCreditAccessMeansDTO;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/unicredit/hypovereins/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("unicredit")
public class HypoVereinsbankDataProviderHappyFlowIntegrationTest {

    private static final String CERT_PATH = "certificates/unicredit/unicredit_certificate.pem";
    private static final String BASE_CLIENT_REDIRECT_URL = "https://www.yolt.com/callback-acc";
    private static final String PSU_IP_ADDRESS = "10.0.0.2";
    private static final String CONSENT_REDIRECT_URL = "https://authorization.api-sandbox.unicredit.eu:8403/sandbox/psd2/bg/loginPSD2_BG.html?consentId=c3a1880e-cd10-4833-8edb-3296beee5247&correlationid=cSvzeI&country=DE";
    private static final String CONSENT_ID = "c3a1880e-cd10-4833-8edb-3296beee5247";
    private static final ZoneId BERLIN_ZONE_ID = ZoneId.of("Europe/Berlin");
    private static final int CONSENT_VALIDITY_DAYS = 90;

    @Mock
    private Signer signer;

    @Autowired
    @Qualifier("Unicredit")
    private ObjectMapper objectMapper;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private RestTemplateManager restTemplateManager;

    private UnicreditSampleTypedAuthenticationMeans testAuthenticationMeans;

    @Qualifier("HypoVereinsbankDataProvider")
    @Autowired
    private UniCreditDataProvider dataProvider;

    @BeforeEach
    public void setup() throws Exception {
        testAuthenticationMeans = new UnicreditSampleTypedAuthenticationMeans(CERT_PATH);
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @Test
    public void shouldReturnCorrectAutoconfigurationMeansForGetAutoConfiguredMeansWithCorrectData() {
        // when
        Map<String, TypedAuthenticationMeans> result = dataProvider.getAutoConfiguredMeans();

        // then
        assertThat(result)
                .hasSize(1)
                .containsEntry(REGISTRATION_STATUS, TypedAuthenticationMeans.TPP_ID);
    }

    @Test
    public void shouldReturnClientConfigurationMeansForAutoConfigurationMeans() {
        // given
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                testAuthenticationMeans.getAuthMeans(), restTemplateManager, signer, null);

        // when
        Map<String, BasicAuthenticationMean> configureMeans = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans).containsKey(REGISTRATION_STATUS);
    }

    @Test
    public void shouldReturnCorrectTypedAuthenticationMeansForGetTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> result = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(result)
                .hasSize(4)
                .containsEntry(EIDAS_CERTIFICATE, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM)
                .containsEntry(EIDAS_KEY_ID, TypedAuthenticationMeans.KEY_ID)
                .containsEntry(CLIENT_EMAIL, TypedAuthenticationMeans.CLIENT_EMAIL)
                .containsEntry(REGISTRATION_STATUS, TypedAuthenticationMeans.TPP_ID);
    }

    @Test
    public void shouldReturnCorrectKeyRequirementsForGeTransportKeyRequirements() {
        // when
        Optional<KeyRequirements> result = dataProvider.getTransportKeyRequirements();

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPrivateKidAuthenticationMeanReference()).isEqualTo(EIDAS_KEY_ID);
        assertThat(result.get().getPublicKeyAuthenticationMeanReference()).isEqualTo(EIDAS_CERTIFICATE);
        assertThat(result.get().getKeyRequirements()).isNotNull();
    }

    @Test
    public void shouldReturnRedirectStepWithProperRedirectUrlForGetLoginUrlWithCorrectData() {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(testAuthenticationMeans.getAuthMeans())
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setState(UUID.randomUUID().toString())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        Step result = dataProvider.getLoginInfo(request);

        // then
        assertThat(result).isInstanceOf(RedirectStep.class);
        RedirectStep redirectStep = (RedirectStep) result;
        assertThat(redirectStep.getExternalConsentId()).isNullOrEmpty();
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(CONSENT_REDIRECT_URL);
        assertThat(redirectStep.getProviderState()).isNotEmpty();
        UniCreditAccessMeansDTO uniCreditAccessMeansDTO = AccessMeansTestMapper.with(objectMapper).retrieveAccessMeans(redirectStep.getProviderState());
        assertThat(uniCreditAccessMeansDTO.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(uniCreditAccessMeansDTO.getCreated()).isCloseTo(Instant.now(), within(1, ChronoUnit.MINUTES));
        assertThat(uniCreditAccessMeansDTO.getExpireTime()).isCloseTo(LocalDate.now().atStartOfDay(BERLIN_ZONE_ID).toInstant().plus(CONSENT_VALIDITY_DAYS, ChronoUnit.DAYS), within(1, ChronoUnit.MINUTES));
    }

    @Test
    public void shouldReturnCorrectAccessMeansForCreateNewAccessMeansWithCorrectData() {
        // given
        Instant updated = Instant.now();
        Instant expires = Instant.now();
        String providerState = AccessMeansTestMapper.with(objectMapper).compactAccessMeans(new UniCreditAccessMeansDTO(CONSENT_ID, updated, expires));
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(UUID.randomUUID())
                .setProviderState(providerState)
                .build();

        // when
        AccessMeansOrStepDTO newAccessMeans = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(newAccessMeans.getStep()).isNull();
        assertThat(newAccessMeans.getAccessMeans()).isNotNull();
        AccessMeansDTO accessMeans = newAccessMeans.getAccessMeans();
        assertThat(accessMeans.getUserId()).isEqualTo(request.getUserId());
        assertThat(accessMeans.getUpdated()).isEqualTo(Date.from(updated));
        assertThat(accessMeans.getExpireTime()).isEqualTo(Date.from(expires));
        assertThat(accessMeans.getAccessMeans()).isEqualTo(providerState);
    }

    @Test
    public void shouldThrowInvalidTokenExceptionForRefreshAccessMeansWhenRefreshFlowIsNotSupported() {
        // given
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder().build();

        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> dataProvider.refreshAccessMeans(request);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldReturnCorrectDataProviderResponseForFetchDataWithCorrectData() throws TokenInvalidException, ProviderFetchDataException {
        // given
        Instant accessMeansCreated = Instant.now();
        Instant accessMeansExpires = accessMeansCreated.plus(Duration.ofDays(CONSENT_VALIDITY_DAYS));
        AccessMeansDTO accessMeans = new AccessMeansDTO(UUID.randomUUID(),
                AccessMeansTestMapper.with(objectMapper).compactAccessMeans(new UniCreditAccessMeansDTO(CONSENT_ID,
                        accessMeansCreated,
                        accessMeansExpires)),
                Date.from(accessMeansCreated),
                Date.from(accessMeansExpires));
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(testAuthenticationMeans.getAuthMeans())
                .setSigner(signer)
                .setTransactionsFetchStartTime(ZonedDateTime.of(2020, 9, 1, 0, 0, 0, 0, ZoneId.of("Z")).toInstant())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        DataProviderResponse result = dataProvider.fetchData(request);

        // then
        assertThat(result.getAccounts()).hasSize(2);

        validateAccountDto(result.getAccounts().get(0));
    }

    private void validateAccountDto(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("312fdgyiuy374yr349fh8923hf0823h8757973482qop238rh39fh3920340923u");
        assertThat(providerAccountDTO.getAccountNumber()).extracting(ProviderAccountNumberDTO::getScheme, ProviderAccountNumberDTO::getIdentification)
                .contains(ProviderAccountNumberDTO.Scheme.IBAN, "DE49700202700123456785");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(providerAccountDTO.getLastRefreshed()).isCloseTo(ZonedDateTime.now(ZoneId.of("Europe/Berlin")), within(1, ChronoUnit.MINUTES));
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("779.8");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("829.8");
        assertThat(providerAccountDTO.getName()).isEqualTo("HypoVereinsbank current account");

        validateExtendedAccountDto(providerAccountDTO.getExtendedAccount());

        assertThat(providerAccountDTO.getTransactions()).hasSize(5);
        validateTransactionsDto(providerAccountDTO.getTransactions());
    }

    private void validateExtendedAccountDto(ExtendedAccountDTO extendedAccountDTO) {
        assertThat(extendedAccountDTO.getResourceId()).isEqualTo("312fdgyiuy374yr349fh8923hf0823h8757973482qop238rh39fh3920340923u");
        assertThat(extendedAccountDTO.getAccountReferences()).extracting(AccountReferenceDTO::getType, AccountReferenceDTO::getValue)
                .contains(tuple(AccountReferenceType.IBAN, "DE49700202700123456785"));
        assertThat(extendedAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedAccountDTO.getCashAccountType()).isEqualTo(ExternalCashAccountType.CURRENT);
        assertThat(extendedAccountDTO.getProduct()).isEqualTo("23434123");
        assertThat(extendedAccountDTO.getBalances()).hasSize(3);

        validateBalancesDto(extendedAccountDTO.getBalances());
    }

    private void validateBalancesDto(List<BalanceDTO> balancesDTO) {
        BalanceDTO balanceDTO = balancesDTO.get(0);
        assertThat(balanceDTO.getBalanceAmount()).extracting(BalanceAmountDTO::getCurrency, BalanceAmountDTO::getAmount)
                .contains(CurrencyCode.EUR, new BigDecimal("779.8"));
        assertThat(balanceDTO.getBalanceType()).isEqualTo(BalanceType.EXPECTED);
        assertThat(balanceDTO.getLastChangeDateTime()).isEqualTo("2020-10-22T23:11+02:00[Europe/Berlin]");
    }

    private void validateTransactionsDto(List<ProviderTransactionDTO> transactionsDTO) {
        ProviderTransactionDTO providerTransactionDTO = transactionsDTO.get(0);
        assertThat(providerTransactionDTO.getDateTime()).isEqualTo("2019-10-22T00:00:00+02:00[Europe/Berlin]");
        assertThat(providerTransactionDTO.getAmount()).isEqualTo("11.99");
        assertThat(providerTransactionDTO.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(providerTransactionDTO.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(providerTransactionDTO.getDescription()).isEqualTo("NETFLIX");

        validateExtendedTransactionDto(providerTransactionDTO.getExtendedTransaction());
    }

    private void validateExtendedTransactionDto(ExtendedTransactionDTO extendedTransactionDTO) {
        assertThat(extendedTransactionDTO.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extendedTransactionDTO.getBookingDate()).isEqualTo("2019-10-22T00:00:00+02:00[Europe/Berlin]");
        assertThat(extendedTransactionDTO.getValueDate()).isEqualTo("2019-10-22T00:00:00+02:00[Europe/Berlin]");
        assertThat(extendedTransactionDTO.getTransactionAmount()).extracting(BalanceAmountDTO::getCurrency, BalanceAmountDTO::getAmount)
                .contains(CurrencyCode.EUR, new BigDecimal("-11.99"));
        assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isEqualTo("NETFLIX");
        assertThat(extendedTransactionDTO.getProprietaryBankTransactionCode()).isEqualTo("LASTSCHRIFT");
    }
}

