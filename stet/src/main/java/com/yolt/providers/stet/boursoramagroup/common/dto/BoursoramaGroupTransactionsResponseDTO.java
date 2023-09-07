package com.yolt.providers.stet.boursoramagroup.common.dto;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface BoursoramaGroupTransactionsResponseDTO extends StetTransactionsResponseDTO {

    @Override
    @JsonPath("$.transactions")
    List<BoursoramaGroupTransactionDTO> getTransactions();
}
