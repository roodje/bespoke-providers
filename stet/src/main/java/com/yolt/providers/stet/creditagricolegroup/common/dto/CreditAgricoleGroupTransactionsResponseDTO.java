package com.yolt.providers.stet.creditagricolegroup.common.dto;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface CreditAgricoleGroupTransactionsResponseDTO extends StetTransactionsResponseDTO {

    @Override
    @JsonPath("$.transactions")
    List<CreditAgricoleGroupTransactionDTO> getTransactions();
}
