package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.YoltBankSepaPaymentStatusesMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class YoltBankSepaSubmitPaymentStatusesExtractorTest {

    @InjectMocks
    private YoltBankSepaSubmitPaymentStatusesExtractor paymentStatusesExtractor;

    @Mock
    private YoltBankSepaPaymentStatusesMapper sepaPaymentStatusesMapper;

    @Test
    void shouldReturnPaymentStatusesProperlyMapped() {
        // given
        SepaPaymentStatusResponse responseBody = new SepaPaymentStatusResponse("123",SepaPaymentStatus.INITIATED);
        given(sepaPaymentStatusesMapper.mapToInternalPaymentStatus(SepaPaymentStatus.INITIATED))
                .willReturn(EnhancedPaymentStatus.INITIATION_SUCCESS);

        // when
        PaymentStatuses result = paymentStatusesExtractor.extractPaymentStatuses(responseBody, null);

        // then
        assertThat(result.getRawBankPaymentStatus()).satisfies(rawBankPaymentStatus -> {
            assertThat(rawBankPaymentStatus.getStatus()).isEqualTo(SepaPaymentStatus.INITIATED.name());
            assertThat(rawBankPaymentStatus.getReason()).isEqualTo("");
        });
        assertThat(result.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
    }
}
