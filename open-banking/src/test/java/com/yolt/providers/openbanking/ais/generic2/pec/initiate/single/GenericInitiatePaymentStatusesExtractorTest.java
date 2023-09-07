package com.yolt.providers.openbanking.ais.generic2.pec.initiate.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.ResponseStatusMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericInitiatePaymentStatusesExtractorTest {

    @InjectMocks
    private GenericInitiatePaymentStatusesExtractor subject;

    @Mock
    private ResponseStatusMapper<OBWriteDomesticConsentResponse5Data.StatusEnum> responseStatusMapper;

    @Mock
    private GenericInitiatePaymentPreExecutionResult preExecutionResult;

    @Test
    void shouldReturnPaymentStatusesWhenCorrectDataAreProvided() {
        // given
        OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 = new OBWriteDomesticConsentResponse5()
                .data(new OBWriteDomesticConsentResponse5Data()
                        .status(OBWriteDomesticConsentResponse5Data.StatusEnum.AUTHORISED));

        given(responseStatusMapper.mapToEnhancedPaymentStatus(any(OBWriteDomesticConsentResponse5Data.StatusEnum.class)))
                .willReturn(EnhancedPaymentStatus.ACCEPTED);

        // when
        PaymentStatuses result = subject.extractPaymentStatuses(obWriteDomesticConsentResponse5, preExecutionResult);

        // then
        then(responseStatusMapper)
                .should()
                .mapToEnhancedPaymentStatus(OBWriteDomesticConsentResponse5Data.StatusEnum.AUTHORISED);

        assertThat(result.getRawBankPaymentStatus()).satisfies(rawBankPaymentStatus -> {
            assertThat(rawBankPaymentStatus.getStatus()).isEqualTo(OBWriteDomesticConsentResponse5Data.StatusEnum.AUTHORISED.toString());
            assertThat(rawBankPaymentStatus.getReason()).isEmpty();
        });
        assertThat(result.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.ACCEPTED);
    }

    @Test
    void shouldReturnPaymentStatusesWhenStatusIsRejected() {
        // given
        OBWriteDomesticConsentResponse5 obWriteDomesticConsentResponse5 = new OBWriteDomesticConsentResponse5()
                .data(new OBWriteDomesticConsentResponse5Data()
                        .status(OBWriteDomesticConsentResponse5Data.StatusEnum.REJECTED));

        given(responseStatusMapper.mapToEnhancedPaymentStatus(any(OBWriteDomesticConsentResponse5Data.StatusEnum.class)))
                .willReturn(EnhancedPaymentStatus.REJECTED);

        // when
        PaymentStatuses result = subject.extractPaymentStatuses(obWriteDomesticConsentResponse5, preExecutionResult);

        // then
        then(responseStatusMapper)
                .should()
                .mapToEnhancedPaymentStatus(OBWriteDomesticConsentResponse5Data.StatusEnum.REJECTED);

        assertThat(result.getRawBankPaymentStatus()).satisfies(rawBankPaymentStatus -> {
            assertThat(rawBankPaymentStatus.getStatus()).isEqualTo(OBWriteDomesticConsentResponse5Data.StatusEnum.REJECTED.toString());
            assertThat(rawBankPaymentStatus.getReason()).isEmpty();
        });
        assertThat(result.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
    }
}