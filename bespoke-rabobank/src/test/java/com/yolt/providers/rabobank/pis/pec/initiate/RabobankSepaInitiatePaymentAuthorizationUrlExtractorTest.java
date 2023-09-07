package com.yolt.providers.rabobank.pis.pec.initiate;

import com.yolt.providers.rabobank.dto.external.HrefType;
import com.yolt.providers.rabobank.dto.external.InitiatedTransactionResponse;
import com.yolt.providers.rabobank.dto.external.Links;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabobankSepaInitiatePaymentAuthorizationUrlExtractorTest {

    private RabobankSepaInitiatePaymentAuthorizationUrlExtractor subject;

    @BeforeEach
    void setUp() {
        subject = new RabobankSepaInitiatePaymentAuthorizationUrlExtractor();
    }

    @Test
    void shouldExtractAuthorizationUrlFromTransactionInitiateResponse() {
        //given
        String expectedAuthorizationUrl = "https://rabobank.com/autorizationUrl";
        HrefType hrefType = mock(HrefType.class);
        when(hrefType.getHref()).thenReturn(expectedAuthorizationUrl);
        Links links = mock(Links.class);
        when(links.getScaRedirect()).thenReturn(hrefType);
        InitiatedTransactionResponse initiatedTransactionResponse = mock(InitiatedTransactionResponse.class);
        when(initiatedTransactionResponse.getLinks()).thenReturn(links);

        //when
        String returnedAuthorizationUrl = subject.extractAuthorizationUrl(initiatedTransactionResponse, null);

        //then
        assertThat(returnedAuthorizationUrl).isEqualTo(expectedAuthorizationUrl);
    }

}
