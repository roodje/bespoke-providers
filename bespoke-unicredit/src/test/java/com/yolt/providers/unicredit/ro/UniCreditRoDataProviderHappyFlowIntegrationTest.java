package com.yolt.providers.unicredit.ro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.FakeRestTemplateManager;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
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
import nl.ing.lovebird.providershared.form.Field;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.FormComponent;
import nl.ing.lovebird.providershared.form.TextField;
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

import static com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans.REGISTRATION_STATUS;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/unicredit/ro/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("unicredit")
public class UniCreditRoDataProviderHappyFlowIntegrationTest {

    private static final String CONSENT_ID = "c3a1880e-cd10-4833-8edb-3296beee5247";
    private static final String CONSENT_URL = "https://authorization.api-sandbox.unicredit.eu:8403/sandbox/psd2/bg/loginPSD2_BG.html?consentId=c3a1880e-cd10-4833-8edb-3296beee5247&correlationid=cSvzeI&country=RO";
    private static final String REDIRECT_URL = "https://www.yolt.com/callback-acc";
    private static final String STATE = "8b6dee15-ea2a-49b2-b100-f5f96d31cd90";
    private static final String CERT_PATH = "certificates/unicredit/unicredit_certificate.pem";
    private static final String PSU_IP_ADDRESS = "192.160.1.2";
    private static final String PSU_IBAN = "IT18L0200811770000019486580";
    private static final int CONSENT_VALIDITY_DAYS = 89;
    private static final ZoneId BUCHAREST_ZONE_ID = ZoneId.of("Europe/Bucharest");
    private static final String IBAN_FORM_FIELD_ID = "Iban";
    private static final String IBAN_FORM_FIELD_DISPLAY_NAME = "IBAN";

    @Mock
    private Signer signer;

    @Autowired
    @Qualifier("Unicredit")
    private ObjectMapper objectMapper;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private RestTemplateManager restTemplateManager;

    private UnicreditSampleTypedAuthenticationMeans testAuthenticationMeans;

    @Qualifier("UniCreditRoDataProviderV1")
    @Autowired
    private UniCreditDataProvider dataProvider;

