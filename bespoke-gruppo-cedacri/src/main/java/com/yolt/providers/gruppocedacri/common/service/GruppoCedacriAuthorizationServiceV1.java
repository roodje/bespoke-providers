package com.yolt.providers.gruppocedacri.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.gruppocedacri.common.GruppoCedacriAccessMeans;
import com.yolt.providers.gruppocedacri.common.dto.consent.AuthorizationUrlResponse;
import com.yolt.providers.gruppocedacri.common.dto.consent.ConsentAccess;
import com.yolt.providers.gruppocedacri.common.dto.consent.ConsentRequest;
import com.yolt.providers.gruppocedacri.common.dto.consent.ConsentResponse;
import com.yolt.providers.gruppocedacri.common.dto.token.TokenResponse;
import com.yolt.providers.gruppocedacri.common.http.GruppoCedacriHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public class GruppoCedacriAuthorizationServiceV1 implements GruppoCedacriAuthorizationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int CONSENT_VALIDITY_IN_DAYS = 89;
    public static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";
    private static final int MAX_FREQUENCY_PER_DAY = 4;
    private static final String ALL_ACCOUNTS_ACCESS = "allAccounts";
    private static final String AIS_SCOPE = "aisp.base";

    private static final String REDIRECT_URI_QUERY_PARAM_NAME = "redirect_uri";
    private static final String CODE_QUERY_PARAM_NAME = "code";
    private static final String CLIENT_ID_QUERY_PARAM_NAME = "client_id";
    private static final String CLIENT_SECRET_QUERY_PARAM_NAME = "client_secret";
    private static final String GRANT_TYPE_PARAM_NAME = "grant_type";
    private static final String SCOPE_PARAM_NAME = "scope";
    private static final String STATE_NAME = "state";

    private final Clock clock;
    private final ObjectMapper objectMapper;

    @Override
    public TokenResponse getAccessToken(GruppoCedacriHttpClient httpClient,
                                        String redirectUrlPostedBackFromSite,
                                        String redirectUrl,
                                        String clientId,
                                        String clientSecret) {
        final String authorizationCode = UriComponentsBuilder
                .fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams()
                .toSingleValueMap().get("code");

        if (!StringUtils.hasText(authorizationCode)) {
            throw new MissingDataException("Missing data for key code");
        }

        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add(CODE_QUERY_PARAM_NAME, authorizationCode);
        payload.add(CLIENT_ID_QUERY_PARAM_NAME, clientId);
        payload.add(CLIENT_SECRET_QUERY_PARAM_NAME, clientSecret);
        payload.add(REDIRECT_URI_QUERY_PARAM_NAME, redirectUrl);
        payload.add(GRANT_TYPE_PARAM_NAME, AUTHORIZATION_CODE_GRANT_TYPE);

        return httpClient.getAccessToken(payload);
    }

    @Override
    public String getAuthorizationUrl(GruppoCedacriHttpClient httpClient,
                                      String authorizationToken,
                                      String redirectUrl,
                                      String psuIpAddress,
                                      String state,
                                      String clientId) {
        String redirectUrlWithState = redirectUrl + "?state=" + state;
        ConsentRequest consentRequest = createConsentRequest();

        String authorizationUrlResponseBody = httpClient.getAuthorizationUrl(authorizationToken, redirectUrlWithState, psuIpAddress, consentRequest);
        AuthorizationUrlResponse authorizationUrlResponse = deserializeAuthorizationUrlResponse(authorizationUrlResponseBody);

        return addParametersToAuthorizationUrl(authorizationUrlResponse.getResult().getUrl(), clientId, redirectUrl, state);
    }

    @Override
    public ConsentResponse createConsent(GruppoCedacriHttpClient httpClient,
                                         String authorizationToken,
                                         String redirectUrl,
                                         String psuIpAddress,
                                         String state) {
        String redirectUrlWithState = redirectUrl + "?state=" + state;
        return httpClient.createConsent(authorizationToken, redirectUrlWithState, psuIpAddress, createConsentRequest());
    }

    @Override
    public void deleteConsent(GruppoCedacriHttpClient httpClient,
                              GruppoCedacriAccessMeans accessMeans) {
        httpClient.deleteConsent(accessMeans);
    }

    private ConsentRequest createConsentRequest() {
        String consentExpirationDate = LocalDate.now(clock).plusDays(CONSENT_VALIDITY_IN_DAYS).format(DATE_TIME_FORMATTER);
        ConsentAccess consentsAccess = ConsentAccess.builder()
                .availableAccounts(ALL_ACCOUNTS_ACCESS)
                .build();
        return ConsentRequest.builder()
                .access(consentsAccess)
                .recurringIndicator(true)
                .validUntil(consentExpirationDate)
                .frequencyPerDay(MAX_FREQUENCY_PER_DAY)
                .combinedServiceIndicator(false)
                .build();
    }

    private String addParametersToAuthorizationUrl(String authorizationUrl, String clientId, String redirectUrl, String state) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(authorizationUrl);
        uriComponentsBuilder.queryParam(CLIENT_ID_QUERY_PARAM_NAME, clientId);
        uriComponentsBuilder.queryParam(REDIRECT_URI_QUERY_PARAM_NAME, redirectUrl);
        uriComponentsBuilder.queryParam(SCOPE_PARAM_NAME, AIS_SCOPE);
        uriComponentsBuilder.queryParam(STATE_NAME, state);
        return uriComponentsBuilder.toUriString();
    }

    private AuthorizationUrlResponse deserializeAuthorizationUrlResponse(String accessMeans) throws GetLoginInfoUrlFailedException {
        try {
            return objectMapper.readValue(accessMeans, AuthorizationUrlResponse.class);
        } catch (IOException e) {
            throw new GetLoginInfoUrlFailedException("Unable to deserialize authorization URL response");
        }
    }
}
