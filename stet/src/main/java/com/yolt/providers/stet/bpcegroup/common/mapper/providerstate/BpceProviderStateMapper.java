package com.yolt.providers.stet.bpcegroup.common.mapper.providerstate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.bpcegroup.common.dto.BpceGroupPreMigrationAccessMeansDTO;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.mapper.providerstate.DefaultProviderStateMapper;

import java.util.Optional;

public class BpceProviderStateMapper extends DefaultProviderStateMapper {

    private DefaultProperties properties;

    public BpceProviderStateMapper(ObjectMapper objectMapper, DefaultProperties properties) {
        super(objectMapper);
        this.properties = properties;
    }

    @Override
    protected Optional<DataProviderState> fallbackMapToProviderState(String jsonProviderState) throws JsonProcessingException {
        BpceGroupPreMigrationAccessMeansDTO oldAccessMeans = objectMapper.readValue(jsonProviderState, BpceGroupPreMigrationAccessMeansDTO.class);
        Region region = properties.getRegionByBaseUrl(oldAccessMeans.getRegionalBaseUrl());
        return Optional.of(DataProviderState.authorizedProviderState(
                region,
                oldAccessMeans.getAccessToken(),
                oldAccessMeans.getRefreshToken()));
    }
}
