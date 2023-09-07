package com.yolt.providers.stet.generic.service.authorization;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenStrategy;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenUnsupportedStrategy;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RefreshTokenUnsupportedStrategyTest {

    private final RefreshTokenStrategy sut = new RefreshTokenUnsupportedStrategy();

    @Test
    void shouldThrowUnsupportedOperationExceptionForRefreshAccessMeans() {
        // given / when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> sut.refreshAccessMeans(null, null);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isInstanceOf(TokenInvalidException.class);
    }
}
