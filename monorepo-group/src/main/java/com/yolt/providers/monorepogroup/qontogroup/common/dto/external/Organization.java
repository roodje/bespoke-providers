package com.yolt.providers.monorepogroup.qontogroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface Organization {

    @JsonPath("$.organization.bank_accounts")
    List<Account> getAccounts();

}
