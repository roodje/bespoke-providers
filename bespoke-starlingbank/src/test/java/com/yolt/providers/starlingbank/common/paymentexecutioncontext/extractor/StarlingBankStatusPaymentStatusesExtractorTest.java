package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.starlingbank.common.model.PaymentDetailsResponse;
import com.yolt.providers.starlingbank.common.model.PaymentStatusDetails;
import com.yolt.providers.starlingbank.common.model.PaymentStatusResponse;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.mapper.StarlingBankCommonPaymentStatusMapper;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
public class StarlingBankStatusPaymentStatusesExtractorTest {

    private StarlingBankCommonPaymentStatusMapper starlingBankCommonPaymentStatusMapper = new StarlingBankCommonPaymentStatusMapper();
    private StarlingBankStatusPaymentStatusesExtractor statusPaymentStatusesExtractor = new StarlingBankStatusPaymentStatusesExtractor(starlingBankCommonPaymentStatusMapper);


    @ParameterizedTest
    @MethodSource("getInvalidStatuses")
    void shouldThrowExceptionWhenPaymentResponseIsInvalid(final PaymentDetailsResponse paymentDetailsResponse) {
        //given
        PaymentStatusResponse paymentStatusResponse = new PaymentStatusResponse();
        paymentStatusResponse.setPayments(List.of(paymentDetailsResponse));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> statusPaymentStatusesExtractor.extractPaymentStatuses(paymentStatusResponse, null);

        // then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(callable)
                .withMessage("Unexpected value: " + paymentDetailsResponse.getPaymentStatusDetails().getPaymentStatus().toLowerCase());
    }

    @ParameterizedTest
    @MethodSource("getValidStatuses")
    void shouldExtractPaymentStatusesWhenResponseIsValid(final PaymentDetailsResponse paymentDetailsResponse,
                                                         final EnhancedPaymentStatus mappedEnhancedPaymentStatus) {
        //given
        PaymentStatusResponse paymentStatusResponse = new PaymentStatusResponse();
        paymentStatusResponse.setPayments(List.of(paymentDetailsResponse));

        //when
        PaymentStatuses result = statusPaymentStatusesExtractor.extractPaymentStatuses(paymentStatusResponse, null);

        //then
        assertThat(result).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo(paymentDetailsResponse.getPaymentStatusDetails().getPaymentStatus());
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo(paymentDetailsResponse.getPaymentStatusDetails().getDescription());
                    assertThat(statuses.getPaymentStatus()).isEqualTo(mappedEnhancedPaymentStatus);
                }
        );
    }

    private static List<Arguments> getValidStatuses() {
        return List.of(
                Arguments.of(preparePaymentDetailsResponse("PENDING", "Pending because we have holiday!" ), EnhancedPaymentStatus.ACCEPTED),
                Arguments.of(preparePaymentDetailsResponse("ACCEPTED", "QUALIFIED_ACCEPT_AFTER_NEXT_WORKING_DAY" ), EnhancedPaymentStatus.COMPLETED),
                Arguments.of(preparePaymentDetailsResponse("REJECTED", "DESTINATION_ACCOUNT_NAME_MISMATCH" ), EnhancedPaymentStatus.REJECTED),
                Arguments.of(preparePaymentDetailsResponse("REJECTED", "" ), EnhancedPaymentStatus.REJECTED)
        );
    }

    private static List<Arguments> getInvalidStatuses() {
        return List.of(
                Arguments.of(preparePaymentDetailsResponse("", "")),
                Arguments.of(preparePaymentDetailsResponse("PEEEEENDING", ""))
        );
    }

    private static PaymentDetailsResponse preparePaymentDetailsResponse(String paymentStatus, String reason) {
        PaymentStatusDetails paymentStatusDetails = new PaymentStatusDetails();
        paymentStatusDetails.setPaymentStatus(paymentStatus);
        paymentStatusDetails.setDescription(reason);
        PaymentDetailsResponse payment = new PaymentDetailsResponse();
        payment.setPaymentStatusDetails(paymentStatusDetails);
        return payment;
    }
}
