package com.yolt.providers.redsys.common.dto;

import lombok.Data;

@Data
public class LinksAccountsTransactions {
    private LinkReference account;

    private LinkReference first;

    private LinkReference next;

    private LinkReference previous;

    private LinkReference last;
}
