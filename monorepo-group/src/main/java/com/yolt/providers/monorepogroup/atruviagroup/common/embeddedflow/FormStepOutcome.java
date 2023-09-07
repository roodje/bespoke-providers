package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow;

import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.StepState;
import lombok.Value;
import nl.ing.lovebird.providershared.form.Form;

@Value
public class FormStepOutcome extends StepOutcome {
    Form form;
    EncryptionDetails encryptionDetails;
    StepState stepState;
}
