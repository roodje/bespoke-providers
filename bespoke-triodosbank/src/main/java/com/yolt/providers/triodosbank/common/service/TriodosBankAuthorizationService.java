package com.yolt.providers.triodosbank.common.service;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.triodosbank.common.model.AuthorisationResponse;
import com.yolt.providers.triodosbank.common.model.Links;
import com.yolt.providers.triodosbank.common.model.http.ConsentCreationRequest;
import com.yolt.providers.triodosbank.common.model.http.ConsentCreationResponse;
import com.yolt.providers.triodosbank.common.model.http.ConsentStatusResponse;
import com.yolt.providers.triodosbank.common.model.http.TokenResponse;
import com.yolt.providers.triodosbank.common.rest.TriodosBankHttpClient;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriodosBankAuthorizationService {

    private final Clock clock;

    private static final String VALID = "valid";
    private static final String RECEIVED = "received";

    public ConsentCreationResponse initiateConsentAndUpdateScaRedirectUrl(TriodosBankHttpClient httpClient,
                                                                          String baseClientRedirectUrl,
                                                                          String psuIpAddress,
                                                                          OAuth2ProofKeyCodeExchange codeExchange) {
        ConsentCreationRequest request = ConsentCreationRequest.builder()
                .recurringIndicator(true)
                .validUntil(LocalDate.now(clock).plusDays(90).minusDays(1).toString())
                .frequencyPerDay(4)
                .combinedServiceIndicator(false)
                .build();

        ConsentCreationResponse response = httpClient.getConsentResponse(request, baseClientRedirectUrl, psuIpAddress);
        Links links = response.getLinks();

        String scaRedirectUrl = UriComponentsBuilder.fromUriString(links.getScaRedirect())
                .queryParam("code_challenge", codeExchange.getCodeChallenge())
                .queryParam("code_challenge_method", codeExchange.getCodeChallengeMethod())
                .build()
                .toUriString();

        links.setScaRedirect(scaRedirectUrl);
        return response;
    }

    public void validateConsentStatus(TriodosBankHttpClient httpClient, String consentId) {
        try {
            ConsentStatusResponse response = httpClient.getConsentStatus(consentId);
            String consentStatus = response.getConsentStatus();

            if (!(VALID.equals(consentStatus) || RECEIVED.equals(consentStatus))) {
                throw new GetAccessTokenFailedException("Consent is not valid for getting data. Consent status: " + consentStatus);
            }
        } catch (HttpStatusCodeException e) {
            throw new GetAccessTokenFailedException("Something went wrong on getting consent status verification: HTTP " + e.getStatusCode());
        }
    }

    public void authoriseConsent(TriodosBankHttpClient httpClient,
                                 String consentId,
                                 String authorisationId,
                                 String psuIpAddress,
                                 String accessToken) {
        AuthorisationResponse authorisationResponse = httpClient.getConsentAuthorisation(
                consentId, authorisationId, psuIpAddress, accessToken);

        String scaStatus = authorisationResponse.getScaStatus();
        try {
            if (!"finalised".equals(scaStatus)) {
                throw new GetAccessTokenFailedException("Consent authorisation is not finalised. Authorisation status: " + scaStatus);
            }
        } catch (HttpStatusCodeException e) {
            throw new GetAccessTokenFailedException("Consent authorisation is not finalised. Authorisation status: " + scaStatus);
        }
    }

    public TokenResponse getAccessToken(TriodosBankHttpClient httpClient,
                                        String callbackUrl,
                                        String codeVerifier) {
        String authorizationCode = UriComponentsBuilder.fromUriString(callbackUrl)
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get("code");

        if (StringUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing authorization code");
        }
        String redirectUrl = UriComponentsBuilder.fromUriString(callbackUrl)
                .replaceQueryParams(null)
                .build()
                .toUriString();

        return httpClient.getTokenResponse(authorizationCode, redirectUrl, codeVerifier);
    }

    public TokenResponse refreshAccessToken(TriodosBankHttpClient httpClient, String refreshToken) throws TokenInvalidException {
        return httpClient.getRefreshedToken(refreshToken);
    }
}
