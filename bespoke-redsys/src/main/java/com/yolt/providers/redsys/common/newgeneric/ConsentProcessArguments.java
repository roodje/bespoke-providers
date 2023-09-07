package com.yolt.providers.redsys.common.newgeneric;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ConsentProcessArguments<T extends SerializableConsentProcessData> {
    RedsysAuthenticationMeans authenticationMeans;
    T consentProcessData;
    RestTemplateManager restTemplateManager;
    Signer signer;
    String psuIpAddress;
    UUID userId;
    String state;
    String redirectUriPostedBackFromSite;
    String baseClientRedirectUrl;
}
