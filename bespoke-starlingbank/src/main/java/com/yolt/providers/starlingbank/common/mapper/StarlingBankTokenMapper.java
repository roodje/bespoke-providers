package com.yolt.providers.starlingbank.common.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.starlingbank.common.model.domain.Token;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * The purpose of this class is two-way conversion between the Token as "provider state" and the Access Means
 */
@AllArgsConstructor
public class StarlingBankTokenMapper {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AccessMeansDTO mapToAccessMeansDTO(UUID userId, Token token) {
        return new AccessMeansDTO(userId, serializeToJson(token), Date.from(Instant.now(clock)), calculateExpiryDate(token.getExpiresIn()));
    }

    private String serializeToJson(Token token) {
        try {
            return objectMapper.writeValueAsString(token);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize token");
        }
    }

    private Date calculateExpiryDate(long expiresInSeconds) {
        return Date.from(Instant.now(clock).plusSeconds(expiresInSeconds));
    }

    public Token mapToToken(AccessMeansDTO accessMeansDTO) throws TokenInvalidException {
        return deserializeFromJson(accessMeansDTO.getAccessMeans());
    }

    private Token deserializeFromJson(String json) throws TokenInvalidException {
        try {
            return objectMapper.readValue(json, Token.class);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Unable to deserialize token");
        }
    }
}
