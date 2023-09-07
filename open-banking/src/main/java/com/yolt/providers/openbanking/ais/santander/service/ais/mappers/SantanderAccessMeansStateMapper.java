package com.yolt.providers.openbanking.ais.santander.service.ais.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.santander.dto.SantanderAccessMeansV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
public class SantanderAccessMeansStateMapper extends DefaultAccessMeansStateMapper<AccessMeansState<SantanderAccessMeansV2>> {
    private final ObjectMapper objectMapper;
    private final TypeReference<AccessMeansState<SantanderAccessMeansV2>> type;

    public SantanderAccessMeansStateMapper(ObjectMapper objectMapper) {
        super(objectMapper);
        this.objectMapper = objectMapper;
        type = new TypeReference<>() {};
    }

    @Override
    public AccessMeansState<SantanderAccessMeansV2> fromJson(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, type);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize access means state");
        }
    }
}
