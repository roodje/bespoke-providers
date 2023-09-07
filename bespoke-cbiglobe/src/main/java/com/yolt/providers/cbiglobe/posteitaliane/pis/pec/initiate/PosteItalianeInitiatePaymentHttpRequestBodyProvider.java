package com.yolt.providers.cbiglobe.posteitaliane.pis.pec.initiate;

import com.yolt.providers.cbiglobe.common.exception.PaymentFailedException;
import com.yolt.providers.cbiglobe.common.model.InitiatePaymentRequest;
import com.yolt.providers.cbiglobe.common.pis.pec.initiate.CbiGlobeInitiatePaymentHttpRequestBodyProvider;
import com.yolt.providers.cbiglobe.common.pis.pec.initiate.CbiGlobeSepaInitiatePreExecutionResult;
import com.yolt.providers.cbiglobe.posteitaliane.model.PosteItalianeInitiatePaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.sepa.DynamicFields;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;

import java.util.Optional;

public class PosteItalianeInitiatePaymentHttpRequestBodyProvider
        implements PaymentExecutionHttpRequestBodyProvider<CbiGlobeSepaInitiatePreExecutionResult, InitiatePaymentRequest> {

    private CbiGlobeInitiatePaymentHttpRequestBodyProvider cbiGlobeInitiatePaymentHttpRequestBodyProvider =
            new CbiGlobeInitiatePaymentHttpRequestBodyProvider(
                    new PosteItalianeAccountToCurrencyMapper(),
                    new PosteItalianeInstructedAmountToCurrencyMapper()
            );

    @Override
    public PosteItalianeInitiatePaymentRequest provideHttpRequestBody(CbiGlobeSepaInitiatePreExecutionResult preExecutionResult) {
        var requestDTO = preExecutionResult.getRequestDTO();
        var response = new PosteItalianeInitiatePaymentRequest(cbiGlobeInitiatePaymentHttpRequestBodyProvider
                .provideHttpRequestBody(preExecutionResult));
        response.setCreditorAddress(toCreditorAddress(requestDTO));
        return response;
    }

    private PosteItalianeInitiatePaymentRequest.CreditorAddress toCreditorAddress(SepaInitiatePaymentRequestDTO request) {
        var creditorPostalCode = Optional.ofNullable(request.getDynamicFields())
                .map(DynamicFields::getCreditorPostalCountry)
                .orElseThrow(() -> new PaymentFailedException("Missing creditor postal country within the request"));

        return new PosteItalianeInitiatePaymentRequest.CreditorAddress(creditorPostalCode);
    }
}
