package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow;

import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.StepState;

import java.util.Optional;

public interface StateInspector {

    Optional<ProcessStep> getProcessStep(StepState stepState);

    default StateInspector orElse(StateInspector next) {
        return new ChainedStateInspector(this, next);
    }
}