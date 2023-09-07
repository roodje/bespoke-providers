package com.yolt.providers.monorepogroup.chebancagroup.common.dto.internal;

import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class SignatureDTO {

    private SignatureAlgorithm algorithm;
    private String keyId;
    private String method;
    private String path;
    private UUID signingKid;
}
