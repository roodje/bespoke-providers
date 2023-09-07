package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.parties;

import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBParty2;

import java.util.function.Function;

public class DefaultPartiesMapper implements Function<OBParty2, PartyDto> {
    @Override
    public PartyDto apply(OBParty2 obParty2) {
        return new PartyDto(obParty2.getName());
    }
}
