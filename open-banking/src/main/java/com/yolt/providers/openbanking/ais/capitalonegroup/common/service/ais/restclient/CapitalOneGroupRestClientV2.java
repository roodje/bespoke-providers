package com.yolt.providers.openbanking.ais.capitalonegroup.common.service.ais.restclient;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.model.CapitalOneDynamicRegistrationResponse;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1;
import lombok.NonNull;
import org.springframework.http.*;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Optional;

public class CapitalOneGroupRestClientV2 extends DefaultRestClient {

    public CapitalOneGroupRestClientV2(final PaymentRequestSigner paymentRequestSigner) {
        super(paymentRequestSigner);
    }

    public Optional<CapitalOneDynamicRegistrationResponse> register(@NonNull final HttpClient httpClient,
                                                                    @NonNull final String payload,
                                                                    @NonNull final String registrationUrl) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.add(HttpHeaders.CONTENT_TYPE, "application/jwt");
        HttpEntity<String> httpEntity = new HttpEntity<>(payload, headers);

        ResponseEntity<CapitalOneDynamicRegistrationResponse> responseEntity = httpClient.exchange(registrationUrl,
                HttpMethod.POST,
                httpEntity,
                ProviderClientEndpoints.REGISTER,
                CapitalOneDynamicRegistrationResponse.class);
        return Optional.ofNullable(responseEntity.getBody());
    }

    public <T> T fetchAccounts(HttpClient httpClient,
                               String currentPath,
                               AccessMeans accessToken,
                               String institutionId,
                               Class<T> responseType,
                               String psuIpAddress) throws TokenInvalidException {
        return httpClient.exchange(currentPath,
                HttpMethod.GET,
                new HttpEntity<>(getFetchDataHeaders(accessToken, institutionId, psuIpAddress)),
                ProviderClientEndpoints.GET_ACCOUNTS,
                responseType,
                getErrorHandler()).getBody();
    }

    public <T> T fetchTransactions(HttpClient httpClient,
                                   String currentPath,
                                   AccessMeans accessToken,
                                   String institutionId,
                                   Class<T> responseType,
                                   String psuIpAddress) throws TokenInvalidException {
        return httpClient.exchange(currentPath,
                HttpMethod.GET,
                new HttpEntity<>(getFetchDataHeaders(accessToken, institutionId, psuIpAddress)),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                responseType,
                getErrorHandler()).getBody();
    }

    public OBReadBalance1 fetchBalances(HttpClient httpClient,
                                        String currentPath,
                                        AccessMeans accessToken,
                                        String institutionId,
                                        String psuIpAddress) throws TokenInvalidException {
        return httpClient.exchange(currentPath,
                HttpMethod.GET,
                new HttpEntity<>(getFetchDataHeaders(accessToken, institutionId, psuIpAddress)),
                ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID,
                OBReadBalance1.class,
                getErrorHandler()).getBody();
    }

    private HttpHeaders getFetchDataHeaders(AccessMeans accessToken, String institutionId, String psuIpAddress) {
        HttpHeaders headers = getHeaders(accessToken, institutionId);
        if (!StringUtils.isEmpty(psuIpAddress)) {
            headers.add(HttpExtraHeaders.CUSTOMER_IP_ADDRESS_HEADER_NAME, psuIpAddress);
        }
        return headers;
    }
}