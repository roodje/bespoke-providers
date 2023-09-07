package com.yolt.providers.abnamrogroup.common.data;

import com.yolt.providers.abnamro.dto.TransactionResponseTransactions;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * This is the previous implementation for transaction date field mapping used before
 * changes applied for C4PO-5385.
 * It simply uses {@link TransactionResponseTransactions#getBookDate()}
 * and clears time to start of a day according to the Amsterdam Timezone
 */
public class TransactionBookDateDateExtractor implements TransactionDateExtractor {

    public static final ZoneId AMSTERDAM_TIMEZONE = ZoneId.of("Europe/Amsterdam");

    @Override
    public ZonedDateTime extractTransactionDate(TransactionResponseTransactions transaction) {
        return transaction.getBookDate().atStartOfDay(AMSTERDAM_TIMEZONE);
    }
}
