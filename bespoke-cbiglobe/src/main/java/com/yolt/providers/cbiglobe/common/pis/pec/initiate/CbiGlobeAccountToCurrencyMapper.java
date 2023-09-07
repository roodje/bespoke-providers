package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.common.pis.sepa.SepaAccountDTO;

public interface CbiGlobeAccountToCurrencyMapper {
    String map(SepaAccountDTO sepaAccountDTO);
}
