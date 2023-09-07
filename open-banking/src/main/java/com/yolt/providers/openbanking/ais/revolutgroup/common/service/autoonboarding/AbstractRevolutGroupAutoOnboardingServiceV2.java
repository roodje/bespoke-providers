package com.yolt.providers.openbanking.ais.revolutgroup.common.service.autoonboarding;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.revolutgroup.common.RevolutPropertiesV2;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
public abstract class AbstractRevolutGroupAutoOnboardingServiceV2 implements RevolutGroupAutoOnboardingServiceV2 {

    private static final String SCOPE_CLAIM = "scope";
    private static final String SCOPE = "openid accounts";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_CREDENTIALS_GRANT = "client_credentials";
    private static final String CLIENT_ID_CLAIM = "client_id";
    private static final String REGISTRATION_MANAGEMENT_PATH_PATTERN = "/register/{clientId}";
    private static final String GET_CERTIFICATE_DN = "get_certificate_dn";
    private static final String GET_REGISTRATION = "get_certificate_dn";
    private static final String CERTIFICATE_DN_ENDPOINT = "/distinguished-name";
    private static final String TLS_CLIENT_AUTH_DN_CLAIM = "tls_client_auth_dn";

    private final RevolutPropertiesV2 properties;
    private final String clientIdAuthMeanName;

    @Override
    public Optional<RevolutAutoOnboardingResponse> register(final HttpClient httpClient,
                                                            final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                            final DefaultAuthMeans defaultAuthMeans) throws TokenInvalidException {
        if (urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(clientIdAuthMeanName)) {
            invokeRegistrationUpdate(httpClient, defaultAuthMeans, urlAutoOnboardingRequest);
            return Optional.empty();
        }
        return invokeRegistration(httpClient, urlAutoOnboardingRequest, defaultAuthMeans);
    }

    @Override
    public void removeAutoConfiguration(final HttpClient httpClient,
                                        final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                        final DefaultAuthMeans defaultAuthMeans) throws TokenInvalidException {

        removeRegistration(httpClient, defaultAuthMeans);
    }

    private Optional<RevolutAutoOnboardingResponse> invokeRegistration(final HttpClient httpClient,
                                                                       final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                                       final DefaultAuthMeans defaultAuthMeans) throws TokenInvalidException {
        String tlsClientAuthDn = getCertificateDistinguishedNameResponse(httpClient)
                .getTlsClientAuthDn();

        String payload = createRegistrationPayload(
                urlAutoOnboardingRequest,
                defaultAuthMeans,
                urlAutoOnboardingRequest.getSigner(),
                tlsClientAuthDn);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.CONTENT_TYPE, "application/jwt");

        ResponseEntity<RevolutAutoOnboardingResponse> responseEntity = httpClient.exchange(
                properties.getRegistrationUrl(),
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                ProviderClientEndpoints.REGISTER,
                RevolutAutoOnboardingResponse.class);

