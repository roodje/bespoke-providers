package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow;

import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.StepState;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class InitialStateInspector implements StateInspector {
    private final ProcessStep processStep;

    @Override
    public Optional<ProcessStep> getProcessStep(StepState stepState) {
        if (stepState == null) {
            return Optional.of(processStep);
        }
        return Optional.empty();
    }
}
