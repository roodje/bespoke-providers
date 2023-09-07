package com.yolt.providers.monorepogroup.qontogroup.common.filter;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Account;

import java.util.List;
import java.util.function.UnaryOperator;

public class DefaultQontoGroupAccountFilter implements UnaryOperator<List<Account>> {
    private static final String CLOSED = "closed";

    @Override
    public List<Account> apply(List<Account> accounts) {
        return accounts.stream().filter(acc -> !CLOSED.equals(acc.getStatus())).toList();
    }
}
