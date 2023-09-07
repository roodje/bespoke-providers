package com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class App {

    private String name;
    private String description;
    private String oauthRedirectURI;
}
