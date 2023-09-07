package com.yolt.providers.openbanking.ais.hsbcgroup.common.model.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.model.HsbcGroupAccessMeansV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
public class HsbcAccessMeansStateMapper extends DefaultAccessMeansStateMapper<AccessMeansState<HsbcGroupAccessMeansV2>> {

    private final ObjectMapper objectMapper;
    private final TypeReference<AccessMeansState<HsbcGroupAccessMeansV2>> type;

    public HsbcAccessMeansStateMapper(ObjectMapper objectMapper) {
        super(objectMapper);
        this.objectMapper = objectMapper;
        type = new TypeReference<>() {
        };
    }

    @Override
    public AccessMeansState<HsbcGroupAccessMeansV2> fromJson(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, type);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize access means state");
        }
    }
}

