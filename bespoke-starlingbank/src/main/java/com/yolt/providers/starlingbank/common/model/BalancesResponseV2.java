package com.yolt.providers.starlingbank.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalancesResponseV2 {
    private CurrencyAndAmountV2 clearedBalance;
    private CurrencyAndAmountV2 effectiveBalance;
    private CurrencyAndAmountV2 pendingTransactions;
    private CurrencyAndAmountV2 availableToSpend;
    private CurrencyAndAmountV2 acceptedOverdraft;
    private CurrencyAndAmountV2 amount;
}
