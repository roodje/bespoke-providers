package com.yolt.providers.openbanking.ais.virginmoney2group.common.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.model.VirginMoney2GroupAccessMeans;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
public class VirginMoney2GroupAccessMeansStateMapper extends DefaultAccessMeansStateMapper<AccessMeansState<VirginMoney2GroupAccessMeans>> {

    private final ObjectMapper objectMapper;
    private final TypeReference<AccessMeansState<VirginMoney2GroupAccessMeans>> type;

    public VirginMoney2GroupAccessMeansStateMapper(ObjectMapper objectMapper) {
        super(objectMapper);
        this.objectMapper = objectMapper;
        type = new TypeReference<>() {
        };
    }

    @Override
    public AccessMeansState<VirginMoney2GroupAccessMeans> fromJson(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, type);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize access means state");
        }
    }
}

