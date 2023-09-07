package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedDataInitiationException;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2Data;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericSubmitPaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<GenericSubmitPaymentPreExecutionResult, OBWriteDomestic2> {

    private final ObjectMapper objectMapper;
    private final OBRisk1.PaymentContextCodeEnum paymentContextCode;

    @Override
    public OBWriteDomestic2 provideHttpRequestBody(GenericSubmitPaymentPreExecutionResult preExecutionResult) {
        UkProviderState ukProviderState = preExecutionResult.getProviderState();
        return new OBWriteDomestic2()
                .risk(new OBRisk1()
                        .paymentContextCode(paymentContextCode))
                .data(new OBWriteDomestic2Data()
                        .consentId(ukProviderState.getConsentId())
                        .initiation(parseDataInitiation((String) ukProviderState.getOpenBankingPayment())));
    }

    private OBWriteDomestic2DataInitiation parseDataInitiation(String dataInitiation) {
        try {
            return objectMapper.readValue(dataInitiation, OBWriteDomestic2DataInitiation.class);
        } catch (JsonProcessingException ex) {
            throw new MalformedDataInitiationException("Unable to parse data initiation");
        }
    }
}
