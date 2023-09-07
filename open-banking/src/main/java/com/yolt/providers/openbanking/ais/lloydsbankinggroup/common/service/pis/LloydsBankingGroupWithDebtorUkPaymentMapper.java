package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.pis;

import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.UkPaymentMapper;
import com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithDebtorUkPaymentMapperDecorator;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.UkSchemeMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4;
import org.apache.commons.lang3.StringUtils;

public class LloydsBankingGroupWithDebtorUkPaymentMapper extends WithDebtorUkPaymentMapperDecorator {

    private static final int REFERENCE_MAX_LENGTH = 35;

    public LloydsBankingGroupWithDebtorUkPaymentMapper(UkPaymentMapper wrappe, UkSchemeMapper schemeMapper) {
        super(wrappe, schemeMapper);
    }

    @Override
    public OBWriteDomesticConsent4 mapToSetupRequest(InitiateUkDomesticPaymentRequest request) {
        OBWriteDomesticConsent4 setupRequest = super.mapToSetupRequest(request);
        validateLimitedFields(setupRequest);
        return setupRequest;
    }

    private void validateLimitedFields(OBWriteDomesticConsent4 request) {
        verifyReference(request.getData().getInitiation().getRemittanceInformation().getReference());
    }

    private void verifyReference(String reference) {
        if (StringUtils.isNotEmpty(reference) && reference.length() > REFERENCE_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("Remittance Information Reference is too long %d. Maximum length for Lloyds Banking Group is %d", reference.length(), REFERENCE_MAX_LENGTH));
        }
    }
}
