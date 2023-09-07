package com.yolt.providers.stet.creditagricolegroup.creditagricole.mapper.providerstate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.creditagricolegroup.creditagricole.domain.CreditAgricoleAccessMeansDTO;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.mapper.providerstate.DefaultProviderStateMapper;

import java.util.Optional;

public class CreditAgricoleGroupProviderStateMapper extends DefaultProviderStateMapper {

    private final DefaultProperties properties;

    public CreditAgricoleGroupProviderStateMapper(ObjectMapper objectMapper, DefaultProperties properties) {
        super(objectMapper);
        this.properties = properties;
    }

    @Override
    protected Optional<DataProviderState> fallbackMapToProviderState(String jsonProviderState) throws JsonProcessingException {
        CreditAgricoleAccessMeansDTO accessMeans = objectMapper.readValue(jsonProviderState, CreditAgricoleAccessMeansDTO.class);

        Region region = properties.getRegionByCode(accessMeans.getRegion().name());
        DataProviderState providerState = DataProviderState.authorizedProviderState(
                region,
                accessMeans.getAccessToken(),
                accessMeans.getRefreshToken(),
                accessMeans.isRefreshed());

        return Optional.of(providerState);
    }
}
