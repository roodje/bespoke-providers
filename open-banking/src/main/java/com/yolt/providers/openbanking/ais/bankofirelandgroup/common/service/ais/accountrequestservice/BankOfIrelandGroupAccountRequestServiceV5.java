package com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.ais.accountrequestservice;

import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking300.OBExternalPermissions1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking300.OBReadConsent1;
import com.yolt.providers.openbanking.dto.ais.openbanking300.OBReadData1;
import com.yolt.providers.openbanking.dto.ais.openbanking300.OBRisk2;

import java.util.Arrays;
import java.util.List;

public class BankOfIrelandGroupAccountRequestServiceV5 extends DefaultAccountAccessConsentRequestService {

    private static final List<OBExternalPermissions1Code> DEFAULT_PERMISSIONS = Arrays.asList(
            OBExternalPermissions1Code.READACCOUNTSDETAIL,
            OBExternalPermissions1Code.READBALANCES,
            OBExternalPermissions1Code.READPRODUCTS,
            OBExternalPermissions1Code.READSTANDINGORDERSDETAIL,
            OBExternalPermissions1Code.READTRANSACTIONSCREDITS,
            OBExternalPermissions1Code.READTRANSACTIONSDEBITS,
            OBExternalPermissions1Code.READTRANSACTIONSDETAIL);

    private static final OBReadConsent1 ACCOUNT_ACCESS_CONSENT = new OBReadConsent1();

    static {
        ACCOUNT_ACCESS_CONSENT.data(new OBReadData1().permissions(DEFAULT_PERMISSIONS)).risk(new OBRisk2());
    }

    public BankOfIrelandGroupAccountRequestServiceV5(AuthenticationService authenticationService, RestClient restClient, String endpointsVersion) {
        super(authenticationService, restClient, endpointsVersion);
    }

    @Override
    protected Object getAccountAccessConsentBody() {
        return ACCOUNT_ACCESS_CONSENT;
    }
}
