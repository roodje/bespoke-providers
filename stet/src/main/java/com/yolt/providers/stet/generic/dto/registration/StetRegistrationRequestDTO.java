package com.yolt.providers.stet.generic.dto.registration;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class StetRegistrationRequestDTO {

    private String clientName;
    private List<String> contacts;
    private String providerLegalId;
    private List<String> redirectUris;
    private List<String> grantTypes;
    private List<StetKeyDTO> jwks;
    private String registrationAccessToken;
    private String scope;
    private String tokenEndpointAuthMethod;
    private String description;
}
