package com.yolt.providers.openbanking.ais.generic2.pec.mapper.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.ScheduledPaymentStatusResponse;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsentResponse5Data;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericDelegatingScheduledPaymentStatusResponseMapperTest {

    private GenericDelegatingScheduledPaymentStatusResponseMapper subject;

    @Mock
    ResponseStatusMapper<OBWriteDomesticScheduledConsentResponse5Data.StatusEnum> writeDomesticConsentResponseStatusMapper;
    @Mock
    ResponseStatusMapper<OBWriteDomesticScheduledResponse5Data.StatusEnum> writeDomesticResponseStatusMapper;

    @BeforeEach
    void beforeEach() {
        subject = new GenericDelegatingScheduledPaymentStatusResponseMapper(writeDomesticConsentResponseStatusMapper, writeDomesticResponseStatusMapper);
    }

    @Test
    void shouldDelegateToWriteDomesticScheduledConsentResponseStatusMapperForMapToEnhancedPaymentStatusWhenStatusIsMappedIntoOBWriteDomesticScheduledConsentResponse5DataStatus() {
        // given
        given(writeDomesticConsentResponseStatusMapper.mapToEnhancedPaymentStatus(OBWriteDomesticScheduledConsentResponse5Data.StatusEnum.AUTHORISED))
                .willReturn(EnhancedPaymentStatus.INITIATION_SUCCESS);

        // when
        EnhancedPaymentStatus result = subject.mapToEnhancedPaymentStatus(ScheduledPaymentStatusResponse.Data.Status.AUTHORISED);

        // then
        then(writeDomesticResponseStatusMapper)
                .shouldHaveNoInteractions();
        assertThat(result).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
    }

    @Test
    void shouldDelegateToWriteDomesticScheduledResponseStatusMapperForMapToEnhancedPaymentStatusWhenStatusIsMappedIntoOBWriteDomesticScheduledResponse5DataStatus() {
        // given
        given(writeDomesticResponseStatusMapper.mapToEnhancedPaymentStatus(OBWriteDomesticScheduledResponse5Data.StatusEnum.INITIATIONCOMPLETED))
                .willReturn(EnhancedPaymentStatus.COMPLETED);

        // when
        EnhancedPaymentStatus result = subject.mapToEnhancedPaymentStatus(ScheduledPaymentStatusResponse.Data.Status.INITIATIONCOMPLETED);

        // then
        then(writeDomesticConsentResponseStatusMapper)
                .shouldHaveNoInteractions();
        assertThat(result).isEqualTo(EnhancedPaymentStatus.COMPLETED);
    }
}