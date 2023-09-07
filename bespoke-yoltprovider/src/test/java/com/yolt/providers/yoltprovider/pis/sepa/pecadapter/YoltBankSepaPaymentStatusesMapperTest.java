package com.yolt.providers.yoltprovider.pis.sepa.pecadapter;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class YoltBankSepaPaymentStatusesMapperTest {

    private YoltBankSepaPaymentStatusesMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new YoltBankSepaPaymentStatusesMapper();
    }

    @ParameterizedTest
    @MethodSource("mapToInternalPaymentStatusTestData")
    public void shouldProperlyMapStatusWhenMappingInternalPaymentStatus(SepaPaymentStatus mappedFrom, EnhancedPaymentStatus expected) {
        //given
        //when
        EnhancedPaymentStatus status = mapper.mapToInternalPaymentStatus(mappedFrom);
        //then
        assertThat(status).isEqualTo(expected);
    }

    private static Stream<Arguments> mapToInternalPaymentStatusTestData() {
        return Stream.of(
                Arguments.of(SepaPaymentStatus.INITIATED, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(SepaPaymentStatus.ACCEPTED, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(SepaPaymentStatus.COMPLETED, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(SepaPaymentStatus.REJECTED, EnhancedPaymentStatus.REJECTED)
        );
    }
}
