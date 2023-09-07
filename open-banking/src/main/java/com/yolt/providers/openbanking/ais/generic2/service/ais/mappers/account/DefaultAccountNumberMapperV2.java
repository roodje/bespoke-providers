package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account;

import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.SchemeMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

@RequiredArgsConstructor
public class DefaultAccountNumberMapperV2 implements AccountNumberMapperV2 {

    private final SchemeMapper schemeMapper;

    @Override
    public ProviderAccountNumberDTO map(OBAccount4Account account, List<PartyDto> parties) {
        ProviderAccountNumberDTO.Scheme scheme = schemeMapper.mapToScheme(account.getSchemeName());
        String identification = account.getIdentification();
        String secondaryIdentification = account.getSecondaryIdentification();
        ProviderAccountNumberDTO accountNumberDTO = new ProviderAccountNumberDTO(scheme, identification);
        accountNumberDTO.setHolderName(extractHolderName(parties));
        accountNumberDTO.setSecondaryIdentification(secondaryIdentification);
        return accountNumberDTO;
    }

    private String extractHolderName(List<PartyDto> parties) {
        if (!CollectionUtils.isEmpty(parties)) {
            String name = parties.get(0).getName();
            if (StringUtils.isNotBlank(name)) {
                return name;
            }
        }
        return null;
    }
}
