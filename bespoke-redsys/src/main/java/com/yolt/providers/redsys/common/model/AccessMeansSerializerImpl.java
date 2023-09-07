package com.yolt.providers.redsys.common.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public class AccessMeansSerializerImpl implements AccessMeansSerializer {
    private final ObjectMapper mapper;

    @Override
    public String serialize(final RedsysAccessMeans redsysAccessMeans) {
        try {
            return mapper.writeValueAsString(redsysAccessMeans);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize access means.");
        }
    }

    @Override
    public RedsysAccessMeans deserialize(final String accessMean) throws TokenInvalidException {
        RedsysAccessMeans redsysAccessMeans;
        try {
            redsysAccessMeans = mapper.readValue(accessMean, RedsysAccessMeans.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize access means.");
        }
        return redsysAccessMeans;
    }
}
