package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.cbiglobe.common.model.TransactionStatus;
import com.yolt.providers.cbiglobe.pis.dto.PaymentInitiationRequestResponseType;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CbiGlobeInitiatePaymentStatusesExtractorTest {

    @InjectMocks
    private CbiGlobeInitiatePaymentStatusesExtractor subject;

    @Mock
    PaymentInitiationRequestResponseType initiatePaymentResponse;

    private static Stream<Arguments> providePaymentStatuses() {
        return Stream.of(
                Arguments.of("RCVD", EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of("PDNG", EnhancedPaymentStatus.ACCEPTED),
                Arguments.of("ACSC", EnhancedPaymentStatus.COMPLETED),
                Arguments.of("ACCP", EnhancedPaymentStatus.ACCEPTED),
                Arguments.of("ACSP", EnhancedPaymentStatus.ACCEPTED),
                Arguments.of("ACTC", EnhancedPaymentStatus.ACCEPTED),
                Arguments.of("ACWC", EnhancedPaymentStatus.ACCEPTED),
                Arguments.of("ACWP", EnhancedPaymentStatus.ACCEPTED),
                Arguments.of("RJCT", EnhancedPaymentStatus.REJECTED),
                Arguments.of("DAS_SR", EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of("DAS_CR", EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of("DAS_I", EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of("DAS_FAILED", EnhancedPaymentStatus.REJECTED)
        );
    }

    @ParameterizedTest
    @MethodSource("providePaymentStatuses")
    void shouldReturnProperPaymentStatusesWhenCorrectDataAreProvided(String bankStatus, EnhancedPaymentStatus paymentStatus) {
        // given
        when(initiatePaymentResponse.getTransactionStatus()).thenReturn(bankStatus);

        // when
        PaymentStatuses result = subject.extractPaymentStatuses(initiatePaymentResponse, null);

        // then
        assertThat(result.getRawBankPaymentStatus()).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .containsExactly(bankStatus, "");
        assertThat(result.getPaymentStatus()).isEqualTo(paymentStatus);
    }
}