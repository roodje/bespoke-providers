package com.yolt.providers.monorepogroup.atruviagroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

/**
 * Body of the JSON response for a Start SCA authorisation request.
 */
@ProjectedPayload
public interface StartScaProcessResponse {

    @JsonPath("$.scaStatus")
    ScaStatus getScaStatus();

    @JsonPath("$.authorisationId")
    String getAuthorisationId();

    @JsonPath("$.scaMethods")
    List<AuthenticationObject> getScaMethods();

    @JsonPath("$.chosenScaMethod")
    AuthenticationObject getChosenScaMethod();

    @JsonPath("$.challengeData")
    ChallengeData getChallengeData();

    @JsonPath("$.psuMessage")
    String getPsuMessage();
}

