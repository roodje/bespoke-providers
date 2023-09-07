package com.yolt.providers.openbanking.ais.revolutgroup.common.service.ais.mappers;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.SupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

public class RevolutSupportedSchemeAccountFilter implements SupportedSchemeAccountFilter {
    public static final String OB_3_0_0_SCHEME_PREFIX = "UK.OBIE.";

    private static final Set<String> SUPPORTED_ACCOUNT_IDENTIFICATION = Stream.of(ProviderAccountNumberDTO.Scheme.values())
            .map(scheme -> OB_3_0_0_SCHEME_PREFIX + scheme.name()
                    .toUpperCase())
            .collect(Collectors.toSet());

    /**
     * Method below is meant to stabilize i.e. select the same identification regardless of serialization order (see: C4PO-10160)
     * Assumptions:
     *      in case IBAN and SORTCODEACCOUNTNUMBER are present - return IBAN
     *      in case two IBANs are present - return lexicographically greater (to simplify solution - all IBANs are lexicographically greater than SORTCODEACCOUNTNUMBERs).
     *
     *      As a side effect we cover case with two identical IBANs, single IBAN, single SORTCODEACCOUNTNUMBER and no identification at all
     *
     * According to Revolut:
     * In EUR pocket you can have 2 addresses, one for SWIFT and one for SEPA.
     * In GBP, you can have local details (account number and sort code) and IBAN for SWIFT transfers.
     * Some information may be missing from specific pocket if the account is not eligible to receive local GBP details or if the currency pocket was just created.
     * It may take few hours (and for local GBP around 24 hours) for details to be generated.
     */

    @Override
    public OBAccount4Account findFirstAccountWhereSchemeIsSupported(List<OBAccount4Account> accountList) {
        if (CollectionUtils.isEmpty(accountList)) {
            return null;
        }
        return accountList
                .stream()
                .filter(account -> SUPPORTED_ACCOUNT_IDENTIFICATION.contains(account.getSchemeName().toUpperCase()))
                .min(comparing(OBAccount4Account::getSchemeName)
                        .thenComparing(comparing(OBAccount4Account::getIdentification).reversed()))
                .orElse(null);
    }
}
