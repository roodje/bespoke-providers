package com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme;

import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CamelCaseUkSchemeMapperTest {

    @InjectMocks
    private CamelCaseUkSchemeMapper subject;

    @Test
    void shouldReturnCamelCasedSortCodeAccountNumberSchemeNameWithUkObiePrefixWhenSortCodeAccountNumberSchemeIsProvided() {
        // given
        AccountIdentifierScheme scheme = AccountIdentifierScheme.SORTCODEACCOUNTNUMBER;

        // when
        String result = subject.map(scheme);

        // then
        assertThat(result).isEqualTo("UK.OBIE.SortCodeAccountNumber");
    }

    @Test
    void shouldReturnIbanSchemeNameWithUkObiePrefixWhenIbanSchemeIsProvided() {
        // given
        AccountIdentifierScheme scheme = AccountIdentifierScheme.IBAN;

        // when
        String result = subject.map(scheme);

        // then
        assertThat(result).isEqualTo("UK.OBIE.IBAN");
    }
}