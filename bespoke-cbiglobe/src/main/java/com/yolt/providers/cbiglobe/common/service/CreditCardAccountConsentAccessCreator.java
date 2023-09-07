package com.yolt.providers.cbiglobe.common.service;

import com.yolt.providers.cbiglobe.dto.ConsentsAccess;
import com.yolt.providers.cbiglobe.dto.ConsentsAccessBalances;
import com.yolt.providers.cbiglobe.dto.ConsentsAccessTransactions;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

public class CreditCardAccountConsentAccessCreator implements ConsentAccessCreator {
    @Override
    public ConsentsAccess createConsentAccess(ProviderAccountDTO accountToConsent) {
        ConsentsAccess consentsAccess = new ConsentsAccess();
        String maskedPan = accountToConsent.getAccountMaskedIdentification();
        consentsAccess.addBalancesItem(new ConsentsAccessBalances()
                .accountId(accountToConsent.getAccountId())
                .maskedPan(maskedPan));
        consentsAccess.addTransactionsItem(new ConsentsAccessTransactions()
                .accountId(accountToConsent.getAccountId())
                .maskedPan(maskedPan));
        return consentsAccess;
    }
}
