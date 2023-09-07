package com.yolt.providers.monorepogroup.atruviagroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

/**
 * Body of the JSON response for a successful consent request.
 */
@ProjectedPayload
public interface ConsentsResponse201 {

    @JsonPath("$.consentStatus")
    ConsentStatus getConsentStatus();

    @JsonPath("$.consentId")
    String getConsentId();

    @JsonPath("$.chosenScaMethod")
    AuthenticationObject getChosenScaMethod();

    @JsonPath("$.challengeData")
    ChallengeData getChallengeData();

    @JsonPath("$.message")
    String getMessage();
}

