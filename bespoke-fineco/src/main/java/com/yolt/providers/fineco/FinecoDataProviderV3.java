package com.yolt.providers.fineco;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.fineco.auth.FinecoAuthenticationMeans;
import com.yolt.providers.fineco.auth.FinecoAuthorizationServiceV2;
import com.yolt.providers.fineco.data.FinecoFetchDataServiceV3;
import com.yolt.providers.fineco.dto.FinecoAccessMeans;
import com.yolt.providers.fineco.exception.FinecoMalformedException;
import com.yolt.providers.fineco.util.HsmUtils;
import com.yolt.providers.fineco.v2.dto.ConsentsResponse201;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_3;
import static com.yolt.providers.fineco.auth.FinecoAuthenticationMeans.*;

@Service
public class FinecoDataProviderV3 implements UrlDataProvider {

    private final FinecoAuthorizationServiceV2 authorizationService;
    private final FinecoFetchDataServiceV3 fetchDataService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public FinecoDataProviderV3(final FinecoAuthorizationServiceV2 authorizationService,
                                final FinecoFetchDataServiceV3 fetchDataService,
                                @Qualifier("FinecoObjectMapper") final ObjectMapper objectMapper,
                                final Clock clock) {
        this.authorizationService = authorizationService;
        this.fetchDataService = fetchDataService;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        final Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);

        return typedAuthenticationMeans;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(CLIENT_TRANSPORT_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_NAME);
    }

    @Override
    public DataProviderResponse fetchData(UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        FinecoAuthenticationMeans authenticationMeans = FinecoAuthenticationMeans.fromAuthenticationMeans(
                urlFetchData.getAuthenticationMeans(), getProviderIdentifier());

        FinecoAccessMeans accessMeansDTO = deserializeAccessMeans(urlFetchData.getAccessMeans().getAccessMeans());

        return fetchDataService.fetchData(urlFetchData, authenticationMeans, accessMeansDTO, getProviderIdentifierDisplayName());
    }

    @Override
    public Step getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        FinecoAuthenticationMeans authenticationMeans = FinecoAuthenticationMeans.fromAuthenticationMeans(urlGetLogin.getAuthenticationMeans(), getProviderIdentifier());
        String redirectUrl = urlGetLogin.getBaseClientRedirectUrl();
        String loginState = urlGetLogin.getState();
        ConsentsResponse201 consentIdWithRedirectUrl = authorizationService.getConsentIdWithRedirectUrl(urlGetLogin.getRestTemplateManager(), authenticationMeans, redirectUrl + "?state=" + loginState, urlGetLogin.getPsuIpAddress());

        Instant createTime = LocalDate.now(clock).atStartOfDay(ZoneId.of("Europe/Rome")).toInstant();
        Instant expireTime = createTime.plus(90, ChronoUnit.DAYS);
        FinecoAccessMeans finecoAccessMeans = new FinecoAccessMeans(consentIdWithRedirectUrl.getConsentId(), createTime, expireTime);
        return new RedirectStep(consentIdWithRedirectUrl.getLinks().getScaRedirect(), null, serializeAccessMeans(finecoAccessMeans));
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        String providerState = urlCreateAccessMeans.getProviderState();
        FinecoAccessMeans finecoAccessMeans = deserializeAccessMeans(providerState);

        return new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        urlCreateAccessMeans.getUserId(),
                        providerState,
                        new Date(finecoAccessMeans.getConsentCreateTime().toEpochMilli()),
                        new Date(finecoAccessMeans.getConsentExpireTime().toEpochMilli())
                )
        );
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        throw new TokenInvalidException("Refresh access means is not supported by Fineco");
    }

    @Override
    public String getProviderIdentifier() {
        return "FINECO";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Fineco";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_3;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    private String serializeAccessMeans(final FinecoAccessMeans finecoAccessMeans) {
        try {
            return objectMapper.writeValueAsString(finecoAccessMeans);
        } catch (IOException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    private FinecoAccessMeans deserializeAccessMeans(final String accessMean) {
        try {
            return objectMapper.readValue(accessMean, FinecoAccessMeans.class);
        } catch (IOException e) {
            throw new FinecoMalformedException("Unable to obtain token from access means", e);
        }
    }
}
