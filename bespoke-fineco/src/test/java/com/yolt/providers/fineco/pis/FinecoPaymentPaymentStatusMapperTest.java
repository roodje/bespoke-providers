package com.yolt.providers.fineco.pis;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.fineco.dto.PaymentStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus.*;
import static com.yolt.providers.fineco.dto.PaymentStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

public class FinecoPaymentPaymentStatusMapperTest {

    private FinecoPaymentStatusMapper mapper = new FinecoPaymentStatusMapper();

    @ParameterizedTest
    @MethodSource("getStatuses")
    public void mapToStatusDtoWhenACCPStatusReceivedTest(PaymentStatus testedStatus, EnhancedPaymentStatus expectedStatus) {
        //given
        //when
        var result = mapper.mapPaymentStatus(testedStatus.name());

        //then
        assertThat(result.getPaymentStatus()).isEqualTo(expectedStatus);
    }

    private static Stream<Arguments> getStatuses() {
        return Stream.of(
                Arguments.of(ACCC, ACCEPTED),
                Arguments.of(ACCP, ACCEPTED),
                Arguments.of(ACSP, ACCEPTED),
                Arguments.of(ACTC, ACCEPTED),
                Arguments.of(ACWC, ACCEPTED),
                Arguments.of(ACWP, ACCEPTED),
                Arguments.of(RCVD, INITIATION_SUCCESS),
                Arguments.of(PDNG, INITIATION_SUCCESS),
                Arguments.of(RJCT, REJECTED),
                Arguments.of(ACSC, COMPLETED),
                Arguments.of(CANC, NO_CONSENT_FROM_USER),
                Arguments.of(ACFC, ACCEPTED),
                Arguments.of(PATC, ACCEPTED)
        );
    }
}
