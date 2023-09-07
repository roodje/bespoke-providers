package com.yolt.providers.abancagroup.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.util.List;

@ProjectedPayload
public interface Account {

    @JsonPath("$.id")
    String getAccountId();

    @JsonPath("$.attributes.identifier.number")
    String getIdentifierNumber();

    @JsonPath("$.attributes.identifier.type")
    String getIdentifierType();

    @JsonPath("$.attributes.type")
    String getAccountType();

    @JsonPath("$.attributes.participants")
    List<Participant> getParticipants();
}
