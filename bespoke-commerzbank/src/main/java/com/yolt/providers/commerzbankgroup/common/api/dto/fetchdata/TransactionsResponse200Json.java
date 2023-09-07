package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Body of the JSON response for a successful read transaction list request. This account report contains transactions resulting from the query parameters. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class TransactionsResponse200Json   {

  @JsonProperty("account")
  private AccountReference account;

  @JsonProperty("transactions")
  private AccountReport transactions;

  @JsonProperty("balances")
  @Valid
  private List<Balance> balances = null;

  @JsonProperty("_links")
  private LinksDownload links;

  public TransactionsResponse200Json account(AccountReference account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
  */
  @Valid 
  public AccountReference getAccount() {
    return account;
  }

  public void setAccount(AccountReference account) {
    this.account = account;
  }

  public TransactionsResponse200Json transactions(AccountReport transactions) {
    this.transactions = transactions;
    return this;
  }

  /**
   * Get transactions
   * @return transactions
  */
  @Valid 
  public AccountReport getTransactions() {
    return transactions;
  }

  public void setTransactions(AccountReport transactions) {
    this.transactions = transactions;
  }

  public TransactionsResponse200Json balances(List<Balance> balances) {
    this.balances = balances;
    return this;
  }

  public TransactionsResponse200Json addBalancesItem(Balance balancesItem) {
    if (this.balances == null) {
      this.balances = new ArrayList<>();
    }
    this.balances.add(balancesItem);
    return this;
  }

  /**
   * A list of balances regarding this account, e.g. the current balance, the last booked balance. The list might be restricted to the current balance. 
   * @return balances
  */
  @Valid 
  public List<Balance> getBalances() {
    return balances;
  }

  public void setBalances(List<Balance> balances) {
    this.balances = balances;
  }

  public TransactionsResponse200Json links(LinksDownload links) {
    this.links = links;
    return this;
  }

  /**
   * Get links
   * @return links
  */
  @Valid 
  public LinksDownload getLinks() {
    return links;
  }

  public void setLinks(LinksDownload links) {
    this.links = links;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransactionsResponse200Json transactionsResponse200Json = (TransactionsResponse200Json) o;
    return Objects.equals(this.account, transactionsResponse200Json.account) &&
        Objects.equals(this.transactions, transactionsResponse200Json.transactions) &&
        Objects.equals(this.balances, transactionsResponse200Json.balances) &&
        Objects.equals(this.links, transactionsResponse200Json.links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(account, transactions, balances, links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionsResponse200Json {\n");
    sb.append("    account: ").append(toIndentedString(account)).append("\n");
    sb.append("    transactions: ").append(toIndentedString(transactions)).append("\n");
    sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

