package com.yolt.providers.rabobank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.ProviderMetaData;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.rabobank.config.RabobankProperties;
import com.yolt.providers.rabobank.dto.Access;
import com.yolt.providers.rabobank.dto.AccessTokenResponseDTO;
import com.yolt.providers.rabobank.dto.AccountConsentData;
import com.yolt.providers.rabobank.http.RabobankAisHttpClient;
import com.yolt.providers.rabobank.http.RabobankAisHttpClientFactory;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.yolt.providers.rabobank.RabobankAuthenticationMeans.*;
import static com.yolt.providers.rabobank.RabobankObjectMapperV3.OBJECT_MAPPER;

@RequiredArgsConstructor
public class RabobankDataProvider implements UrlDataProvider {

    private final Clock clock;
    private final RabobankProperties properties;
    private final RabobankAisHttpClientFactory httpClientFactory;
    private final RabobankAuthenticationService authenticationService;
    private final RabobankAccountsAndTransactionsService accountsAndTransactionsService;
    private final ProviderVersion version;

    private static final String METADATA_CONSENT_ID_EXTRACTION_PART = "a:consentId ";


    @Override
    public ProviderMetaData getProviderMetadata() {
        return ProviderMetaData.builder().maximumRedirectUrlsPerAuthenticationMeans(1).build();
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return getTypedAuthenticationMeansForAISAndPIS();
    }

