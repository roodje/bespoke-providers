package com.yolt.providers.bunq.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Psd2ProviderRequest {
    @JsonProperty("client_payment_service_provider_certificate")
    final String clientCertificate;

    @JsonProperty("client_payment_service_provider_certificate_chain")
    final String clientCertificateChain;

    @JsonProperty("client_public_key_signature")
    final String clientSignedPublicKey;

}
