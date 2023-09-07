package com.yolt.providers.brdgroup.common.authorization;

import com.yolt.providers.brdgroup.common.dto.consent.GetConsentResponse;
import com.yolt.providers.brdgroup.common.http.BrdGroupHttpClient;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class BrdGroupConsentStatusValidatorV1 implements BrdGroupConsentStatusValidator {

    private final ScheduledExecutorService scheduledExecutorService;
    private final int consentStatusCheckTotalDelayLimitInSeconds;
    private final int initialDelay;

    private static final String CONSENT_STATUS_VALID = "valid";

    @Override
    public void validate(BrdGroupHttpClient httpClient, String consentId) {

        GetConsentResponse getConsentResponse;
        ScheduledFuture<GetConsentResponse> scheduledFuture;

        int numberOfTries = 0;
        int delayInSeconds = calculateNextDelay(numberOfTries, 0);
        int totalDelayInSeconds = delayInSeconds;

        Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();
        do {
            scheduledFuture = scheduledExecutorService.schedule(() -> {
                MDC.setContextMap(mdcContextMap);
                return httpClient.getConsentStatus(consentId);
            }, delayInSeconds, TimeUnit.SECONDS);
            numberOfTries++;
            delayInSeconds = calculateNextDelay(numberOfTries, delayInSeconds);
            totalDelayInSeconds += delayInSeconds;
            getConsentResponse = unwrapFutureResult(scheduledFuture);
        } while (shouldScheduleNextConsentCheck(getConsentResponse, totalDelayInSeconds));

        if (!isConsentStatusValid(getConsentResponse)) {
            throw new GetAccessTokenFailedException("Consent is not valid for getting data. Consent status: " + getConsentResponse.getConsentStatus());
        }
    }

    private boolean shouldScheduleNextConsentCheck(GetConsentResponse statusResponse,
                                                   int totalDelay) {
        return !isConsentStatusValid(statusResponse)
               && delayDoesNotExceedLimit(totalDelay);
    }

    private boolean delayDoesNotExceedLimit(int totalDelay) {
        return totalDelay <= consentStatusCheckTotalDelayLimitInSeconds;
    }

    private GetConsentResponse unwrapFutureResult(ScheduledFuture<GetConsentResponse> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GetAccessTokenFailedException(e);
        } catch (ExecutionException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    private boolean isConsentStatusValid(GetConsentResponse getConsentResponse) {
        return CONSENT_STATUS_VALID.equalsIgnoreCase(getConsentResponse.getConsentStatus());
    }

    private int calculateNextDelay(int attempt, int previousDelay) {
        if (attempt == 0) {
            return initialDelay;
        } else if (attempt == 1) {
            return 1;
        } else {
            return previousDelay * 2;
        }
    }
}
