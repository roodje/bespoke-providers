package com.yolt.providers.openbanking.ais.revolutgroup.common.service.ais.mappers;

import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBRisk2;

import java.util.Arrays;
import java.util.List;

public class RevolutAccountRequestServiceV4 extends DefaultAccountAccessConsentRequestService {

    private static final String ACCOUNT_ACCESS_CONSENTS_PATH = "/account-access-consents";

    private static final List<OBReadConsent1Data.PermissionsEnum> DEFAULT_PERMISSIONS = Arrays.asList(
            OBReadConsent1Data.PermissionsEnum.READACCOUNTSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READBALANCES,
            OBReadConsent1Data.PermissionsEnum.READDIRECTDEBITS,
            OBReadConsent1Data.PermissionsEnum.READSTANDINGORDERSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSCREDITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDEBITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDETAIL);

    private static final OBReadConsent1 ACCOUNT_ACCESS_CONSENT = new OBReadConsent1();

    static {
        ACCOUNT_ACCESS_CONSENT.data(new OBReadConsent1Data().permissions(DEFAULT_PERMISSIONS)).risk(new OBRisk2());
    }

    public RevolutAccountRequestServiceV4(AuthenticationService authenticationService, RestClient restClient, String endpointsVersion) {
        super(authenticationService, restClient, endpointsVersion);
    }


    @Override
    protected Object getAccountAccessConsentBody() {
        return ACCOUNT_ACCESS_CONSENT;
    }

    @Override
    protected String getAccountAccessConsentsPath() {
        return ACCOUNT_ACCESS_CONSENTS_PATH;
    }
}