package com.yolt.providers.ing.common.pec;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.ing.common.dto.SepaTransactionStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultCommonPaymentStatusMapperTest {


    private DefaultCommonPaymentStatusMapper sut;

    @ParameterizedTest
    @MethodSource("statuses")
    void shouldReturnCorrectlyMappedPaymentStatusesForKnownStatusValue(final String status, final EnhancedPaymentStatus enhancedPaymentStatus) {
        // given
        sut = new DefaultCommonPaymentStatusMapper();

        // when
        PaymentStatuses result = sut.mapTransactionStatus(status);

        // then
        assertThat(result).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                .contains(status, "", enhancedPaymentStatus);
    }


    private static List<Arguments> statuses() {
        return List.of(
                Arguments.of(SepaTransactionStatus.PDNG.name(), EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(SepaTransactionStatus.RCVD.name(), EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(SepaTransactionStatus.ACTC.name(), EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(SepaTransactionStatus.PATC.name(), EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(SepaTransactionStatus.ACCC.name(), EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(SepaTransactionStatus.ACCP.name(), EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(SepaTransactionStatus.ACFC.name(), EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(SepaTransactionStatus.ACSP.name(), EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(SepaTransactionStatus.ACWC.name(), EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(SepaTransactionStatus.ACWP.name(), EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(SepaTransactionStatus.PART.name(), EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(SepaTransactionStatus.ACSC.name(), EnhancedPaymentStatus.COMPLETED),
                Arguments.of(SepaTransactionStatus.CANC.name(), EnhancedPaymentStatus.NO_CONSENT_FROM_USER),
                Arguments.of(SepaTransactionStatus.RJCT.name(), EnhancedPaymentStatus.REJECTED)
        );
    }
}