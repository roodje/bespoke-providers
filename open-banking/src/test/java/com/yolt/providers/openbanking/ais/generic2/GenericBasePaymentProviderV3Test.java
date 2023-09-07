package com.yolt.providers.openbanking.ais.generic2;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.UkProviderStateDeserializer;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.ScheduledPaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.single.GenericPaymentStatusPreExecutionResult;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.GenericSubmitPaymentPreExecutionResult;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5;
import org.apache.commons.lang3.NotImplementedException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GenericBasePaymentProviderV3Test {

    GenericBasePaymentProviderV3 subject;

    @Mock
    UkSubmitPaymentExecutionContextAdapter<OBWriteDomestic2, OBWriteDomesticResponse5, GenericSubmitPaymentPreExecutionResult> submitPaymentExecutionContextAdapter;
    @Mock
    UkSubmitPaymentExecutionContextAdapter<OBWriteDomesticScheduled2, OBWriteDomesticScheduledResponse5, GenericSubmitPaymentPreExecutionResult> submitScheduledPaymentExecutionContextAdapter;
    @Mock
    UkStatusPaymentExecutionContextAdapter<Void, PaymentStatusResponse, GenericPaymentStatusPreExecutionResult> statusPaymentExecutionContextAdapter;
    @Mock
    UkStatusPaymentExecutionContextAdapter<Void, ScheduledPaymentStatusResponse, GenericPaymentStatusPreExecutionResult> statusScheduledPaymentExecutionContextAdapter;
    @Mock
    ConsentValidityRules consentValidityRules;
    @Mock
    UkProviderStateDeserializer ukProviderStateDeserializer;

    @BeforeEach
    void setUp() {
        subject = new GenericBasePaymentProviderV3(
                null, null,
                submitPaymentExecutionContextAdapter,
                submitScheduledPaymentExecutionContextAdapter,
                statusPaymentExecutionContextAdapter,
                statusScheduledPaymentExecutionContextAdapter,
                null, null, null, null,
                consentValidityRules,
                ukProviderStateDeserializer
        );
    }

    @Test
    void shouldCallSubmitSinglePaymentExecutionContextAdapterWhenSinglePaymentTypeIsReceivedInRequest() {
        //given
        UkProviderState ukProviderState = new UkProviderState(null, PaymentType.SINGLE, null);
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest("providerState",
                new HashMap<>(),
                "https://redirecturi.com",
                mock(Signer.class),
                mock(RestTemplateManager.class),
                null,
                null);
        given(ukProviderStateDeserializer.deserialize("providerState")).willReturn(ukProviderState);
        PaymentStatusResponseDTO expectedResult = mock(PaymentStatusResponseDTO.class);
        given(submitPaymentExecutionContextAdapter.submitPayment(submitPaymentRequest)).willReturn(expectedResult);

        //when
        PaymentStatusResponseDTO result = subject.submitPayment(submitPaymentRequest);

        //then
        assertThat(result).isEqualTo(expectedResult);
        then(submitScheduledPaymentExecutionContextAdapter).shouldHaveNoInteractions();
    }

    @Test
    void shouldCallSubmitScheduledPaymentExecutionContextAdapterWhenScheduledPaymentTypeIsReceivedInRequest() {
        //given
        UkProviderState ukProviderState = new UkProviderState(null, PaymentType.SCHEDULED, null);
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest("providerState",
                new HashMap<>(),
                "https://redirecturi.com",
                mock(Signer.class),
                mock(RestTemplateManager.class),
                null,
                null);
        given(ukProviderStateDeserializer.deserialize("providerState")).willReturn(ukProviderState);
        PaymentStatusResponseDTO expectedResult = mock(PaymentStatusResponseDTO.class);
        given(submitScheduledPaymentExecutionContextAdapter.submitPayment(submitPaymentRequest)).willReturn(expectedResult);

        //when
        PaymentStatusResponseDTO result = subject.submitPayment(submitPaymentRequest);

        //then
        assertThat(result).isEqualTo(expectedResult);
        then(submitPaymentExecutionContextAdapter).shouldHaveNoInteractions();
    }

    @Test
    void shouldThrowNotImplementedExceptionWhenPeriodicPaymentTypeIsReceivedInSubmitRequest() {
        //given
        UkProviderState ukProviderState = new UkProviderState(null, PaymentType.PERIODIC, null);
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest("providerState",
                new HashMap<>(),
                "https://redirecturi.com",
                mock(Signer.class),
                mock(RestTemplateManager.class),
                null,
                null);
        given(ukProviderStateDeserializer.deserialize("providerState")).willReturn(ukProviderState);

        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.submitPayment(submitPaymentRequest);

        //then
        assertThatExceptionOfType(NotImplementedException.class)
                .isThrownBy(call)
                .withMessage("Periodic payments are mot implemented yet");
    }

    @Test
    void shouldCallStatusSinglePaymentExecutionContextAdapterWhenSinglePaymentTypeIsReceivedInRequest() {
        //given
        UkProviderState ukProviderState = new UkProviderState(null, PaymentType.SINGLE, null);
        GetStatusRequest getStatusRequest = new GetStatusRequest(
                "providerState",
                null,
                new HashMap<>(),
                mock(Signer.class),
                mock(RestTemplateManager.class),
                null,
                null
        );
        given(ukProviderStateDeserializer.deserialize("providerState")).willReturn(ukProviderState);
        PaymentStatusResponseDTO expectedResult = mock(PaymentStatusResponseDTO.class);
        given(statusPaymentExecutionContextAdapter.getPaymentStatus(getStatusRequest)).willReturn(expectedResult);

        //when
        PaymentStatusResponseDTO result = subject.getStatus(getStatusRequest);

        //then
        assertThat(result).isEqualTo(expectedResult);
        then(statusScheduledPaymentExecutionContextAdapter).shouldHaveNoInteractions();
    }

    @Test
    void shouldCallStatusScheduledPaymentExecutionContextAdapterWhenScheduledPaymentTypeIsReceivedInRequest() {
        //given
        UkProviderState ukProviderState = new UkProviderState(null, PaymentType.SCHEDULED, null);
        GetStatusRequest getStatusRequest = new GetStatusRequest(
                "providerState",
                null,
                new HashMap<>(),
                mock(Signer.class),
                mock(RestTemplateManager.class),
                null,
                null
        );
        given(ukProviderStateDeserializer.deserialize("providerState")).willReturn(ukProviderState);
        PaymentStatusResponseDTO expectedResult = mock(PaymentStatusResponseDTO.class);
        given(statusScheduledPaymentExecutionContextAdapter.getPaymentStatus(getStatusRequest)).willReturn(expectedResult);

        //when
        PaymentStatusResponseDTO result = subject.getStatus(getStatusRequest);

        //then
        assertThat(result).isEqualTo(expectedResult);
        then(statusPaymentExecutionContextAdapter).shouldHaveNoInteractions();
    }

    @Test
    void shouldThrowNotImplementedExceptionWhenPeriodicPaymentTypeIsReceivedInStatusRequest() {
        //given
        UkProviderState ukProviderState = new UkProviderState(null, PaymentType.PERIODIC, null);
        GetStatusRequest getStatusRequest = new GetStatusRequest(
                "providerState",
                null,
                new HashMap<>(),
                mock(Signer.class),
                mock(RestTemplateManager.class),
                null,
                null
        );
        given(ukProviderStateDeserializer.deserialize("providerState")).willReturn(ukProviderState);

        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.getStatus(getStatusRequest);

        //then
        assertThatExceptionOfType(NotImplementedException.class)
                .isThrownBy(call)
                .withMessage("Periodic payments are mot implemented yet");
    }

}