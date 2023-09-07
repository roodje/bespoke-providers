package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroHttpClientFactory;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;

@RequiredArgsConstructor
public class AbnAmroPisAccessTokenProvider {

    private final AbnAmroHttpClientFactory httpClientFactory;
    private final AbnAmroAuthorizationHttpHeadersProvider authorizationHttpHeadersProvider;

    public AccessTokenResponseDTO provideAccessToken(RestTemplateManager restTemplateManager,
                                                     AbnAmroAuthenticationMeans authenticationMeans,
                                                     MultiValueMap<String, String> body) throws TokenInvalidException {
        var httpClient = httpClientFactory.createAbnAmroPisHttpClient(restTemplateManager, authenticationMeans);
        var httpEntity = new HttpEntity<>(body, authorizationHttpHeadersProvider.provideHttpHeadersForPisToken());
        return httpClient.getPisAccessToken(httpEntity);
    }
}
