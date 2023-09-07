package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.oauth2;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;

public interface PermanentTsbGroupOauth2Client extends Oauth2Client {

    /**
     * This is a method responsible for calling consent authorization endpoint. `AuthorizationUrl` argument has to include
     * all necessary parameters like redirectUrl, clientId, scope and request. As a result String with valid
     * authorization url will be returned.
     */
    String getAuthorizationUrl(final HttpClient httpClient, final String authorizationUrl) throws TokenInvalidException;
}
