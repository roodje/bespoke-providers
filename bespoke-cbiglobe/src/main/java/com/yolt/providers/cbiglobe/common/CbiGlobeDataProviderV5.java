package com.yolt.providers.cbiglobe.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.AspspData;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.cbiglobe.common.exception.CbiGlobeMalformedObjectException;
import com.yolt.providers.cbiglobe.common.model.CbiGlobeAccessMeansDTO;
import com.yolt.providers.cbiglobe.common.model.SignatureData;
import com.yolt.providers.cbiglobe.common.model.Token;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeAuthorizationService;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeConsentRequestServiceV4;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeFetchDataService;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeHttpClientFactory;
import com.yolt.providers.cbiglobe.common.util.CbiGlobeAspspProductsUtil;
import com.yolt.providers.cbiglobe.common.util.HsmUtils;
import com.yolt.providers.cbiglobe.dto.EstablishConsentResponseType;
import com.yolt.providers.cbiglobe.dto.GetConsentResponseType;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.SelectField;
import nl.ing.lovebird.providershared.form.SelectOptionValue;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@RequiredArgsConstructor
public abstract class CbiGlobeDataProviderV5 implements UrlDataProvider {

    private static final int ONE_HOUR_IN_SECONDS = 3600;

    protected final CbiGlobeAuthorizationService authorizationService;
    protected final CbiGlobeConsentRequestServiceV4 consentRequestService;
    protected final CbiGlobeHttpClientFactory httpClientFactory;
    protected final CbiGlobeFetchDataService fetchDataService;
    protected final CbiGlobeBaseProperties properties;
    protected final ObjectMapper mapper;
    protected final Clock clock;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> authMeans = new HashMap<>();
        authMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        authMeans.put(TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        authMeans.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        authMeans.put(SIGNING_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        authMeans.put(CLIENT_ID_STRING_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        authMeans.put(CLIENT_SECRET_STRING_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        return authMeans;
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmUtils.getKeyRequirements(TRANSPORT_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmUtils.getKeyRequirements(SIGNING_KEY_ID_NAME, SIGNING_CERTIFICATE_NAME);
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

        if (properties.hasSingleAspsp()) {
            //create global one-off consent to get user accounts in case if there is only one aspsp
            SignatureData signatureData = authMeans.getSigningData(request.getSigner());
            AspspData aspspData = properties.getFirstAspspData();
            return establishConsent(restTemplate, accessMeans, null, signatureData, aspspData, FALSE);
        }
        return createFormStep(accessMeans);
    }

    protected String getCallbackUrlWithState(String baseClientRedirectUrl, String state) {
        return UriComponentsBuilder.fromUriString(baseClientRedirectUrl)
                .queryParam("state", state)
                .toUriString();
    }

    private Step createFormStep(CbiGlobeAccessMeansDTO accessMeans) {
        SelectField selectField = new SelectField("bank", "Banca", 0, 0, false);

        for (AspspData aspspData : properties.getAspsps()) {
            selectField.addSelectOptionValue(new SelectOptionValue(aspspData.getCode(), aspspData.getDisplayName()));
        }
        Form selectForm = new Form(Collections.singletonList(selectField), null, null);

        String providerState = toProviderState(accessMeans);
        return new FormStep(selectForm, EncryptionDetails.noEncryption(), Instant.now(clock).plusSeconds(ONE_HOUR_IN_SECONDS), providerState);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest request) {
        CbiGlobeAuthenticationMeans authMeans = getCbiGlobeAuthenticationMeans(request.getAuthenticationMeans(), getProviderIdentifier());
        RestTemplate restTemplate = httpClientFactory.buildRestTemplate(request.getRestTemplateManager(), authMeans);
        SignatureData signatureData = authMeans.getSigningData(request.getSigner());

        CbiGlobeAccessMeansDTO accessMeans;
        try {
            accessMeans = toAccessMeansDTO(request.getProviderState());
        } catch (TokenInvalidException e) {
            throw new CbiGlobeMalformedObjectException(e.getMessage());
        }

        if (isDynamicFlow(request)) {
            accessMeans.setBank(request.getFilledInUserSiteFormValues().get("bank"));
        }

        AspspData aspspData = properties.getAdjustedAspspData(accessMeans);
        accessMeans.setCallBackUrl(getCallbackUrlWithState(request.getBaseClientRedirectUrl(), request.getState()));

        if (isDynamicFlow(request)) {
            return new AccessMeansOrStepDTO(establishConsent(restTemplate, accessMeans, null, signatureData, aspspData, FALSE));
        }


        if (accessMeans.hasNoCachedAccounts()) {
            //use global one-off consent to get all user accounts and create detailed recurring consent for first of it
            String consentStatus = validateConsentStatus(restTemplate, accessMeans.getAccessToken(), accessMeans.getConsentId(), signatureData, aspspData);
            if (!"valid".equals(consentStatus)) {
                throw new GetAccessTokenFailedException("Consent is not valid for getting data. Consent status: " + consentStatus);
            }

            List<ProviderAccountDTO> accounts = fetchDataService.fetchAccounts(
                    restTemplate, accessMeans, signatureData, aspspData.getCode(), request.getPsuIpAddress());
            if (!CollectionUtils.isEmpty(accounts)) {
                accessMeans.setCachedAccounts(accounts);
                return new AccessMeansOrStepDTO(establishConsent(restTemplate, accessMeans, accounts.get(0), signatureData, aspspData, TRUE));
            }
        } else if (accessMeans.getCachedAccounts().size() == accessMeans.getConsentedAccounts().size()) {
            //all accounts processed - check that consents validity
            for (Map.Entry<String, ProviderAccountDTO> consentedAccount : accessMeans.getConsentedAccounts().entrySet()) {
                String consentId = consentedAccount.getKey();
                String consentStatus = validateConsentStatus(restTemplate, accessMeans.getAccessToken(), consentId, signatureData, aspspData);
                if (!"valid".equals(consentStatus)) {
                    accessMeans.removeAccountFromConsentedAccountList(consentId);
                }
            }
        } else {
            //iterate through all user accounts and create detailed recurring consent for each
            Integer nextAccountToProcess = accessMeans.getCurrentlyProcessAccountNumber() + 1;
            accessMeans.setCurrentlyProcessAccountNumber(nextAccountToProcess);
            return new AccessMeansOrStepDTO(establishConsent(restTemplate, accessMeans, accessMeans.getCachedAccounts().get(nextAccountToProcess), signatureData, aspspData, TRUE));
        }
        String providerState = toProviderState(accessMeans);
        return new AccessMeansOrStepDTO(accessMeans.toAccessMeansDTO(request.getUserId(), providerState));
    }

    protected String validateConsentStatus(RestTemplate restTemplate,
                                           String accessToken,
                                           String consentId,
                                           SignatureData signatureData,
                                           AspspData aspspData) {
        GetConsentResponseType consent = consentRequestService.getConsent(
                restTemplate, accessToken, consentId, signatureData, aspspData);

        return consent.getConsentStatus();
    }

    private boolean isDynamicFlow(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        FilledInUserSiteFormValues formValues = urlCreateAccessMeans.getFilledInUserSiteFormValues();
        return formValues != null && !formValues.getValueMap().isEmpty();
    }

    protected RedirectStep establishConsent(RestTemplate restTemplate,
                                            CbiGlobeAccessMeansDTO accessMeans,
                                            ProviderAccountDTO accountToDetailedConsent,
                                            SignatureData signatureData,
                                            AspspData aspspData,
                                            Boolean recurringIndicator) {
        EstablishConsentResponseType consent;

        try {
            if (ObjectUtils.isEmpty(accountToDetailedConsent)) {
                consent = consentRequestService
                        .createConsent(restTemplate, accessMeans, recurringIndicator, signatureData, aspspData);

                accessMeans.setConsentId(consent.getConsentId());
            } else {
                consent = consentRequestService
                        .createConsent(restTemplate, accessMeans, accountToDetailedConsent, recurringIndicator, signatureData, aspspData);
                accessMeans.addConsentForAccount(consent.getConsentId(), accountToDetailedConsent);
            }
            String providerState = toProviderState(accessMeans);
            return new RedirectStep(consentRequestService.extractScaRedirectUrl(consent), null, providerState);
        } catch (RestClientResponseException e) {
            if (e.getResponseBodyAsString().contains("Invalid ASPSP product code")) {
                CbiGlobeAspspProductsUtil.fetchAndLogAspspProductCodes(
                        restTemplate,
                        properties,
                        accessMeans.getAccessToken(),
                        aspspData);
            }
            throw e;
        }
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest request) throws TokenInvalidException {
        throw new TokenInvalidException("Refresh token flow is not supported by CBI Globe");
    }

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        CbiGlobeAuthenticationMeans authMeans = getCbiGlobeAuthenticationMeans(request.getAuthenticationMeans(), getProviderIdentifier());
        RestTemplate restTemplate = httpClientFactory.buildRestTemplate(request.getRestTemplateManager(), authMeans);
        CbiGlobeAccessMeansDTO accessMeans = toAccessMeansDTO(request.getAccessMeans().getAccessMeans());

        Token token = authorizationService.getClientAccessToken(
                restTemplate, request.getAuthenticationMeansReference(), authMeans);

        accessMeans.setAccessToken(token.getAccessToken());
        return fetchDataService.fetchTransactionsForAccounts(
                restTemplate,
                accessMeans,
                request.getTransactionsFetchStartTime(),
                authMeans.getSigningData(request.getSigner()),
                properties.getAdjustedAspspData(accessMeans).getCode(),
                request.getPsuIpAddress());
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }

    protected CbiGlobeAccessMeansDTO toAccessMeansDTO(String providerState) throws TokenInvalidException {
        try {
            return mapper.readValue(providerState, CbiGlobeAccessMeansDTO.class);
        } catch (IOException e) {
            throw new TokenInvalidException("Error while creating access means object");
        }
    }

    protected String toProviderState(final CbiGlobeAccessMeansDTO accessMeansDTO) {
        try {
            return mapper.writeValueAsString(accessMeansDTO);
        } catch (JsonProcessingException e) {
            throw new CbiGlobeMalformedObjectException("Error while creating provider state object");
        }
    }
}
