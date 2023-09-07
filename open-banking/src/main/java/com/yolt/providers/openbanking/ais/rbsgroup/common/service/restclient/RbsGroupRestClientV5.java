package com.yolt.providers.openbanking.ais.rbsgroup.common.service.restclient;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.rbsgroup.common.http.RbsGroupUserSiteDeleteHandlerV2;
import lombok.NonNull;
import org.springframework.http.*;

import java.util.Collections;
import java.util.Optional;

public class RbsGroupRestClientV5 extends DefaultRestClient {

    private static final HttpErrorHandler ERROR_HANDLER = RbsGroupUserSiteDeleteHandlerV2.RBS_GROUP_USER_SITE_DELETE_HANDLER;

    public RbsGroupRestClientV5(final PaymentRequestSigner paymentRequestSigner) {
        super(paymentRequestSigner);
    }

    @Override
    public void deleteAccountAccessConsent(final HttpClient httpClient,
                                           final String exchangePath,
                                           final AccessMeans clientAccessToken,
                                           final String consentId,
                                           final DefaultAuthMeans authMeans) throws TokenInvalidException {
        httpClient.exchange(exchangePath + "/" + consentId,
                HttpMethod.DELETE,
                new HttpEntity<>(getHeaders(clientAccessToken, authMeans.getInstitutionId())),
                ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT,
                Void.class,
                ERROR_HANDLER);
    }

    public Optional<AutoOnboardingResponse> register(@NonNull final HttpClient httpClient,
                                                     @NonNull final String payload,
                                                     @NonNull final String registrationUrl) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add(HttpHeaders.CONTENT_TYPE, "application/jwt");
        HttpEntity<String> httpEntity = new HttpEntity<>(payload, headers);

        ResponseEntity<AutoOnboardingResponse> responseEntity = httpClient.exchange(registrationUrl,
                HttpMethod.POST,
                httpEntity,
                ProviderClientEndpoints.REGISTER,
                AutoOnboardingResponse.class);
        return Optional.ofNullable(responseEntity.getBody());
    }

    public void deleteRegistration(HttpClient httpClient,
                                   AccessMeans clientAccessToken,
                                   String deletePath,
                                   String clientId) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(clientAccessToken.getAccessToken());
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        httpClient.exchange(deletePath, HttpMethod.DELETE, new HttpEntity<>(headers), ProviderClientEndpoints.DELETE_REGISTRATION, Void.class, clientId);
    }

    public Optional<AutoOnboardingResponse> updateRegistration(HttpClient httpClient,
                                                               String payload,
                                                               AccessMeans accessMeans,
                                                               String updatePath,
                                                               String clientId) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessMeans.getAccessToken());
        headers.add(HttpHeaders.CONTENT_TYPE, "application/jwt");
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<AutoOnboardingResponse> responseEntity = httpClient.exchange(updatePath, HttpMethod.PUT, new HttpEntity<>(payload, headers), ProviderClientEndpoints.REGISTER, AutoOnboardingResponse.class, clientId);
        return Optional.ofNullable(responseEntity.getBody());
    }
}
