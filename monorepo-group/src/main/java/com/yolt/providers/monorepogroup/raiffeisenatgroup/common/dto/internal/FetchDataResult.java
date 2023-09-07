package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal;

import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Transaction;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode
public class FetchDataResult {

    private List<Account> accountsList;

    private Map<TransactionKey, List<Transaction>> transactionsMap;

    public FetchDataResult() {
        accountsList = new ArrayList<>();
        transactionsMap = new HashMap<>();
    }

    public void addResources(final Account account, final List<Transaction> bookedTransactions, final List<Transaction> pendingTransaction) {
        if (!accountsList.contains(account)) {
            accountsList.add(account);
        }
        TransactionKey keyForBookedTransactions = new TransactionKey(account.getResourceId(), Type.BOOKED);
        List<Transaction> bookedTransactionsList = transactionsMap.get(keyForBookedTransactions);
        if (CollectionUtils.isEmpty(bookedTransactionsList)) {
            bookedTransactionsList = new ArrayList<>();
        }
        bookedTransactionsList.addAll(bookedTransactions);

        TransactionKey keyForPendingTransactions = new TransactionKey(account.getResourceId(), Type.PENDING);
        List<Transaction> pendingTransactionsList = transactionsMap.get(keyForPendingTransactions);
        if (CollectionUtils.isEmpty(pendingTransactionsList)) {
            pendingTransactionsList = new ArrayList<>();
        }
        pendingTransactionsList.addAll(pendingTransaction);

        transactionsMap.put(keyForBookedTransactions, bookedTransactionsList);
        transactionsMap.put(keyForPendingTransactions, pendingTransactionsList);
    }

    public List<Account> getAccounts() {
        return accountsList;
    }

    public List<Transaction> getBookedTransactions(final String resourceId) {
        return transactionsMap.get(new TransactionKey(resourceId, Type.BOOKED));
    }

    public List<Transaction> getPendingTransactions(final String resourceId) {
        return transactionsMap.get(new TransactionKey(resourceId, Type.PENDING));
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private class TransactionKey {
        private final String resourceId;
        private final Type type;
    }

    private enum Type {
        BOOKED,
        PENDING
    }
}
