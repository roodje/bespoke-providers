package com.yolt.providers.dkbgroup.common.auth;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.dkbgroup.common.http.DKBGroupHttpClient;
import com.yolt.providers.dkbgroup.common.model.DKBAccessMeans;
import com.yolt.providers.dkbgroup.common.model.api.StartScaprocessResponse;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;

public interface AuthenticationService {
    String getEmbeddedToken(DKBGroupHttpClient httpClient, FilledInUserSiteFormValues filledInUserSiteFormValues) throws TokenInvalidException;

    String getConsentId(DKBGroupHttpClient httpClient, String embeddedToken, String psuIpAddress) throws TokenInvalidException;

    StartScaprocessResponse startSCAProcess(DKBGroupHttpClient httpClient, String embeddedToken, String psuIpAddress,
                                            String consentId) throws TokenInvalidException;

    String chooseSCAMethod(DKBGroupHttpClient httpClient, String embeddedToken, String psuIpAddress, String consentId,
                           StartScaprocessResponse startScaprocessResponse) throws TokenInvalidException;

    void authoriseConsent(DKBGroupHttpClient httpClient, String otp, String psuIpAddress, DKBAccessMeans accessMeans) throws TokenInvalidException;

    void register(DKBGroupHttpClient httpClient, String providerIdentifierDisplayName) throws TokenInvalidException;
}
