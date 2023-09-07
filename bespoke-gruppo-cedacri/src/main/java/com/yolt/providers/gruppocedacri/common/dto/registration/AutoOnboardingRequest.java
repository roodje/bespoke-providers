package com.yolt.providers.gruppocedacri.common.dto.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutoOnboardingRequest {

    String email;

    @JsonProperty("redirect_url")
    RedirectObject redirectUrl;

    @JsonProperty("cancel_link")
    String cancelLink;
}
