package com.yolt.providers.monorepogroup.atruviagroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

/**
 * Body of the JSON response for a successful select PSU Authentication Method request.
 */
@ProjectedPayload
public
interface SelectPsuAuthenticationMethodResponse {

    @JsonPath("$.chosenScaMethod")
    AuthenticationObject getChosenScaMethod();

    @JsonPath("$.challengeData")
    ChallengeData getChallengeData();

    @JsonPath("$.scaStatus")
    ScaStatus getScaStatus();

    @JsonPath("$.psuMessage")
    String getPsuMessage();
}

