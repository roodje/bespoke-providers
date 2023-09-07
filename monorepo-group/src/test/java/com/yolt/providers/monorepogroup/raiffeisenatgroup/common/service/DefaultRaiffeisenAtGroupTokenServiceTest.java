package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Token;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.RaiffeisenAtGroupHttpClient;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;

import java.util.UUID;

import static com.yolt.providers.common.constants.OAuth.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultRaiffeisenAtGroupTokenServiceTest {

    private DefaultRaiffeisenAtGroupTokenService tokenService = new DefaultRaiffeisenAtGroupTokenService();

    @Test
    void shouldCreateClientCredentialToken() throws TokenInvalidException {
        //given
        var clientId = UUID.randomUUID().toString();
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, clientId);
        var requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add(GRANT_TYPE, CLIENT_CREDENTIALS);
        requestBody.add(CLIENT_ID, clientId);
        requestBody.add(SCOPE, "apic-psd2");
        var expectedAccessToken = "CLIENT-ACCESS-TOKEN";
        var tokenResponse = mock(Token.class);
        given(tokenResponse.getAccessToken()).willReturn(expectedAccessToken);
        var httpClient = mock(RaiffeisenAtGroupHttpClient.class);
        given(httpClient.createClientCredentialToken(requestBody)).willReturn(tokenResponse);

        //when
        var result = tokenService.createClientCredentialToken(httpClient, authenticationMeans);

        //then
        assertThat(result).isEqualTo(expectedAccessToken);
    }

    @Test
    void shouldThrowTokenInvalidExceptionWhenItIsThrownByHttpClient() throws TokenInvalidException {
        //given
        var clientId = UUID.randomUUID().toString();
        var authenticationMeans = new RaiffeisenAtGroupAuthenticationMeans(UUID.randomUUID(), null, clientId);
        var requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add(GRANT_TYPE, CLIENT_CREDENTIALS);
        requestBody.add(CLIENT_ID, clientId);
        requestBody.add(SCOPE, "apic-psd2");
        var httpClient = mock(RaiffeisenAtGroupHttpClient.class);
        given(httpClient.createClientCredentialToken(requestBody)).willThrow(TokenInvalidException.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> tokenService.createClientCredentialToken(httpClient, authenticationMeans);

        //then
        assertThatExceptionOfType(TokenInvalidException.class)
                .isThrownBy(call);
    }
}