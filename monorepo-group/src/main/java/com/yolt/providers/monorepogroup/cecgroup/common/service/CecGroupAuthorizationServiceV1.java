package com.yolt.providers.monorepogroup.cecgroup.common.service;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.cecgroup.common.CecGroupAccessMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.token.TokenResponse;
import com.yolt.providers.monorepogroup.cecgroup.common.http.CecGroupHttpClient;
import com.yolt.providers.monorepogroup.cecgroup.common.mapper.CecGroupDateConverter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@RequiredArgsConstructor
public class CecGroupAuthorizationServiceV1 implements CecGroupAuthorizationService {

    private final Clock clock;
    private final CecGroupDateConverter dateConverter;

    @SneakyThrows(TokenInvalidException.class)
    public String getConsentId(CecGroupHttpClient httpClient,
                               CecGroupAuthenticationMeans authMeans,
                               Signer signer,
                               String psuIpAddress,
                               String redirectUrl,
                               String state) {
        return httpClient.createConsent(authMeans,
                        signer,
                        LocalDate.now(clock).plusDays(89),
                        psuIpAddress,
                        redirectUrl,
                        state)
                .getConsentId();
    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public CecGroupAccessMeans createAccessMeans(CecGroupHttpClient httpClient,
                                                 String clientId,
                                                 String clientSecret,
                                                 String redirectUri,
                                                 String authCode,
                                                 String consentId) {
        TokenResponse tokenResponse = httpClient.createToken(clientId, clientSecret, redirectUri, authCode);
        return new CecGroupAccessMeans(consentId,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                dateConverter.toTimestamp(LocalDateTime.now(clock).plus(tokenResponse.getExpiresIn(), ChronoUnit.MILLIS)));
    }

    @SneakyThrows(TokenInvalidException.class)
    @Override
    public CecGroupAccessMeans refreshAccessMeans(CecGroupHttpClient httpClient, String clientId, CecGroupAccessMeans oldAccessMeans) {
        TokenResponse tokenResponse = httpClient.refreshToken(clientId, oldAccessMeans.getRefreshToken());
        return new CecGroupAccessMeans(oldAccessMeans.getConsentId(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                dateConverter.toTimestamp(LocalDateTime.now(clock).plus(tokenResponse.getExpiresIn(), ChronoUnit.MILLIS)));
    }
}
