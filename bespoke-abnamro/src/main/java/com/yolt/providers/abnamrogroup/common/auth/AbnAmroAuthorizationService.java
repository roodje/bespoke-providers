package com.yolt.providers.abnamrogroup.common.auth;

import com.yolt.providers.abnamrogroup.common.AbnAmroAccessTokenMapper;
import com.yolt.providers.abnamrogroup.abnamro.AbnAmroProperties;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RequiredArgsConstructor
public class AbnAmroAuthorizationService {

    private static final String GRANT_TYPE_KEY = "grant_type";
    private static final String CLIENT_ID = "client_id";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String CODE = "code";

    private final AbnAmroAccessTokenMapper accessTokenMapper;

    public AccessMeansOrStepDTO createNewAccessMeans(final AbnAmroAuthenticationMeans authenticationMeans,
                                                     final UUID userId,
                                                     final String redirectUrlPostedBackFromSite,
                                                     final RestTemplate restTemplate,
                                                     final AbnAmroProperties properties) throws TokenInvalidException {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE_KEY, "authorization_code");
        body.add(CODE, UriComponentsBuilder
                .fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams()
                .toSingleValueMap().get(CODE));
        body.add(REDIRECT_URI, getBaseUrl(redirectUrlPostedBackFromSite));
        body.add(CLIENT_ID, authenticationMeans.getClientId());

        try {
            final ResponseEntity<AccessTokenResponseDTO> tokenResponse = restTemplate.exchange(
                    properties.getTokenUrl(), HttpMethod.POST, new HttpEntity<>(body, httpHeaders), AccessTokenResponseDTO.class);
            return new AccessMeansOrStepDTO(accessTokenMapper.accessMeansFromToken(userId, tokenResponse.getBody()));
        } catch (HttpStatusCodeException e) {
            handleNon2xxResponseCode(e.getStatusCode());
            return null;
        }
    }

    public AccessMeansDTO refreshAccessMeans(final AbnAmroAuthenticationMeans authenticationMeans,
                                             final String accessMeans,
                                             final UUID userId,
                                             final RestTemplate restTemplate,
                                             final AbnAmroProperties properties) throws TokenInvalidException {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final AccessTokenResponseDTO accessTokenResponseDTO = accessTokenMapper.tokenFromAccessMeans(accessMeans);
        final MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE_KEY, "refresh_token");
        body.add(CLIENT_ID, authenticationMeans.getClientId());
        body.add("refresh_token", accessTokenResponseDTO.getRefreshToken());

        try {
            final ResponseEntity<AccessTokenResponseDTO> tokenResponse = restTemplate.exchange(
                    properties.getTokenUrl(), HttpMethod.POST, new HttpEntity<>(body, httpHeaders), AccessTokenResponseDTO.class);
            return accessTokenMapper.accessMeansFromToken(userId, tokenResponse.getBody());
        } catch (HttpStatusCodeException e) {
            handleNon2xxResponseCode(e.getStatusCode());
            return null;
        }
    }

    private static void handleNon2xxResponseCode(final HttpStatus status) throws TokenInvalidException {
        final String errorMessage;
        switch (status) {
            case BAD_REQUEST:
                errorMessage = "Get/refresh token request formed incorrectly: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case UNAUTHORIZED:
                errorMessage = "We are not authorized to call get/refresh token: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case FORBIDDEN:
                errorMessage = "Access to get/refresh token call is forbidden: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case INTERNAL_SERVER_ERROR:
                errorMessage = "Something went wrong on ABN AMRO side: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            default:
                errorMessage = "Unknown get/refresh token exception: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
        }
    }

    private static String getBaseUrl(final String urlWithParameters) {
        return UriComponentsBuilder.fromHttpUrl(urlWithParameters)
                .replaceQuery("") // wiping out query parameters because they are not required anymore
                .build()
                .toUriString();
    }
}
