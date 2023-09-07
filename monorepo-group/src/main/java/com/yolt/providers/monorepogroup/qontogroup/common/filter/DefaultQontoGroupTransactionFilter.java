package com.yolt.providers.monorepogroup.qontogroup.common.filter;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transaction;

import java.util.List;
import java.util.function.UnaryOperator;

public class DefaultQontoGroupTransactionFilter implements UnaryOperator<List<Transaction>> {
    private static final String REJECTED = "rejected";

    @Override
    public List<Transaction> apply(List<Transaction> accounts) {

        return accounts.stream().filter(trx -> !REJECTED.equals(trx.getStatus())).toList();
    }
}
