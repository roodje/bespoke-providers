package com.yolt.providers.openbanking.ais.newdaygroup.common.service;

import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBRisk2;

import java.util.List;

public class NewDayGroupAccountAccessConsentRequestServiceV2 extends DefaultAccountAccessConsentRequestService {

    private static final List<OBReadConsent1Data.PermissionsEnum> DEFAULT_PERMISSIONS = List.of(
            OBReadConsent1Data.PermissionsEnum.READACCOUNTSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READBALANCES,
            OBReadConsent1Data.PermissionsEnum.READPRODUCTS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSCREDITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDEBITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDETAIL);

    private static final OBReadConsent1 ACCOUNT_ACCESS_CONSENT = new OBReadConsent1();

    static {
        ACCOUNT_ACCESS_CONSENT.data(new OBReadConsent1Data().permissions(DEFAULT_PERMISSIONS)).risk(new OBRisk2());
    }

    public NewDayGroupAccountAccessConsentRequestServiceV2(final AuthenticationService authenticationService, final RestClient restClient, final String endpointsVersion) {
        super(authenticationService, restClient, endpointsVersion);
    }

    @Override
    protected Object getAccountAccessConsentBody() {
        return ACCOUNT_ACCESS_CONSENT;
    }

}
