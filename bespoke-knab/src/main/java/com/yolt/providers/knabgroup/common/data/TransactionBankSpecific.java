package com.yolt.providers.knabgroup.common.data;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TransactionBankSpecific extends AbstractBankSpecific {

    private String dayStartBalanceAmount;

    private String dayStartBalanceCurrency;
}
