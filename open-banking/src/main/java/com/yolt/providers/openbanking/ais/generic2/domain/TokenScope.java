package com.yolt.providers.openbanking.ais.generic2.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class TokenScope {
    private final String grantScope;
    private final String authorizationUrlScope;
}
