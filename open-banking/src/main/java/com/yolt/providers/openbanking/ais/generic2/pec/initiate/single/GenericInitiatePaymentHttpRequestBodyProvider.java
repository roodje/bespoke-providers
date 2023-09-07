package com.yolt.providers.openbanking.ais.generic2.pec.initiate.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.DataMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericInitiatePaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<GenericInitiatePaymentPreExecutionResult, OBWriteDomesticConsent4> {

    private final DataMapper<OBWriteDomestic2DataInitiation, InitiateUkDomesticPaymentRequestDTO> dataInitiationMapper;
    private final OBRisk1.PaymentContextCodeEnum paymentContextCode;

    @Override
    public OBWriteDomesticConsent4 provideHttpRequestBody(GenericInitiatePaymentPreExecutionResult preExecutionResult) {
        return new OBWriteDomesticConsent4()
                .risk(new OBRisk1()
                        .paymentContextCode(paymentContextCode))
                .data(new OBWriteDomesticConsent4Data()
                        .initiation(dataInitiationMapper.map(preExecutionResult.getPaymentRequestDTO())));
    }
}
