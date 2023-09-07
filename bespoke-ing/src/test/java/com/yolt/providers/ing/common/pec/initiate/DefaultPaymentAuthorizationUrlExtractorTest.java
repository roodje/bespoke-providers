package com.yolt.providers.ing.common.pec.initiate;

import com.yolt.providers.common.pis.sepa.SepaLinksDTO;
import com.yolt.providers.ing.common.dto.InitiatePaymentResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultPaymentAuthorizationUrlExtractorTest {

    private DefaultPaymentAuthorizationUrlExtractor sut;

    @Test
    void shouldExtractScaRedirectUrlFromProperResponse() {
        // given
        var expectedUrl = "fakeRedirectUrl";
        var response = new InitiatePaymentResponse(
                null,
                null,
                null,
                new SepaLinksDTO(expectedUrl, null)
        );

        sut = new DefaultPaymentAuthorizationUrlExtractor();

        // when
        var result = sut.extractAuthorizationUrl(response, null);

        // then
        assertThat(result).isEqualTo(expectedUrl);
    }
}