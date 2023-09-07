package com.yolt.providers.redsys.common.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import java.util.List;

/**
 * AccountAccess
 */
@Data
@Builder
public final class AccountAccess {
  @Valid
  private final List<AccountReference> accounts;

  @Valid
  private final List<AccountReference>  balances;

  @Valid
  private final List<AccountReference>  transactions;

  private final String availableAccounts;

  private final String availableAccountsWithBalance;

  private final String allPsd2;

}
