package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow;

import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.StepState;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
class ChainedStateInspector implements StateInspector {

    private final StateInspector current;
    private final StateInspector next;

    @Override
    public Optional<ProcessStep> getProcessStep(StepState stepState) {
        return current
                .getProcessStep(stepState)
                .or(() -> next.getProcessStep(stepState));
    }
}
