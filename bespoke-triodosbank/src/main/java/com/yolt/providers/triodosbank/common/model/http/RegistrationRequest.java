package com.yolt.providers.triodosbank.common.model.http;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RegistrationRequest {


    private String registrationToken;

    private String sectorIdentifierUri;

    private List<String> redirectUris;
}
