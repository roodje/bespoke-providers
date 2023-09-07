package com.yolt.providers.sparkassenandlandesbanks.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.sparkassenandlandesbanks.common.dto.SparkassenAndLandesbanksProviderState;
import com.yolt.providers.sparkassenandlandesbanks.common.dto.SparkassenAndLandesbanksTokenResponse;
import com.yolt.providers.sparkassenandlandesbanks.common.service.SparkassenAndLandesbanksAuthenticationService;
import com.yolt.providers.sparkassenandlandesbanks.common.service.SparkassenAndLandesbanksFetchDataService;
import com.yolt.providers.sparkassenandlandesbanks.common.util.HsmUtils;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.sparkassenandlandesbanks.common.SparkassenAndLandesbanksAuthMeans.CLIENT_TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.sparkassenandlandesbanks.common.SparkassenAndLandesbanksAuthMeans.CLIENT_TRANSPORT_KEY_ID_NAME;

@RequiredArgsConstructor
public abstract class SparkassenAndLandesbanksDataProvider implements UrlDataProvider {

    private final SparkassenAndLandesbanksAuthenticationService authenticationService;
    private final ObjectMapper objectMapper;
    private final SparkassenAndLandesbanksFetchDataService sparkassenAndLandesbanksFetchDataService;
    private final Clock clock;

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        SparkassenAndLandesbanksAuthMeans authMeans = SparkassenAndLandesbanksAuthMeans
                .createAuthMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());

        SparkassenAndLandesbanksTokenResponse tokenResponse;
        SparkassenAndLandesbanksProviderState providerState;
        try {
            providerState = objectMapper.readValue(urlCreateAccessMeans.getProviderState(), SparkassenAndLandesbanksProviderState.class);
            tokenResponse = authenticationService.obtainAccessToken(urlCreateAccessMeans,
                    authMeans,
                    providerState,
                    getProviderIdentifierDisplayName());
        } catch (TokenInvalidException | JsonProcessingException e) {
            throw new GetAccessTokenFailedException(e.getMessage());
        }

        String serializedAccessMeans = serializeAccessMeans(tokenResponse,
                providerState.getConsentId(),
                providerState.getDepartment(),
                providerState.getWellKnownEndpoint());

        return new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        urlCreateAccessMeans.getUserId(),
                        serializedAccessMeans,
                        new Date(),
                        Date.from(Instant.now(clock).plusSeconds(tokenResponse.getExpiresIn()))
                )
        );
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        SparkassenAndLandesbanksAuthMeans authMeans = SparkassenAndLandesbanksAuthMeans.createAuthMeans(urlRefreshAccessMeans.getAuthenticationMeans(), getProviderIdentifier());
        SparkassenAndLandesbanksAccessMeans accessMeans = deserializeAccessMeans(urlRefreshAccessMeans.getAccessMeans().getAccessMeans());
        SparkassenAndLandesbanksTokenResponse tokenResponse = authenticationService.refreshAccessToken(urlRefreshAccessMeans, authMeans, accessMeans, getProviderIdentifierDisplayName());
        tokenResponse.setRefreshToken(accessMeans.getRefreshToken());
        AccessMeansDTO accessMeansDTO = urlRefreshAccessMeans.getAccessMeans();
        SparkassenAndLandesbanksAccessMeans sparkassenAndLandesbanksAccessMeans = deserializeAccessMeans(accessMeansDTO.getAccessMeans());
        String serializedAccessMeans = serializeAccessMeans(tokenResponse,
                sparkassenAndLandesbanksAccessMeans.getConsentId(),
                sparkassenAndLandesbanksAccessMeans.getDepartment(),
                accessMeans.getWellKnownEndpoint());
        return new AccessMeansDTO(
                accessMeansDTO.getUserId(),
                serializedAccessMeans,
                new Date(),
                Date.from(Instant.now(clock).plusSeconds(tokenResponse.getExpiresIn()))
        );
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        SparkassenAndLandesbanksAuthMeans authMeans = SparkassenAndLandesbanksAuthMeans.createAuthMeans(urlFetchData.getAuthenticationMeans(), getProviderIdentifier());
        SparkassenAndLandesbanksAccessMeans accessMeans = deserializeAccessMeans(urlFetchData.getAccessMeans().getAccessMeans());
        return sparkassenAndLandesbanksFetchDataService.fetchData(urlFetchData,
                authMeans,
                accessMeans,
                getProviderIdentifierDisplayName());
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID_HEADER_STRING);
        return typedAuthenticationMeans;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(CLIENT_TRANSPORT_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_NAME);
    }

    protected String serializeAccessMeans(SparkassenAndLandesbanksTokenResponse sparkassenAndLandesbanksTokenResponse,
                                          String consentId,
                                          Department department,
                                          String wellKnownEndpoint) {
        validateAccessToken(sparkassenAndLandesbanksTokenResponse);
        try {
            return objectMapper.writeValueAsString(new SparkassenAndLandesbanksAccessMeans(sparkassenAndLandesbanksTokenResponse.getAccessToken(),
                    sparkassenAndLandesbanksTokenResponse.getRefreshToken(),
                    consentId,
                    department,
                    wellKnownEndpoint));
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Cannot serialize access token");
        }
    }

    private void validateAccessToken(SparkassenAndLandesbanksTokenResponse sparkassenAndLandesbanksGroupTokenResponse) {
        if (sparkassenAndLandesbanksGroupTokenResponse == null || sparkassenAndLandesbanksGroupTokenResponse.getAccessToken() == null
                || sparkassenAndLandesbanksGroupTokenResponse.getExpiresIn() == null || sparkassenAndLandesbanksGroupTokenResponse.getRefreshToken() == null) {
            throw new GetAccessTokenFailedException("Missing token data");
        }
    }

    protected SparkassenAndLandesbanksAccessMeans deserializeAccessMeans(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, SparkassenAndLandesbanksAccessMeans.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize token from access means");
        }
    }
}
