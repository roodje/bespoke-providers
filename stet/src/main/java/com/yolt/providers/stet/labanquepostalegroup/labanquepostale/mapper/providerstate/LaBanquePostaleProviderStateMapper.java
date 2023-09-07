package com.yolt.providers.stet.labanquepostalegroup.labanquepostale.mapper.providerstate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.mapper.providerstate.DefaultProviderStateMapper;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.config.LaBanquePostaleProperties;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.domain.AccessTokenResponse;

import java.util.List;
import java.util.Optional;

public class LaBanquePostaleProviderStateMapper extends DefaultProviderStateMapper {

    private final LaBanquePostaleProperties properties;

    public LaBanquePostaleProviderStateMapper(ObjectMapper objectMapper,
                                              LaBanquePostaleProperties properties) {
        super(objectMapper);
        this.properties = properties;
    }

    @Override
    protected Optional<DataProviderState> fallbackMapToProviderState(String jsonProviderState) throws JsonProcessingException {
        AccessTokenResponse response = objectMapper.readValue(jsonProviderState, AccessTokenResponse.class);

        List<Region> regions = properties.getRegions();
        if (regions.size() > 1) {
            throw new IllegalStateException("Unknown which region should be selected");
        }
        return Optional.of(DataProviderState.authorizedProviderState(regions.get(0), response.getAccessToken()));
    }
}
