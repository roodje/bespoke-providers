package com.yolt.providers.gruppocedacri.common.dto.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApplicationConfiguration {

    @JsonProperty("redirect_url")
    List<String> redirectUrls;

    @JsonProperty("client_id")
    String clientId;

    @JsonProperty("client_secret")
    String clientSecret;

    String email;
}
