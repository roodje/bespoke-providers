package com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

public class DefaultConsentPermissions implements ConsentPermissions {
    @Getter
    private final List<String> permissions;

    public DefaultConsentPermissions(List<OBReadConsent1Data.PermissionsEnum> permissions) {
        this.permissions = permissions.stream().map(OBReadConsent1Data.PermissionsEnum::toString).collect(Collectors.toList());
    }
}