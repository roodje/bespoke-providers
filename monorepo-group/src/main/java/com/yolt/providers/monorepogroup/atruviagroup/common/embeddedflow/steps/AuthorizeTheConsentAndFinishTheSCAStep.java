package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps;

import com.yolt.providers.monorepogroup.atruviagroup.common.dto.external.ScaStatus;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.AtruviaAccessMeans;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.ObtainChallengeOutcomeInputStep;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.AccessMeansStepOutcome;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.ProcessStep;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.ProcessStepData;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.StepOutcome;
import com.yolt.providers.monorepogroup.atruviagroup.common.exception.UnexpectedConsentStatusException;
import com.yolt.providers.monorepogroup.atruviagroup.common.http.AtruviaGroupHttpClient;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

import static com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.Constants.CHALLENGE_DATA_FIELD_ID;
import static com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.Constants.CONSENT_VALIDITY_IN_DAYS;

@RequiredArgsConstructor
public class AuthorizeTheConsentAndFinishTheSCAStep implements ProcessStep {

    @Override
    public StepOutcome getNextStep(ProcessStepData processStepData, Supplier<AtruviaGroupHttpClient> atruviaGroupHttpClientSupplier) {
        var state = (ObtainChallengeOutcomeInputStep) processStepData.getStepState();
        var encryptedAuthenticationData = processStepData.getFormValue(CHALLENGE_DATA_FIELD_ID);
        var authMeans = processStepData.getAtruviaGroupAuthenticationMeans();
        var decryptor = state.atruviaEncryptionData();
        var httpClient = atruviaGroupHttpClientSupplier.get();

        var scaStatusResponse = httpClient.putAuthenticationData(state.consentId(), state.authorisationId(), decryptor.decryptJwe(encryptedAuthenticationData), authMeans.getClientCertificate(), authMeans.getClientCertificateKey(), processStepData.getSigner());
        if (scaStatusResponse.getScaStatus() != ScaStatus.FINALISED) {
            throw new UnexpectedConsentStatusException("Not expected state exception during embedded flow!");
        }

        return new AccessMeansStepOutcome(new AtruviaAccessMeans(state.consentId(), state.selectedRegionalBankCode()), processStepData.getUserId(), CONSENT_VALIDITY_IN_DAYS);
    }

    @Override
    public Class<?> getAcceptedInputState() {
        return ObtainChallengeOutcomeInputStep.class;
    }
}
