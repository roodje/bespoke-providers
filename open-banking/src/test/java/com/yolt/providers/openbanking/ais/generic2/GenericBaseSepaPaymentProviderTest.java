package com.yolt.providers.openbanking.ais.generic2;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiateScheduledPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentResponseDTO;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.UkProviderStateDeserializer;
import com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled.GenericInitiateScheduledPaymentPreExecutionResult;
import com.yolt.providers.openbanking.ais.generic2.pec.initiate.single.GenericInitiatePaymentPreExecutionResult;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.ScheduledPaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.single.GenericPaymentStatusPreExecutionResult;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.GenericSubmitPaymentPreExecutionResult;
import com.yolt.providers.openbanking.ais.generic2.sepa.mapper.SepaPaymentMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.apache.commons.lang3.NotImplementedException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GenericBaseSepaPaymentProviderTest {

    private GenericBaseSepaPaymentProvider subject;

    private SepaPaymentMapper sepaPaymentMapper = new SepaPaymentMapper();

    @Mock
    private UkInitiateSinglePaymentExecutionContextAdapter<OBWriteDomesticConsent4, OBWriteDomesticConsentResponse5, GenericInitiatePaymentPreExecutionResult> initiatePaymentExecutionContextAdapter;
    @Mock
    private UkInitiateScheduledPaymentExecutionContextAdapter<OBWriteDomesticScheduledConsent4, OBWriteDomesticScheduledConsentResponse5, GenericInitiateScheduledPaymentPreExecutionResult> initiateScheduledPaymentExecutionContextAdapter;
    @Mock
    private UkSubmitPaymentExecutionContextAdapter<OBWriteDomestic2, OBWriteDomesticResponse5, GenericSubmitPaymentPreExecutionResult> submitPaymentExecutionContextAdapter;
    @Mock
    private UkSubmitPaymentExecutionContextAdapter<OBWriteDomesticScheduled2, OBWriteDomesticScheduledResponse5, GenericSubmitPaymentPreExecutionResult> submitScheduledPaymentExecutionContextAdapter;
    @Mock
    private UkStatusPaymentExecutionContextAdapter<Void, PaymentStatusResponse, GenericPaymentStatusPreExecutionResult> statusPaymentExecutionContextAdapter;
    @Mock
    private UkStatusPaymentExecutionContextAdapter<Void, ScheduledPaymentStatusResponse, GenericPaymentStatusPreExecutionResult> statusScheduledPaymentExecutionContextAdapter;
    @Mock
    private ConsentValidityRules consentValidityRules;
    @Mock
    private UkProviderStateDeserializer ukProviderStateDeserializer;

    @BeforeEach
    void setUp() {
        subject = new GenericBaseSepaPaymentProvider(
                initiatePaymentExecutionContextAdapter,
                initiateScheduledPaymentExecutionContextAdapter,
                submitPaymentExecutionContextAdapter,
                submitScheduledPaymentExecutionContextAdapter,
                statusPaymentExecutionContextAdapter,
                statusScheduledPaymentExecutionContextAdapter,
                null, null, null, null,
                consentValidityRules,
                ukProviderStateDeserializer,
                sepaPaymentMapper
        );
    }

    @Test
    void shouldCallInitiateSinglePaymentExecutionContextAdapterWhenSinglePaymentTypeIsReceivedInRequest() {
        //given
        var sepaRequest = createSepaInitiatePaymentRequest(null);
        var ukRequest = sepaPaymentMapper.mapInitiateSinglePaymentRequest(sepaRequest);
        var ukResponse = new InitiateUkDomesticPaymentResponseDTO("loginUrl", "providerState", null);
        var sepaResponse = sepaPaymentMapper.mapInitiatePaymentResponse(ukResponse);
        given(initiatePaymentExecutionContextAdapter.initiateSinglePayment(ukRequest)).willReturn(ukResponse);

        //when
        var response = subject.initiatePayment(sepaRequest);

        //then
        assertThat(response).usingRecursiveComparison().isEqualTo(sepaResponse);
        then(initiateScheduledPaymentExecutionContextAdapter).shouldHaveNoInteractions();
    }

    @Test
    void shouldCallInitiateScheduledPaymentExecutionContextAdapterWhenScheduledPaymentTypeIsReceivedInRequest() {
        //given
        var sepaRequest = createSepaInitiatePaymentRequest(LocalDate.now());
        var ukRequest = sepaPaymentMapper.mapInitiateScheduledPaymentRequest(sepaRequest);
        var ukResponse = new InitiateUkDomesticPaymentResponseDTO("loginUrl", "providerState", null);
        var sepaResponse = sepaPaymentMapper.mapInitiatePaymentResponse(ukResponse);
        given(initiateScheduledPaymentExecutionContextAdapter.initiateScheduledPayment(ukRequest)).willReturn(ukResponse);

        //when
        var response = subject.initiateScheduledPayment(sepaRequest);

        //then
        assertThat(response).usingRecursiveComparison().isEqualTo(sepaResponse);
        then(initiatePaymentExecutionContextAdapter).shouldHaveNoInteractions();
    }

    @Test
    void shouldCallSubmitSinglePaymentExecutionContextAdapterWhenSinglePaymentTypeIsReceivedInRequest() {
        //given
        var ukProviderState = new UkProviderState(null, PaymentType.SINGLE, null);
        var sepaRequest = new SubmitPaymentRequest("providerState",
                new HashMap<>(),
                "https://redirecturi.com",
                mock(Signer.class),
                mock(RestTemplateManager.class),
                null,
                null);
        var ukRequest = sepaPaymentMapper.mapSubmitPaymentRequest(sepaRequest);
        var ukResponse = new PaymentStatusResponseDTO("providerState", "paymentId", null);
        var sepaResponse = sepaPaymentMapper.mapSubmitPaymentResponse(ukResponse);

        given(ukProviderStateDeserializer.deserialize("providerState")).willReturn(ukProviderState);
        given(submitPaymentExecutionContextAdapter.submitPayment(ukRequest)).willReturn(ukResponse);

        //when
        var response = subject.submitPayment(sepaRequest);

        //then
        assertThat(response).isEqualTo(sepaResponse);
        then(submitScheduledPaymentExecutionContextAdapter).shouldHaveNoInteractions();
    }

    @Test
    void shouldCallSubmitScheduledPaymentExecutionContextAdapterWhenScheduledPaymentTypeIsReceivedInRequest() {
        //given
        var ukProviderState = new UkProviderState(null, PaymentType.SCHEDULED, null);
        var sepaRequest = new SubmitPaymentRequest("providerState",
                new HashMap<>(),
                "https://redirecturi.com",
                mock(Signer.class),
                mock(RestTemplateManager.class),
                null,
                null);
        var ukRequest = sepaPaymentMapper.mapSubmitPaymentRequest(sepaRequest);
        var ukResponse = new PaymentStatusResponseDTO("providerState", "paymentId", null);
        var sepaResponse = sepaPaymentMapper.mapSubmitPaymentResponse(ukResponse);

        given(ukProviderStateDeserializer.deserialize("providerState")).willReturn(ukProviderState);
        given(submitScheduledPaymentExecutionContextAdapter.submitPayment(ukRequest)).willReturn(ukResponse);

        //when
        var response = subject.submitPayment(sepaRequest);

        //then
        assertThat(response).isEqualTo(sepaResponse);
        then(submitPaymentExecutionContextAdapter).shouldHaveNoInteractions();
    }

    @Test
    void shouldThrowNotImplementedExceptionWhenPeriodicPaymentTypeIsReceivedInSubmitRequest() {
        //given
        var ukProviderState = new UkProviderState(null, PaymentType.PERIODIC, null);
        var sepaRequest = new SubmitPaymentRequest("providerState",
                new HashMap<>(),
                "https://redirecturi.com",
                mock(Signer.class),
                mock(RestTemplateManager.class),
                null,
                null);
        given(ukProviderStateDeserializer.deserialize("providerState")).willReturn(ukProviderState);

        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.submitPayment(sepaRequest);

        //then
        assertThatExceptionOfType(NotImplementedException.class)
                .isThrownBy(call)
                .withMessage("Periodic payments are mot implemented yet");
    }

    @Test
    void shouldCallStatusSinglePaymentExecutionContextAdapterWhenSinglePaymentTypeIsReceivedInRequest() {
        //given
        var ukProviderState = new UkProviderState(null, PaymentType.SINGLE, null);
        var sepaRequest = new GetStatusRequest(
                "providerState",
                null,
                new HashMap<>(),
                mock(Signer.class),
                mock(RestTemplateManager.class),
                null,
                null
        );
        var ukRequest = sepaPaymentMapper.mapStatusRequest(sepaRequest);
        var ukResponse = new PaymentStatusResponseDTO("providerState", "paymentId", null);
        var sepaResponse = sepaPaymentMapper.mapStatusResponse(ukResponse);
        given(ukProviderStateDeserializer.deserialize("providerState")).willReturn(ukProviderState);
        given(statusPaymentExecutionContextAdapter.getPaymentStatus(ukRequest)).willReturn(ukResponse);

        //when
        var result = subject.getStatus(sepaRequest);

        //then
        assertThat(result).isEqualTo(sepaResponse);
        then(statusScheduledPaymentExecutionContextAdapter).shouldHaveNoInteractions();
    }

    @Test
    void shouldCallStatusScheduledPaymentExecutionContextAdapterWhenScheduledPaymentTypeIsReceivedInRequest() {
        //given
        var ukProviderState = new UkProviderState(null, PaymentType.SCHEDULED, null);
        var sepaRequest = new GetStatusRequest(
                "providerState",
                null,
                new HashMap<>(),
                mock(Signer.class),
                mock(RestTemplateManager.class),
                null,
                null
        );
        var ukRequest = sepaPaymentMapper.mapStatusRequest(sepaRequest);
        var ukResponse = new PaymentStatusResponseDTO("providerState", "paymentId", null);
        var sepaResponse = sepaPaymentMapper.mapStatusResponse(ukResponse);
        given(ukProviderStateDeserializer.deserialize("providerState")).willReturn(ukProviderState);
        given(statusScheduledPaymentExecutionContextAdapter.getPaymentStatus(ukRequest)).willReturn(ukResponse);

        //when
        var result = subject.getStatus(sepaRequest);

        //then
        assertThat(result).isEqualTo(sepaResponse);
        then(statusPaymentExecutionContextAdapter).shouldHaveNoInteractions();
    }

    @Test
    void shouldThrowNotImplementedExceptionWhenPeriodicPaymentTypeIsReceivedInStatusRequest() {
        //given
        var ukProviderState = new UkProviderState(null, PaymentType.PERIODIC, null);
        var sepaRequest = new GetStatusRequest(
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
        ThrowableAssert.ThrowingCallable call = () -> subject.getStatus(sepaRequest);

        //then
        assertThatExceptionOfType(NotImplementedException.class)
                .isThrownBy(call)
                .withMessage("Periodic payments are mot implemented yet");
    }

    private InitiatePaymentRequest createSepaInitiatePaymentRequest(LocalDate executionDate) {
        var sepaRequestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "GB91ABNA0417164300"))
                .creditorName("Some Name")
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, "GB91ABNA0417164322"))
                .endToEndIdentification("123456789012345")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("5877.78")))
                .remittanceInformationUnstructured("unstructured-information")
                .executionDate(executionDate)
                .build();
        return new InitiatePaymentRequestBuilder()
                .setRequestDTO(sepaRequestDTO)
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setAuthenticationMeans(new HashMap<>())
                .setSigner(mock(Signer.class))
                .setState("providerState")
                .setRestTemplateManager(mock(RestTemplateManager.class))
                .setPsuIpAddress(null)
                .build();
    }
}