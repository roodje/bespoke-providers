package com.yolt.providers.fineco.rest;

import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.fineco.errorhandling.FetchDataErrorHandler;
import com.yolt.providers.fineco.v2.dto.ConsentInformationResponse200;
import com.yolt.providers.fineco.v2.dto.Consents;
import com.yolt.providers.fineco.v2.dto.ConsentsResponse201;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationTargetException;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class FinecoHttpClientV2 {

    private static final String TPP_REDIRECT_URI_HEADER = "TPP-Redirect-URI";
    private static final String PSU_ID_HEADER = "PSU-ID";
    private static final String CONSENT_ID_HEADER = "Consent-ID";

    private final RestTemplate restTemplate;

    public ConsentsResponse201 generateConsentUrl(final Consents consentObject,
                                                  final String tppRedirectUri,
                                                  final String clientId,
                                                  final String relativeUrl,
                                                  final String psuIpAddress) {
        HttpHeaders headers = createDefaultHeaders(psuIpAddress);
        headers.add(TPP_REDIRECT_URI_HEADER, tppRedirectUri);
        headers.add(PSU_ID_HEADER, clientId);

        return restTemplate.postForEntity(
                relativeUrl,
                new HttpEntity<>(consentObject, headers),
                ConsentsResponse201.class).getBody();
    }

    public <T> T getAccounts(final String consentId,
                             final String psuIpAddress,
                             final String relativeUrl,
                             final Class<T> responseType) throws TokenInvalidException, ProviderFetchDataException {
        return genericGetForFetchData(consentId, psuIpAddress, relativeUrl, responseType);
    }

    public <T> T getTransactionsForAccount(final String consentId,
                                           final String psuIpAddress,
                                           final String relativeUrl,
                                           final Class<T> responseType) throws TokenInvalidException, ProviderFetchDataException {
        return genericGetForFetchData(consentId, psuIpAddress, relativeUrl, responseType);
    }

    public <T> T getTransactionsForNextAccount(final String consentId,
                                               final String psuIpAddress,
                                               final String absoluteUrl,
                                               final Class<T> responseType) throws TokenInvalidException, ProviderFetchDataException {
        return genericGetForFetchData(consentId, psuIpAddress, absoluteUrl, responseType);
    }

    public <T> T getBalancesForAccount(final String consentId,
                                       final String psuIpAddress,
                                       final String relativeUrl,
                                       final Class<T> responseType) throws TokenInvalidException, ProviderFetchDataException {
        return genericGetForFetchData(consentId, psuIpAddress, relativeUrl, responseType);
    }

    public ConsentInformationResponse200 getConsentInformation(final String psuIpAddress,
                                                               final String relativeUrl) throws TokenInvalidException, ProviderFetchDataException {
        try {
            return restTemplate.exchange(
                    relativeUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(createDefaultHeaders(psuIpAddress)),
                    ConsentInformationResponse200.class).getBody();
        } catch (HttpStatusCodeException e) {
            FetchDataErrorHandler.handleNon2xxResponseCodeFetchData(e, psuIpAddress);
            throw e;
        }
    }

    private <T> T genericGetForFetchData(final String consentId,
                                         final String psuIpAddress,
                                         final String url,
                                         final Class<T> responseType) throws TokenInvalidException, ProviderFetchDataException {
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createFetchDataDefaultHeaders(consentId, psuIpAddress)),
                    responseType).getBody();
        } catch (HttpStatusCodeException e) {
            FetchDataErrorHandler.handleNon2xxResponseCodeFetchData(e, psuIpAddress);
        }
        return createEmptyResponse(responseType);
    }

    private <T> T createEmptyResponse(final Class<T> responseType) throws ProviderFetchDataException {
        try {
            return responseType.getDeclaredConstructor((Class[]) new Class[]{}).newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ProviderFetchDataException("Empty instance not created in generic code");
        }
    }

    // According to fineco documentation : X-Request-ID is unique per request:
    // "ID of the request, unique to the call, as determined by the initiating party."
    // We won't be able to trace request on base of request_trace_id field.
    private HttpHeaders createDefaultHeaders(final String psuIpAddress) {
        HttpHeaders headers = new HttpHeaders();

        if (psuIpAddress != null) {
            headers.add("PSU-IP-Address", psuIpAddress);
        }

        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private HttpHeaders createFetchDataDefaultHeaders(final String consentId,
                                                      final String psuIpAddress) {
        HttpHeaders headers = createDefaultHeaders(psuIpAddress);
        headers.add(CONSENT_ID_HEADER, consentId);
        return headers;
    }
}