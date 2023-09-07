package com.yolt.providers.stet.generic.service.authorization.tool;

import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.service.authorization.request.StepRequest;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class DefaultPKCEAuthorizationRedirectUrlSupplier extends DefaultAuthorizationRedirectUrlSupplier {

    protected static final String CODE_CHALLENGE = "code_challenge";
    protected static final String CODE_CHALLENGE_METHOD = "code_challenge_method";

    protected final Supplier<OAuth2ProofKeyCodeExchange> codeExchangeSupplier;

    @Override
    public AuthorizationRedirect createAuthorizationRedirectUrl(String authUrl, Scope accessTokenScope, StepRequest stepRequest) {
        OAuth2ProofKeyCodeExchange codeExchange = codeExchangeSupplier.get();
        UriComponentsBuilder builder = configureUriComponents(authUrl, accessTokenScope, stepRequest)
                .queryParam(CODE_CHALLENGE, codeExchange.getCodeChallenge())
                .queryParam(CODE_CHALLENGE_METHOD, codeExchange.getCodeChallengeMethod());

        return AuthorizationRedirect.createWithProofKeyCodeExchangeCodeVerifier(builder.toUriString(), codeExchange.getCodeVerifier());
    }
}
