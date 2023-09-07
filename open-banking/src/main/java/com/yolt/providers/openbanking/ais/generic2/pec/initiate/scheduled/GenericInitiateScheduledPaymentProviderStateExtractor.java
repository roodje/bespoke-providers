package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedDataInitiationException;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsentResponse5;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericInitiateScheduledPaymentProviderStateExtractor implements UkPaymentProviderStateExtractor<OBWriteDomesticScheduledConsentResponse5, GenericInitiateScheduledPaymentPreExecutionResult> {

    private final ObjectMapper objectMapper;

    @Override
    public UkProviderState extractUkProviderState(OBWriteDomesticScheduledConsentResponse5 httpResponseBody, GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult) {
        return new UkProviderState(httpResponseBody.getData().getConsentId(),
                PaymentType.SCHEDULED,
                serializeDataInitiation(httpResponseBody.getData().getInitiation()));
    }

    private String serializeDataInitiation(OBWriteDomesticScheduled2DataInitiation dataInitiation) {
        try {
            return objectMapper.writeValueAsString(dataInitiation);
        } catch (JsonProcessingException ex) {
            throw new MalformedDataInitiationException("Data initiation object cannot be parsed into JSON");
        }
    }
}
