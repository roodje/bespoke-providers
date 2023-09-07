package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Body of the JSON response for a successful get signing basket request.    * &#39;payments&#39;: payment initiations which shall be authorised through this signing basket.   * &#39;consents&#39;: consent objects which shall be authorised through this signing basket.   * &#39;transactionStatus&#39;: Only the codes RCVD, ACTC, RJCT are used.   * &#39;_links&#39;: The ASPSP might integrate hyperlinks to indicate next (authorisation) steps to be taken. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class SigningBasketResponse200   {

  @JsonProperty("payments")
  @Valid
  private List<String> payments = null;

  @JsonProperty("consents")
  @Valid
  private List<String> consents = null;

  @JsonProperty("transactionStatus")
  private TransactionStatusSBS transactionStatus;

  @JsonProperty("_links")
  private LinksSigningBasket links;

  public SigningBasketResponse200 payments(List<String> payments) {
    this.payments = payments;
    return this;
  }

  public SigningBasketResponse200 addPaymentsItem(String paymentsItem) {
    if (this.payments == null) {
      this.payments = new ArrayList<>();
    }
    this.payments.add(paymentsItem);
    return this;
  }

  /**
   * A list of paymentIds.
   * @return payments
  */
  @Size(min = 1) 
  public List<String> getPayments() {
    return payments;
  }

  public void setPayments(List<String> payments) {
    this.payments = payments;
  }

  public SigningBasketResponse200 consents(List<String> consents) {
    this.consents = consents;
    return this;
  }

  public SigningBasketResponse200 addConsentsItem(String consentsItem) {
    if (this.consents == null) {
      this.consents = new ArrayList<>();
    }
    this.consents.add(consentsItem);
    return this;
  }

  /**
   * A list of consentIds.
   * @return consents
  */
  @Size(min = 1) 
  public List<String> getConsents() {
    return consents;
  }

  public void setConsents(List<String> consents) {
    this.consents = consents;
  }

  public SigningBasketResponse200 transactionStatus(TransactionStatusSBS transactionStatus) {
    this.transactionStatus = transactionStatus;
    return this;
  }

  /**
   * Get transactionStatus
   * @return transactionStatus
  */
  @NotNull @Valid 
  public TransactionStatusSBS getTransactionStatus() {
    return transactionStatus;
  }

  public void setTransactionStatus(TransactionStatusSBS transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public SigningBasketResponse200 links(LinksSigningBasket links) {
    this.links = links;
    return this;
  }

  /**
   * Get links
   * @return links
  */
  @Valid 
  public LinksSigningBasket getLinks() {
    return links;
  }

  public void setLinks(LinksSigningBasket links) {
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
    SigningBasketResponse200 signingBasketResponse200 = (SigningBasketResponse200) o;
    return Objects.equals(this.payments, signingBasketResponse200.payments) &&
        Objects.equals(this.consents, signingBasketResponse200.consents) &&
        Objects.equals(this.transactionStatus, signingBasketResponse200.transactionStatus) &&
        Objects.equals(this.links, signingBasketResponse200.links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(payments, consents, transactionStatus, links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SigningBasketResponse200 {\n");
    sb.append("    payments: ").append(toIndentedString(payments)).append("\n");
    sb.append("    consents: ").append(toIndentedString(consents)).append("\n");
    sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
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

