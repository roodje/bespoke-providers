package com.yolt.providers.openbanking.ais.barclaysgroup.common.service.restclient;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.http.BarclaysGroupDeleteAccountAccessErrorHandlerV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import static com.yolt.providers.openbanking.ais.barclaysgroup.common.http.BarclaysGroupErrorHandlerV2.BARCLAYS_GROUP_ERROR_HANDLER;

public class BarclaysGroupRestClientV5 extends DefaultRestClient {

    public BarclaysGroupRestClientV5(PaymentRequestSigner payloadSigner) {
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
                new BarclaysGroupDeleteAccountAccessErrorHandlerV2());
    }

    @Override
    protected HttpErrorHandler getErrorHandler() {
        return BARCLAYS_GROUP_ERROR_HANDLER;
    }
}
