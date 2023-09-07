package com.yolt.providers.openbanking.ais.generic2.pec.mapper.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GenericDelegatingPaymentStatusResponseMapperIntegrationTest {

    private GenericDelegatingPaymentStatusResponseMapper subject;

    @BeforeEach
    void beforeEach() {
        subject = new GenericDelegatingPaymentStatusResponseMapper(new GenericConsentResponseStatusMapper(),
                new GenericStatusResponseStatusMapper());
    }

    private static Stream<Arguments> provideEnhancedPaymentStatusesMapping() {
        return Stream.of(
                Arguments.of(PaymentStatusResponse.Data.Status.AWAITINGAUTHORISATION, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(PaymentStatusResponse.Data.Status.AUTHORISED, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(PaymentStatusResponse.Data.Status.CONSUMED, EnhancedPaymentStatus.INITIATION_SUCCESS),
                Arguments.of(PaymentStatusResponse.Data.Status.REJECTED, EnhancedPaymentStatus.REJECTED),
                Arguments.of(PaymentStatusResponse.Data.Status.PENDING, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(PaymentStatusResponse.Data.Status.ACCEPTEDSETTLEMENTINPROCESS, EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(PaymentStatusResponse.Data.Status.ACCEPTEDSETTLEMENTCOMPLETED, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(PaymentStatusResponse.Data.Status.ACCEPTEDWITHOUTPOSTING, EnhancedPaymentStatus.COMPLETED),
                Arguments.of(PaymentStatusResponse.Data.Status.ACCEPTEDCREDITSETTLEMENTCOMPLETED, EnhancedPaymentStatus.COMPLETED)
        );
    }

    @ParameterizedTest
    @MethodSource("provideEnhancedPaymentStatusesMapping")
    void shouldReturnProperEnhancedPaymentStatusToEnhancedPaymentStatusWhenSpecificBankStatusIsProvided(PaymentStatusResponse.Data.Status bankStatus,
                                                                                                        EnhancedPaymentStatus paymentStatus) {
        // when
        EnhancedPaymentStatus result = subject.mapToEnhancedPaymentStatus(bankStatus);

        // then
        assertThat(result).isEqualTo(paymentStatus);
    }
}