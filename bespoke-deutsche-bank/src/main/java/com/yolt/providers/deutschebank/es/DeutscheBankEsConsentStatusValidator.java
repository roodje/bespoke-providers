package com.yolt.providers.deutschebank.es;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.deutschebank.common.domain.model.consent.ConsentStatusResponse;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClient;
import com.yolt.providers.deutschebank.common.service.authorization.DeutscheBankGroupAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class DeutscheBankEsConsentStatusValidator {

    private final DeutscheBankGroupAuthorizationService authorizationService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final int consentStatusCheckTotalDelayLimitInSeconds;
    private final int initialDelay;

    private static final String CONSENT_STATUS_VALID = "valid";

    public void validate(DeutscheBankGroupHttpClient httpClient, String consentId, String psuIpAddress) {

        ConsentStatusResponse consentStatusResponse;
        ScheduledFuture<ConsentStatusResponse> scheduledFuture;

        int numberOfTries = 0;
        int delayInSeconds = calculateNextDelay(numberOfTries, 0);
        int totalDelayInSeconds = delayInSeconds;

        Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();
        do {
            scheduledFuture = scheduledExecutorService.schedule(() -> {
                try {
                    MDC.setContextMap(mdcContextMap);
                    return authorizationService.getConsentStatus(httpClient, consentId, psuIpAddress);
                } catch (TokenInvalidException e) {
                    throw new GetAccessTokenFailedException("Something went wrong while verifying consent status");
                }
            }, delayInSeconds, TimeUnit.SECONDS);
            numberOfTries++;
            delayInSeconds = calculateNextDelay(numberOfTries, delayInSeconds);
            totalDelayInSeconds += delayInSeconds;
            consentStatusResponse = unwrapFutureResult(scheduledFuture);
        } while (shouldScheduleNextConsentCheck(consentStatusResponse, totalDelayInSeconds));

        if (!isConsentStatusValid(consentStatusResponse)) {
            throw new GetAccessTokenFailedException("Consent is not valid for getting data. Consent status: " + consentStatusResponse.getConsentStatus());
        }
    }

    private boolean shouldScheduleNextConsentCheck(ConsentStatusResponse statusResponse,
                                                   int totalDelay) {
        return !isConsentStatusValid(statusResponse)
               && delayDoesNotExceedLimit(totalDelay);
    }

    private boolean delayDoesNotExceedLimit(int totalDelay) {
        return totalDelay <= consentStatusCheckTotalDelayLimitInSeconds;
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

    private boolean isConsentStatusValid(ConsentStatusResponse consentStatusResponse) {
        return CONSENT_STATUS_VALID.equalsIgnoreCase(consentStatusResponse.getConsentStatus());
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
