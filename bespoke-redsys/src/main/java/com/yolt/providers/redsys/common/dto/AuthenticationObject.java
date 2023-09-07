package com.yolt.providers.redsys.common.dto;

import lombok.Data;

@Data
public class AuthenticationObject {
    private String authenticationType;

    private String authenticationVersion;

    private String authenticationMethodId;

    private String name;

    private String explanation;
}
