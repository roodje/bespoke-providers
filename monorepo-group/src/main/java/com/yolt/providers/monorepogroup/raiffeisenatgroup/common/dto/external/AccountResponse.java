package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface AccountResponse {

    @JsonPath("$.accounts")
    List<Account> getAccounts();

}
