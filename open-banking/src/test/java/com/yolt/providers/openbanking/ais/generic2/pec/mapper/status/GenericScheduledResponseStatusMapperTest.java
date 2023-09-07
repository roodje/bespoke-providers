package com.yolt.providers.openbanking.ais.generic2.pec.mapper.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5Data;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GenericScheduledResponseStatusMapperTest {

    @InjectMocks
    GenericScheduledResponseStatusMapper responseStatusMapper;

    private static Stream<Arguments> provideEnhancedPaymentStatusesMapping() {
        return Stream.of(
                Arguments.of(OBWriteDomesticScheduledResponse5Data.StatusEnum.INITIATIONPENDING, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(OBWriteDomesticScheduledResponse5Data.StatusEnum.INITIATIONCOMPLETED, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(OBWriteDomesticScheduledResponse5Data.StatusEnum.INITIATIONFAILED, EnhancedPaymentStatus.EXECUTION_FAILED),
                Arguments.of(OBWriteDomesticScheduledResponse5Data.StatusEnum.CANCELLED, EnhancedPaymentStatus.NO_CONSENT_FROM_USER)

        );
    }

    @ParameterizedTest
    @MethodSource("provideEnhancedPaymentStatusesMapping")
    void shouldReturnProperEnhancedPaymentStatusWhenSpecificBankStatusIsProvided(OBWriteDomesticScheduledResponse5Data.StatusEnum source, EnhancedPaymentStatus expectedResult) {
        //when
        EnhancedPaymentStatus result = responseStatusMapper.mapToEnhancedPaymentStatus(source);

        //then
        assertThat(result).isEqualTo(expectedResult);
    }

}