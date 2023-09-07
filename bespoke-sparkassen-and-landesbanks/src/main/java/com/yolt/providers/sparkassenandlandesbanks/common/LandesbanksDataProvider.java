package com.yolt.providers.sparkassenandlandesbanks.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.sparkassenandlandesbanks.common.dto.ConsentUrlData;
import com.yolt.providers.sparkassenandlandesbanks.common.dto.SparkassenAndLandesbanksProviderState;
import com.yolt.providers.sparkassenandlandesbanks.common.exception.LoginNotFoundException;
import com.yolt.providers.sparkassenandlandesbanks.common.service.SparkassenAndLandesbanksAuthenticationService;
import com.yolt.providers.sparkassenandlandesbanks.common.service.SparkassenAndLandesbanksFetchDataService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.Clock;

public class LandesbanksDataProvider extends SparkassenAndLandesbanksDataProvider {

    private final SparkassenAndLandesbanksAuthenticationService authenticationService;
    private final ObjectMapper objectMapper;
    private final String bankCode;
    @Getter
    private final String providerIdentifier;
    @Getter
    private final String providerIdentifierDisplayName;
    @Getter
    private final ProviderVersion version;
    @Getter
    private final ConsentValidityRules consentValidityRules;

    public LandesbanksDataProvider(SparkassenAndLandesbanksAuthenticationService authenticationService,
                                   @Qualifier("SparkassenAndLandesbanksObjectMapper") ObjectMapper objectMapper,
                                   SparkassenAndLandesbanksFetchDataService sparkassenAndLandesbanksFetchDataService,
                                   String bankCode,
                                   String providerIdentifier,
                                   String providerIdentifierDisplayName,
                                   ProviderVersion version,
                                   ConsentValidityRules consentValidityRules,
                                   Clock clock) {
        super(authenticationService, objectMapper, sparkassenAndLandesbanksFetchDataService, clock);
        this.bankCode = bankCode;
        this.objectMapper = objectMapper;
        this.authenticationService = authenticationService;
        this.providerIdentifier = providerIdentifier;
        this.providerIdentifierDisplayName = providerIdentifierDisplayName;
        this.version = version;
        this.consentValidityRules = consentValidityRules;
    }

    @Override
    public Step getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        SparkassenAndLandesbanksAuthMeans authMeans = SparkassenAndLandesbanksAuthMeans
                .createAuthMeans(urlGetLogin.getAuthenticationMeans(), getProviderIdentifier());

        try {
            Department department = new Department(bankCode);
            ConsentUrlData consentUrlData = authenticationService.generateLoginUrl(
                    authMeans,
                    department,
                    getProviderIdentifierDisplayName(),
                    urlGetLogin.getRestTemplateManager(),
                    urlGetLogin.getBaseClientRedirectUrl(),
                    urlGetLogin.getPsuIpAddress(),
                    urlGetLogin.getState());
            String providerState = objectMapper.writeValueAsString(new SparkassenAndLandesbanksProviderState(consentUrlData.getCodeVerifier(), department, consentUrlData.getWellKnownEndpoint(), consentUrlData.getConsentId()));
            return new RedirectStep(consentUrlData.getConsentUrl(), null, providerState);
        } catch (Exception e) {
            throw new LoginNotFoundException(e);
        }
    }
}
