package com.yolt.providers.triodosbank.common.model.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentStatusResponse {

    private String consentStatus;
}
