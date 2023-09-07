package com.yolt.providers.monorepogroup.chebancagroup.common.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.ChaBancaGroupAccessMeans;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
public class CheBancaGroupTokenMapper {

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final ZoneId zoneId;

    public ChaBancaGroupAccessMeans mapToToken(final AccessMeansDTO accessMeansDTO) throws TokenInvalidException {
        return deserializeFromJson(accessMeansDTO.getAccessMeans());
    }

    private ChaBancaGroupAccessMeans deserializeFromJson(final String json) throws TokenInvalidException {
        try {
            return objectMapper.readValue(json, ChaBancaGroupAccessMeans.class);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Unable to deserialize accessMeans");
        }
    }

    public AccessMeansDTO mapToAccessMeans(final UUID userId, final ChaBancaGroupAccessMeans accessMeans) {
        return new AccessMeansDTO(userId,
                serializeToJson(accessMeans),
                Date.from(LocalDate.now(clock).atStartOfDay(zoneId).toInstant()),
                calculateTokenValidityDate(accessMeans));
    }

    private Date calculateTokenValidityDate(final ChaBancaGroupAccessMeans accessMeans) {
        return Date.from(LocalDate.now(clock).atStartOfDay(zoneId).toInstant()
                .plusSeconds(accessMeans.getTokenValidityTimeInSeconds()));
    }

    private String serializeToJson(final ChaBancaGroupAccessMeans token) {
        try {
            return objectMapper.writeValueAsString(token);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize token");
        }
    }
}
