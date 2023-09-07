package com.yolt.providers.stet.labanquepostalegroup.common.service.authorization.tool;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.service.authorization.request.StepRequest;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationRedirectUrlSupplier;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

public class LaBanquePostaleRedirectUrlSupplier extends DefaultAuthorizationRedirectUrlSupplier {

    @Override
    public AuthorizationRedirect createAuthorizationRedirectUrl(String authUrl, Scope accessTokenScope, StepRequest stepRequest) {
        UriComponentsBuilder builder = configureUriComponents(authUrl, accessTokenScope, stepRequest);
        return AuthorizationRedirect.create(builder.build().toUriString());
    }

    @Override
    protected UriComponentsBuilder configureUriComponents(String authUrl, Scope accessTokenScope, StepRequest stepRequest) {
        String encodedBaseClientRedirectUrl = UriUtils.encode(stepRequest.getBaseClientRedirectUrl(), StandardCharsets.UTF_8);
        return UriComponentsBuilder.fromHttpUrl(authUrl)
                .queryParam(OAuth.CLIENT_ID, stepRequest.getAuthMeans().getClientId())
                .queryParam(OAuth.RESPONSE_TYPE, OAuth.CODE)
                .queryParam(OAuth.SCOPE, accessTokenScope.getValue())
                .queryParam(OAuth.STATE, stepRequest.getState())
                .queryParam(OAuth.REDIRECT_URI, encodedBaseClientRedirectUrl);
    }
}
