package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.RegistrationResponse;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.RaiffeisenAtGroupHttpClient;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultRaiffeisenAtGroupRegistrationServiceTest {

    private DefaultRaiffeisenAtGroupRegistrationService registrationService =
            new DefaultRaiffeisenAtGroupRegistrationService("RAIFFEISEN_AT");

    @Test
    void shouldReturnOptionalRegistrationResponse() throws TokenInvalidException {
        //given
        var expectedRegistrationResponse = mock(RegistrationResponse.class);
        var httpClient = mock(RaiffeisenAtGroupHttpClient.class);
        given(httpClient.register())
                .willReturn(expectedRegistrationResponse);

        //when
        var result = registrationService.register(httpClient);

        //then
        assertThat(result).isPresent()
                .contains(expectedRegistrationResponse);
    }

    @Test
    void shouldThrowAutoOnboardingExceptionWhenTokenInvalidExceptionIsThrownByHttpClient() throws TokenInvalidException {
        //given
        var expectedRegistrationResponse = mock(RegistrationResponse.class);
        var httpClient = mock(RaiffeisenAtGroupHttpClient.class);
        var tokenInvalidException = new TokenInvalidException("Unauthorized");
        given(httpClient.register())
                .willThrow(tokenInvalidException);

        //when
        ThrowableAssert.ThrowingCallable call = () -> registrationService.register(httpClient);

        //then
        assertThatExceptionOfType(AutoOnboardingException.class)
                .isThrownBy(call)
                .withMessage("Auto-onboarding failed for RAIFFEISEN_AT, message=Error during registration")
                .withCause(tokenInvalidException);
    }
}