package com.yolt.providers.openbanking.ais.vanquisgroup.common.service.ais.accountrequestservice;

import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBRisk2;

import java.util.Arrays;
import java.util.List;

public class VanquisGroupAccountRequestServiceV2 extends DefaultAccountAccessConsentRequestService {

    public VanquisGroupAccountRequestServiceV2(final AuthenticationService authenticationService,
                                               final RestClient restClient,
                                               final String endpointsVersion) {
        super(authenticationService, restClient, endpointsVersion);
    }

    private static final List<OBReadConsent1Data.PermissionsEnum> DEFAULT_PERMISSIONS = Arrays.asList(
            OBReadConsent1Data.PermissionsEnum.READACCOUNTSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READBALANCES,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSCREDITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDEBITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSBASIC,
            OBReadConsent1Data.PermissionsEnum.READACCOUNTSBASIC,
            OBReadConsent1Data.PermissionsEnum.READOFFERS,
            OBReadConsent1Data.PermissionsEnum.READSTATEMENTSBASIC,
            OBReadConsent1Data.PermissionsEnum.READSTATEMENTSDETAIL
    );

    private static final OBReadConsent1 ACCOUNT_ACCESS_CONSENT = new OBReadConsent1();

    static {
        ACCOUNT_ACCESS_CONSENT.data(new OBReadConsent1Data().permissions(DEFAULT_PERMISSIONS)).risk(new OBRisk2());
    }

    @Override
    protected Object getAccountAccessConsentBody() {
        return ACCOUNT_ACCESS_CONSENT;
    }
}