    @Override
    public RedirectStep getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        RabobankAuthenticationMeans authenticationMeans = fromAISAuthenticationMeans(urlGetLogin.getAuthenticationMeans());
        String loginUrl = authenticationService.generateAuthorizationUrl(authenticationMeans.getClientId(),
                urlGetLogin.getBaseClientRedirectUrl(),
                urlGetLogin.getState());
        return new RedirectStep(loginUrl);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        RabobankAuthenticationMeans authenticationMeans = fromAISAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans());
        RestTemplate restTemplate = RestTemplateSupplierV3.getRestTemplate(urlCreateAccessMeans.getRestTemplateManager(), authenticationMeans, properties);
        AccessTokenResponseDTO token = authenticationService.getAccessToken(authenticationMeans, restTemplate, urlCreateAccessMeans.getRedirectUrlPostedBackFromSite());
        return new AccessMeansOrStepDTO(accessMeansFromToken(urlCreateAccessMeans.getUserId(), token));
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        AccessMeansDTO accessMeans = urlRefreshAccessMeans.getAccessMeans();
        AccessTokenResponseDTO oldToken = tokenFromAccessMeans(accessMeans.getAccessMeans());
        RabobankAuthenticationMeans authenticationMeans = fromAISAuthenticationMeans(urlRefreshAccessMeans.getAuthenticationMeans());
        RabobankAisHttpClient httpClient = httpClientFactory.createRabobankHttpClient(urlRefreshAccessMeans.getRestTemplateManager(), authenticationMeans);
        AccessTokenResponseDTO token = authenticationService.refreshToken(authenticationMeans,
                httpClient,
                oldToken);
        return accessMeansFromToken(accessMeans.getUserId(), token);
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws TokenInvalidException, ProviderFetchDataException {
        AccessTokenResponseDTO accessToken = tokenFromAccessMeans(urlFetchData.getAccessMeans().getAccessMeans());
        RabobankAuthenticationMeans authenticationMeans = fromAISAuthenticationMeans(urlFetchData.getAuthenticationMeans());

        RestTemplate restTemplate = RestTemplateSupplierV3.getRestTemplate(urlFetchData.getRestTemplateManager(), authenticationMeans, properties);

        Instant from = urlFetchData.getTransactionsFetchStartTime();
        Integer transactionsFetchStartTimeMaxDays = properties.getTransactionsFetchStartTimeMaxDays();
        if (transactionsFetchStartTimeMaxDays != null) {
            Instant maximumFromDate = Instant.now(clock).minus(transactionsFetchStartTimeMaxDays, ChronoUnit.DAYS);
            if (from.isBefore(maximumFromDate)) {
                from = maximumFromDate;
            }
        }
        try {
            List<ProviderAccountDTO> accounts = accountsAndTransactionsService.getAccountsAndTransactions(
                    restTemplate,
                    from,
                    urlFetchData.getPsuIpAddress(),
                    accessToken,
                    authenticationMeans,
                    urlFetchData.getSigner());
            return new DataProviderResponse(accounts);
        } catch (CertificateEncodingException e) {
            throw new ProviderFetchDataException(e);
        } catch (HttpStatusCodeException e) {
            HttpStatus status = e.getStatusCode();
            if (HttpStatus.TOO_MANY_REQUESTS.equals(e.getStatusCode()) && StringUtils.isEmpty(urlFetchData.getPsuIpAddress())) {
                throw new BackPressureRequestException(e.getStatusCode().getReasonPhrase() + " " + e.getStatusCode().value());
            } else if (HttpStatus.UNAUTHORIZED.equals(status)) {
                if (e.getResponseBodyAsString().contains("This server could not verify that you are authorized to access the URL")) {
                    throw new ProviderFetchDataException("Rabobank token synchronisation problem on their side");
                }
                throw new TokenInvalidException("Token was invalid or expired.");
            } else if (HttpStatus.FORBIDDEN.equals(status)) {
                String consentId = extractConsentId(accessToken.getMetadata());
                if (StringUtils.isNotBlank(consentId)) {
                    validateConsentStatus(restTemplate, authenticationMeans, consentId, urlFetchData.getSigner());
                } else {
                    throw new TokenInvalidException("Couldn't check the consent details, assuming it's expired");
                }
            }
            throw e;
        }
    }

    private String extractConsentId(String metadata) {
        if (metadata != null && metadata.contains(METADATA_CONSENT_ID_EXTRACTION_PART)) {
            return metadata.substring(metadata.indexOf(METADATA_CONSENT_ID_EXTRACTION_PART) + METADATA_CONSENT_ID_EXTRACTION_PART.length());
        }
        return null;
    }

    private void validateConsentStatus(RestTemplate restTemplate,
                                       RabobankAuthenticationMeans authMeans,
                                       String consentId,
                                       Signer signer) throws TokenInvalidException {

        GetConsentResponse consentResponse = authenticationService.getConsent(authMeans, restTemplate, signer, consentId);
        Access access = consentResponse.getAccess();
        validateAccountConsentData(access.getBalancesRead());
        validateAccountConsentData(access.getTransactionsRead());
        validateAccountConsentData(access.getTransactionsHistoryRead());
    }

    private void validateAccountConsentData(List<AccountConsentData> accountConsentData) throws TokenInvalidException {
        for (AccountConsentData data : accountConsentData) {
            if (!"valid".equals(data.getStatus())) {
                throw new TokenInvalidException("Consent is expired");
            }
        }
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmUtils.getKeyRequirements(CLIENT_SIGNING_KEY_ID, CLIENT_SIGNING_CERTIFICATE);
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(CLIENT_TRANSPORT_KEY_ID, CLIENT_TRANSPORT_CERTIFICATE);
    }

    AccessMeansDTO accessMeansFromToken(final UUID userId, final AccessTokenResponseDTO accessToken) {
        try {
            // Convert time to live to expiry date
            Date expiryDate = Date.from(Instant.now(clock).plusSeconds(accessToken.getExpiresIn()));
            String accessMeansValue = OBJECT_MAPPER.writeValueAsString(accessToken);
            return new AccessMeansDTO(userId, accessMeansValue, new Date(), expiryDate);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to write access token.");
        }
    }

    AccessTokenResponseDTO tokenFromAccessMeans(final String accessMeans) throws TokenInvalidException {
        try {
            return OBJECT_MAPPER.readValue(accessMeans, AccessTokenResponseDTO.class);
        } catch (IOException e) {
            throw new TokenInvalidException();
        }
    }

    @Override
    public String getProviderIdentifier() {
        return "RABOBANK";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Rabobank";
    }

    @Override
    public ProviderVersion getVersion() {
        return version;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
