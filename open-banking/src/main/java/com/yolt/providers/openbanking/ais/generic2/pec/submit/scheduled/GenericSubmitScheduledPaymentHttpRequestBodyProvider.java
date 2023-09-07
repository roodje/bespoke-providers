package com.yolt.providers.openbanking.ais.generic2.pec.submit.scheduled;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedDataInitiationException;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.GenericSubmitPaymentPreExecutionResult;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2Data;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2DataInitiation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericSubmitScheduledPaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<GenericSubmitPaymentPreExecutionResult, OBWriteDomesticScheduled2> {

    private final ObjectMapper objectMapper;
    private final OBRisk1.PaymentContextCodeEnum paymentContextCode;

    @Override
    public OBWriteDomesticScheduled2 provideHttpRequestBody(GenericSubmitPaymentPreExecutionResult preExecutionResult) {
        UkProviderState ukProviderState = preExecutionResult.getProviderState();
        String consentId = ukProviderState.getConsentId();
        OBWriteDomesticScheduled2DataInitiation dataInitiation = parseDataInitiation((String) ukProviderState.getOpenBankingPayment());

        return new OBWriteDomesticScheduled2()
                .risk(new OBRisk1()
                        .paymentContextCode(paymentContextCode))
                .data(new OBWriteDomesticScheduled2Data()
                        .consentId(consentId)
                        .initiation(dataInitiation));
    }

    private OBWriteDomesticScheduled2DataInitiation parseDataInitiation(String dataInitiation) {
        try {
            return objectMapper.readValue(dataInitiation, OBWriteDomesticScheduled2DataInitiation.class);
        } catch (JsonProcessingException ex) {
            throw new MalformedDataInitiationException("Unable to parse data initiation");
        }
    }
}
