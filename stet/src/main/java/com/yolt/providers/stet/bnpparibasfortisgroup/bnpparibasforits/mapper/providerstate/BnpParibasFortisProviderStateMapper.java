package com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.mapper.providerstate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.config.BnpParibasFortisProperties;
import com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.domain.BnpParibasFortisOldProviderState;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.mapper.providerstate.DefaultProviderStateMapper;

import java.util.Optional;

public class BnpParibasFortisProviderStateMapper extends DefaultProviderStateMapper {

    private final BnpParibasFortisProperties properties;

    public BnpParibasFortisProviderStateMapper(ObjectMapper objectMapper, BnpParibasFortisProperties properties) {
        super(objectMapper);
        this.properties = properties;
    }

    @Override
    protected Optional<DataProviderState> fallbackMapToProviderState(String providerState) throws JsonProcessingException {
        BnpParibasFortisOldProviderState oldProviderState = objectMapper.readValue(providerState, BnpParibasFortisOldProviderState.class);
        return Optional.of(DataProviderState.builder()
                .region(properties.getRegions().get(0))
                .accessToken(oldProviderState.getAccessToken())
                .build());
    }
}
