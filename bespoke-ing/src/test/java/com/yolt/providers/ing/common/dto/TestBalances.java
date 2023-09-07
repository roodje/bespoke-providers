package com.yolt.providers.ing.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestBalances implements Balances {
    private List<Balance> data;
}
