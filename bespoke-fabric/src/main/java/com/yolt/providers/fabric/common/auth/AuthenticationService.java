package com.yolt.providers.fabric.common.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.fabric.common.http.FabricDefaultHttpClient;
import com.yolt.providers.fabric.common.model.*;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
public class AuthenticationService {
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public RedirectStep getConsentStep(final FabricDefaultHttpClient httpClient,
                                       final String baseClientRedirectUrl,
                                       final String psuIpAddress) throws TokenInvalidException {

        LocalDate consentValidTo = LocalDate.now(clock).plusDays(89);
        ConsentResponse consentResponse = httpClient.initiateConsent(baseClientRedirectUrl, psuIpAddress, new ConsentRequest(consentValidTo, 4));
        consentResponse.validate();

        GroupProviderState groupProviderState = new GroupProviderState(
                consentResponse.getConsentId(),
                Instant.now(clock).toEpochMilli(),
                consentValidTo);

        String providerState = serialize(groupProviderState);
        return new RedirectStep(consentResponse.getScaRedirect(), consentResponse.getConsentId(), providerState);
    }

    public void deleteConsent(final FabricDefaultHttpClient httpClient,
                              final GroupProviderState providerState) throws TokenInvalidException {
        httpClient.deleteConsent(providerState.getConsentId());
    }

    public AccessMeansDTO createAccessMeans(final FabricDefaultHttpClient httpClient,
                                            final String consentId,
                                            final LocalDate consentValidTo,
                                            final UUID userId,
                                            final String baseClientRedirectUrl,
                                            final String psuIpAddress) throws TokenInvalidException, JsonProcessingException {

        AuthorizationConsentResourceResponse authorizationResponse = httpClient.initiateAuthorizationResource(baseClientRedirectUrl, psuIpAddress, consentId);
        authorizationResponse.validate();

        if (!ScaStatuses.FINALISED.getValue().equals(authorizationResponse.getScaStatus())) {
            throw new TokenInvalidException("SCA is not finalised");
        }

        GroupProviderState newProviderState = new GroupProviderState(
                consentId,
                Instant.now(clock).toEpochMilli(),
                consentValidTo);

        return new AccessMeansDTO(userId, serialize(newProviderState), new Date(), Date.from(consentValidTo.atStartOfDay(clock.getZone()).toInstant()));
    }

    private String serialize(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize provider state");
        }
    }
}
