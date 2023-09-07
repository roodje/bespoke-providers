package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.YoltBankSepaPaymentStatusesMapper;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.SepaInitiatePaymentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class YoltBankSepaInitiatePaymentStatusesExtractorTest {

    @InjectMocks
    private YoltBankSepaInitiatePaymentStatusesExtractor paymentStatusesExtractor;

    @Mock
    private YoltBankSepaPaymentStatusesMapper sepaPaymentStatusesMapper;

    @Test
    void shouldReturnPaymentStatusesWithProperlyMappedFields() {
        // given
        SepaInitiatePaymentResponse responseBody = new SepaInitiatePaymentResponse("sca", "123", SepaPaymentStatus.INITIATED);
        EnhancedPaymentStatus expectedInternalStatus = EnhancedPaymentStatus.INITIATION_SUCCESS;
        given(sepaPaymentStatusesMapper.mapToInternalPaymentStatus(SepaPaymentStatus.INITIATED))
                .willReturn(expectedInternalStatus);

        // when
        PaymentStatuses result = paymentStatusesExtractor.extractPaymentStatuses(responseBody, null);

        // then
        assertThat(result.getRawBankPaymentStatus()).satisfies(rawBankPaymentStatus ->
                assertThat(rawBankPaymentStatus).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                        .contains(SepaPaymentStatus.INITIATED.name(), ""));
        assertThat(result.getPaymentStatus()).isEqualTo(expectedInternalStatus);
    }
}
