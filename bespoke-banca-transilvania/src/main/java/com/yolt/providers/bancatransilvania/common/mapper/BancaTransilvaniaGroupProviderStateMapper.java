package com.yolt.providers.bancatransilvania.common.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bancatransilvania.common.domain.BancaTransilvaniaGroupProviderState;
import com.yolt.providers.bancatransilvania.common.domain.model.token.TokenResponse;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.time.Clock;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
public class BancaTransilvaniaGroupProviderStateMapper {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AccessMeansDTO toAccessMeansDTO(AccessMeansDTO accessMeansDTO, BancaTransilvaniaGroupProviderState providerState, TokenResponse tokenResponse) {
        return toAccessMeansDTO(accessMeansDTO.getUserId(), providerState, tokenResponse);
    }

    public AccessMeansDTO toAccessMeansDTO(UUID userId, BancaTransilvaniaGroupProviderState providerState, TokenResponse tokenResponse) {
        return new AccessMeansDTO(userId, toJson(providerState), new Date(), tokenResponse.getExpirationDate(clock));
    }

    public String toJson(BancaTransilvaniaGroupProviderState providerState) {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize provider state");
        }
    }

    public BancaTransilvaniaGroupProviderState fromAccessMeansDTO(AccessMeansDTO accessMeansDTO) throws TokenInvalidException {
        return fromJson(accessMeansDTO.getAccessMeans());
    }

    public BancaTransilvaniaGroupProviderState fromJson(String json) throws TokenInvalidException {
        try {
            return objectMapper.readValue(json, BancaTransilvaniaGroupProviderState.class);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Unable to deserialize provider state");
        }
    }
}
