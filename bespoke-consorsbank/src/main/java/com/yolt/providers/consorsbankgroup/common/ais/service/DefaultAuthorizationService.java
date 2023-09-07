package com.yolt.providers.consorsbankgroup.common.ais.service;

import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.consorsbankgroup.common.ais.DefaultAccessMeans;
import com.yolt.providers.consorsbankgroup.common.ais.exception.LoginNotFoundException;
import com.yolt.providers.consorsbankgroup.common.ais.http.DefaultRestClient;
import com.yolt.providers.consorsbankgroup.dto.AccountAccess;
import com.yolt.providers.consorsbankgroup.dto.Consents;
import com.yolt.providers.consorsbankgroup.dto.ConsentsResponse201;
import com.yolt.providers.consorsbankgroup.dto.HrefType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultAuthorizationService {

    private static final String STATE_PARAMETER_NAME = "state";

    private final DefaultRestClient restClient;
    private final Clock clock;

    public RedirectStep getLoginInfo(final String redirectUrl,
                                     final String psuIpAddress,
                                     final String state,
                                     final HttpClient httpClient) {
        String redirectUrlWithState = enrichRedirectUrlWithState(redirectUrl, state);

        Consents requestBody = new Consents();
        requestBody.setValidUntil(LocalDate.now(clock).plusDays(89));
        requestBody.setRecurringIndicator(Boolean.TRUE);
        requestBody.setCombinedServiceIndicator(Boolean.FALSE);
        requestBody.setFrequencyPerDay(4);

        AccountAccess access = new AccountAccess();
        access.setAccounts(Collections.emptyList());
        access.setBalances(Collections.emptyList());
        access.setTransactions(Collections.emptyList());
        requestBody.access(access);

        ConsentsResponse201 response;
        try {
            response = restClient.generateConsentUrl(requestBody, redirectUrlWithState, psuIpAddress, httpClient);
        } catch (TokenInvalidException e) {
            throw new LoginNotFoundException(e);
        }

        return mapResponseToStep(response);
    }

    public void deleteConsent(final DefaultAccessMeans accessMeans,
                              final String psuIpAddress,
                              final HttpClient httpClient) throws TokenInvalidException  {
        restClient.deleteConsent(accessMeans.getConsentId(), psuIpAddress, httpClient);
    }

    private String enrichRedirectUrlWithState(final String redirectUrl, final String state) {
        return UriComponentsBuilder.fromHttpUrl(redirectUrl)
                .queryParam(STATE_PARAMETER_NAME, state)
                .build()
                .toUriString();
    }

    private static RedirectStep mapResponseToStep(final ConsentsResponse201 response) {
        String redirectUrl = Optional.of(response)
                .map(ConsentsResponse201::getLinks)
                .map(m -> m.get("scaRedirect"))
                .map(HrefType::getHref)
                .orElseThrow(() -> new LoginNotFoundException("SCA redirect url is missing"));

        return new RedirectStep(redirectUrl, null, response.getConsentId());
    }
}
