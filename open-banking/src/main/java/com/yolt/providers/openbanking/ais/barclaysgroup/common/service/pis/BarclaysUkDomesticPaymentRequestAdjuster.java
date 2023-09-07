package com.yolt.providers.openbanking.ais.barclaysgroup.common.service.pis;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.PaymentRequestAdjuster;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationCreditorAccount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationDebtorAccount;
import org.apache.commons.lang3.ObjectUtils;

public class BarclaysUkDomesticPaymentRequestAdjuster implements PaymentRequestAdjuster<OBWriteDomestic2DataInitiation> {

    private static final String INCORRECT_SCHEMA_NAME = "SORTCODEACCOUNTNUMBER";
    private static final String CORRECT_SCHEMA_NAME = "SortCodeAccountNumber";

    @Override
    public OBWriteDomestic2DataInitiation adjust(final OBWriteDomestic2DataInitiation dataInitiation) {
        OBWriteDomestic2DataInitiationCreditorAccount creditorAccount = dataInitiation.getCreditorAccount();
        OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = dataInitiation.getDebtorAccount();
        String correctCreditorSchemaName = creditorAccount.getSchemeName().replace(INCORRECT_SCHEMA_NAME, CORRECT_SCHEMA_NAME);
        creditorAccount.setSchemeName(correctCreditorSchemaName);
        if (ObjectUtils.isNotEmpty(debtorAccount)) {
            String correctDebtorSchemaName = debtorAccount.getSchemeName().replace(INCORRECT_SCHEMA_NAME, CORRECT_SCHEMA_NAME);
            debtorAccount.setSchemeName(correctDebtorSchemaName);

        }
        return dataInitiation;
    }
}
