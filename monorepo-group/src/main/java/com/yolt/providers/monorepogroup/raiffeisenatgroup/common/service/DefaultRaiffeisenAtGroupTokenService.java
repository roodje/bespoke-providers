package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.RaiffeisenAtGroupHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.*;

@RequiredArgsConstructor
public class DefaultRaiffeisenAtGroupTokenService implements RaiffeisenAtGroupTokenService {

    private static final String SCOPE_VALUE = "apic-psd2";

    @Override
    public String createClientCredentialToken(final RaiffeisenAtGroupHttpClient httpClient, final RaiffeisenAtGroupAuthenticationMeans authenticationMeans) throws TokenInvalidException {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(GRANT_TYPE, CLIENT_CREDENTIALS);
        requestBody.add(CLIENT_ID, authenticationMeans.getClientId());
        requestBody.add(SCOPE, SCOPE_VALUE);
        return httpClient.createClientCredentialToken(requestBody).getAccessToken();
    }
}
