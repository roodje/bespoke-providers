package com.yolt.providers.stet.generic.dto.transaction;

import com.yolt.providers.stet.generic.dto.PaginationDTO;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface StetTransactionsResponseDTO {

    @JsonPath("$.transactions")
    List<? extends StetTransactionDTO> getTransactions(); //NOSONAR It enables to override JSON paths for mapping

    @JsonPath("$._links")
    PaginationDTO getLinks();
}
