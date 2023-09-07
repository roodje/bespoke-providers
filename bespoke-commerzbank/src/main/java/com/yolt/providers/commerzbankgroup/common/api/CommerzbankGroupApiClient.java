package com.yolt.providers.commerzbankgroup.common.api;

import com.yolt.providers.commerzbankgroup.common.api.dto.authorization.CommerzbankGroupTokenResponse;
import com.yolt.providers.commerzbankgroup.common.api.dto.authorization.ConsentCreationResponse;
import com.yolt.providers.commerzbankgroup.common.api.dto.authorization.Consents;
import com.yolt.providers.commerzbankgroup.common.api.dto.authorization.OAuthLinksResponse;
import com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata.AccountList;
import com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata.ReadAccountBalanceResponse200;
import com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata.Transactions;
import com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata.TransactionsResponse200Json;
import com.yolt.providers.commerzbankgroup.common.authentication.LoginNotFoundCommerzbankException;
import com.yolt.providers.commerzbankgroup.common.authmeans.CommerzbankGroupAuthenticationMeans;
import com.yolt.providers.commerzbankgroup.common.util.ErrorHandlerUtil;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CommerzbankGroupApiClient {

    public static final ErrorHandlerUtil ERROR_HANDLER = new ErrorHandlerUtil();
    private static final String HEADER_PSU_IP_ADDRESS_NAME = "PSU-IP-Address";
    private static final String HEADER_TPP_REDIRECT_URI_NAME = "TPP-Redirect-URI";
    private static final String HEADER_NAME_CONSENT_ID = "Consent-ID";
    private final HttpClient httpClient;
    private final CommerzbankGroupAuthenticationMeans authMeans;
    private final int paginationLimit;
    private final String psuIpAddress;

    public CommerzbankGroupApiClient(HttpClient httpClient, CommerzbankGroupAuthenticationMeans commerzbankGroupAuthenticationMeans, int paginationLimit, String psuIpAddress) {
        this.httpClient = httpClient;
        this.authMeans = commerzbankGroupAuthenticationMeans;
        this.paginationLimit = paginationLimit;
        this.psuIpAddress = psuIpAddress;
    }

    public ConsentCreationResponse createConsent(Consents request, String baseClientRedirectUrl) {
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, authMeans.getOrganizationIdentifier());
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HEADER_TPP_REDIRECT_URI_NAME, baseClientRedirectUrl);
        Optional.ofNullable(psuIpAddress)
                .ifPresent((psu) -> headers.add(HEADER_PSU_IP_ADDRESS_NAME, psu));
        var body = new HttpEntity<>(request, headers);
        try {
            return httpClient.exchange("/berlingroup/v1/consents", HttpMethod.POST, body, ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT, ConsentCreationResponse.class, ERROR_HANDLER)
                    .getBody();
        } catch (TokenInvalidException e) {
            throw new LoginNotFoundCommerzbankException(e);
        }
    }

    public OAuthLinksResponse fetchAuthorizationServerUrl(String scaOauthUrl) {
        var headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        Optional.ofNullable(psuIpAddress)
                .ifPresent((psu) -> headers.add(HEADER_PSU_IP_ADDRESS_NAME, psu));
        var body = new HttpEntity<>(headers);
        ResponseEntity<OAuthLinksResponse> wellKnownResponse;
        try {
            wellKnownResponse = httpClient.exchange(scaOauthUrl, HttpMethod.GET, body, "well_known", OAuthLinksResponse.class, ERROR_HANDLER);
        } catch (TokenInvalidException e) {
            throw new LoginNotFoundCommerzbankException(e);
        }
        return wellKnownResponse.getBody();
    }


    public CommerzbankGroupTokenResponse fetchAccessToken(CreateAccessTokenRequest accessTokenRequest) {
        var httpHeaders = createTokenEndpointHttpHeaders();
        var body = new HttpEntity<>(accessTokenRequest.asMultiValueMapForOid(authMeans.getOrganizationIdentifier()), httpHeaders);
        ResponseEntity<CommerzbankGroupTokenResponse> token;
        try {
            token = httpClient.exchange("/berlingroup/v1/token", HttpMethod.POST, body, ProviderClientEndpoints.GET_ACCESS_TOKEN, CommerzbankGroupTokenResponse.class, ERROR_HANDLER);
        } catch (TokenInvalidException e) {
            throw new GetAccessTokenFailedException(e);
        }
        return token.getBody();
    }

    public CommerzbankGroupTokenResponse refreshAccessToken(RefreshAccessTokenRequest refreshAccessTokenRequest) throws TokenInvalidException {
        var httpHeaders = createTokenEndpointHttpHeaders();
        var body = new HttpEntity<>(refreshAccessTokenRequest.asMultiValueMapForOid(authMeans.getOrganizationIdentifier()), httpHeaders);
        var token = httpClient.exchange("/berlingroup/v1/token", HttpMethod.POST, body, ProviderClientEndpoints.REFRESH_TOKEN, CommerzbankGroupTokenResponse.class, ERROR_HANDLER);
        return token.getBody();
    }


    public AccountList fetchAccounts(ConsentCredentials consentCredentials) throws TokenInvalidException {
        var headers = consentCredentials.asStandardHeadersSet();
        Optional.ofNullable(psuIpAddress)
                .ifPresent((ip) -> headers.add(HEADER_PSU_IP_ADDRESS_NAME, ip));
        var body = new HttpEntity<>(headers);
        var accounts = httpClient.exchange("/berlingroup/v1/accounts", HttpMethod.GET, body, ProviderClientEndpoints.GET_ACCOUNTS, AccountList.class, ERROR_HANDLER);
        return accounts.getBody();
    }

    public List<Transactions> fetchAllPagesOfTransactionsForAnAccount(ConsentCredentials consentCredentials, String accountId, LocalDate dateFrom) throws TokenInvalidException {
        var headers = consentCredentials.asStandardHeadersSet();
        Optional.ofNullable(psuIpAddress)
                .ifPresent((ip) -> headers.add(HEADER_PSU_IP_ADDRESS_NAME, ip));
        var body = new HttpEntity<>(headers);
        TransactionsResponse200Json currentPage;
        var localDateFromAsString = DateTimeFormatter.ISO_LOCAL_DATE.format(dateFrom);
        String nextPage = "/berlingroup/v1/accounts/{accountId}/transactions?bookingStatus=booked&dateFrom={dateFrom}";
        var transactionPages = new ArrayList<Transactions>(1);
        int page = 0;
        do {
            var transactionResponse = httpClient.exchange(nextPage, HttpMethod.GET, body, ProviderClientEndpoints.GET_ACCOUNTS, TransactionsResponse200Json.class, ERROR_HANDLER, accountId, localDateFromAsString);
            currentPage = transactionResponse.getBody();
            Objects.requireNonNull(currentPage);
            if (currentPage.getTransactions() != null && !CollectionUtils.isEmpty(currentPage.getTransactions().getBooked())) {
                transactionPages.addAll(currentPage.getTransactions().getBooked());
            }
            nextPage = extractNextPageAddress(currentPage);
        } while (nextPage != null && ++page < paginationLimit);
        return transactionPages;
    }

    public ReadAccountBalanceResponse200 fetchBalancesForAnAccount(ConsentCredentials consentCredentials, String accountId) throws TokenInvalidException {
        var headers = consentCredentials.asStandardHeadersSet();
        Optional.ofNullable(psuIpAddress)
                .ifPresent((ip) -> headers.add(HEADER_PSU_IP_ADDRESS_NAME, ip));
        var body = new HttpEntity<>(headers);
        var transactionResponse = httpClient.exchange("/berlingroup/v1/accounts/{accountId}/balances", HttpMethod.GET, body, ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID, ReadAccountBalanceResponse200.class, ERROR_HANDLER, accountId);
        return transactionResponse.getBody();
    }

    private HttpHeaders createTokenEndpointHttpHeaders() {
        var httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        Optional.ofNullable(psuIpAddress)
                .ifPresent((psu) -> httpHeaders.add(HEADER_PSU_IP_ADDRESS_NAME, psu));
        return httpHeaders;
    }

    private static String extractNextPageAddress(TransactionsResponse200Json transactionsResponse200Json) {
        var links = transactionsResponse200Json.getLinks();
        if (links == null) {
            return null;
        }
        if (links.containsKey("next")) {
            return links.get("next").getHref();
        }
        return null;
    }

    public record ConsentCredentials(String accessToken, String consentId) {
        public HttpHeaders asStandardHeadersSet() {
            var httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            httpHeaders.add(HEADER_NAME_CONSENT_ID, consentId);
            return httpHeaders;
        }
    }

    public record RefreshAccessTokenRequest(String refreshToken) {
        public MultiValueMap<String, String> asMultiValueMapForOid(String organizationIdentifier) {
            MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
            payload.add("refresh_token", refreshToken);
            payload.add("client_id", organizationIdentifier);
            payload.add("grant_type", "refresh_token");
            return payload;
        }
    }

    public record CreateAccessTokenRequest(String state, String authorizationCode, String codeVerifier,
                                           String baseClientRedirectUrl) {
        public MultiValueMap<String, String> asMultiValueMapForOid(String organizationIdentifier) {
            MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
            payload.add("redirect_uri", baseClientRedirectUrl);
            payload.add("state", state);
            payload.add("code", authorizationCode);
            payload.add("code_verifier", codeVerifier);
            payload.add("client_id", organizationIdentifier);
            payload.add("grant_type", "authorization_code");
            return payload;
        }
    }
}







