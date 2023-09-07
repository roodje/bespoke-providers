package com.yolt.providers.volksbank.common.pis.pec.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.volksbank.dto.v1_1.TransactionStatus;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VolksbankSubmitPaymentStatusesExtractorV2Test {

    @InjectMocks
    private VolksbankSubmitPaymentStatusesExtractorV2 subject;

    private static Stream<Arguments> provideStatusesMapping() {
        return Stream.of(
                Arguments.of(TransactionStatus.ACTC, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatus.PATC, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatus.RCVD, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatus.UNKNOWN, EnhancedPaymentStatus.UNKNOWN),
                Arguments.of(TransactionStatus.ACCP, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(TransactionStatus.ACSP, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(TransactionStatus.ACWC, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(TransactionStatus.PART, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(TransactionStatus.PDNG, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(TransactionStatus.ACSC, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(TransactionStatus.ACCC, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(TransactionStatus.CANC, EnhancedPaymentStatus.NO_CONSENT_FROM_USER),
                Arguments.of(TransactionStatus.RJCT, EnhancedPaymentStatus.REJECTED)
        );
    }

    @ParameterizedTest
    @MethodSource("provideStatusesMapping")
    void shouldForWhen(TransactionStatus bankStatus, EnhancedPaymentStatus paymentStatus) {
        // given
        var paymentStatusResponse = new com.yolt.providers.volksbank.dto.v1_1.PaymentStatus();
        paymentStatusResponse.setTransactionStatus(bankStatus);

        // when
        var result = subject.extractPaymentStatuses(paymentStatusResponse, null);

        // then
        assertThat(result).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                .contains(bankStatus.toString(),
                        "",
                        paymentStatus);
    }
}