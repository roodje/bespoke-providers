package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps;

import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.monorepogroup.atruviagroup.common.AtruviaGroupProperties;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.ObtainUserNameAndPasswordInputStep;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.FormStepOutcome;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.ProcessStep;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.ProcessStepData;
import com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.StepOutcome;
import com.yolt.providers.monorepogroup.atruviagroup.common.http.AtruviaGroupHttpClient;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.SelectField;
import nl.ing.lovebird.providershared.form.SelectOptionValue;

import java.util.Collections;
import java.util.function.Supplier;

import static com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.Constants.REGION_FIELD_DISPLAY_NAME;
import static com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow.steps.Constants.REGION_FIELD_ID;
import static java.util.Comparator.comparing;

@RequiredArgsConstructor
public class ObtainRegionalBankSelectionStep implements ProcessStep {

    private final AtruviaGroupProperties atruviaGroupProperties;

    public StepOutcome getNextStep(ProcessStepData processStepData, Supplier<AtruviaGroupHttpClient> httpClientSupplier) {
        SelectField selectField = new SelectField(REGION_FIELD_ID, REGION_FIELD_DISPLAY_NAME, 0, 100, false, true);
        atruviaGroupProperties.getRegionalBankList().stream()
                .sorted(comparing(AtruviaGroupProperties.RegionalBank::getName))
                .map(region -> new SelectOptionValue(region.getCode(), region.getName()))
                .forEachOrdered(selectField::addSelectOptionValue);

        Form selectForm = new Form(Collections.singletonList(selectField), null, null);
        return new FormStepOutcome(selectForm, EncryptionDetails.noEncryption(), new ObtainUserNameAndPasswordInputStep());
    }
}
