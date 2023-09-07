package com.yolt.providers.monorepogroup.olbgroup.common.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.OlbGroupProviderState;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
public class OlbGroupProviderStateMapper {

    private final ObjectMapper objectMapper;

    public AccessMeansDTO toAccessMeansDTO(UUID userId,
                                           OlbGroupProviderState providerState,
                                           Date updated,
                                           Date expirationDate) {
        return new AccessMeansDTO(userId, toJson(providerState), updated, expirationDate);
    }

    public String toJson(OlbGroupProviderState providerState) {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize provider state");
        }
    }

    public OlbGroupProviderState fromJson(String json) {
        try {
            return objectMapper.readValue(json, OlbGroupProviderState.class);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to deserialize provider state");
        }
    }
}
