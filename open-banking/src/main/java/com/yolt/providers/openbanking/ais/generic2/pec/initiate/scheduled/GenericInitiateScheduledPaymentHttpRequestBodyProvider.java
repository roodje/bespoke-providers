package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequestDTO;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.DataMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsent4;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsent4Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GenericInitiateScheduledPaymentHttpRequestBodyProvider implements PaymentExecutionHttpRequestBodyProvider<GenericInitiateScheduledPaymentPreExecutionResult, OBWriteDomesticScheduledConsent4> {

    private final DataMapper<OBWriteDomesticScheduled2DataInitiation, InitiateUkDomesticScheduledPaymentRequestDTO> mapper;
    private final OBRisk1.PaymentContextCodeEnum paymentContextCode;

    @Override
    public OBWriteDomesticScheduledConsent4 provideHttpRequestBody(GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult) {
        var dataInitiation = mapper.map(preExecutionResult.getPaymentRequestDTO());

        return new OBWriteDomesticScheduledConsent4()
                .risk(new OBRisk1()
                        .paymentContextCode(paymentContextCode))
                .data(new OBWriteDomesticScheduledConsent4Data()
                        .permission(OBWriteDomesticScheduledConsent4Data.PermissionEnum.CREATE)
                        .initiation(dataInitiation));
    }
}
