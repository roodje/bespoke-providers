package com.yolt.providers.redsys.common.service;

import com.yolt.providers.redsys.common.dto.AccountAccess;
import com.yolt.providers.redsys.common.dto.RequestGetConsent;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;

import java.time.LocalDate;

public class RedsysAllAccountsConsentObjectService implements RedsysConsentObjectService {

    private static final String ACCESS_ALL_AVAILABLE_PSD2_ACCOUNTS = "allAccounts";
    public static final boolean PAYMENT_INITIATION_IS_USED_IN_THE_SAME_SESSION = false;
    public static final boolean RECURRING_ACCESS_FOR_USER = true;
    public static final int MAX_FREQUENCY_FOR_ACCESS_WITHOUT_PSU_PER_DAY = 4;

    @Override
    public RequestGetConsent getConsentObject(LocalDate validUntil, FilledInUserSiteFormValues filledInUserSiteFormValues) {
        return RequestGetConsent.builder()
                .combinedServiceIndicator(PAYMENT_INITIATION_IS_USED_IN_THE_SAME_SESSION)
                .recurringIndicator(RECURRING_ACCESS_FOR_USER)
                .validUntil(validUntil.toString())
                .frequencyPerDay(MAX_FREQUENCY_FOR_ACCESS_WITHOUT_PSU_PER_DAY)
                .access(AccountAccess.builder()
                        .allPsd2(ACCESS_ALL_AVAILABLE_PSD2_ACCOUNTS)
                        .build())
                .build();
    }
}
