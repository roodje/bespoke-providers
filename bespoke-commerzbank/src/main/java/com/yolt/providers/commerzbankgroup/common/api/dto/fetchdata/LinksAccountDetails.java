package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Objects;

/**
 * Links to the account, which can be directly used for retrieving account information from this dedicated account.  Links to \&quot;balances\&quot; and/or \&quot;transactions\&quot;  These links are only supported, when the corresponding consent has been already granted. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class LinksAccountDetails extends HashMap<String, HrefType>  {

  @JsonProperty("balances")
  private HrefType balances;

  @JsonProperty("transactions")
  private HrefType transactions;

  public LinksAccountDetails balances(HrefType balances) {
    this.balances = balances;
    return this;
  }

  /**
   * Get balances
   * @return balances
  */
  @Valid 
  public HrefType getBalances() {
    return balances;
  }

  public void setBalances(HrefType balances) {
    this.balances = balances;
  }

  public LinksAccountDetails transactions(HrefType transactions) {
    this.transactions = transactions;
    return this;
  }

  /**
   * Get transactions
   * @return transactions
  */
  @Valid 
  public HrefType getTransactions() {
    return transactions;
  }

  public void setTransactions(HrefType transactions) {
    this.transactions = transactions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinksAccountDetails linksAccountDetails = (LinksAccountDetails) o;
    return Objects.equals(this.balances, linksAccountDetails.balances) &&
        Objects.equals(this.transactions, linksAccountDetails.transactions) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(balances, transactions, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksAccountDetails {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
    sb.append("    transactions: ").append(toIndentedString(transactions)).append("\n");
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

