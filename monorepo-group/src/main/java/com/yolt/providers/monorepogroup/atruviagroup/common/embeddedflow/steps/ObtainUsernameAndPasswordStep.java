package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps;

import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.AtruviaFormDecryptor;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.CreateConsentAndObtainScaMethodInputStep;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.ObtainUserNameAndPasswordInputStep;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.*;
import com.yolt.providers.monorepogroup.atruviagroup.common.http.AtruviaGroupHttpClient;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.PasswordField;
import nl.ing.lovebird.providershared.form.TextField;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.function.Supplier;

import static com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.Constants.*;
import static com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.EncryptionDetailsHelper.*;

@RequiredArgsConstructor
public class ObtainUsernameAndPasswordStep implements ProcessStep {
    private final KeyPairProvider keyPairProvider;

    public StepOutcome getNextStep(ProcessStepData processStepData, Supplier<AtruviaGroupHttpClient> httpClientSupplier) {
        TextField usernameTextField = new TextField(USERNAME_FIELD_ID, USERNAME_FIELD_DISPLAY_NAME, 0, 4096, false, false);
        PasswordField passwordTextField = new PasswordField(PASSWORD_FIELD_ID, PASSWORD_FIELD_DISPLAY_NAME, 0, 4096, false, ".*");
        Form selectForm = new Form(List.of(usernameTextField, passwordTextField), null, null);
        var keyPair = keyPairProvider.generateKeypair();
        var aPublic = (RSAPublicKey) keyPair.getPublic();
        var aPrivate = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        var encryptionDetails = createEncryptionDetails(aPublic);
        var nextStep = new CreateConsentAndObtainScaMethodInputStep(processStepData.getFormValue(REGION_FIELD_ID), new AtruviaFormDecryptor(ALGORITHM, ENCRYPTION_METHOD, aPrivate));
        return new FormStepOutcome(selectForm, encryptionDetails, nextStep);
    }

    @Override
    public Class<?> getAcceptedInputState() {
        return ObtainUserNameAndPasswordInputStep.class;
    }
}
