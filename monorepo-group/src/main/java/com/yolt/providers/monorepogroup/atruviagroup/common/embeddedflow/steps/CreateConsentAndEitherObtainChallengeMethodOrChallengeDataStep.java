package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps;

import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.external.ConsentStatus;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.external.ScaStatus;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.AtruviaFormDecryptor;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.CreateConsentAndObtainScaMethodInputStep;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.ObtainChallengeDataInputStep;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.ObtainChallengeOutcomeInputStep;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.*;
import com.yolt.providers.monorepogroup.atruviagroup.common.exception.UnexpectedConsentStatusException;
import com.yolt.providers.monorepogroup.atruviagroup.common.http.AtruviaGroupHttpClient;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.SelectField;
import nl.ing.lovebird.providershared.form.SelectOptionValue;

import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.function.Supplier;

import static com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.ChallengeFormHelper.mapChallengeDataToForm;
import static com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.Constants.*;
import static com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.EncryptionDetailsHelper.*;

@RequiredArgsConstructor
public class CreateConsentAndEitherObtainChallengeMethodOrChallengeDataStep implements ProcessStep {

    private final KeyPairProvider keyPairProvider;
    private final Clock clock;

    @Override
    public StepOutcome getNextStep(ProcessStepData processStepData, Supplier<AtruviaGroupHttpClient> atruviaGroupHttpClientSupplier) {
        var state = (CreateConsentAndObtainScaMethodInputStep) processStepData.getStepState();
        var encryptedPasswordValue = processStepData.getFormValue(PASSWORD_FIELD_ID);
        var encryptedPsuIdValue = processStepData.getFormValue(USERNAME_FIELD_ID);
        var psuIpAddress = processStepData.getPsuIpAddress();
        var authMeans = processStepData.getAtruviaGroupAuthenticationMeans();
        var signer = processStepData.getSigner();
        var decryptor = state.atruviaEncryptionData();
        var username = decryptor.decryptJwe(encryptedPsuIdValue);
        var httpClient = atruviaGroupHttpClientSupplier.get();
        var validityDate = LocalDate.now(clock).plusDays(CONSENT_VALIDITY_IN_DAYS);

        var consentForAllAccounts = httpClient.createConsentForAllAccounts(validityDate, username, psuIpAddress, authMeans.getClientCertificate(), authMeans.getClientCertificateKey(), signer);
        if (consentForAllAccounts.getConsentStatus() != ConsentStatus.RECEIVED) {
            throw new GetAccessTokenFailedException("Couldn't obtain ConsentStatus. Obtained status: " + consentForAllAccounts.getConsentStatus());
        }
        var consentId = consentForAllAccounts.getConsentId();
        var consentAuthorisation = httpClient.createAuthorisationForConsent(consentId, username, decryptor.decryptJwe(encryptedPasswordValue), authMeans.getClientCertificate(), authMeans.getClientCertificateKey(), signer);

        if (consentAuthorisation.getScaStatus() == ScaStatus.PSUAUTHENTICATED) {
            var scaMethods = consentAuthorisation.getScaMethods();
            var selectField = new SelectField(SCA_METHOD_FIELD_ID, SCA_METHOD_DISPLAY_NAME, 0, 100, false, false);
            scaMethods.forEach((method) -> selectField.addSelectOptionValue(new SelectOptionValue(method.getAuthenticationMethodId(), method.getName())));
            var scaMethodSelectionForm = new Form();
            scaMethodSelectionForm.setFormComponents(List.of(selectField));
            var nextStep = new ObtainChallengeDataInputStep(state.selectedRegionalBankCode(), username, consentId, consentAuthorisation.getAuthorisationId());
            return new FormStepOutcome(scaMethodSelectionForm, EncryptionDetails.noEncryption(), nextStep);
        } else if (consentAuthorisation.getScaStatus() == ScaStatus.STARTED) {
            var keyPair = keyPairProvider.generateKeypair();
            var aPublic = (RSAPublicKey) keyPair.getPublic();
            var aPrivate = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            var form = mapChallengeDataToForm(consentAuthorisation.getChallengeData(), consentAuthorisation.getChosenScaMethod());
            var nextStep = new ObtainChallengeOutcomeInputStep(state.selectedRegionalBankCode(), username, consentId, consentAuthorisation.getAuthorisationId(), new AtruviaFormDecryptor(ALGORITHM, ENCRYPTION_METHOD, aPrivate));
            return new FormStepOutcome(form, createEncryptionDetails(aPublic), nextStep);
        }
        throw new UnexpectedConsentStatusException("Not expected state exception during embedded flow!");
    }

    @Override
    public Class<?> getAcceptedInputState() {
        return CreateConsentAndObtainScaMethodInputStep.class;
    }
}
