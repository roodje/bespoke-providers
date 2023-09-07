package com.yolt.providers.knabgroup.common.payment.initiate;

import com.yolt.providers.knabgroup.common.payment.dto.external.InitiatePaymentResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultInitiatePaymentAuthorizationUrlExtractorTest {

    private DefaultInitiatePaymentAuthorizationUrlExtractor subject;

    @Test
    void shouldExtractScaRedirectUrlFromProperResponse() {
        // given
        String expectedUrl = "fakeRedirectUrl";
        InitiatePaymentResponse response = new InitiatePaymentResponse(null, null, expectedUrl);

        subject = new DefaultInitiatePaymentAuthorizationUrlExtractor();

        // when
        String result = subject.extractAuthorizationUrl(response, null);

        // then
        assertThat(result).isEqualTo(expectedUrl);
    }
}