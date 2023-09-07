package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentResponse;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VolksbankInitiatePaymentStatusesExtractorV2Test {

    @InjectMocks
    private VolksbankInitiatePaymentStatusesExtractorV2 subject;

    private static Stream<Arguments> providePaymentStatuses() {
        return Stream.of(
                Arguments.of(InitiatePaymentResponse.TransactionStatusEnum.RCVD, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(InitiatePaymentResponse.TransactionStatusEnum.PDNG, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(InitiatePaymentResponse.TransactionStatusEnum.ACSC, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(InitiatePaymentResponse.TransactionStatusEnum.ACCC, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(InitiatePaymentResponse.TransactionStatusEnum.CANC, EnhancedPaymentStatus.NO_CONSENT_FROM_USER),
                Arguments.of(InitiatePaymentResponse.TransactionStatusEnum.REJECTED, EnhancedPaymentStatus.REJECTED),
                Arguments.of(InitiatePaymentResponse.TransactionStatusEnum.UNKNOWN, EnhancedPaymentStatus.UNKNOWN)
        );
    }

    @ParameterizedTest
    @MethodSource("providePaymentStatuses")
    void shouldReturnProperPaymentStatusesWhenCorrectDataAreProvided(InitiatePaymentResponse.TransactionStatusEnum bankStatus, EnhancedPaymentStatus paymentStatus) {
        // given
        InitiatePaymentResponse initiatePaymentResponse = new InitiatePaymentResponse();
        initiatePaymentResponse.setTransactionStatus(bankStatus);

        // when
        PaymentStatuses result = subject.extractPaymentStatuses(initiatePaymentResponse, null);

        // then
        assertThat(result.getRawBankPaymentStatus()).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains(bankStatus.toString(), "");
        assertThat(result.getPaymentStatus()).isEqualTo(paymentStatus);
    }
}