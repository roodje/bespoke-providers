package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.common.pis.sepa.SepaAccountDTO;

public class CbiGlobeDefaultAccountToCurrencyMapper implements CbiGlobeAccountToCurrencyMapper {

    @Override
    public String map(SepaAccountDTO sepaAccountDTO) {
        return sepaAccountDTO.getCurrency().name();
    }
}
