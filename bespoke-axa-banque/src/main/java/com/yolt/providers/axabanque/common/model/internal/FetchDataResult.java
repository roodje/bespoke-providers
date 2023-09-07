package com.yolt.providers.axabanque.common.model.internal;

import lombok.Getter;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FetchDataResult {

    private final List<ProviderAccountDTO> responseAccounts = new ArrayList<>();

    public void addFetchedAccount(final ProviderAccountDTO account) {
        responseAccounts.add(account);
    }
}