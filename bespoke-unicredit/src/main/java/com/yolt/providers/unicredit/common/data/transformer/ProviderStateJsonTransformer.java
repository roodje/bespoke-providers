package com.yolt.providers.unicredit.common.data.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.unicredit.common.dto.UniCreditAccessMeansDTO;
import com.yolt.providers.unicredit.common.exception.UniCreditMalformedException;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class ProviderStateJsonTransformer implements ProviderStateTransformer<UniCreditAccessMeansDTO> {

    private final ObjectMapper objectMapper;

    @Override
    public String transformToString(final UniCreditAccessMeansDTO providerState) {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new UniCreditMalformedException("Error while creating provider state object");
        }
    }

    @Override
    public UniCreditAccessMeansDTO transformToObject(final String providerState) {
        try {
            return objectMapper.readValue(providerState, UniCreditAccessMeansDTO.class);
        } catch (IOException e) {
            throw new UniCreditMalformedException("Error while creating access means object");
        }
    }
}
