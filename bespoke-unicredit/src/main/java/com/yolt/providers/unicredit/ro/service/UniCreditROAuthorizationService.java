package com.yolt.providers.unicredit.ro.service;

import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans;
import com.yolt.providers.unicredit.common.config.UniCreditBaseProperties;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditAuthMeansMapper;
import com.yolt.providers.unicredit.common.data.transformer.ProviderStateTransformer;
import com.yolt.providers.unicredit.common.dto.ConsentRequestDTO;
import com.yolt.providers.unicredit.common.dto.ConsentResponseDTO;
import com.yolt.providers.unicredit.common.dto.UniCreditAccessMeansDTO;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClient;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClientFactory;
import com.yolt.providers.unicredit.common.service.UniCreditAuthorizationService;
import com.yolt.providers.unicredit.common.util.ProviderInfo;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.TextField;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;

@RequiredArgsConstructor
public class UniCreditROAuthorizationService implements UniCreditAuthorizationService {

    private static final int CONSENT_VALID_DAYS = 89;
    private static final String IBAN_FORM_FIELD_ID = "Iban";
    private static final String IBAN_FORM_FIELD_DISPLAY_NAME = "IBAN";
    private static final ZoneId BUCHAREST_ZONE_ID = ZoneId.of("Europe/Bucharest");
    private static final String REDIRECT_STATE_QUERY_PARAM = "state";
    private static final int ROMANIAN_IBAN_LENGTH = 24;

    private final UniCreditHttpClientFactory httpClientFactory;
    private final UniCreditAuthMeansMapper authMeansMapper;
    private final UniCreditBaseProperties properties;
    private final ProviderStateTransformer<UniCreditAccessMeansDTO> stateTransformer;
    private final Clock clock;

    @Override
    public Step getLoginInfo(final UrlGetLoginRequest urlGetLogin, final ProviderInfo providerInfo) {
        TextField ibanTextField = new TextField(IBAN_FORM_FIELD_ID, IBAN_FORM_FIELD_DISPLAY_NAME, ROMANIAN_IBAN_LENGTH, ROMANIAN_IBAN_LENGTH, false, false);
        Form form = new Form(Collections.singletonList(ibanTextField), null, null);
        return new FormStep(form, EncryptionDetails.noEncryption(), Instant.now(clock).plus(Duration.ofHours(1)), null);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans, final ProviderInfo providerInfo) {
        return ObjectUtils.isEmpty(urlCreateAccessMeans.getFilledInUserSiteFormValues()) ? createAccessMeans(urlCreateAccessMeans) : getLoginUrl(urlCreateAccessMeans, providerInfo);
    }

    private AccessMeansOrStepDTO getLoginUrl(final UrlCreateAccessMeansRequest urlCreateAccessMeansRequest, final ProviderInfo providerInfo) {
        UniCreditAuthMeans uniCreditAuthMeans = authMeansMapper.fromBasicAuthenticationMeans(urlCreateAccessMeansRequest.getAuthenticationMeans(), providerInfo.getIdentifier());
        UniCreditHttpClient httpClient = httpClientFactory.createHttpClient(uniCreditAuthMeans, urlCreateAccessMeansRequest.getRestTemplateManager(), providerInfo.getDisplayName(), properties.getBaseUrl());

        Instant consentExpiration = LocalDate.now(clock).atStartOfDay(BUCHAREST_ZONE_ID).toInstant().plus(CONSENT_VALID_DAYS, ChronoUnit.DAYS);
        String iban = urlCreateAccessMeansRequest.getFilledInUserSiteFormValues().get(IBAN_FORM_FIELD_ID);
        String redirectUrl = UriComponentsBuilder.fromHttpUrl(urlCreateAccessMeansRequest.getBaseClientRedirectUrl())
                .queryParam(REDIRECT_STATE_QUERY_PARAM, urlCreateAccessMeansRequest.getState())
                .build()
                .toUriString();

        try {
            ConsentResponseDTO consentResponseDTO = httpClient.generateConsent(
                    ConsentRequestDTO.createDetailedConsentRequest(iban, new Date(consentExpiration.toEpochMilli()), properties.getFrequencyPerDay(), true),
                    urlCreateAccessMeansRequest.getPsuIpAddress(),
                    redirectUrl,
                    providerInfo.getIdentifier());
            UniCreditAccessMeansDTO uniCreditAccessMeansDTO = new UniCreditAccessMeansDTO(consentResponseDTO.getConsentId(), Instant.now(clock), consentExpiration);
            String providerState = stateTransformer.transformToString(uniCreditAccessMeansDTO);

            return new AccessMeansOrStepDTO(new RedirectStep(consentResponseDTO.getConsentUrl(), null, providerState));
        } catch (TokenInvalidException ex) {
            throw new GetAccessTokenFailedException("Cannot generate consent", ex);
        }
    }

    private AccessMeansOrStepDTO createAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeansRequest) {
        UniCreditAccessMeansDTO uniCreditAccessMeans = stateTransformer.transformToObject(urlCreateAccessMeansRequest.getProviderState());
        return new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        urlCreateAccessMeansRequest.getUserId(),
                        urlCreateAccessMeansRequest.getProviderState(),
                        new Date(uniCreditAccessMeans.getCreated().toEpochMilli()),
                        new Date(uniCreditAccessMeans.getExpireTime().toEpochMilli())
                )
        );
    }

    @Override
    public AccessMeansDTO refreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans, ProviderInfo providerInfo) throws TokenInvalidException {
        throw new TokenInvalidException("Refresh access means is not supported by UniCredit (RO)");
    }
}
