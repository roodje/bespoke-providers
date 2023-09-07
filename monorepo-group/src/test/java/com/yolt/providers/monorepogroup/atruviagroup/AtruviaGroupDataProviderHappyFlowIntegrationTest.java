package com.yolt.providers.monorepogroup.atruviagroup;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.monorepogroup.TestSigner;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.*;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID_HEADER_STRING;
import static com.yolt.providers.monorepogroup.atruviagroup.EncryptorHelper.*;
import static com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans.AtruviaGroupAuthenticationMeansFactory.CLIENT_SIGNING_CERTIFICATE_NAME;
import static com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans.AtruviaGroupAuthenticationMeansFactory.CLIENT_SIGNING_KEY_ID_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.ing.lovebird.extendeddata.account.ExternalCashAccountType.CURRENT;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AtruviaGroupTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/atruviagroup/ais/happy-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("atruviagroup")
class AtruviaGroupDataProviderHappyFlowIntegrationTest extends AtruviaGroupDataProviderIntegrationTestBase {

    private static final String QR_IMAGE_BASE64 = "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";

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("VolksbankenRaiffeisenProvider")
    private UrlDataProvider dataProvider;

    @Autowired
    private Clock clock;

    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        String pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_SIGNING_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID_HEADER_STRING.getType(), KEY_ID_VALUE));
        authenticationMeans.put(CLIENT_SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CLIENT_SIGNING_CERTIFICATE_PEM.getType(), pemCertificate));

        signer = new TestSigner();
    }

    @Test
    void shouldReturnTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans).containsAllEntriesOf(Map.of(CLIENT_SIGNING_KEY_ID_NAME, KEY_ID_HEADER_STRING, CLIENT_SIGNING_CERTIFICATE_NAME, CLIENT_SIGNING_CERTIFICATE_PEM));
    }

    @Test
    void shouldReturnFormStepWithSelectRegionalBankField() {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        Step step = dataProvider.getLoginInfo(request);

        // then
        assertThat(step).isInstanceOf(FormStep.class);
        var formStep = (FormStep) step;
        assertThat(extractExpectedProviderState(ObtainUserNameAndPasswordInputStep.class, formStep.getProviderState()))
                .isInstanceOf(ObtainUserNameAndPasswordInputStep.class);
        assertThat(formStep.getForm()).isEqualTo(expectedRegionalBankSelectionForm());
    }

    @Test
    void shouldReturnFormStepWithFieldsForUsernameAndPassword() {
        // given
        var incomingProviderStateString = toAccessMeansString(new ObtainUserNameAndPasswordInputStep());
        var filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(Collections.singletonMap("bank", SELECTED_REGIONAL_BANK_ID));
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(incomingProviderStateString)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .build();

        // when
        AccessMeansOrStepDTO step = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(step.getStep()).isInstanceOf(FormStep.class);
        var formStep = (FormStep) step.getStep();
        var expectedProviderState = extractExpectedProviderState(CreateConsentAndObtainScaMethodInputStep.class, formStep.getProviderState());
        assertThat(expectedProviderState.selectedRegionalBankCode()).isEqualTo(SELECTED_REGIONAL_BANK_ID);
        assertThat(expectedProviderState.atruviaEncryptionData().algorithm()).isEqualTo(ALGORITHM);
        assertThat(expectedProviderState.atruviaEncryptionData().encryption()).isEqualTo(ENCRYPTION);
        assertThat(formStep.getForm()).isEqualTo(expectedUserNameAndPasswordForm());
        assertThat(formStep.getEncryptionDetails()).isNotEqualTo(EncryptionDetails.noEncryption());
        var jweDetails = formStep.getEncryptionDetails().getJweDetails();
        assertThat(jweDetails.getEncryptionMethod()).isEqualTo(ENCRYPTION);
        assertThat(jweDetails.getRsaPublicJwk().getAlg()).isEqualTo(ALGORITHM);
    }

    @Test
    void shouldReturnFormStepWithSCASelectionMethod() {
        // given
        var incomingProviderState = toAccessMeansString(new CreateConsentAndObtainScaMethodInputStep(SELECTED_REGIONAL_BANK_ID, new AtruviaFormDecryptor(ALGORITHM, ENCRYPTION, PRIVATE_KEY)));
        var filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(Map.of("username", encryptSensitiveFieldValueWithJose4j("John", PUBLIC_KEY),
                "password", encryptSensitiveFieldValueWithJose4j("$secret", PUBLIC_KEY)));
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(incomingProviderState)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setSigner(signer)
                .build();

        // when
        AccessMeansOrStepDTO step = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(step.getStep())
                .isInstanceOf(FormStep.class);
        var formStep = (FormStep) step.getStep();
        var expectedProviderState = extractExpectedProviderState(ObtainChallengeDataInputStep.class, formStep.getProviderState());
        assertThat(expectedProviderState.selectedRegionalBankCode()).isEqualTo(SELECTED_REGIONAL_BANK_ID);
        assertThat(expectedProviderState.consentId()).isEqualTo(CONSENT_ID);
        assertThat(expectedProviderState.authorisationId()).isEqualTo(AUTHORISATION_ID);
        assertThat(expectedProviderState.username()).isEqualTo(USERNAME);
        assertThat(formStep.getForm()).isEqualTo(expectedScaMethodSelectionForm());
        assertThat(formStep.getEncryptionDetails()).isEqualTo(EncryptionDetails.noEncryption());
    }

    @Test
    void shouldReturnFormStepWithChallengeDataBasingOnObtainedSCAMethodAsSMS() {
        // given
        var incomingProviderState = toAccessMeansString(new ObtainChallengeDataInputStep(SELECTED_REGIONAL_BANK_ID, USERNAME, CONSENT_ID, AUTHORISATION_ID));
        var filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(Map.of("scaMethod", "942"));
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(incomingProviderState)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setSigner(signer)
                .build();

        // when
        AccessMeansOrStepDTO step = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(step.getStep()).isInstanceOf(FormStep.class);
        var formStep = (FormStep) step.getStep();
        var expectedProviderState = extractExpectedProviderState(ObtainChallengeOutcomeInputStep.class, formStep.getProviderState());
        assertThat(expectedProviderState.selectedRegionalBankCode()).isEqualTo(SELECTED_REGIONAL_BANK_ID);
        assertThat(expectedProviderState.consentId()).isEqualTo(CONSENT_ID);
        assertThat(expectedProviderState.authorisationId()).isEqualTo(AUTHORISATION_ID);
        assertThat(expectedProviderState.username()).isEqualTo(USERNAME);
        assertThat(expectedProviderState.atruviaEncryptionData().algorithm()).isEqualTo(ALGORITHM);
        assertThat(expectedProviderState.atruviaEncryptionData().encryption()).isEqualTo(ENCRYPTION);

        assertThat(formStep.getEncryptionDetails()).isNotEqualTo(EncryptionDetails.noEncryption());
        var jweDetails = formStep.getEncryptionDetails().getJweDetails();
        assertThat(jweDetails.getEncryptionMethod()).isEqualTo(ENCRYPTION);
        assertThat(jweDetails.getRsaPublicJwk().getAlg()).isEqualTo(ALGORITHM);
        assertThat(formStep.getForm()).isEqualTo(expectedMobileTanForm());
    }

    @Test
    void shouldReturnFormStepWithChallengeDataBasingOnObtainedSCAMethodAsPush() {
        // given
        var incomingProviderState = toAccessMeansString(new ObtainChallengeDataInputStep(SELECTED_REGIONAL_BANK_ID, USERNAME, CONSENT_ID, AUTHORISATION_ID));
        var filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(Map.of("scaMethod", "944"));
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(incomingProviderState)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setSigner(signer)
                .build();

        // when
        AccessMeansOrStepDTO step = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(step.getStep()).isInstanceOf(FormStep.class);
        var formStep = (FormStep) step.getStep();
        var expectedProviderState = extractExpectedProviderState(ObtainChallengeOutcomeInputStep.class, formStep.getProviderState());
        assertThat(expectedProviderState.selectedRegionalBankCode()).isEqualTo(SELECTED_REGIONAL_BANK_ID);
        assertThat(expectedProviderState.consentId()).isEqualTo(CONSENT_ID);
        assertThat(expectedProviderState.authorisationId()).isEqualTo(AUTHORISATION_ID);
        assertThat(expectedProviderState.username()).isEqualTo(USERNAME);
        assertThat(expectedProviderState.atruviaEncryptionData().algorithm()).isEqualTo(ALGORITHM);
        assertThat(expectedProviderState.atruviaEncryptionData().encryption()).isEqualTo(ENCRYPTION);

        assertThat(formStep.getEncryptionDetails()).isNotEqualTo(EncryptionDetails.noEncryption());
        var jweDetails = formStep.getEncryptionDetails().getJweDetails();
        assertThat(jweDetails.getEncryptionMethod()).isEqualTo(ENCRYPTION);
        assertThat(jweDetails.getRsaPublicJwk().getAlg()).isEqualTo(ALGORITHM);
        assertThat(formStep.getForm()).isEqualTo(expectedPushTanForm());
    }

    @Test
    void shouldReturnFormStepWithChallengeDataBasingOnObtainedSCAMethodAsChipOTPWithoutChallengeData() {
        // given
        var incomingProviderState = toAccessMeansString(new ObtainChallengeDataInputStep(SELECTED_REGIONAL_BANK_ID, USERNAME, CONSENT_ID, AUTHORISATION_ID));
        var filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(Map.of("scaMethod", "962"));
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(incomingProviderState)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setSigner(signer)
                .build();

        // when
        AccessMeansOrStepDTO step = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(step.getStep()).isInstanceOf(FormStep.class);
        var formStep = (FormStep) step.getStep();
        var expectedProviderState = extractExpectedProviderState(ObtainChallengeOutcomeInputStep.class, formStep.getProviderState());
        assertThat(expectedProviderState.selectedRegionalBankCode()).isEqualTo(SELECTED_REGIONAL_BANK_ID);
        assertThat(expectedProviderState.consentId()).isEqualTo(CONSENT_ID);
        assertThat(expectedProviderState.authorisationId()).isEqualTo(AUTHORISATION_ID);
        assertThat(expectedProviderState.username()).isEqualTo(USERNAME);
        assertThat(expectedProviderState.atruviaEncryptionData().algorithm()).isEqualTo(ALGORITHM);
        assertThat(expectedProviderState.atruviaEncryptionData().encryption()).isEqualTo(ENCRYPTION);

        assertThat(formStep.getEncryptionDetails()).isNotEqualTo(EncryptionDetails.noEncryption());
        var jweDetails = formStep.getEncryptionDetails().getJweDetails();
        assertThat(jweDetails.getEncryptionMethod()).isEqualTo(ENCRYPTION);
        assertThat(jweDetails.getRsaPublicJwk().getAlg()).isEqualTo(ALGORITHM);
        assertThat(formStep.getForm()).isEqualTo(expectedChipWithoutChallengeForm());
    }

    @Test
    void shouldReturnFormStepWithChallengeDataBasingOnObtainedSCAMethodAsChipOTPWithChallengeData() {
        // given
        var incomingProviderState = toAccessMeansString(new ObtainChallengeDataInputStep(SELECTED_REGIONAL_BANK_ID, USERNAME, CONSENT_ID, AUTHORISATION_ID));
        var filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(Map.of("scaMethod", "972"));
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(incomingProviderState)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setSigner(signer)
                .build();

        // when
        AccessMeansOrStepDTO step = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(step.getStep()).isInstanceOf(FormStep.class);
        var formStep = (FormStep) step.getStep();
        var expectedProviderState = extractExpectedProviderState(ObtainChallengeOutcomeInputStep.class, formStep.getProviderState());
        assertThat(expectedProviderState.selectedRegionalBankCode()).isEqualTo(SELECTED_REGIONAL_BANK_ID);
        assertThat(expectedProviderState.consentId()).isEqualTo(CONSENT_ID);
        assertThat(expectedProviderState.authorisationId()).isEqualTo(AUTHORISATION_ID);
        assertThat(expectedProviderState.username()).isEqualTo(USERNAME);
        assertThat(expectedProviderState.atruviaEncryptionData().algorithm()).isEqualTo(ALGORITHM);
        assertThat(expectedProviderState.atruviaEncryptionData().encryption()).isEqualTo(ENCRYPTION);

        assertThat(formStep.getEncryptionDetails()).isNotEqualTo(EncryptionDetails.noEncryption());
        var jweDetails = formStep.getEncryptionDetails().getJweDetails();
        assertThat(jweDetails.getEncryptionMethod()).isEqualTo(ENCRYPTION);
        assertThat(jweDetails.getRsaPublicJwk().getAlg()).isEqualTo(ALGORITHM);
        assertThat(formStep.getForm()).isEqualTo(expectedChipWithChallengeForm());
    }

    @Test
    void shouldReturnFormStepWithChallengeDataBasingOnObtainedSCAMethodAsPhotoOTP() {
        // given
        var incomingProviderState = toAccessMeansString(new ObtainChallengeDataInputStep(SELECTED_REGIONAL_BANK_ID, USERNAME, CONSENT_ID, AUTHORISATION_ID));
        var filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(Map.of("scaMethod", "982"));
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(incomingProviderState)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setSigner(signer)
                .build();

        // when
        AccessMeansOrStepDTO step = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(step.getStep()).isInstanceOf(FormStep.class);
        var formStep = (FormStep) step.getStep();
        var expectedProviderState = extractExpectedProviderState(ObtainChallengeOutcomeInputStep.class, formStep.getProviderState());
        assertThat(expectedProviderState.selectedRegionalBankCode()).isEqualTo(SELECTED_REGIONAL_BANK_ID);
        assertThat(expectedProviderState.consentId()).isEqualTo(CONSENT_ID);
        assertThat(expectedProviderState.authorisationId()).isEqualTo(AUTHORISATION_ID);
        assertThat(expectedProviderState.username()).isEqualTo(USERNAME);
        assertThat(expectedProviderState.atruviaEncryptionData().algorithm()).isEqualTo(ALGORITHM);
        assertThat(expectedProviderState.atruviaEncryptionData().encryption()).isEqualTo(ENCRYPTION);

        assertThat(formStep.getEncryptionDetails()).isNotEqualTo(EncryptionDetails.noEncryption());
        var jweDetails = formStep.getEncryptionDetails().getJweDetails();
        assertThat(jweDetails.getEncryptionMethod()).isEqualTo(ENCRYPTION);
        assertThat(jweDetails.getRsaPublicJwk().getAlg()).isEqualTo(ALGORITHM);
        assertThat(formStep.getForm()).isEqualTo(expectedPhotoOtpWithChallengeForm());
    }

    @Test
    void shouldFinishTheAuthentication() {
        // given
        var incomingProviderState = toAccessMeansString(new ObtainChallengeOutcomeInputStep(SELECTED_REGIONAL_BANK_ID, USERNAME, CONSENT_ID, AUTHORISATION_ID, new AtruviaFormDecryptor(ALGORITHM, ENCRYPTION, PRIVATE_KEY)));
        var filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(Map.of("challengeData", encryptSensitiveFieldValueWithJose4j("123456", PUBLIC_KEY)));
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(incomingProviderState)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setSigner(signer)
                .setUserId(USER_ID)
                .build();

        // when
        AccessMeansOrStepDTO step = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(step.getAccessMeans())
                .isInstanceOf(AccessMeansDTO.class)
                .isEqualTo(prepareExpectedAccessMeans());
    }

    private AccessMeansDTO prepareExpectedAccessMeans() {
        var expectedAuthenticationMean = toAccessMeansString(new AtruviaAccessMeans("1234-wertiq-983", "82064188"));
        return new AccessMeansDTO(USER_ID, expectedAuthenticationMean, Date.from(Instant.now(clock)), Date.from(Instant.now(clock).plus(89, ChronoUnit.DAYS)));
    }

    @Test
    void shouldFetchData() throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(prepareExpectedAccessMeans())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .setSigner(signer)
                .build();

        DataProviderResponse expectedResponse = createExpectedResponse();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        response.getAccounts().forEach(ProviderAccountDTO::validate);
        assertThat(response).isEqualTo(expectedResponse);
    }

    private Form expectedRegionalBankSelectionForm() {
        var selectField = new SelectField("bank", "Select the bank", 0, 100, false, true);
        selectField.addSelectOptionValue(new SelectOptionValue("82064188", "VR Bank Weimar eG"));
        selectField.addSelectOptionValue(new SelectOptionValue("21762550", "VR Bank Westküste eG"));
        var form = new Form();
        form.setFormComponents(Collections.singletonList(selectField));
        return form;
    }

    private Form expectedUserNameAndPasswordForm() {
        var usernameTextField = new TextField("username", "Username", 0, 4096, false, false);
        var passwordTextField = new PasswordField("password", "Password", 0, 4096, false, ".*");
        var loginForm = new Form(List.of(usernameTextField, passwordTextField), null, null);
        loginForm.setFormComponents(List.of(usernameTextField, passwordTextField));
        return loginForm;
    }

    private Form expectedScaMethodSelectionForm() {
        var selectField = new SelectField("scaMethod", "SCA Method", 0, 100, false, false);
        selectField.addSelectOptionValue(new SelectOptionValue("942", "mobile TAN"));
        selectField.addSelectOptionValue(new SelectOptionValue("944", "SecureGo"));
        selectField.addSelectOptionValue(new SelectOptionValue("946", "SecureGo plus (Direktfreigabe)"));
        selectField.addSelectOptionValue(new SelectOptionValue("962", "SmartTAN plus HHD 1.4"));
        selectField.addSelectOptionValue(new SelectOptionValue("972", "SmartTAN optic/USB HHD 1.4"));
        selectField.addSelectOptionValue(new SelectOptionValue("982", "Smart-TAN photo"));
        return new Form(List.of(selectField), null, null);
    }

    private Form expectedMobileTanForm() {
        return new Form(Collections.singletonList(new TextField("challengeData", "mobile TAN", 0, 4096, false, false)), new ExplanationField("challengeMethodExplanationId", "Explanation provided by ASPSP", "Die mobileTAN zu diesem Auftrag wird als SMS an Ihre Mobil-Telefonnummer gesendet, die bei Ihrer Bank registriert ist."), null);
    }

    private Form expectedPushTanForm() {
        return new Form(Collections.singletonList(new TextField("challengeData", "SecureGo", 0, 4096, false, false)), new ExplanationField("challengeMethodExplanationId", "Explanation provided by ASPSP", "Die SecureSIGN-TAN zu diesem Auftrag wird als Push-Nachricht an die bei Ihrer Bank registrierte ID gesendet."), null);
    }

    private Form expectedChipWithoutChallengeForm() {
        return new Form(Collections.singletonList(new TextField("challengeData", "SmartTAN plus HHD 1.4", 0, 4096, false, false)), new ExplanationField("challengeMethodExplanationId", "Explanation provided by ASPSP", "1. Stecken Sie Ihre Chipkarte in den TAN-Generator und drücken \"TAN\"<br>2. Geben Sie den Startcode \"209301604576\" ein und drücken \"OK\"<br>3. Prüfen Sie die Anzeige auf dem Leserdisplay und drücken \"OK\"<br>4. Geben Sie die mit 'x' markierten Stellen der Empfänger-IBAN \"DExx32460422001786xxxx\" ein und drücken \"OK\"<br>5. Geben Sie \"den Betrag\" ein und drücken \"OK\"<br><br>Bitte geben Sie die auf Ihrem TAN-Generator angezeigte TAN hier ein und bestätigen Sie diese mit \"OK\""), null);
    }

    private Form expectedChipWithChallengeForm() {
        return new Form(List.of(
                new FlickerCodeField("flickerCode", "SmartTAN optic/USB HHD 1.4", "0468C0110930898853522DE84499999310000005140043,33"),
                new TextField("challengeData", "SmartTAN optic/USB HHD 1.4", 0, 4096, false, false)),
                new ExplanationField("challengeMethodExplanationId", "Explanation provided by ASPSP", "1. Stecken Sie Ihre Chipkarte in den TAN-Generator und drücken \"F\"<br>2. Halten Sie den TAN-Generator an die animierte Grafik. Dabei müssen sich die Markierungen<br>   (Dreiecke) von der Grafik mit denen des TAN-Generators berühren<br>3. Prüfen Sie die Anzeige auf dem Leserdisplay und drücken \"OK\"<br>4. Prüfen Sie die Hinweise<br>   \"Empfänger-IBAN\" und \"Betrag\"<br>   auf dem Leserdisplay und bestätigen Sie diese dann jeweils mit \"OK\" auf Ihrem TAN-Generator<br><br>5. Bitte beachten: Überprüfen Sie die Anzeige des TAN-Generators immer anhand der Origninal-Transaktions-Daten - z.B. einer Rechnung<br>Bitte geben Sie die auf ihrem TAN-Generator angezeigte TAN hier ein und bestätigen Sie diese mit \"OK\""), null);
    }

    private Form expectedPhotoOtpWithChallengeForm() {
        return new Form(List.of(
                new ImageField("photoOtp", "Smart-TAN photo", QR_IMAGE_BASE64, "image/png"),
                new TextField("challengeData", "Smart-TAN photo", 0, 4096, false, false)),
                new ExplanationField("challengeMethodExplanationId", "Explanation provided by ASPSP", "1. Stecken Sie Ihre Chipkarte in den Sm@rt-TAN photo-Leser und drücken \"Scan\".<br>2. Halten Sie den Sm@rt-TAN photo-Leser so vor die Farbcode-Grafik, dass der Farbcode in der Anzeige vollständig angezeigt wird.<br>3. Prüfen Sie die Transaktionsdaten auf dem Display und bestätigen diese dann jeweils mit \"OK\".<br><br>4. Bitte beachten: Überprüfen Sie die Anzeige des Sm@rt-TAN photo-Lesers immer anhand der Original-Transaktionsdaten - z.B. einer Rechnung."), null);
    }

    private ZonedDateTime toZonedTime(int year, int month, int day) {
        return ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.MIN, ZONE_ID);
    }

    private DataProviderResponse createExpectedResponse() {
        return new DataProviderResponse(
                List.of(createAccount1(), createAccount2())
        );
    }

    private ProviderAccountDTO createAccount1() {
        return ProviderAccountDTO.builder()
                .accountId("3dc3d5b3-7023-4848-9853-f5400a64e80f")
                .accountNumber(new ProviderAccountNumberDTO(IBAN, "DE2310010010123456789"))
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(new BigDecimal("500.00"))
                .currentBalance(new BigDecimal("500.00"))
                .currency(CurrencyCode.EUR)
                .name("Main Account")
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .transactions(List.of(
                        createAccount1Transaction1(),
                        createAccount1Transaction2(),
                        createAccount1Transaction3(),
                        createAccount1Transaction4()
                ))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("3dc3d5b3-7023-4848-9853-f5400a64e80f")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE2310010010123456789")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Main Account")
                        .cashAccountType(CURRENT)
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.CLOSING_BOOKED)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("500.00"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .referenceDate(toZonedTime(2017, 10, 25))
                                        .build()
                        ))
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount1Transaction1() {
        return ProviderTransactionDTO.builder()
                .externalId("1234567")
                .dateTime(toZonedTime(2017, 10, 25))
                .amount(new BigDecimal("256.67"))
                .status(BOOKED)
                .type(CREDIT)
                .description("Example 1")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .bookingDate(toZonedTime(2017, 10, 25))
                        .valueDate(toZonedTime(2017, 10, 26))
                        .remittanceInformationUnstructured("Example 1")
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("256.67"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("John Miles")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE67100100101306118605")
                                .build())
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount1Transaction2() {
        return ProviderTransactionDTO.builder()
                .externalId("1234568")
                .dateTime(toZonedTime(2017, 10, 25))
                .amount(new BigDecimal("343.01"))
                .status(BOOKED)
                .type(CREDIT)
                .description("Example 2")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .bookingDate(toZonedTime(2017, 10, 25))
                        .valueDate(toZonedTime(2017, 10, 26))
                        .remittanceInformationUnstructured("Example 2")
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("343.01"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .debtorName("Paul Simpson")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("NL76RABO0359400371")
                                .build())
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount1Transaction3() {
        return ProviderTransactionDTO.builder()
                .externalId("2234567")
                .dateTime(toZonedTime(2017, 10, 26))
                .amount(new BigDecimal("356.67"))
                .status(BOOKED)
                .type(CREDIT)
                .description("Example 3")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .bookingDate(toZonedTime(2017, 10, 26))
                        .valueDate(toZonedTime(2017, 10, 27))
                        .remittanceInformationUnstructured("Example 3")
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("356.67"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("John Miles")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE67100100101306118605")
                                .build())
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount1Transaction4() {
        return ProviderTransactionDTO.builder()
                .externalId("2234568")
                .dateTime(toZonedTime(2017, 10, 26))
                .amount(new BigDecimal("443.01"))
                .status(BOOKED)
                .type(CREDIT)
                .description("Example 4")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .bookingDate(toZonedTime(2017, 10, 26))
                        .valueDate(toZonedTime(2017, 10, 27))
                        .remittanceInformationUnstructured("Example 4")
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("443.01"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .debtorName("Paul Simpson")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("NL76RABO0359400371")
                                .build())
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderAccountDTO createAccount2() {
        return ProviderAccountDTO.builder()
                .accountId("3dc3d5b3-7023-4848-9853-f5400a64e81g")
                .accountNumber(new ProviderAccountNumberDTO(IBAN, "DE2310010010123456788"))
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(new BigDecimal("200.00"))
                .currentBalance(new BigDecimal("200.00"))
                .currency(CurrencyCode.EUR)
                .name("Secondary Account")
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .transactions(List.of(
                        createAccount2Transaction1(),
                        createAccount2Transaction2()
                ))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("3dc3d5b3-7023-4848-9853-f5400a64e81g")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE2310010010123456788")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Secondary Account")
                        .cashAccountType(CURRENT)
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.CLOSING_BOOKED)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("200.00"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .referenceDate(toZonedTime(2017, 10, 25))
                                        .build()
                        ))
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount2Transaction1() {
        return ProviderTransactionDTO.builder()
                .externalId("1234567")
                .dateTime(toZonedTime(2017, 10, 25))
                .amount(new BigDecimal("256.67"))
                .status(BOOKED)
                .type(CREDIT)
                .description("Example 1")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .bookingDate(toZonedTime(2017, 10, 25))
                        .valueDate(toZonedTime(2017, 10, 26))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("256.67"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("John Miles")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE67100100101306118605")
                                .build())
                        .remittanceInformationUnstructured("Example 1")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount2Transaction2() {
        return ProviderTransactionDTO.builder()
                .externalId("1234568")
                .dateTime(toZonedTime(2017, 10, 25))
                .amount(new BigDecimal("343.01"))
                .status(BOOKED)
                .type(CREDIT)
                .description("Example 2")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .bookingDate(toZonedTime(2017, 10, 25))
                        .valueDate(toZonedTime(2017, 10, 26))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("343.01"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .debtorName("Paul Simpson")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("NL76RABO0359400371")
                                .build())
                        .remittanceInformationUnstructured("Example 2")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }


}