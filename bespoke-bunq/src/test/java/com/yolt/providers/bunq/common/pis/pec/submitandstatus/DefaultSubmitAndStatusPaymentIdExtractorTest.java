package com.yolt.providers.bunq.common.pis.pec.submitandstatus;

import com.yolt.providers.bunq.AuthMeans;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentStatusResponse;
import com.yolt.providers.bunq.common.model.PaymentStatusValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DefaultSubmitAndStatusPaymentIdExtractorTest {

    private DefaultSubmitAndStatusPaymentIdExtractor paymentIdExtractor;

    @BeforeEach
    void setUp() {
        paymentIdExtractor = new DefaultSubmitAndStatusPaymentIdExtractor();
    }

    @Test
    void shouldReturnExtractedPaymentIdWhenCorrectDataAreProvided() {
        //given
        PaymentServiceProviderDraftPaymentStatusResponse paymentStatusResponse = new PaymentServiceProviderDraftPaymentStatusResponse(PaymentStatusValue.ACCEPTED);
        var authMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(AuthMeans.prepareAuthMeansV2(), "BUNQ");
        DefaultSubmitAndStatusPaymentPreExecutionResult preExecutionResult = new DefaultSubmitAndStatusPaymentPreExecutionResult(
                null, 123, authMeans.getPsd2UserId(), null, 1L, null);

        //when
        String result = paymentIdExtractor.extractPaymentId(paymentStatusResponse, preExecutionResult);

        //then
        assertThat(result).isEqualTo("123");
    }
}