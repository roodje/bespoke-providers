package com.yolt.providers.openbanking.ais.hsbcgroup.common.service.restclient;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.http.HsbcGroupUserSiteDeleteHandlerV2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

public class DefaultHsbcGroupRestClientV5 extends DefaultRestClient {

    public DefaultHsbcGroupRestClientV5(PaymentRequestSigner payloadSigner) {
        super(payloadSigner);
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
                HsbcGroupUserSiteDeleteHandlerV2.GENERIC_HSBC_GROUP_USER_SITE_DELETE_HANDLER);
    }
}
