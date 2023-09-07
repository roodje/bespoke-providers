package com.yolt.providers.stet.cicgroup.common.mapper.providerstate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.cicgroup.common.dto.TokenResponse;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.mapper.providerstate.DefaultProviderStateMapper;

import java.util.Optional;

public class CicGroupStateMapper extends DefaultProviderStateMapper {

    private final DefaultProperties properties;

    public CicGroupStateMapper(ObjectMapper objectMapper, DefaultProperties properties) {
        super(objectMapper);
        this.properties = properties;
    }

    @Override
    protected Optional<DataProviderState> fallbackMapToProviderState(String jsonProviderState) throws JsonProcessingException {
        TokenResponse oldToken = objectMapper.readValue(jsonProviderState, TokenResponse.class);
        return Optional.of(DataProviderState.authorizedProviderState(properties.getRegions().get(0), oldToken.getAccessToken(), oldToken.getRefreshToken()));
    }
}
