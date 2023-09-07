package com.yolt.providers.rabobank.pis.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.rabobank.dto.external.InitiatedTransactionResponse;
import com.yolt.providers.rabobank.dto.external.TransactionStatus;
import com.yolt.providers.rabobank.pis.pec.RabobankSepaPaymentStatusesMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class RabobankSepaInitiatePaymentStatusesExtractorTest {

    private RabobankSepaInitiatePaymentStatusesExtractor subject;

    @BeforeEach
    void setUp() {
        subject = new RabobankSepaInitiatePaymentStatusesExtractor(new RabobankSepaPaymentStatusesMapper());
    }

    @ParameterizedTest
    @MethodSource("getExpectedStatusesMapping")
    void shouldReturnPaymentStatuses(TransactionStatus bankStatus,EnhancedPaymentStatus newStatus) {
        //given
        InitiatedTransactionResponse transactionResponse = new InitiatedTransactionResponse();
        transactionResponse.setTransactionStatus(bankStatus);

        //when
        PaymentStatuses mappedStatuses = subject.extractPaymentStatuses(transactionResponse, null);

        //then
        assertThat(mappedStatuses).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                statuses -> statuses.getRawBankPaymentStatus().getReason(),
                PaymentStatuses::getPaymentStatus)
                .contains(bankStatus.name(), "", newStatus);

    }


    private static Stream<Arguments> getExpectedStatusesMapping() {
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
