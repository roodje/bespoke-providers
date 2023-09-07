package com.yolt.providers.abnamrogroup.common;

import com.yolt.providers.abnamrogroup.abnamro.AbnAmroProperties;
import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthorizationService;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamrogroup.common.data.AbnAmroFetchDataService;
import com.yolt.providers.abnamrogroup.common.data.BankConsentType;
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
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;

@RequiredArgsConstructor
public class AbnAmroDataProvider implements UrlDataProvider {

    private final AbnAmroProperties properties;
    private final AbnAmroAuthorizationService authorizationService;
    private final AbnAmroFetchDataService fetchDataService;
    private final AbnAmroAccessTokenMapper accessTokenMapper;
    private final RestTemplateSupplier restTemplateSupplier;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return AbnAmroAuthenticationMeans.getStringTypedAuthenticationMeansMapForAisAndPis();
    }

    @Override
    public Step getLoginInfo(final UrlGetLoginRequest urlGetLogin) {
        final AbnAmroAuthenticationMeans authenticationMeans = new AbnAmroAuthenticationMeans(urlGetLogin.getAuthenticationMeans());
        final MultiValueMap<String, String> varMap = new LinkedMultiValueMap<>();
        varMap.add("client_id", authenticationMeans.getClientId());
        varMap.add("response_type", "code");
        varMap.add("scope", properties.getAisScope());
        varMap.add("redirect_uri", urlGetLogin.getBaseClientRedirectUrl());
        varMap.add("bank", BankConsentType.NLAA01.name()); // this parameter may be requested from the client in future
        varMap.add("state", urlGetLogin.getState());

        final String loginUrl = UriComponentsBuilder.fromHttpUrl(properties.getOauth2Url()).queryParams(varMap).build().encode().toString();
        return new RedirectStep(loginUrl);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        final AbnAmroAuthenticationMeans authenticationMeans = new AbnAmroAuthenticationMeans(urlCreateAccessMeans.getAuthenticationMeans());
        final RestTemplate restTemplate = restTemplateSupplier.getRestTemplate(urlCreateAccessMeans.getRestTemplateManager(), authenticationMeans, properties);
        try {
            return authorizationService.createNewAccessMeans(authenticationMeans,
                    urlCreateAccessMeans.getUserId(),
                    urlCreateAccessMeans.getRedirectUrlPostedBackFromSite(),
                    restTemplate,
                    properties);
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans) throws TokenInvalidException {
        final AbnAmroAuthenticationMeans authenticationMeans = new AbnAmroAuthenticationMeans(urlRefreshAccessMeans.getAuthenticationMeans());
        final RestTemplate restTemplate = restTemplateSupplier.getRestTemplate(urlRefreshAccessMeans.getRestTemplateManager(), authenticationMeans, properties);
        return authorizationService.refreshAccessMeans(authenticationMeans,
                urlRefreshAccessMeans.getAccessMeans().getAccessMeans(),
                urlRefreshAccessMeans.getAccessMeans().getUserId(),
                restTemplate,
                properties);
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData) throws TokenInvalidException {
        AccessTokenResponseDTO accessToken = accessTokenMapper.tokenFromAccessMeans(urlFetchData.getAccessMeans().getAccessMeans());
        AbnAmroAuthenticationMeans authenticationMeans = new AbnAmroAuthenticationMeans(urlFetchData.getAuthenticationMeans());
        final RestTemplate restTemplate = restTemplateSupplier.getRestTemplate(urlFetchData.getRestTemplateManager(), authenticationMeans, properties);
        List<ProviderAccountDTO> accounts = fetchDataService.fetchAccounts(restTemplate, accessToken,
                urlFetchData.getTransactionsFetchStartTime(), authenticationMeans.getApiKey());
        return new DataProviderResponse(accounts);
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return AbnAmroAuthenticationMeans.getTransportKeyRequirements();
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    @Override
    public String getProviderIdentifier() {
        return "ABN_AMRO";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "ABN AMRO";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_1;
    }
}
