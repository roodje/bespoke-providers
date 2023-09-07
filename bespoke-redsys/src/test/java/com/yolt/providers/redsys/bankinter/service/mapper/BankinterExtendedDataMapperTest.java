package com.yolt.providers.redsys.bankinter.service.mapper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;


class BankinterExtendedDataMapperTest {

    private BankinterExtendedDataMapper dataMapper = new BankinterExtendedDataMapper(null, null);

    @ParameterizedTest
    @CsvSource({"/TXT/D|TRANSF OTRAS ENTID /Jordi ING|20221005,TRANSF OTRAS ENTID /Jordi ING",
            "/TXT/TRANSF OTRAS ENTID /Jordi ING|20221005,TRANSF OTRAS ENTID /Jordi ING",
            "/TXT/D|TRANSF OTRAS ENTID /Jordi ING,TRANSF OTRAS ENTID /Jordi ING",
            "/TXT/H|TRANSF OTRAS ENTID /Jordi ING|20221005,TRANSF OTRAS ENTID /Jordi ING",
            "/TXT/TRANSF OTRAS ENTID /Jordi ING|20221005,TRANSF OTRAS ENTID /Jordi ING",
            "/TXT/H|TRANSF OTRAS ENTID /Jordi ING,TRANSF OTRAS ENTID /Jordi ING",
            "/TXT/TRANSF OTRAS ENTID /Jordi ING|20221005,TRANSF OTRAS ENTID /Jordi ING",
            "/TXT/TRANSF OTRAS ENTID /Jordi ING,TRANSF OTRAS ENTID /Jordi ING",
            "TRANSF OTRAS ENTID /Jordi ING|20221005,TRANSF OTRAS ENTID /Jordi ING",
            "TRANSF OTRAS ENTID /Jordi ING,TRANSF OTRAS ENTID /Jordi ING",})
    void shouldRemovedNotNeededLiteralsFromRemittanceInformation(String given, String expected){
        //when
        String result = dataMapper.formatRemittanceInformation(given);

        //then
        assertThat(result).isEqualTo(expected);
    }

}