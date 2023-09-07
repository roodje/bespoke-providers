package com.yolt.providers.openbanking.ais.generic2.pec.status.single;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedDataInitiationException;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericPaymentStatusProviderStateExtractor implements UkPaymentProviderStateExtractor<PaymentStatusResponse, GenericPaymentStatusPreExecutionResult> {

    private final ObjectMapper objectMapper;

    @Override
    public UkProviderState extractUkProviderState(PaymentStatusResponse httpResponseBody, GenericPaymentStatusPreExecutionResult preExecutionResult) {
        return new UkProviderState(preExecutionResult.getConsentId(),
                PaymentType.SINGLE,
                serializeStatusData(httpResponseBody.getData()));
    }

    private String serializeStatusData(PaymentStatusResponse.Data statusData) {
        try {
            return objectMapper.writeValueAsString(statusData);
        } catch (JsonProcessingException ex) {
            throw new MalformedDataInitiationException("Data initiation object cannot be parsed into JSON");
        }
    }
}
