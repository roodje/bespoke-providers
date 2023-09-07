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
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

import java.util.Arrays;
import java.util.List;

@Deprecated //Use newer version  C4PO-8398
@RequiredArgsConstructor
public class DefaultAccountAccessConsentRequestService implements AccountRequestService, EndpointsVersionable {

    private static final String ACCOUNT_ACCESS_CONSENTS_PATH = "/aisp/account-access-consents";

    private final AuthenticationService authenticationService;
    private final RestClient restClient;
    @Getter
    private final String endpointsVersion;

    private static final List<OBReadConsent1Data.PermissionsEnum> DEFAULT_PERMISSIONS = Arrays.asList(
            OBReadConsent1Data.PermissionsEnum.READACCOUNTSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READBALANCES,
            OBReadConsent1Data.PermissionsEnum.READDIRECTDEBITS,
            OBReadConsent1Data.PermissionsEnum.READPRODUCTS,
            OBReadConsent1Data.PermissionsEnum.READSTANDINGORDERSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSCREDITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDEBITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDETAIL);

    private static final OBReadConsent1 ACCOUNT_ACCESS_CONSENT = new OBReadConsent1();

    static {
        ACCOUNT_ACCESS_CONSENT.data(new OBReadConsent1Data().permissions(DEFAULT_PERMISSIONS)).risk(new OBRisk2());
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
        return ACCOUNT_ACCESS_CONSENT;
    }
}