package com.yolt.providers.stet.lclgroup.common.pec.initiate;

import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePaymentPaymentIdExtractor;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LclGroupInitiatePaymentPaymentIdExtractor extends StetInitiatePaymentPaymentIdExtractor {

    private final LclGroupPaymentHeadersExtractor headersExtractor;

    @Override
    public String extractPaymentId(StetPaymentInitiationResponseDTO unusedDTO, StetInitiatePreExecutionResult unused) {
        String location = headersExtractor.getHeaders().getLocation().toString();
        return location.substring(location.lastIndexOf('/') + 1);
    }
}
