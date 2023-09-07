package com.yolt.providers.starlingbank.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private String id;
    private String currency;
    private BigDecimal amount;
    private String direction;
    private String created;
    private String narrative;
    private String source;
    private BigDecimal balance;

}
