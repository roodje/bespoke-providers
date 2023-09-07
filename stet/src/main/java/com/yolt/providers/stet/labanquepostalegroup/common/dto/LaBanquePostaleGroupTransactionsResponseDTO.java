package com.yolt.providers.stet.labanquepostalegroup.common.dto;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface LaBanquePostaleGroupTransactionsResponseDTO extends StetTransactionsResponseDTO {

    @Override
    @JsonPath("$.transactions")
    List<LaBanquePostaleGroupTransactionDTO> getTransactions();
}
