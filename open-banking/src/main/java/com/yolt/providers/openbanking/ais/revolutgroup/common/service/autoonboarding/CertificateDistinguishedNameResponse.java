package com.yolt.providers.openbanking.ais.revolutgroup.common.service.autoonboarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CertificateDistinguishedNameResponse {

    @JsonProperty("tls_client_auth_dn")
    private String tlsClientAuthDn;
}
