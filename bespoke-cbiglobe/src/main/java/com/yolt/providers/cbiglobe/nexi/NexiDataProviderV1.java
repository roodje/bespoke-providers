package com.yolt.providers.cbiglobe.nexi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.CbiGlobeDataProviderV5;
import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.AspspData;
import com.yolt.providers.cbiglobe.common.model.CbiGlobeAccessMeansDTO;
import com.yolt.providers.cbiglobe.common.model.SignatureData;
import com.yolt.providers.cbiglobe.common.model.Token;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeAuthorizationService;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeConsentRequestServiceV4;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeFetchDataService;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeHttpClientFactory;
import com.yolt.providers.cbiglobe.common.util.CbiGlobeAspspProductsUtil;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.versioning.ProviderVersion;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

import static com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans;
import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;
import static java.lang.Boolean.FALSE;

@Service
public class NexiDataProviderV1 extends CbiGlobeDataProviderV5 {

    public NexiDataProviderV1(@Qualifier("NexiAuthenticationServiceV1") CbiGlobeAuthorizationService authorizationService,
                              @Qualifier("NexiConsentRequestServiceV1") CbiGlobeConsentRequestServiceV4 consentRequestService,
                              @Qualifier("NexiHttpClientFactoryV1") CbiGlobeHttpClientFactory httpClientFactory,
                              @Qualifier("NexiFetchServiceV1") CbiGlobeFetchDataService fetchDataService,
                              NexiProperties properties,
                              @Qualifier("CbiGlobe") final ObjectMapper mapper,
                              Clock clock) {
        super(authorizationService, consentRequestService, httpClientFactory, fetchDataService, properties, mapper, clock);
    }

    @Override
    public Step getLoginInfo(final UrlGetLoginRequest request) {
        CbiGlobeAuthenticationMeans authMeans = getCbiGlobeAuthenticationMeans(request.getAuthenticationMeans(), getProviderIdentifier());
        RestTemplate restTemplate = httpClientFactory.buildRestTemplate(request.getRestTemplateManager(), authMeans);

        Token token = authorizationService.getClientAccessToken(
                restTemplate, request.getAuthenticationMeansReference(), authMeans);

        CbiGlobeAccessMeansDTO accessMeans = new CbiGlobeAccessMeansDTO(
                token, properties.getConsentValidityInDays(), getCallbackUrlWithState(request.getBaseClientRedirectUrl(), request.getState()), clock);

        CbiGlobeAspspProductsUtil.fetchAndLogAspspsProductCodesIfEmpty(restTemplate, token.getAccessToken(), properties);

        SignatureData signatureData = authMeans.getSigningData(request.getSigner());
        AspspData aspspData = properties.getFirstAspspData();
        return establishConsent(restTemplate, accessMeans, null, signatureData, aspspData, FALSE);
    }

    @Override
    public String getProviderIdentifier() {
        return "NEXI";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Nexi";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_1;
    }
}
