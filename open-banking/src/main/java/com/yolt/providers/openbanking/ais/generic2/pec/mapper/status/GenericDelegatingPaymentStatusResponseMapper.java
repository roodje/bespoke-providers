package com.yolt.providers.openbanking.ais.generic2.pec.mapper.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5Data;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5Data;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class GenericDelegatingPaymentStatusResponseMapper implements ResponseStatusMapper<PaymentStatusResponse.Data.Status> {

    private final ResponseStatusMapper<OBWriteDomesticConsentResponse5Data.StatusEnum> writeDomesticConsentResponseStatusMapper;
    private final ResponseStatusMapper<OBWriteDomesticResponse5Data.StatusEnum> writeDomesticResponseStatusMapper;

    @Override
    public EnhancedPaymentStatus mapToEnhancedPaymentStatus(PaymentStatusResponse.Data.Status status) {
        return Optional.ofNullable(OBWriteDomesticConsentResponse5Data.StatusEnum.fromValue(status.toString()))
                .map(writeDomesticConsentResponseStatusMapper::mapToEnhancedPaymentStatus)
                .orElseGet(() -> Optional.ofNullable(OBWriteDomesticResponse5Data.StatusEnum.fromValue(status.toString()))
                        .map(writeDomesticResponseStatusMapper::mapToEnhancedPaymentStatus)
                        .orElse(EnhancedPaymentStatus.UNKNOWN));
    }
}
