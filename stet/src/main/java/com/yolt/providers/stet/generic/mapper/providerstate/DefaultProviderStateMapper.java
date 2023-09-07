package com.yolt.providers.stet.generic.mapper.providerstate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.PaymentProviderState;
import com.yolt.providers.stet.generic.exception.ProviderStateMalformedException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class DefaultProviderStateMapper implements ProviderStateMapper {

    protected final ObjectMapper objectMapper;

    @Override
    public <T> String mapToJson(T providerState) {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new ProviderStateMalformedException("Unable to serialize provider state", e);
        }
    }

    @Override
    public DataProviderState mapToDataProviderState(String jsonProviderState) throws TokenInvalidException {
        try {
            return objectMapper.readValue(jsonProviderState, DataProviderState.class);
        } catch (JsonProcessingException e) {
            try {
                return fallbackMapToProviderState(jsonProviderState).orElseThrow(() -> e);
            } catch (JsonProcessingException ex) {
                throw new TokenInvalidException("Unable to deserialize data provider state. Maybe fallback should be provided?");
            }
        }
    }

    //TODO: Remove this fallback after successful migration of STET providers to generic
    protected Optional<DataProviderState> fallbackMapToProviderState(String jsonProviderState) throws JsonProcessingException { //NOSONAR It can be thrown during fallback mapping
        return Optional.empty();
    }

    /**
     * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
     * TODO: JIRA TICKET
     */
    @Deprecated
    @Override
    public PaymentProviderState mapToPaymentProviderState(String jsonProviderState) {
        try {
            return objectMapper.readValue(jsonProviderState, PaymentProviderState.class);
        } catch (JsonProcessingException e) {
            throw new ProviderStateMalformedException("Unable to deserialize payment provider state", e);
        }
    }
}
