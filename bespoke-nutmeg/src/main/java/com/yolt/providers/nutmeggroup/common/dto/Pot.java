package com.yolt.providers.nutmeggroup.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pot {
    private String uuid;
    private String name;
    private BigDecimal currentValue;
    private String status;
    private Wrapper wrapper;
    private String investmentStyle;
}
