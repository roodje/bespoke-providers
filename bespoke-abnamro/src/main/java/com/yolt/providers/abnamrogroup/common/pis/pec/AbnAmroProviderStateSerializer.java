package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.abnamrogroup.common.pis.pec.exception.ProviderStateSerializationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AbnAmroProviderStateSerializer {

    private final ObjectMapper objectMapper;

    public String serialize(AbnAmroPaymentProviderState providerState) {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new ProviderStateSerializationException("Cannot serialize provider state", e);
        }
    }
}
