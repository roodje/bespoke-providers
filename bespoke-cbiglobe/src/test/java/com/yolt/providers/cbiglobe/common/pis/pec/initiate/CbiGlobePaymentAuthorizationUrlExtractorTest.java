package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.cbiglobe.pis.dto.LinksPaymentinitiationrequestType1;
import com.yolt.providers.cbiglobe.pis.dto.PaymentInitiationRequestResponseType;
import com.yolt.providers.cbiglobe.pis.dto.ScaRedirectLinkType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CbiGlobePaymentAuthorizationUrlExtractorTest {

    @InjectMocks
    private CbiGlobePaymentAuthorizationUrlExtractor subject;

    @Test
    void shouldReturnAuthorizationUrlForExtractAuthorizationUrlWhenCorrectData() {
        // given
        var initiatePaymentResponse = prepareInitiatePaymentResponse();

        // when
        var result = subject.extractAuthorizationUrl(initiatePaymentResponse, null);

        // then
        var uriComponents = UriComponentsBuilder.fromUriString(result).build();
        assertThat(uriComponents).extracting(UriComponents::getScheme, UriComponents::getHost, UriComponents::getPath)
                .contains("https", "localhost", "/authorize");
        assertThat(uriComponents.getQueryParams().toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "paymentId", "SOME-PAYMENT-ID",
                        "tppAuthenticationRedirectUri", "https://www.yolt.com/callback/payment?state=fakeState"
                ));
    }

    private PaymentInitiationRequestResponseType prepareInitiatePaymentResponse() {
        var redirect = new ScaRedirectLinkType();
        redirect.setHref("https://localhost/authorize?paymentId=SOME-PAYMENT-ID&tppAuthenticationRedirectUri=https://www.yolt.com/callback/payment?state=fakeState");
        var links = new LinksPaymentinitiationrequestType1();
        links.setScaRedirect(redirect);
        var initiatePaymentResponse = new PaymentInitiationRequestResponseType();
        initiatePaymentResponse.setPaymentId("fakePaymentId");
        initiatePaymentResponse.setLinks(links);
        return initiatePaymentResponse;
    }
}