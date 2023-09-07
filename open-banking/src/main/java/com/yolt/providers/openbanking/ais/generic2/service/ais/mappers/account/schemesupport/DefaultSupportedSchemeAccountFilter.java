package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultSupportedSchemeAccountFilter implements SupportedSchemeAccountFilter {
    private static final String OB_3_0_0_SCHEME_PREFIX = "UK.OBIE.";

    private static final Set<String> SUPPORTED_ACCOUNT_IDENTIFICATION = Stream.of(ProviderAccountNumberDTO.Scheme.values())
            .map(scheme -> OB_3_0_0_SCHEME_PREFIX + scheme.name()
                    .toUpperCase())
            .collect(Collectors.toSet());

    @Override
    public OBAccount4Account findFirstAccountWhereSchemeIsSupported(List<OBAccount4Account> accountList) {
        if (CollectionUtils.isEmpty(accountList)) {
            return null;
        }
        for (OBAccount4Account cashAccount : accountList) {
            if (SUPPORTED_ACCOUNT_IDENTIFICATION.contains(cashAccount.getSchemeName().toUpperCase())) {
                return cashAccount;
            }
        }
        return null;
    }
}
