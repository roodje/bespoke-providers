package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface OauthClientDetailsResponse {
    @JsonPath("$.Response.[?(@.OauthClient)].OauthClient.id")
    Long getOAuthClientId();

    @JsonPath("$.Response.[?(@.OauthClient)].OauthClient.created")
    String getCreated();

    @JsonPath("$.Response.[?(@.OauthClient)].OauthClient.updated")
    String getUpdated();

    @JsonPath("$.Response.[?(@.OauthClient)].OauthClient.status")
    String getStatus();

    @JsonPath("$.Response.[?(@.OauthClient)].OauthClient.client_id")
    String getClientId();

    @JsonPath("$.Response.[?(@.OauthClient)].OauthClient.secret")
    String getClientSecret();

    @JsonPath("$.Response.[?(@.OauthClient)].OauthClient.callback_url")
    List<CallbackUrl> getCallbackUrls();
}
