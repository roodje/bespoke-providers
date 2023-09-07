package com.yolt.providers.stet.boursoramagroup.common.service;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.stet.boursoramagroup.boursorama.auth.BoursoramaAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.service.authorization.request.StepRequest;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationRedirectUrlSupplier;
import org.springframework.web.util.UriComponentsBuilder;

public class BoursoramaGroupAuthorizationRedirectUrlSupplier extends DefaultAuthorizationRedirectUrlSupplier {

    private static final String SUCCESS_REDIRECT_QUERY_PARAM_NAME = "successRedirect";
    private static final String ERROR_REDIRECT_QUERY_PARAM_NAME = "errorRedirect";

    @Override
    protected UriComponentsBuilder configureUriComponents(String authUrl, Scope accessTokenScope, StepRequest stepRequest) {
        BoursoramaAuthenticationMeans boursoramaAuthenticationMeans = (BoursoramaAuthenticationMeans) stepRequest.getAuthMeans();
        return UriComponentsBuilder.fromHttpUrl(authUrl)
                .path(boursoramaAuthenticationMeans.getCertificateAgreementNumber())
                .queryParam(SUCCESS_REDIRECT_QUERY_PARAM_NAME, stepRequest.getBaseClientRedirectUrl())
                .queryParam(OAuth.STATE, stepRequest.getState())
                .queryParam(ERROR_REDIRECT_QUERY_PARAM_NAME, stepRequest.getBaseClientRedirectUrl())
                .queryParam(OAuth.SCOPE, accessTokenScope.getValue());
    }
}