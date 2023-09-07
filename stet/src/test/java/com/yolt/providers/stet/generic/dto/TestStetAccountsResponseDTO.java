package com.yolt.providers.stet.generic.dto;

import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountsResponseDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TestStetAccountsResponseDTO implements StetAccountsResponseDTO {

    List<StetAccountDTO> accounts;
    private PaginationDTO links;
}
