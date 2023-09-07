package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.yolt.providers.abnamro.pis.TransactionStatusResponse;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AbnAmroPaymentStatusMapperTest {

    @InjectMocks
    private AbnAmroPaymentStatusMapper subject;

    private static Stream<Arguments> providePaymentStatuses() {
        return Stream.of(
                Arguments.of(TransactionStatusResponse.StatusEnum.AUTHORIZED, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatusResponse.StatusEnum.STORED, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatusResponse.StatusEnum.INPROGRESS, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(TransactionStatusResponse.StatusEnum.SCHEDULED, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(TransactionStatusResponse.StatusEnum.FUTURE, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(TransactionStatusResponse.StatusEnum.EXECUTED, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(TransactionStatusResponse.StatusEnum.REJECTED, EnhancedPaymentStatus.REJECTED),
                Arguments.of(TransactionStatusResponse.StatusEnum.UNKNOWN, EnhancedPaymentStatus.UNKNOWN)
        );
    }

    @ParameterizedTest
    @MethodSource("providePaymentStatuses")
    void shouldForWhen(TransactionStatusResponse.StatusEnum bankStatus, EnhancedPaymentStatus paymentStatus) {
        // when
        PaymentStatuses result = subject.mapBankPaymentStatus(bankStatus);

        // then
        assertThat(result.getRawBankPaymentStatus()).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains(bankStatus.toString(), "");
        assertThat(result.getPaymentStatus()).isEqualTo(paymentStatus);
    }
}