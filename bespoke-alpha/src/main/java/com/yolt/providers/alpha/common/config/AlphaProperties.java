package com.yolt.providers.alpha.common.config;

import lombok.Data;

@Data
public abstract class AlphaProperties {
    private String baseUrl;
    private String oAuthAuthorizationUrl;
    private String bankId;
}
