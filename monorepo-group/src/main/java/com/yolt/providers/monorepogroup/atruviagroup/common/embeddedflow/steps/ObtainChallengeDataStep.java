package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps;

import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.AtruviaFormDecryptor;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.ObtainChallengeDataInputStep;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.ObtainChallengeOutcomeInputStep;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.*;
import com.yolt.providers.monorepogroup.atruviagroup.common.http.AtruviaGroupHttpClient;
import lombok.RequiredArgsConstructor;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.function.Supplier;

import static com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.ChallengeFormHelper.mapChallengeDataToForm;
import static com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.Constants.SCA_METHOD_FIELD_ID;
import static com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.EncryptionDetailsHelper.*;

@RequiredArgsConstructor
public class ObtainChallengeDataStep implements ProcessStep {

    private final KeyPairProvider keyPairProvider;

    @Override
    public StepOutcome getNextStep(ProcessStepData processStepData, Supplier<AtruviaGroupHttpClient> httpClientSupplier) {
        var state = (ObtainChallengeDataInputStep) processStepData.getStepState();
        var scaMethod = processStepData.getFormValue(SCA_METHOD_FIELD_ID);
        var authMeans = processStepData.getAtruviaGroupAuthenticationMeans();
        var httpClient = httpClientSupplier.get();
        var selectPsuAuthenticationMethodResponse = httpClient.selectSCAForConsentAndAuthorisation(state.consentId(), state.authorisationId(), scaMethod, authMeans.getClientCertificate(), authMeans.getClientCertificateKey(), processStepData.getSigner());
        var form = mapChallengeDataToForm(selectPsuAuthenticationMethodResponse.getChallengeData(), selectPsuAuthenticationMethodResponse.getChosenScaMethod());
        var keyPair = keyPairProvider.generateKeypair();
        var aPublic = (RSAPublicKey) keyPair.getPublic();
        var aPrivate = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        var nextStep = new ObtainChallengeOutcomeInputStep(state.selectedRegionalBankCode(), state.username(), state.consentId(), state.authorisationId(), new AtruviaFormDecryptor(ALGORITHM, ENCRYPTION_METHOD, aPrivate));
        return new FormStepOutcome(form, createEncryptionDetails(aPublic), nextStep);
    }

    @Override
    public Class<?> getAcceptedInputState() {
        return ObtainChallengeDataInputStep.class;
    }
}
