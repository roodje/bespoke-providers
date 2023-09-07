package com.yolt.providers.openbanking.ais.virginmoney.restclient;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.virginmoney.http.VirginMoneyFetchDataHandlerV2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public class VirginMoneyRestClientV2 extends DefaultRestClient {

    public VirginMoneyRestClientV2(PaymentRequestSigner paymentRequestSigner) {
        super(paymentRequestSigner);
    }

    @Override
    protected HttpErrorHandler getErrorHandler() {
        return VirginMoneyFetchDataHandlerV2.VIRGIN_MONEY_FETCH_DATA_HANDLER;
    }

    @Override
    public void deleteAccountAccessConsent(HttpClient httpClient,
                                           String exchangePath,
                                           AccessMeans clientAccessToken,
                                           String consentId,
                                           DefaultAuthMeans authMeans) throws TokenInvalidException {
        httpClient.exchange(exchangePath + "/" + consentId,
                HttpMethod.DELETE,
                new HttpEntity<>(createDeleteAccountHeaders(clientAccessToken, authMeans.getInstitutionId())),
                ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT,
                Void.class);
    }

    //Readme stands that Accept header cannot be used.
    private HttpHeaders createDeleteAccountHeaders(AccessMeans clientAccessToken, String institutionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(clientAccessToken.getAccessToken());
        headers.add(HttpExtraHeaders.FINANCIAL_ID_HEADER_NAME, institutionId);
        return headers;
    }
}
