package com.yolt.providers.ing.common.service;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.ing.common.auth.*;
import com.yolt.providers.ing.common.config.IngProperties;
import com.yolt.providers.ing.common.exception.ClientCredentialsFailedException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static com.yolt.providers.ing.common.service.HttpErrorHandler.handleNon2xxResponseCode;

@RequiredArgsConstructor
public class IngAuthenticationServiceV3 {

    private static final String GRANT_TYPE = "grant_type";
    private static final String SCOPE = "scope";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String CLIENT_ID = "client_id";
    private static final String STATE = "state";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String RESPONSE_TYPE = "response_type";
    private static final String COUNTRY_CODE = "country_code";
    private static final String CODE_NAME = "code";

    private static final String RESPONSE_TYPE_CODE = "code";

    private static final String SCOPE_VALUE = "payment-accounts%3Abalances%3Aview%20payment-accounts%3Atransactions%3Aview";

    private static final String ACCESS_TOKEN_FAILED_MESSAGE = "Empty body with token response";
    private static final String TIME_FORMAT = "E, dd MMM yyyy HH:mm:ss z";

    private static final DateTimeFormatter ING_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT)
            .withZone(ZoneId.of("GMT")).withLocale(Locale.ENGLISH);
    private static final String DIGEST = "Digest";
    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final String SIGNATURE = "Signature";
    private static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
    private static final String AUTHORIZATION_CODE = "authorization_code";

    private final IngClientAwareRestTemplateService clientAwareRestTemplateService;
    private final IngSigningUtil ingSigningUtil;
    private final IngProperties properties;
    private final String countryCode;
    private final Clock clock;

    private final Map<TokenKey, TokenEntry> clientAccessTokens = new ConcurrentHashMap<>();

    public IngClientAccessMeans getClientAccessMeans(final IngAuthenticationMeans authenticationMeans,
                                                     final AuthenticationMeansReference authenticationMeansReference,
                                                     final RestTemplateManager restTemplateManager,
                                                     final Signer signer) {
        TokenKey tokenKey = new TokenKey(authenticationMeansReference);
        if (clientAccessTokenIsEmptyOrExpired(tokenKey)) {
            createClientAccessMeans(authenticationMeans, authenticationMeansReference, restTemplateManager, signer);
        }
        return clientAccessTokens.get(tokenKey).getClientAccessToken();
    }

    private boolean clientAccessTokenIsEmptyOrExpired(final TokenKey tokenKey) {
        TokenEntry tokenEntry = clientAccessTokens.get(tokenKey);
        if (tokenEntry == null || tokenEntry.getClientAccessToken() == null) {
            return true;
        }
        // Just 120 seconds slack for network latency etc. so we are sure that the token is 'at least' another 120 sec
        // valid
        Instant saveExpiryTime = Instant.ofEpochMilli(tokenEntry.getClientAccessToken().getExpiryTimestamp()).minusSeconds(120);
        return saveExpiryTime.isBefore(Instant.now(clock));
    }

    private void createClientAccessMeans(final IngAuthenticationMeans authenticationMeans,
                                         final AuthenticationMeansReference authenticationMeansReference,
                                         final RestTemplateManager restTemplateManager,
                                         final Signer signer) {
        TokenKey tokenKey = new TokenKey(authenticationMeansReference);
        TokenEntry tokenEntry = clientAccessTokens.computeIfAbsent(tokenKey, reference -> new TokenEntry());
        ReentrantLock lock = tokenEntry.getLock();
        if (lock.tryLock()) {
            try {
                IngClientAccessMeans accessMeans = doClientCredentialsGrant(authenticationMeans, restTemplateManager, signer, authenticationMeansReference);
                tokenEntry.setClientAccessToken(accessMeans);
            } catch (Exception e) {
                throw new ClientCredentialsFailedException("Creating client session (client credentials grant) failed.", e);
            } finally {
                lock.unlock();
            }
        } else {
            try {
                lock.lock();
            } finally {
                lock.unlock();
            }
        }
    }

    private IngClientAccessMeans doClientCredentialsGrant(final IngAuthenticationMeans authenticationMeans,
                                                          final RestTemplateManager restTemplateManager,
                                                          final Signer signer,
                                                          final AuthenticationMeansReference authenticationMeansReference)
            throws TokenInvalidException {
        IngAuthData token = getRefreshedBearerToken(authenticationMeans, restTemplateManager, signer);
        return new IngClientAccessMeans(token, authenticationMeansReference, clock);
    }

    private HttpHeaders createDefaultHttpHeaders(final MultiValueMap<String, Object> requestPayload) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.DATE, ING_DATETIME_FORMATTER.format(Instant.now(clock)));
        headers.add(DIGEST, ingSigningUtil.getDigest(requestPayload));
        return headers;
    }

    private IngAuthData getRefreshedBearerToken(final IngAuthenticationMeans authenticationMeans,
                                                final RestTemplateManager restTemplateManager,
                                                final Signer signer) throws TokenInvalidException {
        RestTemplate restTemplate = clientAwareRestTemplateService.buildRestTemplate(authenticationMeans, restTemplateManager);
        MultiValueMap<String, Object> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, CLIENT_CREDENTIALS);
        HttpHeaders headers = createDefaultHttpHeaders(requestPayload);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String signature = ingSigningUtil.getSignature(
                HttpMethod.POST,
                properties.getOAuthTokenEndpoint(),
                headers,
                authenticationMeans.getSigningCertificateSerialNumber(),
                authenticationMeans.getSigningKeyId(),
                signer);
        headers.add(HttpHeaders.AUTHORIZATION, SIGNATURE + " " + signature);
        headers.add(TPP_SIGNATURE_CERTIFICATE, authenticationMeans.getSigningCertificatePemFormat());

        HttpEntity<?> request = new HttpEntity<>(requestPayload, headers);

        return fetchTokenResponse(restTemplate, request)
                .orElseThrow(() -> new GetAccessTokenFailedException(ACCESS_TOKEN_FAILED_MESSAGE));
    }

    public RedirectUrlResponse getIngRedirectUrl(final IngClientAccessMeans clientAccessMeans,
                                                 final IngAuthenticationMeans authenticationMeans,
                                                 final String clientRedirectUrl,
                                                 final RestTemplateManager restTemplateManager,
                                                 final Signer signer) throws TokenInvalidException {
        RestTemplate restTemplate = clientAwareRestTemplateService
                .buildRestTemplate(authenticationMeans, restTemplateManager);

        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(SCOPE, SCOPE_VALUE);
        requestPayload.add(COUNTRY_CODE, countryCode);
        requestPayload.add(REDIRECT_URI, clientRedirectUrl);

        UriComponents url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl() + properties.getAuthorizationUrlServerEndpoint())
                .queryParams(requestPayload).build();

        HttpHeaders headers = createDefaultHttpHeaders(new LinkedMultiValueMap());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(clientAccessMeans.getAccessToken());
        headers.add(SIGNATURE, ingSigningUtil.getSignature(
                HttpMethod.GET,
                url.getPath() + "?" + url.getQuery(),
                headers,
                clientAccessMeans.getClientId(),
                authenticationMeans.getSigningKeyId(),
                signer));

        HttpEntity<?> request = new HttpEntity<>(headers);

        return fetchRedirectUrlResponse(url.toString(), restTemplate, request)
                .orElseThrow(() -> new GetLoginInfoUrlFailedException("Empty response!"));
    }

    public String getLoginUrl(final String clientId, final String ingRedirectUrl,
                              final String redirectUri, final String loginState) {
        MultiValueMap<String, String> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(CLIENT_ID, clientId);
        requestPayload.add(SCOPE, SCOPE_VALUE);
        requestPayload.add(STATE, loginState);
        requestPayload.add(REDIRECT_URI, redirectUri);
        requestPayload.add(RESPONSE_TYPE, RESPONSE_TYPE_CODE);

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(ingRedirectUrl)
                .queryParams(requestPayload)
                .build();

        return uriComponents.toString();
    }

    public IngUserAccessMeans getUserToken(final IngClientAccessMeans clientAccessMeans,
                                           final IngAuthenticationMeans authenticationMeans,
                                           final RestTemplateManager restTemplateManager,
                                           final Signer signer,
                                           final String accessCode) throws TokenInvalidException {
        MultiValueMap<String, Object> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, AUTHORIZATION_CODE);
        requestPayload.add(CODE_NAME, accessCode);

        return callTokenEndpoint(requestPayload, clientAccessMeans, authenticationMeans, restTemplateManager, signer);
    }

    public IngUserAccessMeans refreshOAuthToken(final IngClientAccessMeans newClientAccessMeans,
                                                final IngUserAccessMeans userAccessMeans,
                                                final IngAuthenticationMeans authenticationMeans,
                                                final RestTemplateManager restTemplateManager,
                                                final Signer signer) throws TokenInvalidException {

        MultiValueMap<String, Object> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, REFRESH_TOKEN);
        requestPayload.add(REFRESH_TOKEN, userAccessMeans.getRefreshToken());

        return callTokenEndpoint(requestPayload,
                newClientAccessMeans,
                authenticationMeans,
                restTemplateManager,
                signer);
    }

    private IngUserAccessMeans callTokenEndpoint(final MultiValueMap<String, Object> requestPayload,
                                                 final IngClientAccessMeans clientAccessMeans,
                                                 final IngAuthenticationMeans authenticationMeans,
                                                 final RestTemplateManager restTemplateManager,
                                                 final Signer signer) throws TokenInvalidException {
        RestTemplate restTemplate = clientAwareRestTemplateService
                .buildRestTemplate(authenticationMeans, restTemplateManager);

        HttpHeaders headers = createDefaultHttpHeaders(requestPayload);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.setBearerAuth(clientAccessMeans.getAccessToken());
        headers.add(SIGNATURE, ingSigningUtil.getSignature(HttpMethod.POST, properties.getOAuthTokenEndpoint(), headers, clientAccessMeans.getClientId(), authenticationMeans.getSigningKeyId(), signer));

        HttpEntity<?> request = new HttpEntity<>(requestPayload, headers);

        IngAuthData response = fetchTokenResponse(restTemplate, request)
                .orElseThrow(() -> new GetAccessTokenFailedException(ACCESS_TOKEN_FAILED_MESSAGE));
        return new IngUserAccessMeans(response, clientAccessMeans, clock);
    }

    private Optional<IngAuthData> fetchTokenResponse(final RestTemplate restTemplate, final HttpEntity tokenRequest) throws TokenInvalidException {
        ResponseEntity<IngAuthData> tokenResponseResponseEntity = null;
        try {
            tokenResponseResponseEntity = restTemplate.postForEntity(properties.getBaseUrl() + properties.getOAuthTokenEndpoint(), tokenRequest, IngAuthData.class);
        } catch (HttpStatusCodeException e) {
            handleNon2xxResponseCode(e.getStatusCode());
        }
        return Optional.ofNullable(tokenResponseResponseEntity.getBody()); //NOSONAR restTemplate has internal nonNull assert assuring result is not null
    }

    private Optional<RedirectUrlResponse> fetchRedirectUrlResponse(final String url, final RestTemplate restTemplate, final HttpEntity request) throws TokenInvalidException {
        ResponseEntity<RedirectUrlResponse> tokenResponseResponseEntity = null;
        try {
            tokenResponseResponseEntity = restTemplate.exchange(url, HttpMethod.GET, request, RedirectUrlResponse.class);
        } catch (HttpStatusCodeException e) {
            handleNon2xxResponseCode(e.getStatusCode());
        }
        return Optional.ofNullable(tokenResponseResponseEntity.getBody()); //NOSONAR restTemplate has internal nonNull assert assuring result is not null
    }

    @Data
    private class TokenKey {

        private final AuthenticationMeansReference authenticationMeansReference;
    }

    @Data
    private class TokenEntry {

        private final ReentrantLock lock = new ReentrantLock();
        private IngClientAccessMeans clientAccessToken;
    }
}