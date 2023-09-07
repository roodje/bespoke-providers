package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow;

import com.yolt.providers.monorepogroup.atruviagroup.common.AtruviaGroupProperties;
import com.yolt.providers.monorepogroup.atruviagroup.common.exception.ProviderStateMalformedException;
import com.yolt.providers.monorepogroup.atruviagroup.common.http.AtruviaGroupHttpClient;
import com.yolt.providers.monorepogroup.atruviagroup.common.http.AtruviaGroupHttpClientFactoryV1;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class AtruviaEmbeddedFlowProcess {

    private final AtruviaGroupProperties atruviaGroupProperties;
    private final AtruviaGroupHttpClientFactoryV1 httpClientFactory;
    private final String providerKey;

    private final StateInspector stateInspector;

    public StepOutcome initiateProcess() {
        var processStep = stateInspector.getProcessStep(null)
                .orElseThrow(() -> new ProviderStateMalformedException("Couldn't find step!"));
        return processStep.getNextStep(null, createDummyHttpClientSupplier());
    }

    public StepOutcome getNextStep(ProcessStepData processStepData) {
        var httpClientSupplier = createAtruviaHttpClientSupplier(processStepData);
        var processStep = stateInspector.getProcessStep(processStepData.getStepState())
                .orElseThrow(() -> new ProviderStateMalformedException("Couldn't find step!"));
        return processStep.getNextStep(processStepData, httpClientSupplier);
    }

    private Supplier<AtruviaGroupHttpClient> createDummyHttpClientSupplier() {
        return () -> {
            throw new IllegalStateException("Cannot create RestTemplate without bank selected");
        };
    }

    private Supplier<AtruviaGroupHttpClient> createAtruviaHttpClientSupplier(ProcessStepData processStepData) {
        return () -> httpClientFactory.createHttpClient(
                processStepData.getRestTemplateManager(),
                providerKey,
                atruviaGroupProperties.getBaseUrlByRegionalBankCode(processStepData.getStepState().selectedRegionalBankCode()));
    }
}
