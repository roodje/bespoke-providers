package com.yolt.providers.stet.generic.dto;

import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalancesResponseDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TestStetBalancesResponseDTO implements StetBalancesResponseDTO {

    private List<StetBalanceDTO> balances;
}
