package com.yolt.providers.bunq.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OauthAddCallbackUrlRequest {

    @JsonProperty("url")
    final String url;
}
