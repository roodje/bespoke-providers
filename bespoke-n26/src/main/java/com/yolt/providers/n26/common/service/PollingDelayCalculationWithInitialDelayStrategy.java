package com.yolt.providers.n26.common.service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PollingDelayCalculationWithInitialDelayStrategy implements PollingDelayCalculationStrategy {

    private final int initialDelay;

    @Override
    public int calculateNextDelay(int attempt, int previousDelay) {
        if (attempt == 0) {
            return initialDelay;
        } else if (attempt == 1) {
            return 1;
        } else {
            return previousDelay * 2;
        }
    }
}
