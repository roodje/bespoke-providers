package com.yolt.providers.stet.generic.service.registration.request;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Supplier;

@Getter
@AllArgsConstructor
public class RegistrationRequest {

    private final DefaultAuthenticationMeans authMeans;
    private final Signer signer;
    private final Supplier<String> lastExternalTraceIdSupplier;
    private final String redirectUrl;
    private final String providerIdentifier;
}
