package com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;

import java.util.List;

public class DefaultPermissions {

    public static final List<OBReadConsent1Data.PermissionsEnum> DEFAULT_PERMISSIONS = List.of(
            OBReadConsent1Data.PermissionsEnum.READACCOUNTSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READBALANCES,
            OBReadConsent1Data.PermissionsEnum.READDIRECTDEBITS,
            OBReadConsent1Data.PermissionsEnum.READPRODUCTS,
            OBReadConsent1Data.PermissionsEnum.READSTANDINGORDERSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSCREDITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDEBITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDETAIL);
}
