package com.yolt.providers.unicredit.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Value
@Builder(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentRequestDTO {

    private static final String VALID_UNTIL_DATE_FORMAT = "yyyy-MM-dd";

    private Access access;
    private boolean recurringIndicator;
    private String validUntil;
    private int frequencyPerDay;
    private boolean combinedServiceIndicator;

    public static ConsentRequestDTO createGlobalConsentRequest(final Date validUntil, final int frequencyPerDay, final boolean recurringIndicator) {
        return ConsentRequestDTO.builder()
                .access(Access.builder()
                        .allPsd2("allAccounts")
                        .build())
                .frequencyPerDay(frequencyPerDay)
                .validUntil(new SimpleDateFormat(VALID_UNTIL_DATE_FORMAT).format(validUntil))
                .recurringIndicator(recurringIndicator)
                .build();
    }

    public static ConsentRequestDTO createDetailedConsentRequest(final String iban, final Date validUntil, final int frequencyPerDay, final boolean recurringIndicator) {
        List<Account> psuAccounts = Collections.singletonList(new Account(iban));
        return ConsentRequestDTO.builder()
                .access(Access.builder()
                        .accounts(psuAccounts)
                        .balances(psuAccounts)
                        .transactions(psuAccounts)
                        .build())
                .frequencyPerDay(frequencyPerDay)
                .validUntil(new SimpleDateFormat(VALID_UNTIL_DATE_FORMAT).format(validUntil))
                .recurringIndicator(recurringIndicator)
                .combinedServiceIndicator(false)
                .build();
    }

    @Value
    @Builder(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Access {
        private String allPsd2;
        private List<Account> accounts;
        private List<Account> balances;
        private List<Account> transactions;
    }

    @Value
    public static class Account {
        private String iban;
    }
}
