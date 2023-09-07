package com.yolt.providers.abancagroup.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Participant {

    @JsonPath("$.participantTypeCode")
    String getParticipantTypeCode();

    @JsonPath("$.name")
    String getName();

    @JsonPath("$.surname")
    String getSurname();

    @JsonPath("$.secondSurname")
    String getSecondSurname();
}
