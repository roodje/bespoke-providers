package com.yolt.providers.openbanking.ais.rbsgroup.common;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.rbsgroup.common.auth.RbsGroupAuthMeansBuilderV4;
import com.yolt.providers.openbanking.ais.rbsgroup.common.service.autoonboarding.RbsGroupAutoonboardingServiceV2;
import com.yolt.providers.openbanking.ais.rbsgroup.common.service.autoonboarding.RbsGroupDynamicRegistrationArguments;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RbsGroupAutoonboardingProviderV2 {
    private static final List<String> AUTO_ON_BOARDING_UNNECESSARY_MEANS = Collections.singletonList(
            RbsGroupAuthMeansBuilderV4.CLIENT_ID_NAME);

    private final String providerIdentifier;
    private final RbsGroupAutoonboardingServiceV2 autoonboardingService;

    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans(Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap) {
        return typedAuthenticationMeansMap.entrySet()
                .stream()
                .filter(entry -> AUTO_ON_BOARDING_UNNECESSARY_MEANS.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, BasicAuthenticationMean> autoConfigureMeans(UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                                   HttpClient httpClient,
                                                                   DefaultAuthMeans authMeans,
                                                                   TokenScope tokenScope) {
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(urlAutoOnboardingRequest.getAuthenticationMeans());
        RbsGroupDynamicRegistrationArguments registrationArguments = adjustRegistrationArguments(createRegistrationArguments(urlAutoOnboardingRequest));
        try {
            autoonboardingService.register(httpClient, registrationArguments, authMeans, tokenScope).ifPresent(clientRegistration -> {
                BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(TypedAuthenticationMeans.CLIENT_ID_STRING.getType(),
                        clientRegistration.getClientId());
                mutableMeans.put(RbsGroupAuthMeansBuilderV4.CLIENT_ID_NAME, clientIdMean);
            });
            return mutableMeans;
        } catch (TokenInvalidException e) {
            throw new AutoOnboardingException(providerIdentifier, String.format("Auto-onboarding failed for %s", providerIdentifier), e);
        }
    }

    private RbsGroupDynamicRegistrationArguments createRegistrationArguments(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        RbsGroupDynamicRegistrationArguments registrationArguments = new RbsGroupDynamicRegistrationArguments();
        registrationArguments.setSigner(urlAutoOnboardingRequest.getSigner());
        registrationArguments.setPrivateSigningKeyId(UUID.fromString(authenticationMeans.get(RbsGroupAuthMeansBuilderV4.SIGNING_PRIVATE_KEY_ID_NAME).getValue()));
        registrationArguments.setSigningKeyHeaderId(authenticationMeans.get(RbsGroupAuthMeansBuilderV4.SIGNING_KEY_HEADER_ID_NAME).getValue());
        registrationArguments.setSoftwareId(authenticationMeans.get(RbsGroupAuthMeansBuilderV4.SOFTWARE_ID_NAME).getValue());
        registrationArguments.setInstitutionId(authenticationMeans.get(RbsGroupAuthMeansBuilderV4.INSTITUTION_ID_NAME).getValue());
        registrationArguments.setOrganizationId(authenticationMeans.get(RbsGroupAuthMeansBuilderV4.ORGANIZATION_ID_NAME).getValue());
        registrationArguments.setSoftwareStatementAssertion(authenticationMeans.get(RbsGroupAuthMeansBuilderV4.SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        registrationArguments.setRedirectUris(urlAutoOnboardingRequest.getRedirectUrls());

        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(urlAutoOnboardingRequest.getScopes());
        registrationArguments.setScope(tokenScope.getRegistrationScope());
        return registrationArguments;
    }

    protected RbsGroupDynamicRegistrationArguments adjustRegistrationArguments(RbsGroupDynamicRegistrationArguments registrationArguments) {
        return registrationArguments;
    }
}
