package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;

import java.util.List;

public interface SupportedSchemeAccountFilter {
    OBAccount4Account findFirstAccountWhereSchemeIsSupported(final List<OBAccount4Account> accountList);
}
