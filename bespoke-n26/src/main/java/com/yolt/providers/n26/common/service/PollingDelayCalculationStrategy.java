package com.yolt.providers.n26.common.service;

public interface PollingDelayCalculationStrategy {
    int calculateNextDelay(int attempt, int previousDelay);
}
