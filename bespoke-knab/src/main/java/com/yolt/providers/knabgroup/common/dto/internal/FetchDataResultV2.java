package com.yolt.providers.knabgroup.common.dto.internal;

import lombok.Getter;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class FetchDataResultV2 {

    private final List<ProviderAccountDTO> responseAccounts = new ArrayList<>();

    public void addFetchedAccount(final ProviderAccountDTO account) {
        responseAccounts.add(account);
    }

}