package com.yolt.providers.openbanking.ais.monzogroup.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBSupplementaryData1;
import lombok.Data;

@Data
public class SupplementaryDataV2 extends OBSupplementaryData1 {

    @JsonProperty("Declined")
    private Boolean declined;
}
