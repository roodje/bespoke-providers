package com.yolt.providers.knabgroup.common.payment;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultPaymentStatusMapperTest {


    private DefaultPaymentStatusMapper subject;

    @ParameterizedTest
    @MethodSource("statuses")
    void shouldReturnCorrectlyMappedPaymentStatusesForKnownStatusValue(final String status, final EnhancedPaymentStatus enhancedPaymentStatus) {
        // given
        subject = new DefaultPaymentStatusMapper();
        PaymentStatuses expectedResult = new PaymentStatuses(RawBankPaymentStatus.forStatus(status, ""), enhancedPaymentStatus);

        // when
        PaymentStatuses result = subject.mapTransactionStatus(status);

        // then
        assertThat(result).usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }


    private static List<Arguments> statuses() {
        return List.of(
                Arguments.of(SepaTransactionStatus.RCVD.name(), EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(SepaTransactionStatus.ACTC.name(), EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(SepaTransactionStatus.ACCP.name(), EnhancedPaymentStatus.COMPLETED),
                Arguments.of(SepaTransactionStatus.CANC.name(), EnhancedPaymentStatus.NO_CONSENT_FROM_USER),
                Arguments.of(SepaTransactionStatus.RJCT.name(), EnhancedPaymentStatus.REJECTED)
        );
    }
}