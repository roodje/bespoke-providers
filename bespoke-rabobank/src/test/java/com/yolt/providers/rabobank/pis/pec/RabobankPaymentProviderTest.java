package com.yolt.providers.rabobank.pis.pec;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.rabobank.dto.external.InitiatedTransactionResponse;
import com.yolt.providers.rabobank.dto.external.SepaCreditTransfer;
import com.yolt.providers.rabobank.dto.external.StatusResponse;
import com.yolt.providers.rabobank.pis.RabobankPaymentProvider;
import com.yolt.providers.rabobank.pis.pec.initiate.RabobankSepaInitiatePreExecutionResult;
import com.yolt.providers.rabobank.pis.pec.submit.RabobankSepaSubmitPaymentPreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static com.yolt.providers.rabobank.RabobankAuthenticationMeans.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabobankPaymentProviderTest {

    private RabobankPaymentProvider subject;

    @Mock
    private SepaInitiatePaymentExecutionContextAdapter<SepaCreditTransfer, InitiatedTransactionResponse, RabobankSepaInitiatePreExecutionResult> sepaInitiatedPaymentExecutionContextAdapter;

    @Mock
    private SepaSubmitPaymentExecutionContextAdapter<Void, StatusResponse, RabobankSepaSubmitPaymentPreExecutionResult> sepaSubmitPaymentExecutionContextAdapter;

    @Mock
    private SepaStatusPaymentExecutionContextAdapter<Void, StatusResponse, RabobankSepaSubmitPaymentPreExecutionResult> sepaStatusPaymentExecutionContextAdapter;

    private

    @BeforeEach
    void setUp() {
        subject = new RabobankPaymentProvider(sepaInitiatedPaymentExecutionContextAdapter,
                sepaSubmitPaymentExecutionContextAdapter,
                sepaStatusPaymentExecutionContextAdapter,
                "RABOBANK",
                "Rabobank",
                ProviderVersion.VERSION_1);
    }

    @Test
    void shouldReturnLoginUrlAndStateDtoWithLoginUrlAndProviderStateAndPecMetadata() {
        //given
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder().build();
        when(sepaInitiatedPaymentExecutionContextAdapter.initiatePayment(initiatePaymentRequest))
                .thenReturn(new LoginUrlAndStateDTO("loginUrl", "providerState", new PaymentExecutionContextMetadata(
                        Instant.now(), Instant.now(), "", "", "[]", "[]",
                        new PaymentStatuses(RawBankPaymentStatus.unknown(), EnhancedPaymentStatus.INITIATION_SUCCESS)
                )));

        //when
        LoginUrlAndStateDTO result = subject.initiatePayment(initiatePaymentRequest);

        //then
        assertThat(result.getLoginUrl()).isEqualTo("loginUrl");
        assertThat(result.getProviderState()).isEqualTo("providerState");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                        .contains("UNKNOWN", "", EnhancedPaymentStatus.INITIATION_SUCCESS));
    }

    @Test
    void shouldSubmitPaymentWithPaymentIdAndPecMetadata() {
        // given
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder().build();
        when(sepaSubmitPaymentExecutionContextAdapter.submitPayment(submitPaymentRequest))
                .thenReturn(new SepaPaymentStatusResponseDTO("paymentId", new PaymentExecutionContextMetadata(
                        Instant.now(), Instant.now(), "", "", "[]", "[]",
                        new PaymentStatuses(
                                RawBankPaymentStatus.unknown(),
                                EnhancedPaymentStatus.ACCEPTED)
                )));

        // when
        SepaPaymentStatusResponseDTO result = subject.submitPayment(submitPaymentRequest);

        // then
        assertThat(result.getPaymentId()).isEqualTo("paymentId");
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                        .contains("UNKNOWN", "", EnhancedPaymentStatus.ACCEPTED));
    }

    @Test
    void shouldReturnSepaPaymentStatusDtoWithPaymentStatusAndPecMetadata() {
        //given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder().setPaymentId("paymentId").build();
        when(sepaStatusPaymentExecutionContextAdapter.getPaymentStatus(getStatusRequest))
                .thenReturn(new SepaPaymentStatusResponseDTO("paymentId", new PaymentExecutionContextMetadata(
                        Instant.now(), Instant.now(), "", "", "[]", "[]", new PaymentStatuses(
                        RawBankPaymentStatus.unknown(),
                        EnhancedPaymentStatus.COMPLETED)
                )));

        //when
        SepaPaymentStatusResponseDTO responseDto = subject.getStatus(getStatusRequest);

        //then
        assertThat(responseDto.getPaymentId()).isEqualTo("paymentId");
        assertThat(responseDto.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                        .contains("UNKNOWN", "", EnhancedPaymentStatus.COMPLETED));
    }

    @Test
    void shouldReturnedTypedAuthenticationMeans() {
        //when
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = subject.getTypedAuthenticationMeans();

        //then
        assertThat(typedAuthenticationMeans).containsExactlyInAnyOrderEntriesOf(Map.of(
                CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING,
                CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING,
                CLIENT_SIGNING_KEY_ID, TypedAuthenticationMeans.KEY_ID,
                CLIENT_SIGNING_CERTIFICATE, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM,
                CLIENT_TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID,
                CLIENT_TRANSPORT_CERTIFICATE, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM
        ));
    }

    @Test
    void returnProviderVersion() {
        //when
        ProviderVersion version = subject.getVersion();

        //then
        assertThat(version).isEqualTo(ProviderVersion.VERSION_1);
    }
}
