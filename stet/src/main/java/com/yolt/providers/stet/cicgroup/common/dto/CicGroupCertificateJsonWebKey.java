package com.yolt.providers.stet.cicgroup.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CicGroupCertificateJsonWebKey {

    private String kty;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String kid;
    private List<String> x5c;
}
