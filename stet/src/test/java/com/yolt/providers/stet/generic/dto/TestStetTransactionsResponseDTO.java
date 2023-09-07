package com.yolt.providers.stet.generic.dto;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionsResponseDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TestStetTransactionsResponseDTO implements StetTransactionsResponseDTO {

    List<StetTransactionDTO> transactions;
    private PaginationDTO links;
}
