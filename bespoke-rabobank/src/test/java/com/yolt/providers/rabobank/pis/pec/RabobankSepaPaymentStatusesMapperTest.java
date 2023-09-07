package com.yolt.providers.rabobank.pis.pec;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.rabobank.dto.external.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RabobankSepaPaymentStatusesMapperTest {

    private RabobankSepaPaymentStatusesMapper subject;

    @BeforeEach
    void setUp() {
        subject = new RabobankSepaPaymentStatusesMapper();
    }

    @ParameterizedTest()
    @MethodSource("getExpectedEnhancedStatusesMapping")
    void shouldReturnEnhancedPaymentStatus(TransactionStatus bankStatus, EnhancedPaymentStatus internalStatus) {
        //when
        EnhancedPaymentStatus mappedStatus = subject.mapToInternalPaymentStatus(bankStatus);

        //then
        assertThat(mappedStatus).isEqualTo(internalStatus);
    }

    private static Stream<Arguments> getExpectedEnhancedStatusesMapping() {
        return Stream.of(
                Arguments.of(TransactionStatus.RCVD, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatus.ACTC, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatus.PDNG, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(TransactionStatus.ACSP, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(TransactionStatus.ACSC, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(TransactionStatus.ACCC, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(TransactionStatus.CANC, EnhancedPaymentStatus.NO_CONSENT_FROM_USER),
                Arguments.of(TransactionStatus.RJCT, EnhancedPaymentStatus.REJECTED),
                Arguments.of(TransactionStatus.ACWP, EnhancedPaymentStatus.UNKNOWN)
        );
    }
}
