package com.yolt.providers.stet.lclgroup.common.service.authorization.tool;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.service.authorization.request.StepRequest;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationRedirectUrlSupplier;
import org.springframework.web.util.UriComponentsBuilder;

public class LclGroupRedirectUrlSupplier extends DefaultAuthorizationRedirectUrlSupplier {

    @Override
    public AuthorizationRedirect createAuthorizationRedirectUrl(String authUrl, Scope accessTokenScope, StepRequest stepRequest) {
        UriComponentsBuilder builder = configureUriComponents(authUrl, accessTokenScope, stepRequest);
        return AuthorizationRedirect.create(builder.toUriString());
    }

    @Override
    protected UriComponentsBuilder configureUriComponents(String authUrl, Scope accessTokenScope, StepRequest stepRequest) {
        return UriComponentsBuilder.fromHttpUrl(authUrl)
                .queryParam(OAuth.CLIENT_ID, stepRequest.getAuthMeans().getClientId())
                .queryParam(OAuth.RESPONSE_TYPE, OAuth.CODE)
                .queryParam(OAuth.SCOPE, accessTokenScope.getValue())
                .queryParam(OAuth.STATE, stepRequest.getState())
                .queryParam(OAuth.REDIRECT_URI, stepRequest.getBaseClientRedirectUrl() + "/");
    }
}
