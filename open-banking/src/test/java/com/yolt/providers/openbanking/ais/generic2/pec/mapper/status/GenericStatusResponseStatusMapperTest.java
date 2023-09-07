package com.yolt.providers.openbanking.ais.generic2.pec.mapper.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5Data;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GenericStatusResponseStatusMapperTest {

    @InjectMocks
    private GenericStatusResponseStatusMapper subject;

    private static Stream<Arguments> provideEnhancedPaymentStatusesMapping() {
        return Stream.of(
                Arguments.of(OBWriteDomesticResponse5Data.StatusEnum.PENDING, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(OBWriteDomesticResponse5Data.StatusEnum.ACCEPTEDSETTLEMENTINPROCESS, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(OBWriteDomesticResponse5Data.StatusEnum.ACCEPTEDSETTLEMENTCOMPLETED, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(OBWriteDomesticResponse5Data.StatusEnum.ACCEPTEDWITHOUTPOSTING, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(OBWriteDomesticResponse5Data.StatusEnum.ACCEPTEDCREDITSETTLEMENTCOMPLETED, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(OBWriteDomesticResponse5Data.StatusEnum.REJECTED, EnhancedPaymentStatus.REJECTED)
        );
    }

    @ParameterizedTest
    @MethodSource("provideEnhancedPaymentStatusesMapping")
    void shouldReturnProperEnhancedPaymentStatusToEnhancedPaymentStatusWhenSpecificBankStatusIsProvided(OBWriteDomesticResponse5Data.StatusEnum bankStatus,
                                                                                                        EnhancedPaymentStatus paymentStatus) {
        // when
        EnhancedPaymentStatus result = subject.mapToEnhancedPaymentStatus(bankStatus);

        // then
        assertThat(result).isEqualTo(paymentStatus);
    }
}