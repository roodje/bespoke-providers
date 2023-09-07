package com.yolt.providers.stet.societegeneralegroup.common.mapper.providerstate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.mapper.providerstate.DefaultProviderStateMapper;
import com.yolt.providers.stet.societegeneralegroup.common.dto.AccessTokenResponseDTO;

import java.util.Optional;

public class SocieteGeneraleGroupStateMapper extends DefaultProviderStateMapper {

    private final DefaultProperties properties;

    public SocieteGeneraleGroupStateMapper(ObjectMapper objectMapper, DefaultProperties properties) {
        super(objectMapper);
        this.properties = properties;
    }

    @Override
    protected Optional<DataProviderState> fallbackMapToProviderState(String jsonProviderState) throws JsonProcessingException {
        AccessTokenResponseDTO oldToken = objectMapper.readValue(jsonProviderState, AccessTokenResponseDTO.class);
        return Optional.of(DataProviderState.authorizedProviderState(properties.getRegions().get(0), oldToken.getAccessToken(), oldToken.getRefreshToken()));
    }
}
