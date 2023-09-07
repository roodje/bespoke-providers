package com.yolt.providers.argentagroup.common.service.consent;

import com.yolt.providers.argentagroup.dto.CreateConsentResponse;
import com.yolt.providers.argentagroup.dto.CreateConsentResponseLinks;
import com.yolt.providers.argentagroup.dto.CreateConsentResponseLinksScaOAuth;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;

import java.util.Optional;

public class InitiateConsentResponseMapper {

    public InitiateConsentResult mapResponse(final CreateConsentResponse response) {
        String authorizationUrl = Optional.ofNullable(response)
                .map(CreateConsentResponse::getLinks)
                .map(CreateConsentResponseLinks::getScaOAuth)
                .map(CreateConsentResponseLinksScaOAuth::getHref)
                .orElseThrow(() -> new GetLoginInfoUrlFailedException("Bank returned an empty authorization url"));

        String consentId = Optional.of(response)
                .map(CreateConsentResponse::getConsentId)
                .orElseThrow(() -> new GetLoginInfoUrlFailedException("Bank returned an empty consent id"));

        return new InitiateConsentResult(authorizationUrl, consentId);
    }
}