        return Optional.ofNullable(responseEntity.getBody());
    }

    private void invokeRegistrationUpdate(final HttpClient httpClient,
                                          final DefaultAuthMeans authMeans,
                                          final UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException {
        AccessTokenResponseDTO accessTokenDTO = doClientCredentialsGrantWithoutClientId(httpClient, authMeans);
        String tlsClientAuthDn = getCertificateDistinguishedNameResponse(httpClient)
                .getTlsClientAuthDn();

        // Get information about registration and log them to RDD, so we have backup before update will be performed
        getRegistrationDetails(httpClient, authMeans, accessTokenDTO);

        updateRegistration(httpClient, authMeans, accessTokenDTO, urlAutoOnboardingRequest, tlsClientAuthDn);

    }

    private void invokeRegistrationRemoval(final HttpClient httpClient,
                                           final DefaultAuthMeans authMeans,
                                           final AccessTokenResponseDTO accessTokenDTO) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessTokenDTO.getAccessToken());

        httpClient.exchange(REGISTRATION_MANAGEMENT_PATH_PATTERN,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                ProviderClientEndpoints.DELETE_REGISTRATION,
                Void.class,
                authMeans.getClientId());
    }

    private CertificateDistinguishedNameResponse getCertificateDistinguishedNameResponse(final HttpClient httpClient) throws TokenInvalidException {
        String certificateDistinguishedNameUrl = UriComponentsBuilder.fromUriString(properties.getOAuthTokenUrl())
                .replacePath(CERTIFICATE_DN_ENDPOINT)
                .build()
                .toUriString();

        return httpClient.exchange(
                certificateDistinguishedNameUrl,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                GET_CERTIFICATE_DN,
                CertificateDistinguishedNameResponse.class)
                .getBody();
    }

    private void getRegistrationDetails(final HttpClient httpClient,
                                        final DefaultAuthMeans authMeans,
                                        final AccessTokenResponseDTO accessTokenDTO) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessTokenDTO.getAccessToken());

        httpClient.exchange(REGISTRATION_MANAGEMENT_PATH_PATTERN,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                GET_REGISTRATION,
                RevolutAutoOnboardingResponse.class,
                authMeans.getClientId());
    }

    private void updateRegistration(final HttpClient httpClient,
                                    final DefaultAuthMeans defaultAuthMeans,
                                    final AccessTokenResponseDTO accessTokenDTO,
                                    final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                    final String tlsClientAuthDn) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessTokenDTO.getAccessToken());
        headers.set(HttpHeaders.CONTENT_TYPE, "application/jwt");

        String payload = createRegistrationPayload(
                urlAutoOnboardingRequest,
                defaultAuthMeans,
                urlAutoOnboardingRequest.getSigner(),
                tlsClientAuthDn);

        httpClient.exchange(REGISTRATION_MANAGEMENT_PATH_PATTERN,
                HttpMethod.PUT,
                new HttpEntity<>(payload, headers),
                ProviderClientEndpoints.UPDATE_REGISTRATION,
                Void.class,
                defaultAuthMeans.getClientId());
    }

    private void removeRegistration(final HttpClient httpClient,
                                    final DefaultAuthMeans defaultAuthMeans) throws TokenInvalidException {
        AccessTokenResponseDTO accessTokenDTO = doClientCredentialsGrantWithoutClientId(httpClient, defaultAuthMeans);

        invokeRegistrationRemoval(httpClient, defaultAuthMeans, accessTokenDTO);
    }

    private AccessTokenResponseDTO doClientCredentialsGrant(final HttpClient httpClient,
                                                            final DefaultAuthMeans authMeans) throws TokenInvalidException {
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, CLIENT_CREDENTIALS_GRANT);
        body.add(SCOPE_CLAIM, SCOPE);
        body.add(CLIENT_ID_CLAIM, authMeans.getClientId());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpExtraHeaders.FINANCIAL_ID_HEADER_NAME, authMeans.getInstitutionId());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        return httpClient.exchange(properties.getOAuthTokenUrl(),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT,
                AccessTokenResponseDTO.class).getBody();
    }

    private AccessTokenResponseDTO doClientCredentialsGrantWithoutClientId(final HttpClient httpClient,
                                                                           final DefaultAuthMeans authMeans) throws TokenInvalidException {
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, CLIENT_CREDENTIALS_GRANT);
        body.add(SCOPE_CLAIM, SCOPE);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpExtraHeaders.FINANCIAL_ID_HEADER_NAME, authMeans.getInstitutionId());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(new String(Base64.encode(authMeans.getClientId().getBytes(UTF_8))));
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        return httpClient.exchange(properties.getOAuthTokenUrl(),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT,
                AccessTokenResponseDTO.class).getBody();
    }

    protected abstract String createRegistrationPayload(final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                        final DefaultAuthMeans defaultAuthMeans,
                                                        final Signer signer,
                                                        final String tlsClientAuthDn) throws TokenInvalidException;

    protected abstract List<String> getRegistrationScopes(Set<TokenScope> tokenScopes);
}
