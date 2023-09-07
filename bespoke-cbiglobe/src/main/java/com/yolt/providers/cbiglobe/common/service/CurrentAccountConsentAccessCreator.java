package com.yolt.providers.cbiglobe.common.service;

import com.yolt.providers.cbiglobe.dto.ConsentsAccess;
import com.yolt.providers.cbiglobe.dto.ConsentsAccessBalances;
import com.yolt.providers.cbiglobe.dto.ConsentsAccessTransactions;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

public class CurrentAccountConsentAccessCreator implements ConsentAccessCreator {
    @Override
    public ConsentsAccess createConsentAccess(ProviderAccountDTO accountToConsent) {
        ConsentsAccess consentsAccess = new ConsentsAccess();
        String accountIban = accountToConsent.getAccountNumber().getIdentification();
        consentsAccess.addBalancesItem(new ConsentsAccessBalances()
                .accountId(accountToConsent.getAccountId())
                .iban(accountIban));
        consentsAccess.addTransactionsItem(new ConsentsAccessTransactions()
                .accountId(accountToConsent.getAccountId())
                .iban(accountIban));
        return consentsAccess;
    }
}
