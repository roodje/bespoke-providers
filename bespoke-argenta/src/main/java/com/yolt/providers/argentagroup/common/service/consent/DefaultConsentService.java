package com.yolt.providers.argentagroup.common.service.consent;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.common.service.ProviderState;
import com.yolt.providers.argentagroup.common.service.ProviderStateMapper;
import com.yolt.providers.argentagroup.common.service.token.AccessMeans;
import com.yolt.providers.argentagroup.common.service.token.AccessMeansMapper;
import com.yolt.providers.argentagroup.dto.CreateConsentRequest;
import com.yolt.providers.argentagroup.dto.CreateConsentResponse;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class DefaultConsentService implements ConsentService {

    private static final String CONSENT_ID_PARAMETER_NAME = "consentId";

    private final String initiateConsentEndpointPath;
    private final String deleteConsentEndpointPath;
    private final InitiateConsentRequestBodyProvider initiateConsentRequestBodyProvider;
    private final InitiateConsentHttpHeadersProvider initiateConsentHttpHeadersProvider;
    private final InitiateConsentResponseMapper initiateConsentResponseMapper;
    private final AuthorizationUrlEnricher authorizationUrlEnricher;
    private final Supplier<OAuth2ProofKeyCodeExchange> pkceSupplier;
    private final DeleteConsentHttpHeadersProvider deleteConsentHttpHeadersProvider;
    private final ProviderStateMapper providerStateMapper;
    private final AccessMeansMapper accessMeansMapper;
    private final HttpErrorHandler consentHttpErrorHandler;


    @Override
    public Step generateAuthorizationUrlStep(final UrlGetLoginRequest request,
                                             final DefaultAuthenticationMeans authenticationMeans,
                                             final HttpClient httpClient) throws TokenInvalidException {
        CreateConsentRequest initiateConsentRequestBody = initiateConsentRequestBodyProvider.provideRequestBody();
        HttpHeaders initiateConsentHeaders = initiateConsentHttpHeadersProvider.provideHeaders(
                request, authenticationMeans, initiateConsentRequestBody);

        CreateConsentResponse response = httpClient.exchange(
                initiateConsentEndpointPath,
                HttpMethod.POST,
                new HttpEntity<>(initiateConsentRequestBody, initiateConsentHeaders),
                ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT,
                CreateConsentResponse.class,
                consentHttpErrorHandler
        ).getBody();

        InitiateConsentResult initiateConsentResult = initiateConsentResponseMapper.mapResponse(response);

        OAuth2ProofKeyCodeExchange proofKeyCodeExchange = pkceSupplier.get();

        String enrichedAuthorizationUrl = authorizationUrlEnricher.enrichAuthorizationUrl(
                initiateConsentResult,
                authenticationMeans,
                request.getState(),
                request.getBaseClientRedirectUrl(),
                proofKeyCodeExchange
        );

        ProviderState providerState = new ProviderState(initiateConsentResult.getConsentId(), proofKeyCodeExchange);

        return new RedirectStep(
                enrichedAuthorizationUrl,
                initiateConsentResult.getConsentId(),
                providerStateMapper.serializeProviderState(providerState)
        );
    }

    @Override
    public void deleteUserConsent(final UrlOnUserSiteDeleteRequest request,
                                  final DefaultAuthenticationMeans authenticationMeans,
                                  final HttpClient httpClient) throws TokenInvalidException {
        AccessMeansDTO accessMeans = request.getAccessMeans();
        AccessMeans deserializedAccessMeans = accessMeansMapper.deserializeAccessMeans(accessMeans.getAccessMeans());

        HttpHeaders headers = deleteConsentHttpHeadersProvider.provideRequestHeaders(
                request, authenticationMeans, deserializedAccessMeans);

        String uri = UriComponentsBuilder.fromPath(deleteConsentEndpointPath)
                .uriVariables(Map.of(CONSENT_ID_PARAMETER_NAME, request.getExternalConsentId()))
                .toUriString();

        httpClient.exchange(
                uri,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT,
                Void.class,
                consentHttpErrorHandler
        );
    }
}
