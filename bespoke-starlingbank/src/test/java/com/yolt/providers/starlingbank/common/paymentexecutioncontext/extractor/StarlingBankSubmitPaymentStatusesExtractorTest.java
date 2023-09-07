package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClient;
import com.yolt.providers.starlingbank.common.model.ConsentInformation;
import com.yolt.providers.starlingbank.common.model.PaymentSubmissionResponse;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class StarlingBankSubmitPaymentStatusesExtractorTest {

    private StarlingBankSubmitPaymentStatusesExtractor submitPaymentStatusesExtractor;

    @Mock
    private StarlingBankHttpClient httpClient;

    @Mock
    private Signer signer;

    @BeforeEach
    void beforeEach() {
        submitPaymentStatusesExtractor = new StarlingBankSubmitPaymentStatusesExtractor();
    }

    @Test
    void shouldCallStatusesEndpointAndExtractPaymentStatusesForExtractPaymentStatusesWithCorrectData() {
        //given
        UUID paymentOrderUid = UUID.randomUUID();
        PaymentSubmissionResponse paymentSubmissionResponse = preparePaymentSubmissionResponse(paymentOrderUid);
        StarlingBankAuthenticationMeans authenticationMeans = StarlingBankAuthenticationMeans.builder().build();
        StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult preExecutionResult = preparePreExecutionResult(authenticationMeans);

        //when
        PaymentStatuses paymentStatusesResponse = submitPaymentStatusesExtractor.extractPaymentStatuses(paymentSubmissionResponse, preExecutionResult);

        //then
        assertThat(paymentStatusesResponse).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("ACCEPTED" );
            assertThat(statuses.getRawBankPaymentStatus().getReason()).isEqualTo("" );
            assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
                }
        );
    }

    private PaymentSubmissionResponse preparePaymentSubmissionResponse(UUID paymentOrderUid) {
        return new PaymentSubmissionResponse(
                paymentOrderUid,
                new ConsentInformation()
        );
    }

    private StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult preparePreExecutionResult(StarlingBankAuthenticationMeans authenticationMeans) {
        return StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult.builder()
                .httpClient(httpClient)
                .authenticationMeans(authenticationMeans)
                .token("fakeAccessToken")
                .signer(signer)
                .build();
    }
}
