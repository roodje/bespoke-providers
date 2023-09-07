package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.RegistrationResponse;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.RaiffeisenAtGroupHttpClient;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class DefaultRaiffeisenAtGroupRegistrationService implements RaiffeisenAtGroupRegistrationService {

    private final String providerIdentifier;

    @Override
    public Optional<RegistrationResponse> register(RaiffeisenAtGroupHttpClient httpClient) {
        try {
            return Optional.ofNullable(httpClient.register());
        } catch (TokenInvalidException e) {
            throw new AutoOnboardingException(providerIdentifier, "Error during registration", e);
        }

    }
}
