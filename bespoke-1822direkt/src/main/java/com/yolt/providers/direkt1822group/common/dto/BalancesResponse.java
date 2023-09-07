package com.yolt.providers.direkt1822group.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalancesResponse {

    private AccountReference account;
    private List<Balance> balances;
}
