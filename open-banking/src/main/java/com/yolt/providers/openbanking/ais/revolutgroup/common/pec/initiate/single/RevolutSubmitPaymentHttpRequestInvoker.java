package com.yolt.providers.openbanking.ais.revolutgroup.common.pec.initiate.single;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentHttpRequestInvoker;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.GenericPaymentRequestInvocationException;
import com.yolt.providers.openbanking.ais.generic2.pec.restclient.PisRestClient;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.GenericSubmitPaymentPreExecutionResult;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class RevolutSubmitPaymentHttpRequestInvoker implements PaymentHttpRequestInvoker<OBWriteDomestic2, GenericSubmitPaymentPreExecutionResult> {

    private static final String DOMESTIC_PAYMENTS_SUBMISSION_PATH = "/domestic-payments";

    private final HttpClientFactory httpClientFactory;
    private final PisRestClient restClient;
    private final ProviderIdentification providerIdentification;

    @Override
    public ResponseEntity<JsonNode> invokeRequest(HttpEntity<OBWriteDomestic2> httpEntity, GenericSubmitPaymentPreExecutionResult preExecutionResult) {
        var httpClient = httpClientFactory.createHttpClient(preExecutionResult.getRestTemplateManager(), preExecutionResult.getAuthMeans(), providerIdentification.getDisplayName());
        try {
            return restClient.submitPayment(httpClient, DOMESTIC_PAYMENTS_SUBMISSION_PATH, httpEntity);
        } catch (TokenInvalidException ex) {
            throw new GenericPaymentRequestInvocationException(ex);
        }
    }
}
