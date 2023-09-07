package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericScheduledConsentResponseStatusMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsentResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsentResponse5Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GenericInitiateScheduledPaymentStatusesExtractorTest {

    @InjectMocks
    private GenericInitiateScheduledPaymentStatusesExtractor subject;

    @Mock
    private GenericScheduledConsentResponseStatusMapper responseStatusMapper;

    @Mock
    private GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult;

    @Test
    void shouldReturnPaymentStatusesWhenCorrectDataAreProvided() {
        // given
        OBWriteDomesticScheduledConsentResponse5 obWriteDomesticScheduledConsentResponse5 = new OBWriteDomesticScheduledConsentResponse5()
                .data(new OBWriteDomesticScheduledConsentResponse5Data()
                        .status(OBWriteDomesticScheduledConsentResponse5Data.StatusEnum.AUTHORISED));
        PaymentStatuses expectedStatuses = new PaymentStatuses(
                RawBankPaymentStatus.forStatus("Authorised", ""),
                EnhancedPaymentStatus.ACCEPTED);

        given(responseStatusMapper.mapToEnhancedPaymentStatus(OBWriteDomesticScheduledConsentResponse5Data.StatusEnum.AUTHORISED))
                .willReturn(EnhancedPaymentStatus.ACCEPTED);

        // when
        PaymentStatuses result = subject.extractPaymentStatuses(obWriteDomesticScheduledConsentResponse5, preExecutionResult);

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedStatuses);
    }
}