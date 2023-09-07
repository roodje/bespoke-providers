package com.yolt.providers.unicredit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.unicredit.common.dto.UniCreditAccessMeansDTO;
import com.yolt.providers.unicredit.common.exception.UniCreditMalformedException;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor(staticName = "with")
public class AccessMeansTestMapper {

    private final ObjectMapper objectMapper;

    public String compactAccessMeans(final UniCreditAccessMeansDTO accessMeansDTO) {
        try {
            return objectMapper.writeValueAsString(accessMeansDTO);
        } catch (JsonProcessingException e) {
            throw new UniCreditMalformedException("Error creating json access means");
        }
    }

    public UniCreditAccessMeansDTO retrieveAccessMeans(final String providerState) {
        try {
            return objectMapper.readValue(providerState, UniCreditAccessMeansDTO.class);
        } catch (IOException e) {
            throw new UniCreditMalformedException("Error reading Unicredit Access Means");
        }
    }

}
