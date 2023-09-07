package com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice;

import lombok.Value;

import java.util.List;

@Value
public class AccountConsentInformation {
    String consentId;
    List<String> permissions;
}
