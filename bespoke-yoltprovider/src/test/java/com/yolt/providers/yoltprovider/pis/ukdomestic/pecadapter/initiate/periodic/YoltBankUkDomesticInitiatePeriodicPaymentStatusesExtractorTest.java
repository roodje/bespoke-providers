package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class YoltBankUkDomesticInitiatePeriodicPaymentStatusesExtractorTest {

    @InjectMocks
    private YoltBankUkDomesticInitiatePeriodicPaymentStatusesExtractor paymentStatusesExtractor;

    @Test
    void shouldReturnProperPaymentStatuses() {
        // given
        InitiatePaymentConsentResponse responseBody = new InitiatePaymentConsentResponse(
                "",
                ""
        );

        // when
        PaymentStatuses result = paymentStatusesExtractor.extractPaymentStatuses(responseBody, null);

        // then
        assertThat(result.getRawBankPaymentStatus()).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains("UNKNOWN", "");
        assertThat(result.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
    }
}
