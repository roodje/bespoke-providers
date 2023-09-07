package com.yolt.providers.stet.generic.dto.payment.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StetPaymentInitiationResponseDTO {

    @JsonProperty("_links")
    private StetLinks links;
}
