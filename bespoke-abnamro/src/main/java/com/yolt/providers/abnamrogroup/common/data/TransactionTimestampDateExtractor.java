package com.yolt.providers.abnamrogroup.common.data;

import com.yolt.providers.abnamro.dto.TransactionResponseTransactions;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * New temporary implementation for transaction date field mapping according to ticket C4PO-5385.
 * It uses {@link TransactionResponseTransactions#getTransactionTimestamp()} which returns
 * datetime as string in pattern: yyyy-MM-dd-HH:mm:ss.SSS.
 * Amsterdam Timezone info is applied on parsed datetime in the end.
 */
public class TransactionTimestampDateExtractor implements TransactionDateExtractor {

    public static final String TRANSACTION_TIMESTAMP_PATTERN = "yyyy-MM-dd-HH:mm:ss.SSS";
    public static final ZoneId AMSTERDAM_TIMEZONE = ZoneId.of("Europe/Amsterdam");

    @Override
    public ZonedDateTime extractTransactionDate(TransactionResponseTransactions transaction) {
        return LocalDateTime.parse(transaction.getTransactionTimestamp(), DateTimeFormatter.ofPattern(TRANSACTION_TIMESTAMP_PATTERN)).atZone(AMSTERDAM_TIMEZONE);
    }
}
