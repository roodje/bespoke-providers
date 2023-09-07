package com.yolt.providers.stet.lclgroup.common.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.mapper.providerstate.DefaultProviderStateMapper;
import com.yolt.providers.stet.lclgroup.lcl.configuration.LclStetProperties;

import java.util.Optional;

public class LclProviderStateMapper extends DefaultProviderStateMapper {

    private final LclStetProperties properties;

    public LclProviderStateMapper(final LclStetProperties properties,
                                  final ObjectMapper objectMapper) {
        super(objectMapper);
        this.properties = properties;
    }

    @Override
    protected Optional<DataProviderState> fallbackMapToProviderState(final String jsonProviderState) throws JsonProcessingException {
        LclToken accessMeans = objectMapper.readValue(jsonProviderState, LclToken.class);
        return Optional.of(DataProviderState.authorizedProviderState(properties.getRegions().get(0), accessMeans.getAccessToken(), accessMeans.getRefreshToken()));
    }
}
