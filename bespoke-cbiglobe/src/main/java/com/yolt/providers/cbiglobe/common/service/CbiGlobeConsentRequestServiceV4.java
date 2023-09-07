package com.yolt.providers.cbiglobe.common.service;


import com.yolt.providers.cbiglobe.common.config.AspspData;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.cbiglobe.common.model.AuthenticationType;
import com.yolt.providers.cbiglobe.common.model.CbiGlobeAccessMeansDTO;
import com.yolt.providers.cbiglobe.common.model.SignatureData;
import com.yolt.providers.cbiglobe.common.util.CbiGlobeHttpHeaderUtil;
import com.yolt.providers.cbiglobe.dto.*;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.yolt.providers.cbiglobe.common.model.AuthenticationType.*;
import static com.yolt.providers.cbiglobe.common.util.CbiGlobeDateUtil.formattedValidityDateTime;
import static java.lang.Boolean.FALSE;
import static org.springframework.http.HttpMethod.PUT;

@AllArgsConstructor
public class CbiGlobeConsentRequestServiceV4 {

    private static final String CONSENTS_ENDPOINT = "/3.0.0/consents";
    private static final String UPDATE_CONSENTS_ENDPOINT = "/2.3.2/consents";
    private static final String CONSENTS_TEMPLATE = "/{consent-id}";

    private static final String ALL_ACCOUNTS_ACCESS = "allAccounts";
    private static final String UPDATE_PSU_DATA = "updatePsuData";

    private final CbiGlobeBaseProperties properties;
    private final Clock clock;

    private final ConsentAccessCreator consentAccessCreator;

    //this method is used to create global one-off consent
    public EstablishConsentResponseType createConsent(RestTemplate restTemplate,
                                                      CbiGlobeAccessMeansDTO accessMeans,
                                                      Boolean recurringIndicator,
                                                      SignatureData signatureData,
                                                      AspspData aspspData) {
        EstablishConsentRequestType consentRequest = new EstablishConsentRequestType();
        consentRequest.setCombinedServiceIndicator(FALSE.toString());
        consentRequest.setRecurringIndicator(String.valueOf(recurringIndicator));
        consentRequest.setValidUntil(formattedValidityDateTime(properties.getConsentValidityInDays(), clock));
        consentRequest.setAccess(new ConsentsAccess().availableAccounts(ALL_ACCOUNTS_ACCESS));

        return processConsentRequest(restTemplate, accessMeans, signatureData, aspspData, consentRequest);
    }

    //this method is used to create detailed recurring consent
    public EstablishConsentResponseType createConsent(RestTemplate restTemplate,
                                                      CbiGlobeAccessMeansDTO accessMeans,
                                                      ProviderAccountDTO accountToConsent,
                                                      Boolean recurringIndicator,
                                                      SignatureData signatureData,
                                                      AspspData aspspData) {
        EstablishConsentRequestType consentRequest = new EstablishConsentRequestType();
        consentRequest.setFrequencyPerDay(properties.getFrequencyPerDay());
        consentRequest.setCombinedServiceIndicator(FALSE.toString());
        consentRequest.setRecurringIndicator(String.valueOf(recurringIndicator));
        consentRequest.setValidUntil(formattedValidityDateTime(properties.getConsentValidityInDays(), clock));
        consentRequest.setAccess(consentAccessCreator.createConsentAccess(accountToConsent));

        return processConsentRequest(restTemplate, accessMeans, signatureData, aspspData, consentRequest);
    }

    private EstablishConsentResponseType processConsentRequest(RestTemplate restTemplate, CbiGlobeAccessMeansDTO accessMeans, SignatureData signatureData, AspspData aspspData, EstablishConsentRequestType consentRequest) {
        HttpHeaders headers = CbiGlobeHttpHeaderUtil.getConsentCreationHeaders(
                accessMeans.getAccessToken(), consentRequest, accessMeans.getCallBackUrl(), signatureData, aspspData, clock);

        EstablishConsentResponseType consent = restTemplate
                .postForEntity(CONSENTS_ENDPOINT, new HttpEntity<>(consentRequest, headers), EstablishConsentResponseType.class)
                .getBody();

        if (consent == null) {
            throw new IllegalStateException("Consent response can't be a null");
        }
        if (isScaRedirectMissing(consent)) {
            UpdateConsentResponseType updatedConsent = updateConsent(restTemplate, accessMeans, signatureData, consent, aspspData.getCode());
            consent.getLinks().setScaRedirect(updatedConsent.getLinks().getScaRedirect());
        }
        return consent;
    }

    private boolean isScaRedirectMissing(EstablishConsentResponseType consent) {
        EstablishConsentResponseTypeLinks links = consent.getLinks();
        return links == null || links.getScaRedirect() == null; //NOSONAR: These links can be a null
    }

    private UpdateConsentResponseType updateConsent(RestTemplate restTemplate,
                                                    CbiGlobeAccessMeansDTO accessMeans,
                                                    SignatureData signatureData,
                                                    EstablishConsentResponseType consentResponse,
                                                    String aspspCode) {
        UpdateConsentRequestType consentRequest = new UpdateConsentRequestType();
        consentRequest.setAuthenticationMethodId(getScaMethodId(consentResponse.getScaMethods()));

        HttpHeaders headers = CbiGlobeHttpHeaderUtil.getConsentUpdateHeaders(
                accessMeans.getAccessToken(), aspspCode, consentRequest, accessMeans.getCallBackUrl(), UPDATE_PSU_DATA, signatureData, clock);

        return restTemplate.exchange(UPDATE_CONSENTS_ENDPOINT + CONSENTS_TEMPLATE, PUT, new HttpEntity<>(consentRequest, headers),
                        UpdateConsentResponseType.class,
                        consentResponse.getConsentId())
                .getBody();
    }

    protected String getScaMethodId(List<EstablishConsentResponseTypeScaMethods> scaMethods) {
        for (AuthenticationType authenticationType : getPreferredAuthenticationTypes()) {
            for (EstablishConsentResponseTypeScaMethods scaMethod : scaMethods) {
                if (authenticationType.name().equals(scaMethod.getAuthenticationType())) {
                    return scaMethod.getAuthenticationMethodId();
                }
            }
        }
        throw new IllegalArgumentException("Unsupported SCA method: " + scaMethods.get(0).getAuthenticationType());
    }

    private List<AuthenticationType> getPreferredAuthenticationTypes() {
        return Arrays.asList(SMS_OTP, PHOTO_OTP, PUSH_OTP);
    }

    public String extractScaRedirectUrl(EstablishConsentResponseType consent) {
        return Optional.of(consent)
                .map(EstablishConsentResponseType::getLinks)
                .map(EstablishConsentResponseTypeLinks::getScaRedirect)
                .map(LinksEstablishconsentType1ScaRedirect::getHref)
                .orElseThrow(() -> new IllegalStateException("SCA redirect URL is missing"));
    }

    public GetConsentResponseType getConsent(RestTemplate restTemplate,
                                             String accessToken,
                                             String consentId,
                                             SignatureData signatureData,
                                             AspspData aspspData) {
        HttpHeaders headers = CbiGlobeHttpHeaderUtil.getConsentHeaders(
                accessToken, signatureData, aspspData, clock);

        return restTemplate.exchange(CONSENTS_ENDPOINT + CONSENTS_TEMPLATE, HttpMethod.GET, new HttpEntity<>(headers),
                        GetConsentResponseType.class,
                        consentId)
                .getBody();
    }
}
