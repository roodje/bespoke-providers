package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account;

import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.DefaultSchemeMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DefaultAccountNumberMapperV2Test {

    AccountNumberMapperV2 accountNumberMapper = new DefaultAccountNumberMapperV2(new DefaultSchemeMapper());

    @Test
    void shouldReturnMappedAccountNumberDtoWhenPartiesAreProvided() {
        //given
        OBAccount4Account account = new OBAccount4Account().schemeName("UK.OBIE.IBAN")
                .identification("Some identification")
                .secondaryIdentification("some secondary identification");
        List<PartyDto> parties = List.of(new PartyDto("Party Name"));
        ProviderAccountNumberDTO expectedMappedAccountNumberDto = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "Some identification");
        expectedMappedAccountNumberDto.setHolderName("Party Name");
        expectedMappedAccountNumberDto.setSecondaryIdentification("some secondary identification");

        //when
        ProviderAccountNumberDTO mappedAccount = accountNumberMapper.map(account, parties);

        //then
        assertThat(mappedAccount).isEqualTo(expectedMappedAccountNumberDto);
    }

    @Test
    void shouldReturnMappedAccountNumberDtoWhenPartiesAreNotProvided() {
        //given
        OBAccount4Account account = new OBAccount4Account().schemeName("UK.OBIE.IBAN")
                .identification("Some identification")
                .secondaryIdentification("some secondary identification");
        List<PartyDto> parties = Collections.emptyList();
        ProviderAccountNumberDTO expectedMappedAccountNumberDto = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "Some identification");
        expectedMappedAccountNumberDto.setSecondaryIdentification("some secondary identification");

        //when
        ProviderAccountNumberDTO mappedAccount = accountNumberMapper.map(account, parties);

        //then
        assertThat(mappedAccount).isEqualTo(expectedMappedAccountNumberDto);
    }
}
