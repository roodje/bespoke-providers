package com.yolt.providers.deutschebank.common.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
public class DeutscheBankGroupProviderStateMapper {

    private final ObjectMapper objectMapper;

    public AccessMeansDTO toAccessMeansDTO(UUID userId, DeutscheBankGroupProviderState providerState, Date expirationDate) {
        return new AccessMeansDTO(userId, toJson(providerState), new Date(), expirationDate);
    }

    public String toJson(DeutscheBankGroupProviderState providerState) {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize provider state");
        }
    }

    public DeutscheBankGroupProviderState fromAccessMeansDTO(AccessMeansDTO accessMeansDTO) throws TokenInvalidException {
        return fromJson(accessMeansDTO.getAccessMeans());
    }

    public DeutscheBankGroupProviderState fromJson(String json) throws TokenInvalidException {
        try {
            return objectMapper.readValue(json, DeutscheBankGroupProviderState.class);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Unable to deserialize provider state");
        }
    }
}
