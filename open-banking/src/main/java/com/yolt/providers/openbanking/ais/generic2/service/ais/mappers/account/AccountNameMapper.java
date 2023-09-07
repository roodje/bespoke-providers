package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;

import java.util.List;

public interface AccountNameMapper {
    String getAccountName(final OBAccount6 account,
                          final OBAccount4Account mainAccountReference,
                          final List<OBAccount4Account> allAccountReferences);
}
