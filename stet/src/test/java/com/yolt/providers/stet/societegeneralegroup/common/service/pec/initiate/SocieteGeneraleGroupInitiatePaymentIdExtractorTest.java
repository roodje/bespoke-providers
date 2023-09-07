package com.yolt.providers.stet.societegeneralegroup.common.service.pec.initiate;

import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SocieteGeneraleGroupInitiatePaymentIdExtractorTest {

    @Mock
    PaymentAuthorizationUrlExtractor authorizationUrlExtractor;

    private SocieteGeneraleGroupInitiatePaymentIdExtractor paymentIdExtractor;

    @BeforeEach
    void setUp() {
        paymentIdExtractor = new SocieteGeneraleGroupInitiatePaymentIdExtractor(authorizationUrlExtractor);
    }

    @Test
    void shouldReturnPaymentIdWhenValidUrlWasReturnedFromBank() {
        //given
        StetPaymentInitiationResponseDTO responseDTO = new StetPaymentInitiationResponseDTO();
        given(authorizationUrlExtractor.extractAuthorizationUrl(responseDTO, null)).willReturn("https://particuliers.societegenerale.fr/app/auth/icd/obu/index-authsec.html#obu/eaefdeff-11ec-429f-bb55-019245fe0604?usuallyAbsentQueryParam=true");

        //when
        String result = paymentIdExtractor.extractPaymentId(responseDTO, null);

        //then
        assertThat(result).isEqualTo("eaefdeff-11ec-429f-bb55-019245fe0604");
    }

    @Test
    void shouldThrowGetGetLoginInfoUrlFailedExceptionWhenIncorrectUrlWasReturnedFromBank() {
        //given
        StetPaymentInitiationResponseDTO responseDTO = new StetPaymentInitiationResponseDTO();
        given(authorizationUrlExtractor.extractAuthorizationUrl(responseDTO, null)).willReturn("https:// particuliers.societegenerale.fr");

        //when
        ThrowableAssert.ThrowingCallable call = () -> paymentIdExtractor.extractPaymentId(responseDTO, null);

        //then
        assertThatExceptionOfType(GetLoginInfoUrlFailedException.class)
                .isThrownBy(call)
                .withMessage("Payment Request Resource Id not found.");
    }
}