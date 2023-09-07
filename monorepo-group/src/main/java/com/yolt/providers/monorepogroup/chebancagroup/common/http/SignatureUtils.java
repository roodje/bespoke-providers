package com.yolt.providers.monorepogroup.chebancagroup.common.http;

import com.yolt.providers.monorepogroup.chebancagroup.common.dto.internal.SignatureDTO;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SignatureUtils {

    public static SignatureDTO constructSignatureDTO(final String keyId, final UUID signingKid, final String path, final HttpMethod method, final SignatureAlgorithm algorithm) {
        return SignatureDTO.builder()
                .algorithm(algorithm)
                .keyId(keyId)
                .method(method.toString())
                .path(path)
                .signingKid(signingKid)
                .build();
    }
}
