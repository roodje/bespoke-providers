package com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.EndpointsVersionable;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsentResponse1;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBRisk2;
import lombok.Getter;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

import java.util.List;

public class DefaultAccountAccessConsentRequestServiceV2 implements AccountRequestService, EndpointsVersionable {

    private static final String ACCOUNT_ACCESS_CONSENTS_PATH = "/aisp/account-access-consents";

    private final AuthenticationService authenticationService;
    private final RestClient restClient;
    @Getter
    private final String endpointsVersion;


    private final OBReadConsent1 accountAccessConsent = new OBReadConsent1();

    public DefaultAccountAccessConsentRequestServiceV2(AuthenticationService authenticationService,
                                                       RestClient restClient,
                                                       String endpointsVersion,
                                                       List<OBReadConsent1Data.PermissionsEnum> permissions) {
        this.authenticationService = authenticationService;
        this.restClient = restClient;
        this.endpointsVersion = endpointsVersion;
        accountAccessConsent.data(new OBReadConsent1Data().permissions(permissions)).risk(new OBRisk2());
    }

    @Override
    public String requestNewAccountRequestId(final HttpClient httpClient,
                                             final DefaultAuthMeans authenticationMeans,
                                             final AuthenticationMeansReference authenticationMeansReference,
                                             final TokenScope scope,
                                             final Signer signer) throws TokenInvalidException {
        AccessMeans clientAccessToken = authenticationService.getClientAccessToken(
                httpClient, authenticationMeans, authenticationMeansReference, scope, signer);

        OBReadConsentResponse1 response = restClient.postAccountAccessConsents(httpClient, getAdjustedUrlPath(getAccountAccessConsentsPath()),
                clientAccessToken, authenticationMeans, getAccountAccessConsentBody(), OBReadConsentResponse1.class);

        return response.getData().getConsentId();
    }

    @Override
    public void deleteAccountRequest(final HttpClient httpClient,
                                     final DefaultAuthMeans authenticationMeans,
                                     final AuthenticationMeansReference authenticationMeansReference,
                                     final String externalConsentId,
                                     final TokenScope scope,
                                     final Signer signer) throws TokenInvalidException {
        AccessMeans clientAccessToken = authenticationService.getClientAccessToken(httpClient, authenticationMeans, authenticationMeansReference, scope, signer);

        restClient.deleteAccountAccessConsent(httpClient, getAdjustedUrlPath(getAccountAccessConsentsPath()),
                clientAccessToken, externalConsentId, authenticationMeans);
    }

    protected String getAccountAccessConsentsPath() {
        return ACCOUNT_ACCESS_CONSENTS_PATH;
    }

    protected Object getAccountAccessConsentBody() {
        return accountAccessConsent;
    }
}