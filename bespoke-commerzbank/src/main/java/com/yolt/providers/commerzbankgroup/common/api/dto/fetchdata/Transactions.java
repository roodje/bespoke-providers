package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Transaction details.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class Transactions   {

  @JsonProperty("transactionId")
  private String transactionId;

  @JsonProperty("entryReference")
  private String entryReference;

  @JsonProperty("endToEndId")
  private String endToEndId;

  @JsonProperty("batchIndicator")
  private Boolean batchIndicator;

  @JsonProperty("batchNumberOfTransactions")
  private Integer batchNumberOfTransactions;

  @JsonProperty("mandateId")
  private String mandateId;

  @JsonProperty("checkId")
  private String checkId;

  @JsonProperty("creditorId")
  private String creditorId;

  @JsonProperty("bookingDate")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate bookingDate;

  @JsonProperty("valueDate")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate valueDate;

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
  private String remittanceInformationStructured;

  @JsonProperty("remittanceInformationStructuredArray")
  @Valid
  private List<RemittanceInformationStructured> remittanceInformationStructuredArray = null;

  @JsonProperty("entryDetails")
  @Valid
  private List<EntryDetailsElement> entryDetails = null;

  @JsonProperty("additionalInformation")
  private String additionalInformation;

  @JsonProperty("additionalInformationStructured")
  private AdditionalInformationStructured additionalInformationStructured;

  @JsonProperty("purposeCode")
  private PurposeCode purposeCode;

  @JsonProperty("bankTransactionCode")
  private String bankTransactionCode;

  @JsonProperty("proprietaryBankTransactionCode")
  private String proprietaryBankTransactionCode;

  @JsonProperty("balanceAfterTransaction")
  private Balance balanceAfterTransaction;

  @JsonProperty("_links")
  private LinksTransactionDetails links;

  public Transactions transactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  /**
   * This identification is given by the attribute transactionId of the corresponding entry of a transaction list. 
   * @return transactionId
  */
  
  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public Transactions entryReference(String entryReference) {
    this.entryReference = entryReference;
    return this;
  }

  /**
   * Is the identification of the transaction as used e.g. for reference for deltafunction on application level. The same identification as for example used within camt.05x messages. 
   * @return entryReference
  */
  @Size(max = 35) 
  public String getEntryReference() {
    return entryReference;
  }

  public void setEntryReference(String entryReference) {
    this.entryReference = entryReference;
  }

  public Transactions endToEndId(String endToEndId) {
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

  public Transactions batchIndicator(Boolean batchIndicator) {
    this.batchIndicator = batchIndicator;
    return this;
  }

  /**
   * If this indicator equals true, then the related entry is a batch entry. 
   * @return batchIndicator
  */
  
  public Boolean getBatchIndicator() {
    return batchIndicator;
  }

  public void setBatchIndicator(Boolean batchIndicator) {
    this.batchIndicator = batchIndicator;
  }

  public Transactions batchNumberOfTransactions(Integer batchNumberOfTransactions) {
    this.batchNumberOfTransactions = batchNumberOfTransactions;
    return this;
  }

  /**
   * Shall be used if and only if the batchIndicator is contained and equals true. 
   * @return batchNumberOfTransactions
  */
  
  public Integer getBatchNumberOfTransactions() {
    return batchNumberOfTransactions;
  }

  public void setBatchNumberOfTransactions(Integer batchNumberOfTransactions) {
    this.batchNumberOfTransactions = batchNumberOfTransactions;
  }

  public Transactions mandateId(String mandateId) {
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

  public Transactions checkId(String checkId) {
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

  public Transactions creditorId(String creditorId) {
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

  public Transactions bookingDate(LocalDate bookingDate) {
    this.bookingDate = bookingDate;
    return this;
  }

  /**
   * The date when an entry is posted to an account on the ASPSPs books. 
   * @return bookingDate
  */
  @Valid 
  public LocalDate getBookingDate() {
    return bookingDate;
  }

  public void setBookingDate(LocalDate bookingDate) {
    this.bookingDate = bookingDate;
  }

  public Transactions valueDate(LocalDate valueDate) {
    this.valueDate = valueDate;
    return this;
  }

  /**
   * The Date at which assets become available to the account owner in case of a credit, or cease to be available to the account owner in case of a debit entry. **Usage:** If entry status is pending and value date is present, then the value date refers to an expected/requested value date.
   * @return valueDate
  */
  @Valid 
  public LocalDate getValueDate() {
    return valueDate;
  }

  public void setValueDate(LocalDate valueDate) {
    this.valueDate = valueDate;
  }

  public Transactions transactionAmount(Amount transactionAmount) {
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

  public Transactions currencyExchange(List<ReportExchangeRate> currencyExchange) {
    this.currencyExchange = currencyExchange;
    return this;
  }

  public Transactions addCurrencyExchangeItem(ReportExchangeRate currencyExchangeItem) {
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

  public Transactions creditorName(String creditorName) {
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

  public Transactions creditorAccount(AccountReference creditorAccount) {
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

  public Transactions creditorAgent(String creditorAgent) {
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

  public Transactions ultimateCreditor(String ultimateCreditor) {
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

  public Transactions debtorName(String debtorName) {
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

  public Transactions debtorAccount(AccountReference debtorAccount) {
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

  public Transactions debtorAgent(String debtorAgent) {
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

  public Transactions ultimateDebtor(String ultimateDebtor) {
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

  public Transactions remittanceInformationUnstructured(String remittanceInformationUnstructured) {
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

  public Transactions remittanceInformationUnstructuredArray(List<String> remittanceInformationUnstructuredArray) {
    this.remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray;
    return this;
  }

  public Transactions addRemittanceInformationUnstructuredArrayItem(String remittanceInformationUnstructuredArrayItem) {
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

  public Transactions remittanceInformationStructured(String remittanceInformationStructured) {
    this.remittanceInformationStructured = remittanceInformationStructured;
    return this;
  }

  /**
   * Structured remittance information Max 
   * @return remittanceInformationStructured
  */
  @Size(max = 140) 
  public String getRemittanceInformationStructured() {
    return remittanceInformationStructured;
  }

  public void setRemittanceInformationStructured(String remittanceInformationStructured) {
    this.remittanceInformationStructured = remittanceInformationStructured;
  }

  public Transactions remittanceInformationStructuredArray(List<RemittanceInformationStructured> remittanceInformationStructuredArray) {
    this.remittanceInformationStructuredArray = remittanceInformationStructuredArray;
    return this;
  }

  public Transactions addRemittanceInformationStructuredArrayItem(RemittanceInformationStructured remittanceInformationStructuredArrayItem) {
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

  public Transactions entryDetails(List<EntryDetailsElement> entryDetails) {
    this.entryDetails = entryDetails;
    return this;
  }

  public Transactions addEntryDetailsItem(EntryDetailsElement entryDetailsItem) {
    if (this.entryDetails == null) {
      this.entryDetails = new ArrayList<>();
    }
    this.entryDetails.add(entryDetailsItem);
    return this;
  }

  /**
   * Might be used by the ASPSP to transport details about transactions within a batch. 
   * @return entryDetails
  */
  @Valid 
  public List<EntryDetailsElement> getEntryDetails() {
    return entryDetails;
  }

  public void setEntryDetails(List<EntryDetailsElement> entryDetails) {
    this.entryDetails = entryDetails;
  }

  public Transactions additionalInformation(String additionalInformation) {
    this.additionalInformation = additionalInformation;
    return this;
  }

  /**
   * Might be used by the ASPSP to transport additional transaction related information to the PSU 
   * @return additionalInformation
  */
  @Size(max = 500) 
  public String getAdditionalInformation() {
    return additionalInformation;
  }

  public void setAdditionalInformation(String additionalInformation) {
    this.additionalInformation = additionalInformation;
  }

  public Transactions additionalInformationStructured(AdditionalInformationStructured additionalInformationStructured) {
    this.additionalInformationStructured = additionalInformationStructured;
    return this;
  }

  /**
   * Get additionalInformationStructured
   * @return additionalInformationStructured
  */
  @Valid 
  public AdditionalInformationStructured getAdditionalInformationStructured() {
    return additionalInformationStructured;
  }

  public void setAdditionalInformationStructured(AdditionalInformationStructured additionalInformationStructured) {
    this.additionalInformationStructured = additionalInformationStructured;
  }

  public Transactions purposeCode(PurposeCode purposeCode) {
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

  public Transactions bankTransactionCode(String bankTransactionCode) {
    this.bankTransactionCode = bankTransactionCode;
    return this;
  }

  /**
   * Bank transaction code as used by the ASPSP and using the sub elements of this structured code defined by ISO 20022.   This code type is concatenating the three ISO20022 Codes    * Domain Code,    * Family Code, and    * SubFamiliy Code  by hyphens, resulting in 'DomainCode'-'FamilyCode'-'SubFamilyCode'. For standing order reports the following codes are applicable:   * \"PMNT-ICDT-STDO\" for credit transfers,   * \"PMNT-IRCT-STDO\"  for instant credit transfers   * \"PMNT-ICDT-XBST\" for cross-border credit transfers   * \"PMNT-IRCT-XBST\" for cross-border real time credit transfers and   * \"PMNT-MCOP-OTHR\" for specific standing orders which have a dynamical amount to move left funds e.g. on month end to a saving account 
   * @return bankTransactionCode
  */
  
  public String getBankTransactionCode() {
    return bankTransactionCode;
  }

  public void setBankTransactionCode(String bankTransactionCode) {
    this.bankTransactionCode = bankTransactionCode;
  }

  public Transactions proprietaryBankTransactionCode(String proprietaryBankTransactionCode) {
    this.proprietaryBankTransactionCode = proprietaryBankTransactionCode;
    return this;
  }

  /**
   * Proprietary bank transaction code as used within a community or within an ASPSP e.g.  for MT94x based transaction reports. 
   * @return proprietaryBankTransactionCode
  */
  @Size(max = 35) 
  public String getProprietaryBankTransactionCode() {
    return proprietaryBankTransactionCode;
  }

  public void setProprietaryBankTransactionCode(String proprietaryBankTransactionCode) {
    this.proprietaryBankTransactionCode = proprietaryBankTransactionCode;
  }

  public Transactions balanceAfterTransaction(Balance balanceAfterTransaction) {
    this.balanceAfterTransaction = balanceAfterTransaction;
    return this;
  }

  /**
   * Get balanceAfterTransaction
   * @return balanceAfterTransaction
  */
  @Valid 
  public Balance getBalanceAfterTransaction() {
    return balanceAfterTransaction;
  }

  public void setBalanceAfterTransaction(Balance balanceAfterTransaction) {
    this.balanceAfterTransaction = balanceAfterTransaction;
  }

  public Transactions links(LinksTransactionDetails links) {
    this.links = links;
    return this;
  }

  /**
   * Get links
   * @return links
  */
  @Valid 
  public LinksTransactionDetails getLinks() {
    return links;
  }

  public void setLinks(LinksTransactionDetails links) {
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
    Transactions transactions = (Transactions) o;
    return Objects.equals(this.transactionId, transactions.transactionId) &&
        Objects.equals(this.entryReference, transactions.entryReference) &&
        Objects.equals(this.endToEndId, transactions.endToEndId) &&
        Objects.equals(this.batchIndicator, transactions.batchIndicator) &&
        Objects.equals(this.batchNumberOfTransactions, transactions.batchNumberOfTransactions) &&
        Objects.equals(this.mandateId, transactions.mandateId) &&
        Objects.equals(this.checkId, transactions.checkId) &&
        Objects.equals(this.creditorId, transactions.creditorId) &&
        Objects.equals(this.bookingDate, transactions.bookingDate) &&
        Objects.equals(this.valueDate, transactions.valueDate) &&
        Objects.equals(this.transactionAmount, transactions.transactionAmount) &&
        Objects.equals(this.currencyExchange, transactions.currencyExchange) &&
        Objects.equals(this.creditorName, transactions.creditorName) &&
        Objects.equals(this.creditorAccount, transactions.creditorAccount) &&
        Objects.equals(this.creditorAgent, transactions.creditorAgent) &&
        Objects.equals(this.ultimateCreditor, transactions.ultimateCreditor) &&
        Objects.equals(this.debtorName, transactions.debtorName) &&
        Objects.equals(this.debtorAccount, transactions.debtorAccount) &&
        Objects.equals(this.debtorAgent, transactions.debtorAgent) &&
        Objects.equals(this.ultimateDebtor, transactions.ultimateDebtor) &&
        Objects.equals(this.remittanceInformationUnstructured, transactions.remittanceInformationUnstructured) &&
        Objects.equals(this.remittanceInformationUnstructuredArray, transactions.remittanceInformationUnstructuredArray) &&
        Objects.equals(this.remittanceInformationStructured, transactions.remittanceInformationStructured) &&
        Objects.equals(this.remittanceInformationStructuredArray, transactions.remittanceInformationStructuredArray) &&
        Objects.equals(this.entryDetails, transactions.entryDetails) &&
        Objects.equals(this.additionalInformation, transactions.additionalInformation) &&
        Objects.equals(this.additionalInformationStructured, transactions.additionalInformationStructured) &&
        Objects.equals(this.purposeCode, transactions.purposeCode) &&
        Objects.equals(this.bankTransactionCode, transactions.bankTransactionCode) &&
        Objects.equals(this.proprietaryBankTransactionCode, transactions.proprietaryBankTransactionCode) &&
        Objects.equals(this.balanceAfterTransaction, transactions.balanceAfterTransaction) &&
        Objects.equals(this.links, transactions.links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionId, entryReference, endToEndId, batchIndicator, batchNumberOfTransactions, mandateId, checkId, creditorId, bookingDate, valueDate, transactionAmount, currencyExchange, creditorName, creditorAccount, creditorAgent, ultimateCreditor, debtorName, debtorAccount, debtorAgent, ultimateDebtor, remittanceInformationUnstructured, remittanceInformationUnstructuredArray, remittanceInformationStructured, remittanceInformationStructuredArray, entryDetails, additionalInformation, additionalInformationStructured, purposeCode, bankTransactionCode, proprietaryBankTransactionCode, balanceAfterTransaction, links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Transactions {\n");
    sb.append("    transactionId: ").append(toIndentedString(transactionId)).append("\n");
    sb.append("    entryReference: ").append(toIndentedString(entryReference)).append("\n");
    sb.append("    endToEndId: ").append(toIndentedString(endToEndId)).append("\n");
    sb.append("    batchIndicator: ").append(toIndentedString(batchIndicator)).append("\n");
    sb.append("    batchNumberOfTransactions: ").append(toIndentedString(batchNumberOfTransactions)).append("\n");
    sb.append("    mandateId: ").append(toIndentedString(mandateId)).append("\n");
    sb.append("    checkId: ").append(toIndentedString(checkId)).append("\n");
    sb.append("    creditorId: ").append(toIndentedString(creditorId)).append("\n");
    sb.append("    bookingDate: ").append(toIndentedString(bookingDate)).append("\n");
    sb.append("    valueDate: ").append(toIndentedString(valueDate)).append("\n");
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
    sb.append("    entryDetails: ").append(toIndentedString(entryDetails)).append("\n");
    sb.append("    additionalInformation: ").append(toIndentedString(additionalInformation)).append("\n");
    sb.append("    additionalInformationStructured: ").append(toIndentedString(additionalInformationStructured)).append("\n");
    sb.append("    purposeCode: ").append(toIndentedString(purposeCode)).append("\n");
    sb.append("    bankTransactionCode: ").append(toIndentedString(bankTransactionCode)).append("\n");
    sb.append("    proprietaryBankTransactionCode: ").append(toIndentedString(proprietaryBankTransactionCode)).append("\n");
    sb.append("    balanceAfterTransaction: ").append(toIndentedString(balanceAfterTransaction)).append("\n");
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

