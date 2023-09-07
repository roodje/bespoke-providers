package com.yolt.providers.stet.generic.service.authorization.tool;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.service.authorization.request.StepRequest;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultPKCEAuthorizationRedirectUrlSupplierTest {

    @InjectMocks
    private DefaultPKCEAuthorizationRedirectUrlSupplier sut;

    @Mock
    private Supplier<OAuth2ProofKeyCodeExchange> codeExchangeSupplier;

    @Test
    void shouldReturnProperResultWithProperUrlAndCodeVerifierForGetAuthorizationRedirectUrlWhenCodeExchangeNotProvided() {
        // given
        String authUrl = "http://localhost/authorize";
        Scope tokenScope = Scope.AISP;
        StepRequest stepRequest = new StepRequest(DefaultAuthenticationMeans.builder()
                .clientId("fakeClient")
                .build(),
                "http://localhost/redirect",
                "fakeState",
                "",
                "");
        OAuth2ProofKeyCodeExchange codeExchange = OAuth2ProofKeyCodeExchange.createRandomS256();
        when(codeExchangeSupplier.get())
                .thenReturn(codeExchange);

        // when
        AuthorizationRedirect result = sut.createAuthorizationRedirectUrl(authUrl, tokenScope, stepRequest);

        // then
        assertThat(result.getProofKeyCodeExchangeCodeVerifier()).isEqualTo(codeExchange.getCodeVerifier());
        assertThat(result.getUrl()).isNotEmpty();
        URI uri = URI.create(result.getUrl());
        assertThat(uri)
                .hasScheme("http")
                .hasHost("localhost")
                .hasPath("/authorize")
                .hasParameter(OAuth.CLIENT_ID, stepRequest.getAuthMeans().getClientId())
                .hasParameter(OAuth.RESPONSE_TYPE, OAuth.CODE)
                .hasParameter(OAuth.SCOPE, tokenScope.getValue())
                .hasParameter(OAuth.STATE, stepRequest.getState())
                .hasParameter(OAuth.REDIRECT_URI, stepRequest.getBaseClientRedirectUrl())
                .hasParameter(DefaultPKCEAuthorizationRedirectUrlSupplier.CODE_CHALLENGE, codeExchange.getCodeChallenge())
                .hasParameter(DefaultPKCEAuthorizationRedirectUrlSupplier.CODE_CHALLENGE_METHOD, codeExchange.getCodeChallengeMethod());
    }
}
