package com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper;

import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.UkSchemeMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationDebtorAccount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WithDebtorUkPaymentMapperDecorator implements UkPaymentMapper {

    private final UkPaymentMapper wrappe;
    private final UkSchemeMapper schemeMapper;

    @Override
    public OBWriteDomesticConsent4 mapToSetupRequest(InitiateUkDomesticPaymentRequest request) {
        OBWriteDomesticConsent4 mappedRequest = wrappe.mapToSetupRequest(request);
        mappedRequest.getData().getInitiation().setDebtorAccount(createDebtorAccount(request.getRequestDTO().getDebtorAccount()));
        return mappedRequest;
    }

    private OBWriteDomestic2DataInitiationDebtorAccount createDebtorAccount(final UkAccountDTO debtorAccount) {
        if (debtorAccount == null || debtorAccount.getAccountIdentifier() == null) {
            return null;
        }
        return new OBWriteDomestic2DataInitiationDebtorAccount()
                .schemeName(schemeMapper.map(debtorAccount.getAccountIdentifierScheme()))
                .identification(debtorAccount.getAccountIdentifier())
                .name(debtorAccount.getAccountHolderName())
                .secondaryIdentification(debtorAccount.getSecondaryIdentification());
    }

    @Override
    public OBWriteDomestic2 mapToSubmitRequest(String consentId, OBWriteDomestic2DataInitiation paymentIntent) {
        return wrappe.mapToSubmitRequest(consentId, paymentIntent);
    }
}
