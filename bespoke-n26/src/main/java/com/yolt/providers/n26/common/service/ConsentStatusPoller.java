package com.yolt.providers.n26.common.service;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.n26.common.dto.N26GroupProviderState;
import com.yolt.providers.n26.common.dto.ais.consent.ConsentStatusResponse;
import com.yolt.providers.n26.common.http.N26GroupHttpClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ConsentStatusPoller {

    private static final String VALID_CONSENT_STATUS = "valid";

    private final ScheduledExecutorService scheduledExecutorService;
    private final PollingDelayCalculationStrategy pollingDelayCalculationStrategy;
    private final int consentStatusCheckTotalDelaySecondsLimit;

    public void pollForConsentStatus(N26GroupHttpClient httpClient,
                                     N26GroupProviderState providerState) {

        Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();
        ConsentStatusResponse consentStatus;
        ScheduledFuture<ConsentStatusResponse> scheduledFuture;

        int attempt = 0;
        int delay = pollingDelayCalculationStrategy.calculateNextDelay(attempt, 0);
        int totalDelay = delay;
        do {
            scheduledFuture = scheduledExecutorService.schedule(() -> {
                try {
                    MDC.setContextMap(mdcContextMap);
                    return getConsentStatus(httpClient, providerState);
                } catch (TokenInvalidException e) {
                    throw new GetAccessTokenFailedException("Something went wrong on getting consent status verification");
                }
            }, delay, TimeUnit.SECONDS);
            attempt++;
            delay = pollingDelayCalculationStrategy.calculateNextDelay(attempt, delay);
            totalDelay += delay;
            consentStatus = unwrapFutureResult(scheduledFuture);
        } while (shouldScheduleNextConsentCheck(consentStatus, totalDelay));

        if (!isConsentStatusValid(consentStatus)) {
            throw new GetAccessTokenFailedException("Consent is not valid for getting data. Consent status: " + consentStatus.getConsentStatus());
        }
    }

    private boolean shouldScheduleNextConsentCheck(ConsentStatusResponse statusResponse,
                                                   int totalDelay) {
        return !isConsentStatusValid(statusResponse)
                && delayDoesNotExceedLimit(totalDelay);
    }

    private boolean delayDoesNotExceedLimit(int totalDelay) {
        return totalDelay <= consentStatusCheckTotalDelaySecondsLimit;
    }

    private ConsentStatusResponse unwrapFutureResult(ScheduledFuture<ConsentStatusResponse> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GetAccessTokenFailedException(e);
        } catch (ExecutionException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    private ConsentStatusResponse getConsentStatus(N26GroupHttpClient httpClient,
                                                   N26GroupProviderState providerState) throws TokenInvalidException {
        try {
            return httpClient.getConsentStatus(providerState);
        } catch (HttpStatusCodeException e) {
            throw new GetAccessTokenFailedException("Something went wrong on getting consent status verification: HTTP " + e.getStatusCode());
        }
    }

    private boolean isConsentStatusValid(ConsentStatusResponse consentStatusResponse) {
        return VALID_CONSENT_STATUS.equalsIgnoreCase(consentStatusResponse.getConsentStatus());
    }
}
