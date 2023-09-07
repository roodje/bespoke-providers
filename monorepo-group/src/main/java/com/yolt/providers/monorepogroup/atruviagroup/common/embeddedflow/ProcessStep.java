package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow;

import com.yolt.providers.monorepogroup.atruviagroup.common.http.AtruviaGroupHttpClient;

import java.util.function.Supplier;

public interface ProcessStep {

    StepOutcome getNextStep(ProcessStepData processStepData, Supplier<AtruviaGroupHttpClient> httpClientSupplier);

    default Class<?> getAcceptedInputState() {
        return null;
    }
}
