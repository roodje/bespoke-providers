package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * JSON Request body for the \&quot;Confirmation of funds service\&quot;.  &lt;table&gt;  &lt;tr&gt;    &lt;td&gt;cardNumber&lt;/td&gt;    &lt;td&gt;String &lt;/td&gt;   &lt;td&gt;Optional&lt;/td&gt;   &lt;td&gt;Card Number of the card issued by the PIISP. Should be delivered if available.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;   &lt;td&gt;account&lt;/td&gt;   &lt;td&gt; Account Reference&lt;/td&gt;   &lt;td&gt;Mandatory&lt;/td&gt;   &lt;td&gt;PSU&#39;s account number.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;    &lt;td&gt;payee&lt;/td&gt;   &lt;td&gt;Max70Text&lt;/td&gt;   &lt;td&gt;Optional&lt;/td&gt;   &lt;td&gt;The merchant where the card is accepted as an information to the PSU.&lt;/td&gt; &lt;/tr&gt;  &lt;tr&gt;   &lt;td&gt;instructedAmount&lt;/td&gt;   &lt;td&gt;Amount&lt;/td&gt;   &lt;td&gt;Mandatory&lt;/td&gt;   &lt;td&gt;Transaction amount to be checked within the funds check mechanism.&lt;/td&gt; &lt;/tr&gt;  &lt;/table&gt; 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class ConfirmationOfFunds   {

  @JsonProperty("cardNumber")
  private String cardNumber;

  @JsonProperty("account")
  private AccountReference account;

  @JsonProperty("payee")
  private String payee;

  @JsonProperty("instructedAmount")
  private Amount instructedAmount;

  public ConfirmationOfFunds cardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
    return this;
  }

  /**
   * Card Number of the card issued by the PIISP.  Should be delivered if available. 
   * @return cardNumber
  */
  @Size(max = 35) 
  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  public ConfirmationOfFunds account(AccountReference account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
  */
  @NotNull @Valid 
  public AccountReference getAccount() {
    return account;
  }

  public void setAccount(AccountReference account) {
    this.account = account;
  }

  public ConfirmationOfFunds payee(String payee) {
    this.payee = payee;
    return this;
  }

  /**
   * Name payee.
   * @return payee
  */
  @Size(max = 70) 
  public String getPayee() {
    return payee;
  }

  public void setPayee(String payee) {
    this.payee = payee;
  }

  public ConfirmationOfFunds instructedAmount(Amount instructedAmount) {
    this.instructedAmount = instructedAmount;
    return this;
  }

  /**
   * Get instructedAmount
   * @return instructedAmount
  */
  @NotNull @Valid 
  public Amount getInstructedAmount() {
    return instructedAmount;
  }

  public void setInstructedAmount(Amount instructedAmount) {
    this.instructedAmount = instructedAmount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConfirmationOfFunds confirmationOfFunds = (ConfirmationOfFunds) o;
    return Objects.equals(this.cardNumber, confirmationOfFunds.cardNumber) &&
        Objects.equals(this.account, confirmationOfFunds.account) &&
        Objects.equals(this.payee, confirmationOfFunds.payee) &&
        Objects.equals(this.instructedAmount, confirmationOfFunds.instructedAmount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardNumber, account, payee, instructedAmount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConfirmationOfFunds {\n");
    sb.append("    cardNumber: ").append(toIndentedString(cardNumber)).append("\n");
    sb.append("    account: ").append(toIndentedString(account)).append("\n");
    sb.append("    payee: ").append(toIndentedString(payee)).append("\n");
    sb.append("    instructedAmount: ").append(toIndentedString(instructedAmount)).append("\n");
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

