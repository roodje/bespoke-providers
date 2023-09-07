package com.yolt.providers.ing.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestAccounts implements Accounts {

    private List<Account> data;

}
