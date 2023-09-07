package com.yolt.providers.stet.bpcegroup;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.bpcegroup.common.mapper.token.BpceGroupTokenRequestMapper;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.request.RefreshTokenRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

public class BpceGroupTokenRequestMapperTest {

    @Mock
    private Signer signer;

    private final BpceGroupTokenRequestMapper sut = new BpceGroupTokenRequestMapper();

    @Test
    void shouldReturnCorrectSetOfEntriesForMapAccessTokenRequestWhenCorrectDataProvided() {
        // given
        AccessTokenRequest accessTokenRequestDTO = new AccessTokenRequest(
                "tokenUrl",
                DefaultAuthenticationMeans.builder()
                        .clientId("fakeClient")
                        .build(),
                "authCode",
                "redirectUrl",
                DataProviderState.preAuthorizedProviderState(new Region(), AuthorizationRedirect.create("url")),
                Scope.AISP,
                signer
        );
        MultiValueMap<String, String> expectedMap = new LinkedMultiValueMap<>();
        expectedMap.set(OAuth.GRANT_TYPE, OAuth.AUTHORIZATION_CODE);
        expectedMap.set(OAuth.CODE, accessTokenRequestDTO.getAuthorizationCode());
        expectedMap.set(OAuth.CLIENT_ID, accessTokenRequestDTO.getAuthMeans().getClientId());
        expectedMap.set(OAuth.REDIRECT_URI, accessTokenRequestDTO.getRedirectUrl());

        // when
        MultiValueMap<String, String> result = sut.mapAccessTokenRequest(accessTokenRequestDTO);

        // then
        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedMap);
    }

    @Test
    void shouldReturnCorrectSetOfEntriesForMapRefreshTokenRequestWhenCorrectDataProvided() {
        // given
        RefreshTokenRequest refreshTokenRequestDTO = new RefreshTokenRequest(
                "tokenUrl",
                DefaultAuthenticationMeans.builder()
                        .clientId("fakeClientId")
                        .build(),
                "refreshToken",
                Scope.AISP,
                signer
        );
        MultiValueMap<String, String> expectedMap = new LinkedMultiValueMap<>();
        expectedMap.set(OAuth.GRANT_TYPE, OAuth.REFRESH_TOKEN);
        expectedMap.set(OAuth.REFRESH_TOKEN, refreshTokenRequestDTO.getRefreshToken());
        expectedMap.set(OAuth.CLIENT_ID, refreshTokenRequestDTO.getAuthMeans().getClientId());

        // when
        MultiValueMap<String, String> result = sut.mapRefreshTokenRequest(refreshTokenRequestDTO);

        // then
        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedMap);
    }
}
