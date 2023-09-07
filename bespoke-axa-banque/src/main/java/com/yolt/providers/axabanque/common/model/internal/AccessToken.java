package com.yolt.providers.axabanque.common.model.internal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessToken {
    Long expiresIn;
    String refreshToken;
    String scope;
    String tokenType;
    String token;
}
