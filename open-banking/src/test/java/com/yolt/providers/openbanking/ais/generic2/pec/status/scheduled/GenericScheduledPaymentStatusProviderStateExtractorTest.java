package com.yolt.providers.openbanking.ais.generic2.pec.status.scheduled;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedDataInitiationException;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.ScheduledPaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.single.GenericPaymentStatusPreExecutionResult;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GenericScheduledPaymentStatusProviderStateExtractorTest {

    @InjectMocks
    private GenericScheduledPaymentStatusProviderStateExtractor subject;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnUkProviderStateWhenCorrectDataAreProvided() throws JsonProcessingException {
        // given
        GenericPaymentStatusPreExecutionResult preExecutionResult = new GenericPaymentStatusPreExecutionResult(null, null, null, null, "consentId");
        ScheduledPaymentStatusResponse.Data data = new ScheduledPaymentStatusResponse.Data("consentId", "paymentId", ScheduledPaymentStatusResponse.Data.Status.AWAITINGAUTHORISATION);
        ScheduledPaymentStatusResponse paymentStatusResponse = new ScheduledPaymentStatusResponse();
        paymentStatusResponse.setData(data);
        given(objectMapper.writeValueAsString(data))
                .willReturn("paymentStatusResponse");
        UkProviderState expectedState = new UkProviderState("consentId", PaymentType.SCHEDULED, "paymentStatusResponse");

        // when
        UkProviderState result = subject.extractUkProviderState(paymentStatusResponse, preExecutionResult);

        // then
        assertThat(result).isEqualTo(expectedState);
    }

    @Test
    void shouldThrowMalformedDataInitiationExceptionWhenCannotSerializeDataInitiation() throws JsonProcessingException {
        // given
        GenericPaymentStatusPreExecutionResult preExecutionResult = new GenericPaymentStatusPreExecutionResult(null, null, null, null, "consentId");
        ScheduledPaymentStatusResponse.Data data = new ScheduledPaymentStatusResponse.Data("consentId", "paymentId", ScheduledPaymentStatusResponse.Data.Status.AWAITINGAUTHORISATION);
        ScheduledPaymentStatusResponse paymentStatusResponse = new ScheduledPaymentStatusResponse();
        paymentStatusResponse.setData(data);
        given(objectMapper.writeValueAsString(data))
                .willThrow(JsonProcessingException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.extractUkProviderState(paymentStatusResponse, preExecutionResult);

        // then
        assertThatExceptionOfType(MalformedDataInitiationException.class)
                .isThrownBy(callable)
                .withMessage("Data initiation object cannot be parsed into JSON");
    }
}