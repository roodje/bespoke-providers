package com.yolt.providers.bunq.common.pis.pec.submitandstatus;

import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentStatusResponse;
import com.yolt.providers.bunq.common.model.PaymentStatusValue;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSubmitAndStatusPaymentStatusesExtractorTest {

    private final DefaultSubmitAndStatusPaymentStatusesExtractor statusesExtractor = new DefaultSubmitAndStatusPaymentStatusesExtractor();

    private static Stream<Arguments> provideEnhancedPaymentStatusesMapping() {
        return Stream.of(
                Arguments.of(PaymentStatusValue.REJECTED, "REJECTED", EnhancedPaymentStatus.REJECTED),
                Arguments.of(PaymentStatusValue.EXPIRED, "EXPIRED", EnhancedPaymentStatus.NO_CONSENT_FROM_USER),
                Arguments.of(PaymentStatusValue.PENDING, "PENDING", EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(PaymentStatusValue.ACCEPTED, "ACCEPTED", EnhancedPaymentStatus.COMPLETED),
                Arguments.of(PaymentStatusValue.COMPLETED, "COMPLETED", EnhancedPaymentStatus.COMPLETED),
                Arguments.of(PaymentStatusValue.CANCELLED, "CANCELLED", EnhancedPaymentStatus.REJECTED)
        );
    }

    @ParameterizedTest
    @MethodSource("provideEnhancedPaymentStatusesMapping")
    void shouldReturnCorrectlyMappedPaymentStatusesForKnownStatusValue(PaymentStatusValue paymentStatusValue,
                                                                       String rawPaymentStatus,
                                                                       EnhancedPaymentStatus enhancedPaymentStatus) {
        //given
        PaymentServiceProviderDraftPaymentStatusResponse paymentStatusResponse =
                new PaymentServiceProviderDraftPaymentStatusResponse(paymentStatusValue);

        //when
        var result = statusesExtractor.extractPaymentStatuses(paymentStatusResponse, null);

        //then
        assertThat(result).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo(rawPaymentStatus);
                    assertThat(statuses.getPaymentStatus()).isEqualTo(enhancedPaymentStatus);
                }
        );
    }
}