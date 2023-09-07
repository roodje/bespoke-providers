package com.yolt.providers.consorsbankgroup.common.ais.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.consorsbankgroup.dto.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DefaultRestClient {

    private static final String TPP_OK_REDIRECT_URI_HEADER_NAME = "TPP-Redirect-URI";
    private static final String PSU_IP_ADDRESS_HEADER_NAME = "PSU-IP-Address";
    private static final String CONSENT_ID_HEADER_NAME = "Consent-ID";

    private static final String CONSENTS_ENDPOINT = "/v1/consents";
    private static final String DELETE_CONSENT_ENDPOINT = "/v1/consents/{consentId}";
    private static final String GET_ACCOUNTS_ENDPOINT = "/v1/accounts";
    private static final String GET_TRANSACTIONS_ENDPOINT = "/v1/accounts/{accountId}/transactions";
    private static final String GET_BALANCES_ENDPOINT = "/v1/accounts/{accountId}/balances";
    private static final String ACCOUNT_ID_PARAMETER_NAME = "accountId";
    private static final String DATE_FROM_PARAMETER_NAME = "dateFrom";
    private static final String BOOKING_STATUS_PARAMETER_NAME = "bookingStatus";
    private static final String BOOKING_STATUS_BOTH = "both";

    public ConsentsResponse201 generateConsentUrl(final Consents requestBody,
                                                  final String redirectUrl,
                                                  final String psuIpAddress,
                                                  final HttpClient httpClient) throws TokenInvalidException {
        HttpHeaders headers = prepareHeaders(psuIpAddress);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add(TPP_OK_REDIRECT_URI_HEADER_NAME, redirectUrl);

        return httpClient.exchange(CONSENTS_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                ProviderClientEndpoints.RETRIEVE_ACCOUNT_ACCESS_CONSENT,
                ConsentsResponse201.class
        ).getBody();
    }

    public void deleteConsent(final String consentId,
                              final String psuIpAddress,
                              final HttpClient httpClient) throws TokenInvalidException {
        HttpHeaders headers = prepareHeaders(psuIpAddress);

        httpClient.exchange(DELETE_CONSENT_ENDPOINT,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT,
                Void.class,
                consentId
        );
    }

    public AccountList getAccounts(final String consentId,
                                   final String psuIpAddress,
                                   final HttpClient httpClient) throws TokenInvalidException {
        HttpHeaders headers = prepareHeaders(psuIpAddress);
        headers.add(CONSENT_ID_HEADER_NAME, consentId);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return httpClient.exchange(GET_ACCOUNTS_ENDPOINT,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ProviderClientEndpoints.GET_ACCOUNTS,
                AccountList.class).getBody();
    }

    public ReadAccountBalanceResponse200 getBalances(final String accountId,
                                                     final String consentId,
                                                     final String psuIpAddress,
                                                     final HttpClient httpClient) throws TokenInvalidException {
        HttpHeaders headers = prepareHeaders(psuIpAddress);
        headers.add(CONSENT_ID_HEADER_NAME, consentId);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        String uri = UriComponentsBuilder.fromPath(GET_BALANCES_ENDPOINT)
                .uriVariables(Map.of(ACCOUNT_ID_PARAMETER_NAME, accountId))
                .build()
                .toUriString();

        return httpClient.exchange(uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID,
                ReadAccountBalanceResponse200.class).getBody();
    }

    public TransactionsResponse200Json getFirstPageOfTransactions(final String accountId,
                                                                  final String consentId,
                                                                  final String psuIpAddress,
                                                                  final LocalDate dateFrom,
                                                                  final HttpClient httpClient) throws TokenInvalidException {
        String uri = UriComponentsBuilder.fromPath(GET_TRANSACTIONS_ENDPOINT)
                .uriVariables(Map.of(ACCOUNT_ID_PARAMETER_NAME, accountId))
                .queryParam(DATE_FROM_PARAMETER_NAME, dateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .queryParam(BOOKING_STATUS_PARAMETER_NAME, BOOKING_STATUS_BOTH)
                .build()
                .toUriString();

        return getPageOfTransactions(consentId, psuIpAddress, uri, httpClient);
    }

    public TransactionsResponse200Json getPageOfTransactions(final String consentId,
                                                             final String psuIpAddress,
                                                             final String pageUri,
                                                             final HttpClient httpClient) throws TokenInvalidException {
        HttpHeaders headers = prepareHeaders(psuIpAddress);
        headers.add(CONSENT_ID_HEADER_NAME, consentId);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return httpClient.exchange(pageUri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                TransactionsResponse200Json.class).getBody();
    }

    private HttpHeaders prepareHeaders(final String psuIpAddress) {
        HttpHeaders headers = new HttpHeaders();
        if (psuIpAddress != null) {
            headers.add(PSU_IP_ADDRESS_HEADER_NAME, psuIpAddress);
        }
        return headers;
    }
}
