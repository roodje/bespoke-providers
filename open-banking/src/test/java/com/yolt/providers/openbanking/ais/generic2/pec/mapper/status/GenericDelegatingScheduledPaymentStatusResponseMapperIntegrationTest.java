package com.yolt.providers.openbanking.ais.generic2.pec.mapper.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.ScheduledPaymentStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GenericDelegatingScheduledPaymentStatusResponseMapperIntegrationTest {

    private GenericDelegatingScheduledPaymentStatusResponseMapper subject;

    @BeforeEach
    void beforeEach() {
        subject = new GenericDelegatingScheduledPaymentStatusResponseMapper(new GenericScheduledConsentResponseStatusMapper(),
                new GenericScheduledResponseStatusMapper());
    }

    private static Stream<Arguments> provideEnhancedPaymentStatusesMapping() {
        return Stream.of(
                Arguments.of(ScheduledPaymentStatusResponse.Data.Status.AWAITINGAUTHORISATION, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(ScheduledPaymentStatusResponse.Data.Status.AUTHORISED, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(ScheduledPaymentStatusResponse.Data.Status.CONSUMED, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(ScheduledPaymentStatusResponse.Data.Status.REJECTED, EnhancedPaymentStatus.REJECTED),
                Arguments.of(ScheduledPaymentStatusResponse.Data.Status.CANCELLED, EnhancedPaymentStatus.NO_CONSENT_FROM_USER),
                Arguments.of(ScheduledPaymentStatusResponse.Data.Status.INITIATIONCOMPLETED, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(ScheduledPaymentStatusResponse.Data.Status.INITIATIONFAILED, EnhancedPaymentStatus.EXECUTION_FAILED),
                Arguments.of(ScheduledPaymentStatusResponse.Data.Status.INITIATIONPENDING, EnhancedPaymentStatus.ACCEPTED)
        );
    }

    @ParameterizedTest
    @MethodSource("provideEnhancedPaymentStatusesMapping")
    void shouldReturnProperEnhancedPaymentStatusWhenSpecificBankStatusIsProvided(ScheduledPaymentStatusResponse.Data.Status bankStatus,
                                                                                 EnhancedPaymentStatus paymentStatus) {
        // when
        EnhancedPaymentStatus result = subject.mapToEnhancedPaymentStatus(bankStatus);

        // then
        assertThat(result).isEqualTo(paymentStatus);
    }
}