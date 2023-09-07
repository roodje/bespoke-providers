package com.yolt.providers.n26.common.service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.n26.common.dto.N26GroupProviderState;
import com.yolt.providers.n26.common.dto.token.TokenResponse;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
public class N26GroupProviderStateMapper {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AccessMeansDTO toAccessMeansDTO(AccessMeansDTO accessMeansDTO, N26GroupProviderState providerState, TokenResponse tokenResponse) {
        return toAccessMeansDTO(accessMeansDTO.getUserId(), providerState, tokenResponse);
    }

    public AccessMeansDTO toAccessMeansDTO(UUID userId, N26GroupProviderState providerState, TokenResponse tokenResponse) {
        return new AccessMeansDTO(userId, toJson(providerState), new Date(), Date.from(Instant.now(clock).plusSeconds(tokenResponse.getExpiresIn())));
    }

    public String toJson(N26GroupProviderState providerState) {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize provider state");
        }
    }

    public N26GroupProviderState fromAccessMeansDTO(AccessMeansDTO accessMeansDTO) throws TokenInvalidException {
        return fromJson(accessMeansDTO.getAccessMeans());
    }

    public N26GroupProviderState fromJson(String json) throws TokenInvalidException {
        try {
            return objectMapper.readValue(json, N26GroupProviderState.class);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Unable to deserialize provider state");
        }
    }
}
