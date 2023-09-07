package com.yolt.providers.openbanking.ais.generic2.pec.submit.scheduled;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedDataInitiationException;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.GenericSubmitPaymentPreExecutionResult;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericSubmitScheduledPaymentProviderStateExtractor implements UkPaymentProviderStateExtractor<OBWriteDomesticScheduledResponse5, GenericSubmitPaymentPreExecutionResult> {

    private final ObjectMapper objectMapper;

    @Override
    public UkProviderState extractUkProviderState(OBWriteDomesticScheduledResponse5 httpResponseBody, GenericSubmitPaymentPreExecutionResult preExecutionResult) {
        return new UkProviderState(httpResponseBody.getData().getConsentId(),
                PaymentType.SCHEDULED,
                serializeSubmitData(httpResponseBody.getData().getInitiation()));
    }

    private String serializeSubmitData(OBWriteDomesticScheduled2DataInitiation dataInitiation) {
        try {
            return objectMapper.writeValueAsString(dataInitiation);
        } catch (JsonProcessingException ex) {
            throw new MalformedDataInitiationException("Data initiation object cannot be parsed into JSON");
        }
    }
}
