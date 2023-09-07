package com.yolt.providers.stet.generic.dto.balance;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface StetBalancesResponseDTO {

    @JsonPath("$.balances")
    List<? extends StetBalanceDTO> getBalances(); //NOSONAR It enables to override JSON paths for mapping
}
