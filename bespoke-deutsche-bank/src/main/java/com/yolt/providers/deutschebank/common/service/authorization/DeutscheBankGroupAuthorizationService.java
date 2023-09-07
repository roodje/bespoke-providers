package com.yolt.providers.deutschebank.common.service.authorization;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupDateConverter;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import com.yolt.providers.deutschebank.common.domain.model.consent.ConsentCreationResponse;
import com.yolt.providers.deutschebank.common.domain.model.consent.ConsentStatusResponse;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClient;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupProviderStateMapper;
import com.yolt.providers.deutschebank.common.service.authorization.consent.DeutscheBankGroupConsentRequestStrategy;
import com.yolt.providers.deutschebank.common.service.authorization.form.DeutscheBankGroupFormStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class DeutscheBankGroupAuthorizationService {

    private static final String CONSENT_ENDPOINT = "/v1/consents";
    private static final String STATE = "state";
    private static final String ERROR = "error";

    private final DeutscheBankGroupDateConverter dateConverter;
    private final DeutscheBankGroupFormStrategy formStrategy;
    private final DeutscheBankGroupConsentRequestStrategy consentRequestStrategy;
    private final DeutscheBankGroupProviderStateMapper providerStateMapper;
    private final Clock clock;

    public FormStep createFormStepToRetrievePsuId() {
        return formStrategy.createForm();
    }

    public boolean isFormStep(FilledInUserSiteFormValues formValues) {
        return formValues != null && !formValues.getValueMap().isEmpty();
    }

    public AccessMeansOrStepDTO createRedirectStepToInitiatedConsentPage(DeutscheBankGroupHttpClient httpClient,
                                                                         UrlCreateAccessMeansRequest request) {
        ConsentCreationResponse response = createConsent(httpClient, request);
        String providerState = providerStateMapper.toJson(new DeutscheBankGroupProviderState(response.getConsentId()));
        return new AccessMeansOrStepDTO(new RedirectStep(response.getScaRedirectUrl(), null, providerState));
    }

    public ConsentCreationResponse createConsent(DeutscheBankGroupHttpClient httpClient,
                                                 UrlCreateAccessMeansRequest request) {
        String redirectUrl = createRedirectUrl(request.getBaseClientRedirectUrl(), request.getState());
        String nokRedirectUrl = createNokRedirectUri(redirectUrl);
        try {
            return httpClient.postConsentCreation(
                    CONSENT_ENDPOINT,
                    consentRequestStrategy.createConsentRequest(getExpirationDate()),
                    request.getPsuIpAddress(),
                    formStrategy.getPsuId(request.getFilledInUserSiteFormValues()),
                    redirectUrl,
                    nokRedirectUrl);
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    private String createRedirectUrl(String baseRedirectUrl, String state) {
        return UriComponentsBuilder.fromUriString(baseRedirectUrl)
                .queryParam(STATE, state)
                .toUriString();
    }

    private String createNokRedirectUri(String redirectUrl) {
        return UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam(ERROR, "true")
                .toUriString();
    }

    public AccessMeansOrStepDTO createAccessMeans(UrlCreateAccessMeansRequest request) {
        String error = extractErrorQueryParamFromRedirectUri(request.getRedirectUrlPostedBackFromSite());
        if (StringUtils.hasText(error)) {
            throw new MissingDataException("User failed to authenticate on consent page");
        }
        try {
            DeutscheBankGroupProviderState providerState = providerStateMapper.fromJson(request.getProviderState());
            Date expirationDate = dateConverter.toDate(getExpirationDate());

            return new AccessMeansOrStepDTO(providerStateMapper.toAccessMeansDTO(request.getUserId(), providerState, expirationDate));
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
    }

    public AccessMeansOrStepDTO createAccessMeans(DeutscheBankGroupProviderState providerState,
                                                  UUID userId) {
        Date expirationDate = dateConverter.toDate(getExpirationDate());
        return new AccessMeansOrStepDTO(providerStateMapper.toAccessMeansDTO(userId, providerState, expirationDate));
    }

    private String extractErrorQueryParamFromRedirectUri(String uri) {
        return UriComponentsBuilder.fromUriString(uri)
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get(ERROR);
    }

    private LocalDate getExpirationDate() {
        return LocalDate.now(clock).plusDays(89);
    }

    public ConsentStatusResponse getConsentStatus(DeutscheBankGroupHttpClient httpClient,
                                                  String consentId,
                                                  String psuIpAddress) throws TokenInvalidException {
        return httpClient.getConsentStatus(CONSENT_ENDPOINT + "/" + consentId, psuIpAddress);
    }
}
