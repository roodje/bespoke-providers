package com.yolt.providers.openbanking.ais.barclaysgroup.common.service.pis;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.PaymentRequestAdjuster;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationCreditorAccount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationDebtorAccount;

public class BarclaysPaymentRequestAdjuster implements PaymentRequestAdjuster<OBWriteDomestic2DataInitiation> {
    @Override
    public OBWriteDomestic2DataInitiation adjust(final OBWriteDomestic2DataInitiation dataInitiation) {
        OBWriteDomestic2DataInitiationCreditorAccount creditorAccount = dataInitiation.getCreditorAccount();
        OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = dataInitiation.getDebtorAccount();
        if (creditorAccount != null && creditorAccount.getName() != null && creditorAccount.getName().length() > 17) {//NOSONAR - banks prove even not null fields can be null
            creditorAccount.setName(creditorAccount.getName().substring(0, 17));
        }
        if (debtorAccount != null && debtorAccount.getName() != null && debtorAccount.getName().length() > 17) {//NOSONAR - banks prove even not null fields can be null
            debtorAccount.setName(debtorAccount.getName().substring(0, 17));
        }
        return dataInitiation;
    }
}
