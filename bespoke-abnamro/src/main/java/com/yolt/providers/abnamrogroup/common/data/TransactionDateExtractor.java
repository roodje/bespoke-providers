package com.yolt.providers.abnamrogroup.common.data;

import com.yolt.providers.abnamro.dto.TransactionResponseTransactions;

import java.time.ZonedDateTime;

public interface TransactionDateExtractor {
    ZonedDateTime extractTransactionDate(TransactionResponseTransactions transaction);
}
