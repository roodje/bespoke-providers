package com.yolt.providers.stet.bnpparibasgroup.common.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.http.BnpParibasGroupToken;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.mapper.providerstate.DefaultProviderStateMapper;

import java.util.Optional;

public class BnpParibasGroupProviderStateMapper extends DefaultProviderStateMapper {
    private final DefaultProperties properties;

    public BnpParibasGroupProviderStateMapper(ObjectMapper objectMapper, DefaultProperties properties) {
        super(objectMapper);
        this.properties = properties;
    }

    @Override
    protected Optional<DataProviderState> fallbackMapToProviderState(String jsonProviderState) throws JsonProcessingException {
        BnpParibasGroupToken oldToken = objectMapper.readValue(jsonProviderState, BnpParibasGroupToken.class);
        return Optional.of(DataProviderState.authorizedProviderState(properties.getRegions().get(0), oldToken.getAccessToken(), oldToken.getRefreshToken()));
    }
}
