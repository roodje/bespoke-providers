package com.yolt.providers.openbanking.ais.generic2.pec.mapper.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.ScheduledPaymentStatusResponse;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsentResponse5Data;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5Data;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class GenericDelegatingScheduledPaymentStatusResponseMapper implements ResponseStatusMapper<ScheduledPaymentStatusResponse.Data.Status> {

    private final ResponseStatusMapper<OBWriteDomesticScheduledConsentResponse5Data.StatusEnum> writeDomesticConsentResponseStatusMapper;
    private final ResponseStatusMapper<OBWriteDomesticScheduledResponse5Data.StatusEnum> writeDomesticResponseStatusMapper;

    @Override
    public EnhancedPaymentStatus mapToEnhancedPaymentStatus(ScheduledPaymentStatusResponse.Data.Status status) {
        return mapConsentStatusToEnhancedPaymentStatus(status)
                .orElseGet(() -> mapStatusToEnhancedPaymentStatus(status)
                        .orElse(EnhancedPaymentStatus.UNKNOWN));
    }

    private Optional<EnhancedPaymentStatus> mapConsentStatusToEnhancedPaymentStatus(ScheduledPaymentStatusResponse.Data.Status status) {
        return Optional.ofNullable(OBWriteDomesticScheduledConsentResponse5Data.StatusEnum.fromValue(status.toString()))
                .map(writeDomesticConsentResponseStatusMapper::mapToEnhancedPaymentStatus);
    }

    private Optional<EnhancedPaymentStatus> mapStatusToEnhancedPaymentStatus(ScheduledPaymentStatusResponse.Data.Status status) {
        return Optional.ofNullable(OBWriteDomesticScheduledResponse5Data.StatusEnum.fromValue(status.toString()))
                .map(writeDomesticResponseStatusMapper::mapToEnhancedPaymentStatus);
    }
}
