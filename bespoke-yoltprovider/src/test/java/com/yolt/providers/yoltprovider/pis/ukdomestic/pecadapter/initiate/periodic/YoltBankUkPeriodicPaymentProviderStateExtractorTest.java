package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBDomesticStandingOrder1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDataDomesticStandingOrderConsentResponse1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticStandingOrderConsentResponse1;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class YoltBankUkPeriodicPaymentProviderStateExtractorTest {

    @InjectMocks
    private YoltBankUkPeriodicPaymentProviderStateExtractor ukProviderStateExtractor;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnUkProviderStateForExtractUkProviderState() throws JsonProcessingException {
        // given
        InitiatePaymentConsentResponse initiatePaymentConsentResponse = new InitiatePaymentConsentResponse(
                "",
                "fakePaymentConsent"
        );
        OBDomesticStandingOrder1 initiation = new OBDomesticStandingOrder1();
        OBWriteDomesticStandingOrderConsentResponse1 consentResponse1 = new OBWriteDomesticStandingOrderConsentResponse1()
                .data(new OBWriteDataDomesticStandingOrderConsentResponse1()
                        .consentId("fakePaymentId")
                        .initiation(initiation));
        given(objectMapper.readValue("fakePaymentConsent", OBWriteDomesticStandingOrderConsentResponse1.class))
                .willReturn(consentResponse1);

        // when
        UkProviderState result = ukProviderStateExtractor.extractUkProviderState(initiatePaymentConsentResponse, null);

        // then
        assertThat(result).extracting(UkProviderState::getConsentId, UkProviderState::getPaymentType, UkProviderState::getOpenBankingPayment)
                .contains(null, PaymentType.PERIODIC, null);
    }

    @Test
    void shouldThrowExceptionWhenObjectMappingFails() throws JsonProcessingException {
        // given
        InitiatePaymentConsentResponse initiatePaymentConsentResponse = new InitiatePaymentConsentResponse(
                "",
                "fakePaymentConsent"
        );

        doThrow(new JsonProcessingException("error") {
        }).when(objectMapper).readValue(anyString(), any(Class.class));
        //when
        ThrowableAssert.ThrowingCallable callable = () -> ukProviderStateExtractor.extractUkProviderState(initiatePaymentConsentResponse, null);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(JsonProcessingException.class)
                .satisfies(throwable -> assertThat(throwable.getCause().getMessage()).isEqualTo("error"));
    }
}