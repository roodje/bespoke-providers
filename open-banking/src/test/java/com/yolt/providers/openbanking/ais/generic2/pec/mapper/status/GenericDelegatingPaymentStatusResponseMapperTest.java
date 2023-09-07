package com.yolt.providers.openbanking.ais.generic2.pec.mapper.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5Data;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GenericDelegatingPaymentStatusResponseMapperTest {

    private GenericDelegatingPaymentStatusResponseMapper subject;

    private ResponseStatusMapper<OBWriteDomesticConsentResponse5Data.StatusEnum> writeDomesticConsentResponseStatusMapper;

    private ResponseStatusMapper<OBWriteDomesticResponse5Data.StatusEnum> writeDomesticResponseStatusMapper;

    @BeforeEach
    void beforeEach() {
        writeDomesticConsentResponseStatusMapper = mock(ResponseStatusMapper.class);
        writeDomesticResponseStatusMapper = mock(ResponseStatusMapper.class);
        subject = new GenericDelegatingPaymentStatusResponseMapper(writeDomesticConsentResponseStatusMapper, writeDomesticResponseStatusMapper);
    }

    @Test
    void shouldDelegateToWriteDomesticConsentResponseStatusMapperForMapToEnhancedPaymentStatusWhenStatusIsMappedIntoOBWriteDomesticConsentResponse5DataStatus() {
        // given
        given(writeDomesticConsentResponseStatusMapper.mapToEnhancedPaymentStatus(any(OBWriteDomesticConsentResponse5Data.StatusEnum.class)))
                .willReturn(EnhancedPaymentStatus.INITIATION_SUCCESS);

        // when
        EnhancedPaymentStatus result = subject.mapToEnhancedPaymentStatus(PaymentStatusResponse.Data.Status.AUTHORISED);

        // then
        then(writeDomesticConsentResponseStatusMapper)
                .should()
                .mapToEnhancedPaymentStatus(OBWriteDomesticConsentResponse5Data.StatusEnum.AUTHORISED);
        then(writeDomesticResponseStatusMapper)
                .shouldHaveNoInteractions();
        assertThat(result).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
    }

    @Test
    void shouldDelegateToWriteDomesticResponseStatusMapperForMapToEnhancedPaymentStatusWhenStatusIsMappedIntoOBWriteDomesticResponse5DataStatus() {
        // given
        given(writeDomesticResponseStatusMapper.mapToEnhancedPaymentStatus(any(OBWriteDomesticResponse5Data.StatusEnum.class)))
                .willReturn(EnhancedPaymentStatus.COMPLETED);

        // when
        EnhancedPaymentStatus result = subject.mapToEnhancedPaymentStatus(PaymentStatusResponse.Data.Status.ACCEPTEDCREDITSETTLEMENTCOMPLETED);

        // then
        then(writeDomesticConsentResponseStatusMapper)
                .shouldHaveNoInteractions();
        then(writeDomesticResponseStatusMapper)
                .should()
                .mapToEnhancedPaymentStatus(OBWriteDomesticResponse5Data.StatusEnum.ACCEPTEDCREDITSETTLEMENTCOMPLETED);
        assertThat(result).isEqualTo(EnhancedPaymentStatus.COMPLETED);
    }

}