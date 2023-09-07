package com.yolt.providers.redsys.common.newgeneric;

import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.Step;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ConsentProcess<T extends SerializableConsentProcessData> {
    private final List<Action<T>> actionList;

    public Step start(ConsentProcessArguments<T> processArguments) {
        return actionList.get(0).run(processArguments).getStep();
    }

    public AccessMeansOrStepDTO continueFrom(ConsentProcessArguments<T> processArguments) {
        int stepNumber = processArguments.getConsentProcessData().getConsentStepNumber();
        final int nextStepNumber = stepNumber + 1;
        processArguments.getConsentProcessData().setConsentStepNumber(nextStepNumber);
        return actionList.get(nextStepNumber).run(processArguments);
    }
}