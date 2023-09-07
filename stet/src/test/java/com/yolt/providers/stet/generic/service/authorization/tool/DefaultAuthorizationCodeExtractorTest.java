package com.yolt.providers.stet.generic.service.authorization.tool;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DefaultAuthorizationCodeExtractorTest {

    @InjectMocks
    private DefaultAuthorizationCodeExtractor sut;

    @Test
    void shouldReturnAuthorizationCodeForExtractAuthorizationCodeWhenCorrectRedirectUrlProvided() {
        // given
        String redirectUrlPostedBackFromSite = "http://localhost/redirect?code=1234";

        // when
        String result = sut.extractAuthorizationCode(redirectUrlPostedBackFromSite);

        // then
        assertThat(result).isEqualTo("1234");
    }

    @Test
    void shouldThrowGetAccessTokenFailedExceptionForExtractAuthorizationCodeWhenErrorRedirectUrlProvided() {
        // given
        String redirectUrlPostedBackFromSite = "http://localhost/redirect?error=Something went wrong";

        // when
        ThrowableAssert.ThrowingCallable extractAuthorizationCodeCallable = () -> sut.extractAuthorizationCode(redirectUrlPostedBackFromSite);

        // then
        assertThatThrownBy(extractAuthorizationCodeCallable)
                .isInstanceOf(GetAccessTokenFailedException.class)
                .hasMessage("Cannot extract authorization code due to an error in callback URL. Error details: Something went wrong");
    }

    @Test
    void shouldThrowGetAccessTokenFailedExceptionForExtractAuthorizationCodeWhenNoCodeProvided() {
        // given
        String redirectUrlPostedBackFromSite = "http://localhost/redirect?state=state";

        // when
        ThrowableAssert.ThrowingCallable extractAuthorizationCodeCallable = () -> sut.extractAuthorizationCode(redirectUrlPostedBackFromSite);

        // then
        assertThatThrownBy(extractAuthorizationCodeCallable)
                .isInstanceOf(GetAccessTokenFailedException.class)
                .hasMessage("Cannot extract authorization code due to an error in callback URL. Error details: Authorization code not provided");
    }
}
