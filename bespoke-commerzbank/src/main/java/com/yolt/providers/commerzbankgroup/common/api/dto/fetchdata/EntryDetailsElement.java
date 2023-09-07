package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * EntryDetailsElement
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class EntryDetailsElement   {

  @JsonProperty("endToEndId")
  private String endToEndId;

  @JsonProperty("mandateId")
  private String mandateId;

  @JsonProperty("checkId")
  private String checkId;

  @JsonProperty("creditorId")
  private String creditorId;

  @JsonProperty("transactionAmount")
  private Amount transactionAmount;

  @JsonProperty("currencyExchange")
  @Valid
  private List<ReportExchangeRate> currencyExchange = null;

  @JsonProperty("creditorName")
  private String creditorName;

  @JsonProperty("creditorAccount")
  private AccountReference creditorAccount;

  @JsonProperty("creditorAgent")
  private String creditorAgent;

  @JsonProperty("ultimateCreditor")
  private String ultimateCreditor;

  @JsonProperty("debtorName")
  private String debtorName;

  @JsonProperty("debtorAccount")
  private AccountReference debtorAccount;

  @JsonProperty("debtorAgent")
  private String debtorAgent;

  @JsonProperty("ultimateDebtor")
  private String ultimateDebtor;

  @JsonProperty("remittanceInformationUnstructured")
  private String remittanceInformationUnstructured;

  @JsonProperty("remittanceInformationUnstructuredArray")
  @Valid
  private List<String> remittanceInformationUnstructuredArray = null;

  @JsonProperty("remittanceInformationStructured")
  private RemittanceInformationStructured remittanceInformationStructured;

  @JsonProperty("remittanceInformationStructuredArray")
  @Valid
  private List<RemittanceInformationStructured> remittanceInformationStructuredArray = null;

  @JsonProperty("purposeCode")
  private PurposeCode purposeCode;

  public EntryDetailsElement endToEndId(String endToEndId) {
    this.endToEndId = endToEndId;
    return this;
  }

  /**
   * Unique end to end identity.
   * @return endToEndId
  */
  @Size(max = 35) 
  public String getEndToEndId() {
    return endToEndId;
  }

  public void setEndToEndId(String endToEndId) {
    this.endToEndId = endToEndId;
  }

  public EntryDetailsElement mandateId(String mandateId) {
    this.mandateId = mandateId;
    return this;
  }

  /**
   * Identification of Mandates, e.g. a SEPA Mandate ID.
   * @return mandateId
  */
  @Size(max = 35) 
  public String getMandateId() {
    return mandateId;
  }

  public void setMandateId(String mandateId) {
    this.mandateId = mandateId;
  }

  public EntryDetailsElement checkId(String checkId) {
    this.checkId = checkId;
    return this;
  }

  /**
   * Identification of a Cheque.
   * @return checkId
  */
  @Size(max = 35) 
  public String getCheckId() {
    return checkId;
  }

  public void setCheckId(String checkId) {
    this.checkId = checkId;
  }

  public EntryDetailsElement creditorId(String creditorId) {
    this.creditorId = creditorId;
    return this;
  }

  /**
   * Identification of Creditors, e.g. a SEPA Creditor ID.
   * @return creditorId
  */
  @Size(max = 35) 
  public String getCreditorId() {
    return creditorId;
  }

  public void setCreditorId(String creditorId) {
    this.creditorId = creditorId;
  }

  public EntryDetailsElement transactionAmount(Amount transactionAmount) {
    this.transactionAmount = transactionAmount;
    return this;
  }

  /**
   * Get transactionAmount
   * @return transactionAmount
  */
  @NotNull @Valid 
  public Amount getTransactionAmount() {
    return transactionAmount;
  }

  public void setTransactionAmount(Amount transactionAmount) {
    this.transactionAmount = transactionAmount;
  }

  public EntryDetailsElement currencyExchange(List<ReportExchangeRate> currencyExchange) {
    this.currencyExchange = currencyExchange;
    return this;
  }

  public EntryDetailsElement addCurrencyExchangeItem(ReportExchangeRate currencyExchangeItem) {
    if (this.currencyExchange == null) {
      this.currencyExchange = new ArrayList<>();
    }
    this.currencyExchange.add(currencyExchangeItem);
    return this;
  }

  /**
   * Array of exchange rates.
   * @return currencyExchange
  */
  @Valid 
  public List<ReportExchangeRate> getCurrencyExchange() {
    return currencyExchange;
  }

  public void setCurrencyExchange(List<ReportExchangeRate> currencyExchange) {
    this.currencyExchange = currencyExchange;
  }

  public EntryDetailsElement creditorName(String creditorName) {
    this.creditorName = creditorName;
    return this;
  }

  /**
   * Creditor name.
   * @return creditorName
  */
  @Size(max = 70) 
  public String getCreditorName() {
    return creditorName;
  }

  public void setCreditorName(String creditorName) {
    this.creditorName = creditorName;
  }

  public EntryDetailsElement creditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
    return this;
  }

  /**
   * Get creditorAccount
   * @return creditorAccount
  */
  @Valid 
  public AccountReference getCreditorAccount() {
    return creditorAccount;
  }

  public void setCreditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
  }

  public EntryDetailsElement creditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
    return this;
  }

  /**
   * BICFI 
   * @return creditorAgent
  */
  @Pattern(regexp = "[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}") 
  public String getCreditorAgent() {
    return creditorAgent;
  }

  public void setCreditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
  }

  public EntryDetailsElement ultimateCreditor(String ultimateCreditor) {
    this.ultimateCreditor = ultimateCreditor;
    return this;
  }

  /**
   * Ultimate creditor.
   * @return ultimateCreditor
  */
  @Size(max = 70) 
  public String getUltimateCreditor() {
    return ultimateCreditor;
  }

  public void setUltimateCreditor(String ultimateCreditor) {
    this.ultimateCreditor = ultimateCreditor;
  }

  public EntryDetailsElement debtorName(String debtorName) {
    this.debtorName = debtorName;
    return this;
  }

  /**
   * Debtor name.
   * @return debtorName
  */
  @Size(max = 70) 
  public String getDebtorName() {
    return debtorName;
  }

  public void setDebtorName(String debtorName) {
    this.debtorName = debtorName;
  }

  public EntryDetailsElement debtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
    return this;
  }

  /**
   * Get debtorAccount
   * @return debtorAccount
  */
  @Valid 
  public AccountReference getDebtorAccount() {
    return debtorAccount;
  }

  public void setDebtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
  }

  public EntryDetailsElement debtorAgent(String debtorAgent) {
    this.debtorAgent = debtorAgent;
    return this;
  }

  /**
   * BICFI 
   * @return debtorAgent
  */
  @Pattern(regexp = "[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}") 
  public String getDebtorAgent() {
    return debtorAgent;
  }

  public void setDebtorAgent(String debtorAgent) {
    this.debtorAgent = debtorAgent;
  }

  public EntryDetailsElement ultimateDebtor(String ultimateDebtor) {
    this.ultimateDebtor = ultimateDebtor;
    return this;
  }

  /**
   * Ultimate debtor.
   * @return ultimateDebtor
  */
  @Size(max = 70) 
  public String getUltimateDebtor() {
    return ultimateDebtor;
  }

  public void setUltimateDebtor(String ultimateDebtor) {
    this.ultimateDebtor = ultimateDebtor;
  }

  public EntryDetailsElement remittanceInformationUnstructured(String remittanceInformationUnstructured) {
    this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    return this;
  }

  /**
   * Unstructured remittance information. 
   * @return remittanceInformationUnstructured
  */
  @Size(max = 140) 
  public String getRemittanceInformationUnstructured() {
    return remittanceInformationUnstructured;
  }

  public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
    this.remittanceInformationUnstructured = remittanceInformationUnstructured;
  }

  public EntryDetailsElement remittanceInformationUnstructuredArray(List<String> remittanceInformationUnstructuredArray) {
    this.remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray;
    return this;
  }

  public EntryDetailsElement addRemittanceInformationUnstructuredArrayItem(String remittanceInformationUnstructuredArrayItem) {
    if (this.remittanceInformationUnstructuredArray == null) {
      this.remittanceInformationUnstructuredArray = new ArrayList<>();
    }
    this.remittanceInformationUnstructuredArray.add(remittanceInformationUnstructuredArrayItem);
    return this;
  }

  /**
   * Array of unstructured remittance information. 
   * @return remittanceInformationUnstructuredArray
  */
  
  public List<String> getRemittanceInformationUnstructuredArray() {
    return remittanceInformationUnstructuredArray;
  }

  public void setRemittanceInformationUnstructuredArray(List<String> remittanceInformationUnstructuredArray) {
    this.remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray;
  }

  public EntryDetailsElement remittanceInformationStructured(RemittanceInformationStructured remittanceInformationStructured) {
    this.remittanceInformationStructured = remittanceInformationStructured;
    return this;
  }

  /**
   * Get remittanceInformationStructured
   * @return remittanceInformationStructured
  */
  @Valid 
  public RemittanceInformationStructured getRemittanceInformationStructured() {
    return remittanceInformationStructured;
  }

  public void setRemittanceInformationStructured(RemittanceInformationStructured remittanceInformationStructured) {
    this.remittanceInformationStructured = remittanceInformationStructured;
  }

  public EntryDetailsElement remittanceInformationStructuredArray(List<RemittanceInformationStructured> remittanceInformationStructuredArray) {
    this.remittanceInformationStructuredArray = remittanceInformationStructuredArray;
    return this;
  }

  public EntryDetailsElement addRemittanceInformationStructuredArrayItem(RemittanceInformationStructured remittanceInformationStructuredArrayItem) {
    if (this.remittanceInformationStructuredArray == null) {
      this.remittanceInformationStructuredArray = new ArrayList<>();
    }
    this.remittanceInformationStructuredArray.add(remittanceInformationStructuredArrayItem);
    return this;
  }

  /**
   * Array of structured remittance information. 
   * @return remittanceInformationStructuredArray
  */
  @Valid 
  public List<RemittanceInformationStructured> getRemittanceInformationStructuredArray() {
    return remittanceInformationStructuredArray;
  }

  public void setRemittanceInformationStructuredArray(List<RemittanceInformationStructured> remittanceInformationStructuredArray) {
    this.remittanceInformationStructuredArray = remittanceInformationStructuredArray;
  }

  public EntryDetailsElement purposeCode(PurposeCode purposeCode) {
    this.purposeCode = purposeCode;
    return this;
  }

  /**
   * Get purposeCode
   * @return purposeCode
  */
  @Valid 
  public PurposeCode getPurposeCode() {
    return purposeCode;
  }

  public void setPurposeCode(PurposeCode purposeCode) {
    this.purposeCode = purposeCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EntryDetailsElement entryDetailsElement = (EntryDetailsElement) o;
    return Objects.equals(this.endToEndId, entryDetailsElement.endToEndId) &&
        Objects.equals(this.mandateId, entryDetailsElement.mandateId) &&
        Objects.equals(this.checkId, entryDetailsElement.checkId) &&
        Objects.equals(this.creditorId, entryDetailsElement.creditorId) &&
        Objects.equals(this.transactionAmount, entryDetailsElement.transactionAmount) &&
        Objects.equals(this.currencyExchange, entryDetailsElement.currencyExchange) &&
        Objects.equals(this.creditorName, entryDetailsElement.creditorName) &&
        Objects.equals(this.creditorAccount, entryDetailsElement.creditorAccount) &&
        Objects.equals(this.creditorAgent, entryDetailsElement.creditorAgent) &&
        Objects.equals(this.ultimateCreditor, entryDetailsElement.ultimateCreditor) &&
        Objects.equals(this.debtorName, entryDetailsElement.debtorName) &&
        Objects.equals(this.debtorAccount, entryDetailsElement.debtorAccount) &&
        Objects.equals(this.debtorAgent, entryDetailsElement.debtorAgent) &&
        Objects.equals(this.ultimateDebtor, entryDetailsElement.ultimateDebtor) &&
        Objects.equals(this.remittanceInformationUnstructured, entryDetailsElement.remittanceInformationUnstructured) &&
        Objects.equals(this.remittanceInformationUnstructuredArray, entryDetailsElement.remittanceInformationUnstructuredArray) &&
        Objects.equals(this.remittanceInformationStructured, entryDetailsElement.remittanceInformationStructured) &&
        Objects.equals(this.remittanceInformationStructuredArray, entryDetailsElement.remittanceInformationStructuredArray) &&
        Objects.equals(this.purposeCode, entryDetailsElement.purposeCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(endToEndId, mandateId, checkId, creditorId, transactionAmount, currencyExchange, creditorName, creditorAccount, creditorAgent, ultimateCreditor, debtorName, debtorAccount, debtorAgent, ultimateDebtor, remittanceInformationUnstructured, remittanceInformationUnstructuredArray, remittanceInformationStructured, remittanceInformationStructuredArray, purposeCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EntryDetailsElement {\n");
    sb.append("    endToEndId: ").append(toIndentedString(endToEndId)).append("\n");
    sb.append("    mandateId: ").append(toIndentedString(mandateId)).append("\n");
    sb.append("    checkId: ").append(toIndentedString(checkId)).append("\n");
    sb.append("    creditorId: ").append(toIndentedString(creditorId)).append("\n");
    sb.append("    transactionAmount: ").append(toIndentedString(transactionAmount)).append("\n");
    sb.append("    currencyExchange: ").append(toIndentedString(currencyExchange)).append("\n");
    sb.append("    creditorName: ").append(toIndentedString(creditorName)).append("\n");
    sb.append("    creditorAccount: ").append(toIndentedString(creditorAccount)).append("\n");
    sb.append("    creditorAgent: ").append(toIndentedString(creditorAgent)).append("\n");
    sb.append("    ultimateCreditor: ").append(toIndentedString(ultimateCreditor)).append("\n");
    sb.append("    debtorName: ").append(toIndentedString(debtorName)).append("\n");
    sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
    sb.append("    debtorAgent: ").append(toIndentedString(debtorAgent)).append("\n");
    sb.append("    ultimateDebtor: ").append(toIndentedString(ultimateDebtor)).append("\n");
    sb.append("    remittanceInformationUnstructured: ").append(toIndentedString(remittanceInformationUnstructured)).append("\n");
    sb.append("    remittanceInformationUnstructuredArray: ").append(toIndentedString(remittanceInformationUnstructuredArray)).append("\n");
    sb.append("    remittanceInformationStructured: ").append(toIndentedString(remittanceInformationStructured)).append("\n");
    sb.append("    remittanceInformationStructuredArray: ").append(toIndentedString(remittanceInformationStructuredArray)).append("\n");
    sb.append("    purposeCode: ").append(toIndentedString(purposeCode)).append("\n");
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

