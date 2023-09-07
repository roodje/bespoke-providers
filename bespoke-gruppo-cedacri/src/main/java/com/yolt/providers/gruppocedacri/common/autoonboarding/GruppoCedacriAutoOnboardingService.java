package com.yolt.providers.gruppocedacri.common.autoonboarding;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.gruppocedacri.common.dto.registration.AutoOnboardingRequest;
import com.yolt.providers.gruppocedacri.common.dto.registration.AutoOnboardingResponse;
import com.yolt.providers.gruppocedacri.common.dto.registration.RedirectObject;
import com.yolt.providers.gruppocedacri.common.http.GruppoCedacriHttpClient;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.gruppocedacri.common.GruppoCedacriAuthenticationMeans.*;

@NoArgsConstructor
public class GruppoCedacriAutoOnboardingService {

    public Optional<AutoOnboardingResponse> register(GruppoCedacriHttpClient httpClient,
                                                     UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        if (!urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(CLIENT_ID_NAME)) {
            return Optional.ofNullable(invokeRegistration(httpClient, urlAutoOnboardingRequest));
        }
        return Optional.empty();
    }

    protected AutoOnboardingResponse invokeRegistration(GruppoCedacriHttpClient httpClient,
                                                        UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> authMeans = urlAutoOnboardingRequest.getAuthenticationMeans();

        var redirectObjectBuilder = RedirectObject.builder();
        redirectObjectBuilder.aisp(urlAutoOnboardingRequest.getRedirectUrls());
        redirectObjectBuilder.pisp(urlAutoOnboardingRequest.getRedirectUrls());
        redirectObjectBuilder.cisp(urlAutoOnboardingRequest.getRedirectUrls());

        AutoOnboardingRequest autoOnboardingRequest = AutoOnboardingRequest.builder()
                .email(authMeans.get(EMAIL_NAME).getValue())
                .redirectUrl(redirectObjectBuilder.build())
                .cancelLink(authMeans.get(CANCEL_LINK_NAME).getValue())
                .build();

        return httpClient.register(autoOnboardingRequest);
    }
}