package com.yolt.providers.unicredit.common.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface UniCreditAccountsDTO {

    @JsonPath("$.accounts")
    List<UniCreditAccountDTO> getAccounts();
}