    @BeforeEach
    public void setup() throws Exception {
        testAuthenticationMeans = new UnicreditSampleTypedAuthenticationMeans(CERT_PATH);
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @Test
    public void shouldReturnClientConfigurationMeansForAutoConfigurationMeansWithCorrectRequestData() {
        // given
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                testAuthenticationMeans.getAuthMeans(), restTemplateManager, signer, null);

        // when
        Map<String, BasicAuthenticationMean> configureMeans = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans).containsKey(REGISTRATION_STATUS);
    }

    @Test
    public void shouldReturnFormStepWithIbanFieldForGetLoginInfoWithCorrectData() {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL).setState(STATE)
                .setAuthenticationMeans(testAuthenticationMeans.getAuthMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        Step result = dataProvider.getLoginInfo(request);

        // then
        assertThat(result).isInstanceOf(FormStep.class);
        FormStep formStep = (FormStep) result;
        assertThat(formStep.getEncryptionDetails())
                .extracting(EncryptionDetails::getJweDetails)
                .isNull();
        assertThat(formStep.getTimeoutTime()).isCloseTo(Instant.now().plus(Duration.ofHours(1)), within(1, ChronoUnit.MINUTES));
        assertThat(formStep.getProviderState()).isNullOrEmpty();
        assertThat(formStep.getForm().getExplanationField()).isNull();
        assertThat(formStep.getForm().getHiddenComponents()).isNullOrEmpty();
        assertThat(formStep.getForm().getFormComponents()).hasSize(1);
        FormComponent formComponent = formStep.getForm().getFormComponents().get(0);
        assertThat(formComponent).isInstanceOf(TextField.class);
        TextField ibanField = (TextField) formComponent;
        assertThat(ibanField).extracting(Field::getId, Field::getDisplayName, Field::isOptional, Field::isPersist, TextField::getLength, TextField::getMaxLength)
                .contains(IBAN_FORM_FIELD_ID, IBAN_FORM_FIELD_DISPLAY_NAME, false, false, 24, 24);
    }

    @Test
    public void shouldReturnRedirectStepWithConsentUrlAndCorrectAccessMeansForCreateNewAccessMeansWhenUserPostedBackFilledInFormValues() {
        // given
        UUID userId = UUID.randomUUID();
        Map<String, String> filledInFormFieldsValues = new HashMap<>();
        filledInFormFieldsValues.put(IBAN_FORM_FIELD_ID, PSU_IBAN);
        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(filledInFormFieldsValues);
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setAuthenticationMeans(testAuthenticationMeans.getAuthMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(STATE)
                .build();

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(result.getAccessMeans()).isNull();
        assertThat(result.getStep()).isInstanceOf(RedirectStep.class);
        RedirectStep redirectStep = (RedirectStep) result.getStep();
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(CONSENT_URL);
        assertThat(redirectStep.getExternalConsentId()).isNullOrEmpty();
        assertThat(redirectStep.getProviderState()).isNotEmpty();
        UniCreditAccessMeansDTO uniCreditAccessMeansDTO = AccessMeansTestMapper.with(objectMapper).retrieveAccessMeans(redirectStep.getProviderState());
        assertThat(uniCreditAccessMeansDTO.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(uniCreditAccessMeansDTO.getCreated()).isCloseTo(Instant.now(), within(1, ChronoUnit.MINUTES));
        assertThat(uniCreditAccessMeansDTO.getExpireTime()).isCloseTo(LocalDate.now().atStartOfDay(BUCHAREST_ZONE_ID).toInstant().plus(CONSENT_VALIDITY_DAYS, ChronoUnit.DAYS), within(1, ChronoUnit.MINUTES));
    }

    @Test
    public void shouldReturnNewAccessMeansForCreateNewAccessMeansWithCorrectRequestData() {
        // given
        AccessMeansTestMapper accessMeansTestMapper = AccessMeansTestMapper.with(objectMapper);
        UUID userId = UUID.randomUUID();
        UniCreditAccessMeansDTO setUpAccessMeans = new UniCreditAccessMeansDTO(CONSENT_ID, Instant.ofEpochMilli(999), Instant.ofEpochMilli(9999));
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setProviderState(accessMeansTestMapper.compactAccessMeans(setUpAccessMeans))
                .setSigner(signer)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStep = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        AccessMeansDTO resultAccessMeans = accessMeansOrStep.getAccessMeans();
        assertThat(resultAccessMeans.getUserId()).isEqualTo(userId);
        assertThat(resultAccessMeans.getExpireTime()).isEqualTo(new Date(Instant.ofEpochMilli(9999).toEpochMilli()));
        assertThat(resultAccessMeans.getUpdated()).isEqualTo(new Date(Instant.ofEpochMilli(999).toEpochMilli()));
        assertThat(accessMeansTestMapper.retrieveAccessMeans(resultAccessMeans.getAccessMeans())).isEqualTo(setUpAccessMeans);
    }

    @Test
    public void shouldThrowTokenInvalidExceptionForRefreshAccessMeansAsThisFeatureIsUnsupported() {
        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> dataProvider.refreshAccessMeans(null);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldReturnDataProviderResponseForFetchDataWithCorrectRequestData() throws ProviderFetchDataException, TokenInvalidException {
        // given
        Instant accessMeansCreated = Instant.now();
        Instant accessMeansExpires = accessMeansCreated.plus(Duration.ofDays(89));
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
                .setTransactionsFetchStartTime(ZonedDateTime.of(2020, 8, 1, 0, 0, 0, 0, ZoneId.of("Z")).toInstant())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        assertAccounts(response.getAccounts());
        assertTransactionsHaveExpectedValues(response);
    }

    private void assertAccounts(final List<ProviderAccountDTO> accounts) {
        accounts.forEach(ProviderAccountDTO::validate);
        assertThat(accounts).hasSize(2);

        ProviderAccountDTO firstAccount = accounts.get(0);
        ProviderAccountDTO secondAccount = accounts.get(1);

        firstAccount.validate();
        secondAccount.validate();

        assertThat(firstAccount.getAccountId()).isEqualTo("312fdgyiuy374yr349fh8923hf0823h8757973482qop238rh39fh3920340923u");
        assertThat(firstAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(firstAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(firstAccount.getBic()).isEqualTo("UNCRITM1NF9");
        assertThat(firstAccount.getName()).isEqualTo("John Doe");
        assertThat(firstAccount.getCurrentBalance()).isEqualTo("123.45");
        assertThat(firstAccount.getAvailableBalance()).isEqualTo("125.67");
        assertThat(firstAccount.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(firstAccount.getAccountNumber().getIdentification()).isEqualTo("IT75E0100000000000000000001");

        ExtendedAccountDTO firstExtendedAccount = firstAccount.getExtendedAccount();
        assertThat(firstExtendedAccount.getResourceId()).isEqualTo("312fdgyiuy374yr349fh8923hf0823h8757973482qop238rh39fh3920340923u");
        assertThat(firstExtendedAccount.getBic()).isEqualTo("UNCRITM1NF9");
        assertThat(firstExtendedAccount.getCashAccountType()).isEqualTo(ExternalCashAccountType.CURRENT);
        assertThat(firstExtendedAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(firstExtendedAccount.getAccountReferences()).hasSize(1);
        assertThat(firstExtendedAccount.getAccountReferences().get(0).getValue()).isEqualTo("IT75E0100000000000000000001");
        assertThat(firstExtendedAccount.getAccountReferences().get(0).getType()).isEqualTo(AccountReferenceType.IBAN);

        BalanceDTO firstBalanceForFirstAccount = firstExtendedAccount.getBalances().get(0);
        assertThat(firstBalanceForFirstAccount.getBalanceAmount().getAmount()).isEqualTo("123.45");
        assertThat(firstBalanceForFirstAccount.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(firstBalanceForFirstAccount.getBalanceType()).isEqualTo(BalanceType.EXPECTED);

        BalanceDTO secondBalanceForFirstAccount = firstExtendedAccount.getBalances().get(1);
        assertThat(secondBalanceForFirstAccount.getBalanceAmount().getAmount()).isEqualTo("124.56");
        assertThat(secondBalanceForFirstAccount.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(secondBalanceForFirstAccount.getBalanceType()).isEqualTo(BalanceType.OPENING_BOOKED);

        assertThat(secondAccount.getAccountId()).isEqualTo("980981309fdus09fu12uidhy90128123897dasd8012ud0823h832hf08345h09f");
        assertThat(secondAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(secondAccount.getAccountNumber().getIdentification()).isEqualTo("IT75E0100000000000000000002");
        BalanceDTO firstBalanceForSecondAccount = secondAccount.getExtendedAccount().getBalances().get(0);
        assertThat(firstBalanceForSecondAccount.getBalanceAmount().getAmount()).isEqualTo("123.45");
        BalanceDTO secondBalanceForSecondAccount = secondAccount.getExtendedAccount().getBalances().get(1);
        assertThat(secondBalanceForSecondAccount.getBalanceAmount().getAmount()).isEqualTo("124.56");
        assertThat(secondAccount.getExtendedAccount().getCashAccountType()).isEqualTo(ExternalCashAccountType.CURRENT);
    }

    private void assertTransactionsHaveExpectedValues(final DataProviderResponse response) {
        List<ProviderAccountDTO> accounts = response.getAccounts();
        ProviderAccountDTO account0 = accounts.get(0);
        List<ProviderTransactionDTO> transactions0 = account0.getTransactions();
        assertThat(transactions0).hasSize(6);

        ProviderTransactionDTO transaction00 = transactions0.get(0);
        assertThat(transaction00.getDateTime()).isEqualTo("2020-08-01T00:00+03:00[Europe/Bucharest]");
        assertThat(transaction00.getAmount()).isEqualTo("123.12");
        assertThat(transaction00.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction00.getType()).isEqualTo(ProviderTransactionType.CREDIT);

        ExtendedTransactionDTO extTransaction00 = transaction00.getExtendedTransaction();
        assertThat(extTransaction00.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extTransaction00.getEntryReference()).isEqualTo("321");
        assertThat(extTransaction00.getEndToEndId()).isEqualTo("123");
        assertThat(extTransaction00.getMandateId()).isEqualTo("11");
        assertThat(extTransaction00.getCheckId()).isEqualTo("22");
        assertThat(extTransaction00.getCreditorId()).isEqualTo("33");
        assertThat(extTransaction00.getBookingDate()).isEqualTo("2020-08-01T00:00+03:00[Europe/Bucharest]");
        assertThat(extTransaction00.getValueDate()).isEqualTo("2020-08-01T00:00+03:00[Europe/Bucharest]");
        assertThat(extTransaction00.getTransactionAmount())
                .extracting(BalanceAmountDTO::getCurrency, BalanceAmountDTO::getAmount)
                .contains(CurrencyCode.EUR, BigDecimal.valueOf(123.12));
        assertThat(extTransaction00.getDebtorName()).isEqualTo("John Kowalsky");
        assertThat(extTransaction00.getDebtorAccount())
                .extracting(AccountReferenceDTO::getType, AccountReferenceDTO::getValue)
                .contains(AccountReferenceType.IBAN, "123123123");
        assertThat(extTransaction00.getUltimateDebtor()).isEqualTo("UltimateDebtor");
        assertThat(extTransaction00.getCreditorName()).isNullOrEmpty();
        assertThat(extTransaction00.getCreditorAccount()).isNull();
        assertThat(extTransaction00.getUltimateCreditor()).isNullOrEmpty();
        assertThat(extTransaction00.getRemittanceInformationUnstructured()).isEqualTo("Some remittance info");
        assertThat(extTransaction00.getPurposeCode()).isEqualTo("COST");
        assertThat(extTransaction00.getBankTransactionCode()).isEqualTo("ABC");
        assertThat(extTransaction00.isTransactionIdGenerated()).isFalse();
    }
}
