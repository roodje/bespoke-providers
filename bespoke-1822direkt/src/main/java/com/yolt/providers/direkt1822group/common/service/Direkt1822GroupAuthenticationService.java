package com.yolt.providers.direkt1822group.common.service;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.direkt1822group.common.Direkt1822GroupAccessMeans;
import com.yolt.providers.direkt1822group.common.Direkt1822GroupAuthenticationMeans;
import com.yolt.providers.direkt1822group.common.dto.*;
import com.yolt.providers.direkt1822group.common.exception.LoginNotFoundException;
import com.yolt.providers.direkt1822group.common.rest.Direkt1822GroupHttpClient;
import com.yolt.providers.direkt1822group.common.rest.Direkt1822RestTemplateService;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Optional;

@RequiredArgsConstructor
public class Direkt1822GroupAuthenticationService {

    private final Direkt1822RestTemplateService restTemplateService;
    private final Clock clock;

    public ConsentData generateLoginUrl(Direkt1822GroupAuthenticationMeans authMeans,
                                        String provider,
                                        RestTemplateManager restTemplateManager,
                                        Direkt1822GroupLoginFormDTO loginFormDTO,
                                        String iban,
                                        String psuIpAddress,
                                        String state) throws TokenInvalidException {

        Direkt1822GroupHttpClient httpClient = restTemplateService.createHttpClient(authMeans,
                restTemplateManager,
                provider,
                clock);

        ConsentCreationResponse consentResponse = httpClient.createConsent(loginFormDTO.getRedirectUrl(),
                psuIpAddress,
                iban,
                state);

        String redirectUrl = Optional.of(consentResponse)
                .map(ConsentCreationResponse::getLinks)
                .map(Links::getScaRedirect)
                .map(Link::getHref)
                .orElseThrow(() -> new LoginNotFoundException("No SCA Redirect Link in Consent Response"));
        return new ConsentData(redirectUrl, consentResponse.getConsentId());
    }

    public void deleteConsent(Direkt1822GroupAuthenticationMeans authMeans,
                              String provider,
                              RestTemplateManager restTemplateManager,
                              Direkt1822GroupAccessMeans accessMeans,
                              String psuIpAddress) throws TokenInvalidException {
        Direkt1822GroupHttpClient httpClient = restTemplateService.createHttpClient(authMeans,
                restTemplateManager,
                provider,
                clock);

        httpClient.deleteConsent(psuIpAddress, accessMeans.getConsentId());
    }
}
