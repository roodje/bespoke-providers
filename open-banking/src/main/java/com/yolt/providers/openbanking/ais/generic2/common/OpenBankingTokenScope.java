package com.yolt.providers.openbanking.ais.generic2.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.TokenScope;

import java.util.Set;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum OpenBankingTokenScope {
    ACCOUNTS("accounts", "openid accounts", "openid accounts", Set.of(TokenScope.ACCOUNTS)),
    PAYMENTS("payments", "openid payments", "openid payments", Set.of(TokenScope.PAYMENTS)),
    ACCOUNTS_PAYMENTS("openid accounts payments", "openid accounts", "openid accounts payments", Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS));

    private final String grantScope;
    private final String authorizationUrlScope;
    private final String registrationScope;
    private final Set<TokenScope> tokenScopes;

    public static OpenBankingTokenScope getByTokenScopes(Set<TokenScope> tokenScopes) {
        for (OpenBankingTokenScope openBankingTokenScope : values()) {
            Set<TokenScope> scopes = openBankingTokenScope.getTokenScopes();
            if (scopes.size() == tokenScopes.size() && scopes.containsAll(tokenScopes)) {
                return openBankingTokenScope;
            }
        }
        throw new IllegalStateException("Unable to find OpenBankingTokenScope by collection of token scopes");
    }
}
