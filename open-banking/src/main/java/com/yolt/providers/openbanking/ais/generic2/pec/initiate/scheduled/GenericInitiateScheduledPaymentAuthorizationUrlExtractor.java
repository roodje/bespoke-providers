package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsentResponse5;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class GenericInitiateScheduledPaymentAuthorizationUrlExtractor implements PaymentAuthorizationUrlExtractor<OBWriteDomesticScheduledConsentResponse5, GenericInitiateScheduledPaymentPreExecutionResult> {

    private final TokenScope scope;
    private final AuthenticationService authenticationService;

    @SneakyThrows
    @Override
    public String extractAuthorizationUrl(OBWriteDomesticScheduledConsentResponse5 httpResponseBody, GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult) {
        var resourceId = preExecutionResult.getExternalPaymentId();
        if (StringUtils.isEmpty(resourceId)) {
            resourceId = httpResponseBody.getData().getConsentId();
        }
        return authenticationService.generateAuthorizationUrl(preExecutionResult.getAuthMeans(),
                resourceId,
                preExecutionResult.getState(),
                preExecutionResult.getBaseClientRedirectUrl(),
                scope,
                preExecutionResult.getSigner());
    }
}
