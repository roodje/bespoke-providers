package com.yolt.providers.openbanking.ais.generic2.pec.mapper.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5Data;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GenericConsentResponseStatusMapperTest {

    @InjectMocks
    private GenericConsentResponseStatusMapper subject;

    private static Stream<Arguments> provideEnhancedPaymentStatusesMapping() {
        return Stream.of(
                Arguments.of(OBWriteDomesticConsentResponse5Data.StatusEnum.AWAITINGAUTHORISATION, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(OBWriteDomesticConsentResponse5Data.StatusEnum.AUTHORISED, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(OBWriteDomesticConsentResponse5Data.StatusEnum.CONSUMED, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(OBWriteDomesticConsentResponse5Data.StatusEnum.REJECTED, EnhancedPaymentStatus.REJECTED)
        );
    }

    @ParameterizedTest
    @MethodSource("provideEnhancedPaymentStatusesMapping")
    void shouldReturnProperEnhancedPaymentStatusToEnhancedPaymentStatusWhenSpecificBankStatusIsProvided(OBWriteDomesticConsentResponse5Data.StatusEnum bankStatus,
                                                                                                        EnhancedPaymentStatus paymentStatus) {
        // when
        EnhancedPaymentStatus result = subject.mapToEnhancedPaymentStatus(bankStatus);

        // then
        assertThat(result).isEqualTo(paymentStatus);
    }
}