package com.yolt.providers.openbanking.ais.tsbgroup.common.mapper;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.ResponseStatusMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5Data;

public class TsbWriteDomesticResponseStatusMapper implements ResponseStatusMapper<OBWriteDomesticResponse5Data.StatusEnum> {

    @Override
    public EnhancedPaymentStatus mapToEnhancedPaymentStatus(OBWriteDomesticResponse5Data.StatusEnum status) {
        return switch (status) {
            case PENDING, ACCEPTEDSETTLEMENTINPROCESS -> EnhancedPaymentStatus.ACCEPTED;
            case ACCEPTEDSETTLEMENTCOMPLETED, ACCEPTEDWITHOUTPOSTING, ACCEPTEDCREDITSETTLEMENTCOMPLETED -> EnhancedPaymentStatus.COMPLETED;
            case REJECTED -> EnhancedPaymentStatus.REJECTED;
        };
    }
}
