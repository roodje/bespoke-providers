package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common;

import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.SepaInitiatePaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class YoltBankSepaPaymentAuthorizationUrlExtractorTest {

    private YoltBankSepaPaymentAuthorizationUrlExtractor authorizationUrlExtractor;

    @BeforeEach
    public void setup() {
        authorizationUrlExtractor = new YoltBankSepaPaymentAuthorizationUrlExtractor();
    }

    @Test
    void shouldReturnProperLoginUrl() {
        // given
        SepaInitiatePaymentResponse sepaInitiatePaymentResponseDTO = new SepaInitiatePaymentResponse(
                "http://redirect",
                null,
                null
        );

        // when
        String result = authorizationUrlExtractor.extractAuthorizationUrl(sepaInitiatePaymentResponseDTO, null);

        // then
        assertThat(result).isEqualTo("http://redirect");
    }

}
