package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.RegistrationResponse;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.RaiffeisenAtGroupHttpClient;

import java.util.Optional;

public interface RaiffeisenAtGroupRegistrationService {

    public Optional<RegistrationResponse> register(final RaiffeisenAtGroupHttpClient httpClient);
}
