package com.yolt.providers.openbanking.ais.generic2.pec.submit.scheduled;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedDataInitiationException;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.GenericSubmitPaymentPreExecutionResult;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2Data;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2DataInitiation;
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
class GenericSubmitScheduledPaymentHttpRequestBodyProviderTest {

    private GenericSubmitScheduledPaymentHttpRequestBodyProvider subject;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach() {
        subject = new GenericSubmitScheduledPaymentHttpRequestBodyProvider(objectMapper, OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY);
    }

    @Test
    void shouldReturnWriteDomesticScheduledBasedOnUkProviderStateWhenProviderStateIsProvided() throws JsonProcessingException {
        // given
        GenericSubmitPaymentPreExecutionResult preExecutionResult = new GenericSubmitPaymentPreExecutionResult();
        preExecutionResult.setProviderState(prepareUkProviderState());
        OBWriteDomesticScheduled2DataInitiation dataInitiation = new OBWriteDomesticScheduled2DataInitiation();
        given(objectMapper.readValue("dataInitiation", OBWriteDomesticScheduled2DataInitiation.class))
                .willReturn(dataInitiation);
        OBWriteDomesticScheduled2 expectedResult = new OBWriteDomesticScheduled2()
                .risk(new OBRisk1()
                        .paymentContextCode(OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY))
                .data(new OBWriteDomesticScheduled2Data()
                        .consentId("consentId")
                        .initiation(dataInitiation));

        // when
        OBWriteDomesticScheduled2 result = subject.provideHttpRequestBody(preExecutionResult);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void shouldThrowMalformedDataInitiationExceptionWhenCannotParseDataInitiationJson() throws JsonProcessingException {
        // given
        GenericSubmitPaymentPreExecutionResult preExecutionResult = new GenericSubmitPaymentPreExecutionResult();
        preExecutionResult.setProviderState(prepareUkProviderState());

        given(objectMapper.readValue("dataInitiation", OBWriteDomesticScheduled2DataInitiation.class))
                .willThrow(JsonProcessingException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.provideHttpRequestBody(preExecutionResult);

        // then
        assertThatExceptionOfType(MalformedDataInitiationException.class)
                .isThrownBy(callable)
                .withMessage("Unable to parse data initiation");
    }

    private UkProviderState prepareUkProviderState() {
        return new UkProviderState("consentId", PaymentType.SINGLE, "dataInitiation");
    }
}