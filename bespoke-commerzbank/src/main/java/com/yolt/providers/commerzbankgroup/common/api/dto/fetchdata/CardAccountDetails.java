package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Card account details. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class CardAccountDetails   {

  @JsonProperty("resourceId")
  private String resourceId;

  @JsonProperty("maskedPan")
  private String maskedPan;

  @JsonProperty("currency")
  private String currency;

  @JsonProperty("ownerName")
  private String ownerName;

  @JsonProperty("name")
  private String name;

  @JsonProperty("displayName")
  private String displayName;

  @JsonProperty("product")
  private String product;

  @JsonProperty("debitAccounting")
  private Boolean debitAccounting;

  @JsonProperty("status")
  private AccountStatus status;

  /**
   * Specifies the usage of the account:   * PRIV: private personal account   * ORGA: professional account 
   */
  public enum UsageEnum {
    PRIV("PRIV"),
    
    ORGA("ORGA");

    private String value;

    UsageEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static UsageEnum fromValue(String value) {
      for (UsageEnum b : UsageEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @JsonProperty("usage")
  private UsageEnum usage;

  @JsonProperty("details")
  private String details;

  @JsonProperty("creditLimit")
  private Amount creditLimit;

  @JsonProperty("balances")
  @Valid
  private List<Balance> balances = null;

  @JsonProperty("_links")
  private LinksAccountDetails links;

  public CardAccountDetails resourceId(String resourceId) {
    this.resourceId = resourceId;
    return this;
  }

  /**
   * This is the data element to be used in the path when retrieving data from a dedicated account. This shall be filled, if addressable resource are created by the ASPSP on the /card-accounts endpoint. 
   * @return resourceId
  */
  
  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public CardAccountDetails maskedPan(String maskedPan) {
    this.maskedPan = maskedPan;
    return this;
  }

  /**
   * Masked Primary Account Number. 
   * @return maskedPan
  */
  @NotNull @Size(max = 35) 
  public String getMaskedPan() {
    return maskedPan;
  }

  public void setMaskedPan(String maskedPan) {
    this.maskedPan = maskedPan;
  }

  public CardAccountDetails currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * ISO 4217 Alpha 3 currency code. 
   * @return currency
  */
  @NotNull @Pattern(regexp = "[A-Z]{3}") 
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public CardAccountDetails ownerName(String ownerName) {
    this.ownerName = ownerName;
    return this;
  }

  /**
   * Name of the legal account owner.  If there is more than one owner, then e.g. two names might be noted here.  For a corporate account, the corporate name is used for this attribute. Even if supported by the ASPSP, the provision of this field might depend on the fact whether an explicit consent to this specific additional account information has been given by the PSU. 
   * @return ownerName
  */
  @Size(max = 140) 
  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  public CardAccountDetails name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Name of the account, as assigned by the ASPSP,  in agreement with the account owner in order to provide an additional means of identification of the account. 
   * @return name
  */
  @Size(max = 70) 
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CardAccountDetails displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * Name of the account as defined by the PSU within online channels. 
   * @return displayName
  */
  @Size(max = 70) 
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public CardAccountDetails product(String product) {
    this.product = product;
    return this;
  }

  /**
   * Product Name of the Bank for this account, proprietary definition. 
   * @return product
  */
  @Size(max = 35) 
  public String getProduct() {
    return product;
  }

  public void setProduct(String product) {
    this.product = product;
  }

  public CardAccountDetails debitAccounting(Boolean debitAccounting) {
    this.debitAccounting = debitAccounting;
    return this;
  }

  /**
   * If true, the amounts of debits on the reports are quoted positive with the related consequence for balances. If false, the amount of debits on the reports are quoted negative. 
   * @return debitAccounting
  */
  
  public Boolean getDebitAccounting() {
    return debitAccounting;
  }

  public void setDebitAccounting(Boolean debitAccounting) {
    this.debitAccounting = debitAccounting;
  }

  public CardAccountDetails status(AccountStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  */
  @Valid 
  public AccountStatus getStatus() {
    return status;
  }

  public void setStatus(AccountStatus status) {
    this.status = status;
  }

  public CardAccountDetails usage(UsageEnum usage) {
    this.usage = usage;
    return this;
  }

  /**
   * Specifies the usage of the account:   * PRIV: private personal account   * ORGA: professional account 
   * @return usage
  */
  @Size(max = 4) 
  public UsageEnum getUsage() {
    return usage;
  }

  public void setUsage(UsageEnum usage) {
    this.usage = usage;
  }

  public CardAccountDetails details(String details) {
    this.details = details;
    return this;
  }

  /**
   * Specifications that might be provided by the ASPSP:   - characteristics of the account   - characteristics of the relevant card 
   * @return details
  */
  @Size(max = 1000) 
  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public CardAccountDetails creditLimit(Amount creditLimit) {
    this.creditLimit = creditLimit;
    return this;
  }

  /**
   * Get creditLimit
   * @return creditLimit
  */
  @Valid 
  public Amount getCreditLimit() {
    return creditLimit;
  }

  public void setCreditLimit(Amount creditLimit) {
    this.creditLimit = creditLimit;
  }

  public CardAccountDetails balances(List<Balance> balances) {
    this.balances = balances;
    return this;
  }

  public CardAccountDetails addBalancesItem(Balance balancesItem) {
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

  public CardAccountDetails links(LinksAccountDetails links) {
    this.links = links;
    return this;
  }

  /**
   * Get links
   * @return links
  */
  @Valid 
  public LinksAccountDetails getLinks() {
    return links;
  }

  public void setLinks(LinksAccountDetails links) {
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
    CardAccountDetails cardAccountDetails = (CardAccountDetails) o;
    return Objects.equals(this.resourceId, cardAccountDetails.resourceId) &&
        Objects.equals(this.maskedPan, cardAccountDetails.maskedPan) &&
        Objects.equals(this.currency, cardAccountDetails.currency) &&
        Objects.equals(this.ownerName, cardAccountDetails.ownerName) &&
        Objects.equals(this.name, cardAccountDetails.name) &&
        Objects.equals(this.displayName, cardAccountDetails.displayName) &&
        Objects.equals(this.product, cardAccountDetails.product) &&
        Objects.equals(this.debitAccounting, cardAccountDetails.debitAccounting) &&
        Objects.equals(this.status, cardAccountDetails.status) &&
        Objects.equals(this.usage, cardAccountDetails.usage) &&
        Objects.equals(this.details, cardAccountDetails.details) &&
        Objects.equals(this.creditLimit, cardAccountDetails.creditLimit) &&
        Objects.equals(this.balances, cardAccountDetails.balances) &&
        Objects.equals(this.links, cardAccountDetails.links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resourceId, maskedPan, currency, ownerName, name, displayName, product, debitAccounting, status, usage, details, creditLimit, balances, links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardAccountDetails {\n");
    sb.append("    resourceId: ").append(toIndentedString(resourceId)).append("\n");
    sb.append("    maskedPan: ").append(toIndentedString(maskedPan)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    ownerName: ").append(toIndentedString(ownerName)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    product: ").append(toIndentedString(product)).append("\n");
    sb.append("    debitAccounting: ").append(toIndentedString(debitAccounting)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    usage: ").append(toIndentedString(usage)).append("\n");
    sb.append("    details: ").append(toIndentedString(details)).append("\n");
    sb.append("    creditLimit: ").append(toIndentedString(creditLimit)).append("\n");
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

