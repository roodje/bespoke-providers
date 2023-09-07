package com.yolt.providers.argentagroup.common.service.consent;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;

public interface ConsentService {

    Step generateAuthorizationUrlStep(final UrlGetLoginRequest request,
                                      final DefaultAuthenticationMeans authenticationMeans,
                                      final HttpClient httpClient) throws TokenInvalidException;

    void deleteUserConsent(final UrlOnUserSiteDeleteRequest request,
                           final DefaultAuthenticationMeans authenticationMeans,
                           final HttpClient httpClient) throws TokenInvalidException;
}
