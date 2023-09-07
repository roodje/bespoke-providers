package com.yolt.providers.stet.generic.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthorizationRedirect {

    private String url;
    private String proofKeyCodeExchangeCodeVerifier;

    public static AuthorizationRedirect create(String url) {
        return new AuthorizationRedirect(url);
    }

    public static AuthorizationRedirect createWithProofKeyCodeExchangeCodeVerifier(String url, String proofKeyCodeExchangeCodeVerifier) {
        return new AuthorizationRedirect(url, proofKeyCodeExchangeCodeVerifier);
    }

    private AuthorizationRedirect(String url) {
        this.url = url;
    }

    private AuthorizationRedirect(String url, String proofKeyCodeExchangeCodeVerifier) {
        this(url);
        this.proofKeyCodeExchangeCodeVerifier = proofKeyCodeExchangeCodeVerifier;
    }
}
