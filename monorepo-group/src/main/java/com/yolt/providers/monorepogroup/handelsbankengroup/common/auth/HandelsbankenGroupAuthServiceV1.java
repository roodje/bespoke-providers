package com.yolt.providers.monorepogroup.handelsbankengroup.common.auth;

import com.yolt.providers.monorepogroup.handelsbankengroup.common.HandelsbankenGroupAccessMeans;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.config.HandelsbankenGroupProperties;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.ConsentData;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto.ConsentResponse;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto.TokenResponse;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.http.HandelsbankenGroupHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static com.yolt.providers.common.constants.OAuth.*;

@RequiredArgsConstructor
public class HandelsbankenGroupAuthServiceV1 implements HandelsbankenGroupAuthService {

    private final HandelsbankenGroupProperties properties;
    private final Clock clock;

    @Override
    public ConsentData generateConsent(HandelsbankenGroupHttpClient httpClient,
                                       String tppId,
                                       String clientId,
                                       String redirectUri,
                                       String state) {

        String ccgToken = httpClient.doClientCredentialsGrant(tppId).getAccessToken();

        ConsentResponse consent = httpClient.createConsent(clientId, ccgToken);
        String consentId = consent.getConsentId();
        String consentPageUrl = UriComponentsBuilder.fromHttpUrl(properties.getAuthorizationUrl())
                .queryParam(RESPONSE_TYPE, CODE)
                .queryParam(SCOPE, URLEncoder.encode(String.format("AIS:%s", consentId)))
                .queryParam(CLIENT_ID, clientId)
                .queryParam(STATE, state)
                .queryParam(REDIRECT_URI, URLEncoder.encode(redirectUri))
                .toUriString();

        return new ConsentData(consentPageUrl, consentId);
    }

    @Override
    public HandelsbankenGroupAccessMeans createAccessMeans(HandelsbankenGroupHttpClient httpClient,
                                                           String clientId,
                                                           String consentId,
                                                           String authCode,
                                                           String baseClientRedirectUrl) {
        TokenResponse tokenResponse = httpClient.createAccessToken(clientId,
                consentId, authCode, baseClientRedirectUrl);

        return new HandelsbankenGroupAccessMeans(
                consentId,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                ZonedDateTime.now(clock)
                        .plus(tokenResponse.getExpiresIn(), ChronoUnit.SECONDS)
                        .toInstant()
                        .toEpochMilli()
        );
    }

    @Override
    public HandelsbankenGroupAccessMeans refreshAccessMeans(HandelsbankenGroupHttpClient httpClient,
                                                            String clientId,
                                                            HandelsbankenGroupAccessMeans oldAccessMeans) {
        TokenResponse tokenResponse = httpClient.refreshAccessToken(clientId, oldAccessMeans.getRefreshToken());

        return new HandelsbankenGroupAccessMeans(
                oldAccessMeans.getConsentId(),
                tokenResponse.getAccessToken(),
                oldAccessMeans.getRefreshToken(),
                ZonedDateTime.now(clock)
                        .plus(tokenResponse.getExpiresIn(), ChronoUnit.SECONDS)
                        .toInstant()
                        .toEpochMilli()
        );
    }
}
