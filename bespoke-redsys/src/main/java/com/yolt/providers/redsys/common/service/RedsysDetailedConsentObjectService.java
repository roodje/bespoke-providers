package com.yolt.providers.redsys.common.service;

import com.yolt.providers.redsys.common.dto.AccountAccess;
import com.yolt.providers.redsys.common.dto.AccountReference;
import com.yolt.providers.redsys.common.dto.RequestGetConsent;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;

import java.time.LocalDate;
import java.util.List;
import static com.yolt.providers.redsys.common.service.RedsysAllAccountsConsentObjectService.*;


public class RedsysDetailedConsentObjectService implements RedsysConsentObjectService {

    public static final String IBAN_NAME = "Iban";
    public static final String IBAN_DISPLAY = "IBAN";

    @Override
    public RequestGetConsent getConsentObject(final LocalDate validUntil, FilledInUserSiteFormValues filledInUserSiteFormValues) {
        String iban = filledInUserSiteFormValues.get(IBAN_NAME).replace(" ", "").toUpperCase();

        AccountReference accountReference = new AccountReference();
        accountReference.setIban(iban);
        List<AccountReference> transactions = List.of(accountReference);
        List<AccountReference> balances = List.of(accountReference);
        return RequestGetConsent.builder()
                .combinedServiceIndicator(PAYMENT_INITIATION_IS_USED_IN_THE_SAME_SESSION)
                .recurringIndicator(RECURRING_ACCESS_FOR_USER)
                .validUntil(validUntil.toString())
                .frequencyPerDay(MAX_FREQUENCY_FOR_ACCESS_WITHOUT_PSU_PER_DAY)
                .access(AccountAccess.builder()
                        .transactions(transactions)
                        .balances(balances)
                        .build())
                .build();
    }
}
