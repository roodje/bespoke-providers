package com.yolt.providers.stet.cicgroup.beobank.service.auth;

import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.service.authorization.request.StepRequest;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultPKCEAuthorizationRedirectUrlSupplier;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BeobankAuthorizationRedirectUrlSupplier extends DefaultPKCEAuthorizationRedirectUrlSupplier {
    public BeobankAuthorizationRedirectUrlSupplier(Supplier<OAuth2ProofKeyCodeExchange> codeExchangeSupplier) {
        super(codeExchangeSupplier);
    }

    @Override
    public AuthorizationRedirect createAuthorizationRedirectUrl(String authUrl, Scope accessTokenScope, StepRequest stepRequest) {
        OAuth2ProofKeyCodeExchange codeExchange = codeExchangeSupplier.get();
        UriComponentsBuilder builder = configureUriComponents(authUrl, accessTokenScope, stepRequest)
                .queryParam(CODE_CHALLENGE, codeExchange.getCodeChallenge())
                .queryParam(CODE_CHALLENGE_METHOD, codeExchange.getCodeChallengeMethod());

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("lang", stepRequest.getConsentLanguage());

        return AuthorizationRedirect.createWithProofKeyCodeExchangeCodeVerifier(builder.buildAndExpand(urlParams).toUriString(), codeExchange.getCodeVerifier());
    }
}
