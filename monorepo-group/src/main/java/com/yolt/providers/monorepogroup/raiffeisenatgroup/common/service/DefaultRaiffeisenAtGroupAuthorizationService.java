package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.ConsentRequest;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.ConsentStatus;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.CreateConsentResponse;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.GetConsentResponse;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.RaiffeisenAtGroupProviderState;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.RaiffeisenAtGroupHttpClient;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.RaiffeisenAtGroupDateMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans.ProviderStateProcessingException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans.RaiffeisenAtGroupProviderStateMapper;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultRaiffeisenAtGroupAuthorizationService implements RaiffeisenAtGroupAuthorizationService {

    private final RaiffeisenAtGroupTokenService tokenService;
    private final RaiffeisenAtGroupProviderStateMapper providerStateMapper;

    private final RaiffeisenAtGroupDateMapper dateMapper;
    private final Clock clock;

    @Override
    public RedirectStep getLoginInfo(final RaiffeisenAtGroupHttpClient httpClient, final RaiffeisenAtGroupAuthenticationMeans authenticationMeans, final String baseClientRedirectUrl, final String psuIpAddress, final String state) {
        try {
            String clientCredentialToken = tokenService.createClientCredentialToken(httpClient, authenticationMeans);
            ConsentRequest consentRequest = new ConsentRequest(LocalDate.now(clock).plusDays(90), 4);
            String redirectUrlWithState = UriComponentsBuilder.fromHttpUrl(baseClientRedirectUrl)
                    .queryParam("state", state)
                    .toUriString();
            CreateConsentResponse consentResponse = httpClient.createUserConsent(clientCredentialToken, consentRequest, redirectUrlWithState, psuIpAddress);
            RaiffeisenAtGroupProviderState providerState = new RaiffeisenAtGroupProviderState(consentResponse.getConsentId());
            String serializedProviderState = providerStateMapper.serialize(providerState);
            return new RedirectStep(consentResponse.getScaRedirect(), consentResponse.getConsentId(), serializedProviderState);
        } catch (TokenInvalidException | ProviderStateProcessingException e) {
            throw new GetLoginInfoUrlFailedException("Failed to get login info", e);
        }
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final RaiffeisenAtGroupHttpClient httpClient, final RaiffeisenAtGroupAuthenticationMeans authenticationMeans, final String redirectUrlPostedBackFromSite, final UUID userId, final String psuIpAddress, final String providerState) {
        try {
            verifyRedirectUrl(redirectUrlPostedBackFromSite);
            String clientCredentialToken = tokenService.createClientCredentialToken(httpClient, authenticationMeans);
            RaiffeisenAtGroupProviderState raiffeisenProviderState = providerStateMapper.deserialize(providerState);
            GetConsentResponse getConsentResponse = httpClient.getConsentStatus(clientCredentialToken, raiffeisenProviderState.getConsentId(), psuIpAddress);
            verifyConsentStatus(getConsentResponse);
            return new AccessMeansOrStepDTO(
                    new AccessMeansDTO(userId,
                            providerState,
                            dateMapper.toDate(LocalDate.now(clock)),
                            dateMapper.toDate(getConsentResponse.getValidUntil())));
        } catch (TokenInvalidException | ProviderStateProcessingException e) {
            throw new GetAccessTokenFailedException("Failed to create access means", e);
        }

    }

    @Override
    public void deleteUserConsent(final RaiffeisenAtGroupHttpClient httpClient, final RaiffeisenAtGroupAuthenticationMeans authenticationMeans, final String psuIpAddress, final String consentId) throws TokenInvalidException {
        String clientCredentialToken = tokenService.createClientCredentialToken(httpClient, authenticationMeans);
        httpClient.deleteUserConsent(clientCredentialToken, consentId, psuIpAddress);
    }

    private void verifyConsentStatus(final GetConsentResponse consentStatusResponse) {
        if (!ConsentStatus.VALID.equals(consentStatusResponse.getConsentStatus())) {
            throw new IllegalStateException("Consent isn't valid and can't be used");
        }
    }

    private void verifyRedirectUrl(final String redirectUrlPostedBackFromSite) {
        //TODO in documentation there isn't mentioned how redirect url is look like, so for now it is just a lucky shot
        if (StringUtils.contains(redirectUrlPostedBackFromSite, "error")) {
            throw new IllegalStateException("Redirect url contains error");
        }
    }
}
