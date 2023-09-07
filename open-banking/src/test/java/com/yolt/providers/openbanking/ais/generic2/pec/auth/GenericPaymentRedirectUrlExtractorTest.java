package com.yolt.providers.openbanking.ais.generic2.pec.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GenericPaymentRedirectUrlExtractorTest {

    @InjectMocks
    private GenericPaymentRedirectUrlExtractor subject;

    @Test
    void shouldReturnPureRedirectUrlWhenCorrectDataAreProvided() {
        // given
        String redirectUrlPostedBackFromSite = "http://localhost/callback?param1=val1&param2=val2#frag1=val4";

        // when
        String result = subject.extractPureRedirectUrl(redirectUrlPostedBackFromSite);

        // then
        assertThat(result).isEqualTo("http://localhost/callback");
    }
}