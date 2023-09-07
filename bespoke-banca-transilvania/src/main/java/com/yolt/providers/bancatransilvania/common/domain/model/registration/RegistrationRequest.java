package com.yolt.providers.bancatransilvania.common.domain.model.registration;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class RegistrationRequest {

    private List<String> redirectUris;
    private String companyName;
    private String clientName;
    private String companyUrl;
    private String contactPerson;
    private String emailAddress;
    private String phoneNumber;
}
