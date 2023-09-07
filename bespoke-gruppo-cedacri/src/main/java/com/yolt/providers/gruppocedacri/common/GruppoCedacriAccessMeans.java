package com.yolt.providers.gruppocedacri.common;

import com.yolt.providers.gruppocedacri.common.dto.token.TokenResponse;
import lombok.Data;

@Data
public class GruppoCedacriAccessMeans {

    private final TokenResponse tokenResponse;
    private final String consentId;
}
