package com.yolt.providers.rabobank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yolt.providers.rabobank.dto.Access;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetConsentResponse {

    private String consentId;
    private Access access;
}
