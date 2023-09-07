package com.yolt.providers.commerzbankgroup.common.data.mapper;

import com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata.AccountDetails;
import com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata.ReadAccountBalanceResponse200;
import com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata.Transactions;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface CommerzbankGroupDataMapper {

    ProviderAccountDTO toProviderAccountDTO(AccountDetails account, List<Transactions> transactionsList, ReadAccountBalanceResponse200 readAccountBalanceResponse200);

    List<ProviderTransactionDTO> toProviderTransactionsDTOList(List<Transactions> list);
}
