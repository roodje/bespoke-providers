package com.yolt.providers.cbiglobe.common.pis.pec.submit;

import com.yolt.providers.cbiglobe.common.model.TransactionStatus;
import com.yolt.providers.cbiglobe.pis.dto.GetPaymentStatusRequestResponseType;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class CbiGlobeSubmitPaymentStatusesExtractorTest {

    @InjectMocks
    private CbiGlobeSubmitPaymentStatusesExtractor subject;

    private static Stream<Arguments> provideStatusesMapping() {
        return Stream.of(
                Arguments.of(TransactionStatus.RCVD, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatus.PDNG, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatus.ACTC, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatus.ACCP, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(TransactionStatus.ACSP, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(TransactionStatus.ACWC, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(TransactionStatus.ACSC, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(TransactionStatus.ACWP, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(TransactionStatus.RJCT, EnhancedPaymentStatus.REJECTED),
                Arguments.of(TransactionStatus.DAS_SR, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatus.DAS_CR, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatus.DAS_I, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatus.DAS_FAILED, EnhancedPaymentStatus.REJECTED)
        );
    }

    @ParameterizedTest
    @MethodSource("provideStatusesMapping")
    void shouldForWhen(TransactionStatus bankStatus, EnhancedPaymentStatus paymentStatus) {
        // given
        var paymentStatusResponse = new GetPaymentStatusRequestResponseType();
        paymentStatusResponse.setTransactionStatus(bankStatus.toString());

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