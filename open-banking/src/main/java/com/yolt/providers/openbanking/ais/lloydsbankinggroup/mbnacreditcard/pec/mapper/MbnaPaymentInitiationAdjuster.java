package com.yolt.providers.openbanking.ais.lloydsbankinggroup.mbnacreditcard.pec.mapper;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.PaymentRequestAdjuster;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationDebtorAccount;

/**
 * This class has been created only for testing purposes.
 * Site-Management does not allow PAN scheme and we are not able
 * to pass the validation using a PAN number.
 */
public class MbnaPaymentInitiationAdjuster implements PaymentRequestAdjuster<OBWriteDomestic2DataInitiation> {

    private static final String LOCAL_INSTRUMENT = "UK.OBIE.MoneyTransfer";
    private static final String SCHEME_NAME = "UK.OBIE.PAN";

    @Override
    public OBWriteDomestic2DataInitiation adjust(OBWriteDomestic2DataInitiation dataInitiation) {

        OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = dataInitiation.getDebtorAccount();
        dataInitiation.setLocalInstrument(LOCAL_INSTRUMENT);
        if (debtorAccount != null) {
            debtorAccount.schemeName(SCHEME_NAME);
            debtorAccount.setIdentification("5253030000259937");
            dataInitiation.setDebtorAccount(debtorAccount);
        }
        return dataInitiation;
    }
}
