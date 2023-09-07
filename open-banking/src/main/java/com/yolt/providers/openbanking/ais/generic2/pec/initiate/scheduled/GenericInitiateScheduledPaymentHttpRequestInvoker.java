package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.openbanking.ais.generic2.common.EndpointsVersionable;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.GenericPaymentRequestInvocationException;
import com.yolt.providers.openbanking.ais.generic2.pec.restclient.PisRestClient;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsent4;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class GenericInitiateScheduledPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<OBWriteDomesticScheduledConsent4, GenericInitiateScheduledPaymentPreExecutionResult> {

    private static final String DOMESTIC_SCHEDULED_PAYMENTS_PATH = "/pisp/domestic-scheduled-payment-consents";

    private final PisRestClient restClient;
    private final HttpClientFactory httpClientFactory;
    private final EndpointsVersionable endpointsVersionable;
    private final ProviderIdentification providerIdentification;

    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<OBWriteDomesticScheduledConsent4> httpEntity, GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult) {
        var httpClient = httpClientFactory.createHttpClient(preExecutionResult.getRestTemplateManager(), preExecutionResult.getAuthMeans(), providerIdentification.getDisplayName());
        try {
            return restClient.createPayment(httpClient, endpointsVersionable.getAdjustedUrlPath(DOMESTIC_SCHEDULED_PAYMENTS_PATH), httpEntity);
        } catch (TokenInvalidException ex) {
            throw new GenericPaymentRequestInvocationException(ex);
        }
    }
}
