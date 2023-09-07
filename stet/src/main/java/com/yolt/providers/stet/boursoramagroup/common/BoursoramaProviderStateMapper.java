package com.yolt.providers.stet.boursoramagroup.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.boursoramagroup.boursorama.config.BoursoramaProperties;
import com.yolt.providers.stet.boursoramagroup.common.dto.BoursoramaGroupAccessToken;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.mapper.providerstate.DefaultProviderStateMapper;

import java.util.Optional;

public class BoursoramaProviderStateMapper extends DefaultProviderStateMapper {

    private final BoursoramaProperties properties;

    public BoursoramaProviderStateMapper(ObjectMapper objectMapper, BoursoramaProperties properties) {
        super(objectMapper);
        this.properties = properties;
    }

    @Override
    protected Optional<DataProviderState> fallbackMapToProviderState(String jsonProviderState) throws JsonProcessingException {
        BoursoramaGroupAccessToken oldBoursoramaToken = objectMapper.readValue(jsonProviderState, BoursoramaGroupAccessToken.class);
        return Optional.of(DataProviderState.authorizedProviderState(properties.getRegions().get(0), oldBoursoramaToken.getAccessToken(), oldBoursoramaToken.getRefreshToken()));
    }
}