package com.yolt.providers.stet.generic.dto.account;

import com.yolt.providers.stet.generic.dto.PaginationDTO;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface StetAccountsResponseDTO {

    @JsonPath("$.accounts")
    List<? extends StetAccountDTO> getAccounts(); //NOSONAR It enables to override JSON paths for mapping

    @JsonPath("$._links")
    PaginationDTO getLinks();
}
