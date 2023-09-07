package com.yolt.providers.abnamrogroup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
public class AbnAmroAccessTokenMapper {

    private final ObjectMapper mapper;
    private final Clock clock;

    public AccessMeansDTO accessMeansFromToken(final UUID userId, final AccessTokenResponseDTO accessToken) throws TokenInvalidException {
        try {
            // Convert time to live to expiry date
            Date expiryDate = Date.from(Instant.now(clock).plusSeconds(accessToken.getExpiresIn()));
            String accessTokenValue = mapper.writeValueAsString(accessToken);
            return new AccessMeansDTO(userId, accessTokenValue, new Date(), expiryDate);
        } catch (IOException e) {
            throw new TokenInvalidException("Failed to write OAuth2AccessToken to String");
        }
    }

    public AccessTokenResponseDTO tokenFromAccessMeans(final String accessMeans) throws TokenInvalidException {
        try {
            return mapper.readValue(accessMeans, AccessTokenResponseDTO.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Failed to read OAuth2AccessToken from supplied accessMeans");
        }
    }
}
