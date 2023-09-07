package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.yoltprovider.pis.ukdomestic.UkDomesticPaymentStatusMapper;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBTransactionIndividualStatus1Code;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.model.PaymentSubmitResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class YotlBankUkDomesticSubmitPaymentStatusesExtractorTest {

    @InjectMocks
    private YotlBankUkDomesticSubmitPaymentStatusesExtractor statusesExtractor;

    @Mock
    private UkDomesticPaymentStatusMapper ukDomesticPaymentStatusMapper;

    @Test
    void shouldReturnPaymentStatusesProperlyMapped() throws Throwable {
        // given
        PaymentSubmitResponse responseBody = new PaymentSubmitResponse();
        PaymentSubmitResponse.Data data = new PaymentSubmitResponse.Data(null, null, null, OBTransactionIndividualStatus1Code.PENDING.toString());
        responseBody.setData(data);
        given(ukDomesticPaymentStatusMapper.mapToInternalPaymentStatus(OBTransactionIndividualStatus1Code.PENDING.toString()))
                .willReturn(EnhancedPaymentStatus.ACCEPTED);

        // when
        PaymentStatuses result = statusesExtractor.extractPaymentStatuses(responseBody, null);

        // then
        assertThat(result.getRawBankPaymentStatus()).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains("Pending", "");
        assertThat(result.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
    }
}
