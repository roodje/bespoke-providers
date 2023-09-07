package com.yolt.providers.monorepogroup.olbgroup.common.service.authorization;

import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.olbgroup.common.auth.OlbGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.OlbGroupProviderState;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.consent.Access;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.consent.ConsentCreationRequest;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.model.consent.ConsentCreationResponse;
import com.yolt.providers.monorepogroup.olbgroup.common.http.OlbGroupHttpClient;
import com.yolt.providers.monorepogroup.olbgroup.common.http.OlbGroupHttpClientFactory;
import com.yolt.providers.monorepogroup.olbgroup.common.mapper.OlbGroupDateConverter;
import com.yolt.providers.monorepogroup.olbgroup.common.mapper.OlbGroupProviderStateMapper;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.TextField;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.HOURS;

@RequiredArgsConstructor
public class OlbGroupAuthorizationServiceV1 implements OlbGroupAuthorizationService {

    private static final String STATE = "state";
    private static final String ERROR = "error";

    private static final String USERNAME_FIELD_ID = "username";
    private static final String USERNAME_FIELD_NAME = "Username";

    private final OlbGroupProviderStateMapper providerStateMapper;
    private final Clock clock;
    private final OlbGroupDateConverter dateConverter;
    private final OlbGroupHttpClientFactory httpClientFactory;
    private final String providerDisplayName;

    @Override
    public boolean isFormStep(FilledInUserSiteFormValues formValues) {
        return formValues != null && !formValues.getValueMap().isEmpty();
    }

    @Override
    public FormStep createFormStepToRetrievePsuId() {
        var usernameField = new TextField(USERNAME_FIELD_ID, USERNAME_FIELD_NAME, 0, 255, false, false);
        var form = new Form();
        form.setFormComponents(Collections.singletonList(usernameField));
        return new FormStep(form, EncryptionDetails.noEncryption(), Instant.now(clock).plus(1L, HOURS), null);
    }

    @Override
    public AccessMeansOrStepDTO createRedirectStep(OlbGroupAuthenticationMeans authMeans,
                                                   RestTemplateManager restTemplateManager,
                                                   String baseClientRedirectUrl,
                                                   String state,
                                                   String psuIpAddress,
                                                   FilledInUserSiteFormValues formValues) {
        var httpClient = httpClientFactory.createHttpClient(authMeans, restTemplateManager, providerDisplayName);
        ConsentCreationResponse response = createConsent(httpClient, baseClientRedirectUrl, state, psuIpAddress, formValues);
        String providerState = providerStateMapper.toJson(new OlbGroupProviderState(response.getConsentId()));
        return new AccessMeansOrStepDTO(new RedirectStep(response.getScaRedirectUrl(), null, providerState));
    }

    @Override
    public AccessMeansOrStepDTO createAccessMeans(String redirectUrlPostedBackFromSite,
                                                  String providerState,
                                                  UUID userId) {
        String error = UriComponentsBuilder.fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get(ERROR);

        if (StringUtils.hasText(error)) {
            throw new MissingDataException("User failed to authenticate on consent page");
        }

        OlbGroupProviderState providerStateDto = providerStateMapper.fromJson(providerState);
        Date expirationDate = dateConverter.toDate(getExpirationDate());
        Date updated = dateConverter.toDate(getCurrentDate());

        return new AccessMeansOrStepDTO(providerStateMapper.toAccessMeansDTO(userId, providerStateDto, updated, expirationDate));
    }

    public void deleteConsent(OlbGroupAuthenticationMeans authMeans,
                              AccessMeansDTO accessMeans,
                              RestTemplateManager restTemplateManager) throws TokenInvalidException {
        var providerState = providerStateMapper.fromJson(accessMeans.getAccessMeans());
        var httpClient = httpClientFactory.createHttpClient(authMeans, restTemplateManager, providerDisplayName);
        httpClient.deleteConsent(providerState.getConsentId());
    }

    private ConsentCreationResponse createConsent(OlbGroupHttpClient httpClient,
                                                  String baseClientRedirectUrl,
                                                  String state,
                                                  String psuIpAddress,
                                                  FilledInUserSiteFormValues formValues) {
        var redirectUrl = UriComponentsBuilder.fromUriString(baseClientRedirectUrl)
                .queryParam(STATE, state)
                .toUriString();

        var consentRequest = ConsentCreationRequest.builder()
                .access(Access.builder()
                        .allPsd2("allAccounts")
                        .build())
                .recurringIndicator(true)
                .validUntil(getExpirationDate().toString())
                .frequencyPerDay(4)
                .combinedServiceIndicator(false)
                .build();

        var psuId = formValues.get(USERNAME_FIELD_ID);
        if (Objects.isNull(psuId)) {
            throw new IllegalStateException("Cannot compose PSU-ID based on given Form");
        }

        try {
            return httpClient.createConsent(
                    consentRequest,
                    psuIpAddress,
                    psuId,
                    redirectUrl);
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    private LocalDate getExpirationDate() {
        return LocalDate.now(clock).plusDays(89);
    }

    private LocalDate getCurrentDate() {
        return LocalDate.now(clock);
    }
}
