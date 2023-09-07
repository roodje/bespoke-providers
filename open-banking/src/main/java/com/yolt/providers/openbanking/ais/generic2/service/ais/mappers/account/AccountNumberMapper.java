package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.SchemeMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;

import java.util.function.Function;

@AllArgsConstructor
@Deprecated //use newer versoin C4PO-8398
public class AccountNumberMapper implements Function<OBAccount4Account, ProviderAccountNumberDTO> {
    private final SchemeMapper schemeMapper;

    @Override
    public ProviderAccountNumberDTO apply(OBAccount4Account account) {
        String holdername = account.getName();
        ProviderAccountNumberDTO.Scheme scheme = schemeMapper.mapToScheme(account.getSchemeName());
        String identification = account.getIdentification();
        String secondaryIdentification = account.getSecondaryIdentification();
        ProviderAccountNumberDTO accountNumberDTO = new ProviderAccountNumberDTO(scheme, identification);
        accountNumberDTO.setHolderName(holdername);
        accountNumberDTO.setSecondaryIdentification(secondaryIdentification);
        return accountNumberDTO;
    }
}
