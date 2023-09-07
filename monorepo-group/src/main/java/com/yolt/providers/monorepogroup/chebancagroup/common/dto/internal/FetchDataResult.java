package com.yolt.providers.monorepogroup.chebancagroup.common.dto.internal;

import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.Balances;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.Transaction;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.util.*;

@EqualsAndHashCode
public class FetchDataResult {

    private final List<Account> accountsList;

    private final Map<TransactionKey, List<Transaction>> transactionsMap;
    private final Map<String, Balances> balancesMap;

    public FetchDataResult() {
        accountsList = new ArrayList<>();
        transactionsMap = new LinkedHashMap<>();
        balancesMap = new LinkedHashMap<>();
    }

    public void addTransactions(final Account account, final List<Transaction> bookedTransactions, final List<Transaction> pendingTransaction) {
        if (!accountsList.contains(account)) {
            accountsList.add(account);
        }
        TransactionKey keyForBookedTransactions = new TransactionKey(account.getAccountId(), Type.BOOKED);
        List<Transaction> bookedTransactionsList = transactionsMap.get(keyForBookedTransactions);
        if (CollectionUtils.isEmpty(bookedTransactionsList)) {
            bookedTransactionsList = new ArrayList<>();
        }
        bookedTransactionsList.addAll(bookedTransactions);

        TransactionKey keyForPendingTransactions = new TransactionKey(account.getAccountId(), Type.PENDING);
        List<Transaction> pendingTransactionsList = transactionsMap.get(keyForPendingTransactions);
        if (CollectionUtils.isEmpty(pendingTransactionsList)) {
            pendingTransactionsList = new ArrayList<>();
        }
        pendingTransactionsList.addAll(pendingTransaction);

        transactionsMap.put(keyForBookedTransactions, bookedTransactionsList);
        transactionsMap.put(keyForPendingTransactions, pendingTransactionsList);
    }

    public void addBalance(final Account account, final Balances accountBalance) {
        if (!accountsList.contains(account)) {
            accountsList.add(account);
        }
        String balanceKey = account.getAccountId();
        balancesMap.put(balanceKey, accountBalance);
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

    public Balances getBalances(final String accountId) {
        return balancesMap.get(accountId);
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
