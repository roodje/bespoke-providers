package com.yolt.providers.monorepogroup.qontogroup.common.dto.internal;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transaction;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode
public class QontoFetchDataResult {

    private Map<Account, List<Transaction>> resourcesMap;

    public QontoFetchDataResult() {
        this.resourcesMap = new LinkedHashMap<>();
    }

    public void addResources(final Account account, final List<Transaction> transactionsList) {
        if (!resourcesMap.containsKey(account)) {
            resourcesMap.put(account, new ArrayList<>(transactionsList));
        } else {
            var transactionsListForAccount = resourcesMap.get(account);
            transactionsListForAccount.addAll(transactionsList);
            resourcesMap.put(account, transactionsListForAccount);
        }
    }

    public Map<Account, List<Transaction>> getResources() {
        return resourcesMap;
    }
}
