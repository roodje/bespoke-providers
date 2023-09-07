package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Card transaction information.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class CardTransaction   {

  @JsonProperty("cardTransactionId")
  private String cardTransactionId;

  @JsonProperty("terminalId")
  private String terminalId;

  @JsonProperty("transactionDate")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate transactionDate;

  @JsonProperty("acceptorTransactionDateTime")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime acceptorTransactionDateTime;

  @JsonProperty("bookingDate")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate bookingDate;

  @JsonProperty("valueDate")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate valueDate;

  @JsonProperty("transactionAmount")
  private Amount transactionAmount;

  @JsonProperty("grandTotalAmount")
  private Amount grandTotalAmount;

  @JsonProperty("currencyExchange")
  @Valid
  private List<ReportExchangeRate> currencyExchange = null;

  @JsonProperty("originalAmount")
  private Amount originalAmount;

  @JsonProperty("markupFee")
  private Amount markupFee;

  @JsonProperty("markupFeePercentage")
  private String markupFeePercentage;

  @JsonProperty("cardAcceptorId")
  private String cardAcceptorId;

  @JsonProperty("cardAcceptorAddress")
  private Address cardAcceptorAddress;

  @JsonProperty("cardAcceptorPhone")
  private String cardAcceptorPhone;

  @JsonProperty("merchantCategoryCode")
  private String merchantCategoryCode;

  @JsonProperty("maskedPAN")
  private String maskedPAN;

  @JsonProperty("transactionDetails")
  private String transactionDetails;

  @JsonProperty("invoiced")
  private Boolean invoiced;

  @JsonProperty("proprietaryBankTransactionCode")
  private String proprietaryBankTransactionCode;

  public CardTransaction cardTransactionId(String cardTransactionId) {
    this.cardTransactionId = cardTransactionId;
    return this;
  }

  /**
   * Unique end to end identity.
   * @return cardTransactionId
  */
  @Size(max = 35) 
  public String getCardTransactionId() {
    return cardTransactionId;
  }

  public void setCardTransactionId(String cardTransactionId) {
    this.cardTransactionId = cardTransactionId;
  }

  public CardTransaction terminalId(String terminalId) {
    this.terminalId = terminalId;
    return this;
  }

  /**
   * Identification of the Terminal, where the card has been used.
   * @return terminalId
  */
  @Size(max = 35) 
  public String getTerminalId() {
    return terminalId;
  }

  public void setTerminalId(String terminalId) {
    this.terminalId = terminalId;
  }

  public CardTransaction transactionDate(LocalDate transactionDate) {
    this.transactionDate = transactionDate;
    return this;
  }

  /**
   * Date of the actual card transaction.
   * @return transactionDate
  */
  @Valid 
  public LocalDate getTransactionDate() {
    return transactionDate;
  }

  public void setTransactionDate(LocalDate transactionDate) {
    this.transactionDate = transactionDate;
  }

  public CardTransaction acceptorTransactionDateTime(OffsetDateTime acceptorTransactionDateTime) {
    this.acceptorTransactionDateTime = acceptorTransactionDateTime;
    return this;
  }

  /**
   * Timestamp of the actual card transaction within the acceptance system
   * @return acceptorTransactionDateTime
  */
  @Valid 
  public OffsetDateTime getAcceptorTransactionDateTime() {
    return acceptorTransactionDateTime;
  }

  public void setAcceptorTransactionDateTime(OffsetDateTime acceptorTransactionDateTime) {
    this.acceptorTransactionDateTime = acceptorTransactionDateTime;
  }

  public CardTransaction bookingDate(LocalDate bookingDate) {
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

  public CardTransaction valueDate(LocalDate valueDate) {
    this.valueDate = valueDate;
    return this;
  }

  /**
   * The Date at which assets become available to the account owner in case of a credit, or cease to be available to the account owner in case of a debit entry. For card transactions this is the payment due date of related booked transactions of a card.
   * @return valueDate
  */
  @Valid 
  public LocalDate getValueDate() {
    return valueDate;
  }

  public void setValueDate(LocalDate valueDate) {
    this.valueDate = valueDate;
  }

  public CardTransaction transactionAmount(Amount transactionAmount) {
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

  public CardTransaction grandTotalAmount(Amount grandTotalAmount) {
    this.grandTotalAmount = grandTotalAmount;
    return this;
  }

  /**
   * Get grandTotalAmount
   * @return grandTotalAmount
  */
  @Valid 
  public Amount getGrandTotalAmount() {
    return grandTotalAmount;
  }

  public void setGrandTotalAmount(Amount grandTotalAmount) {
    this.grandTotalAmount = grandTotalAmount;
  }

  public CardTransaction currencyExchange(List<ReportExchangeRate> currencyExchange) {
    this.currencyExchange = currencyExchange;
    return this;
  }

  public CardTransaction addCurrencyExchangeItem(ReportExchangeRate currencyExchangeItem) {
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

  public CardTransaction originalAmount(Amount originalAmount) {
    this.originalAmount = originalAmount;
    return this;
  }

  /**
   * Get originalAmount
   * @return originalAmount
  */
  @Valid 
  public Amount getOriginalAmount() {
    return originalAmount;
  }

  public void setOriginalAmount(Amount originalAmount) {
    this.originalAmount = originalAmount;
  }

  public CardTransaction markupFee(Amount markupFee) {
    this.markupFee = markupFee;
    return this;
  }

  /**
   * Get markupFee
   * @return markupFee
  */
  @Valid 
  public Amount getMarkupFee() {
    return markupFee;
  }

  public void setMarkupFee(Amount markupFee) {
    this.markupFee = markupFee;
  }

  public CardTransaction markupFeePercentage(String markupFeePercentage) {
    this.markupFeePercentage = markupFeePercentage;
    return this;
  }

  /**
   * Get markupFeePercentage
   * @return markupFeePercentage
  */
  
  public String getMarkupFeePercentage() {
    return markupFeePercentage;
  }

  public void setMarkupFeePercentage(String markupFeePercentage) {
    this.markupFeePercentage = markupFeePercentage;
  }

  public CardTransaction cardAcceptorId(String cardAcceptorId) {
    this.cardAcceptorId = cardAcceptorId;
    return this;
  }

  /**
   * Get cardAcceptorId
   * @return cardAcceptorId
  */
  @Size(max = 35) 
  public String getCardAcceptorId() {
    return cardAcceptorId;
  }

  public void setCardAcceptorId(String cardAcceptorId) {
    this.cardAcceptorId = cardAcceptorId;
  }

  public CardTransaction cardAcceptorAddress(Address cardAcceptorAddress) {
    this.cardAcceptorAddress = cardAcceptorAddress;
    return this;
  }

  /**
   * Get cardAcceptorAddress
   * @return cardAcceptorAddress
  */
  @Valid 
  public Address getCardAcceptorAddress() {
    return cardAcceptorAddress;
  }

  public void setCardAcceptorAddress(Address cardAcceptorAddress) {
    this.cardAcceptorAddress = cardAcceptorAddress;
  }

  public CardTransaction cardAcceptorPhone(String cardAcceptorPhone) {
    this.cardAcceptorPhone = cardAcceptorPhone;
    return this;
  }

  /**
   * Merchant phone number It consists of a \"+\" followed by the country code (from 1 to 3 characters) then a \"-\" and finally, any combination of numbers, \"(\", \")\", \"+\" and \"-\" (up to 30 characters). pattern according to ISO20022 \\+[0-9]{1,3}-[0-9()+\\-]{1,30} 
   * @return cardAcceptorPhone
  */
  @Pattern(regexp = "\\+[0-9]{1,3}\\-[0-9()+\\-]{1,30}") 
  public String getCardAcceptorPhone() {
    return cardAcceptorPhone;
  }

  public void setCardAcceptorPhone(String cardAcceptorPhone) {
    this.cardAcceptorPhone = cardAcceptorPhone;
  }

  public CardTransaction merchantCategoryCode(String merchantCategoryCode) {
    this.merchantCategoryCode = merchantCategoryCode;
    return this;
  }

  /**
   * Merchant category code.
   * @return merchantCategoryCode
  */
  @Size(min = 4, max = 4) 
  public String getMerchantCategoryCode() {
    return merchantCategoryCode;
  }

  public void setMerchantCategoryCode(String merchantCategoryCode) {
    this.merchantCategoryCode = merchantCategoryCode;
  }

  public CardTransaction maskedPAN(String maskedPAN) {
    this.maskedPAN = maskedPAN;
    return this;
  }

  /**
   * Masked Primary Account Number. 
   * @return maskedPAN
  */
  @Size(max = 35) 
  public String getMaskedPAN() {
    return maskedPAN;
  }

  public void setMaskedPAN(String maskedPAN) {
    this.maskedPAN = maskedPAN;
  }

  public CardTransaction transactionDetails(String transactionDetails) {
    this.transactionDetails = transactionDetails;
    return this;
  }

  /**
   * Get transactionDetails
   * @return transactionDetails
  */
  @Size(max = 1000) 
  public String getTransactionDetails() {
    return transactionDetails;
  }

  public void setTransactionDetails(String transactionDetails) {
    this.transactionDetails = transactionDetails;
  }

  public CardTransaction invoiced(Boolean invoiced) {
    this.invoiced = invoiced;
    return this;
  }

  /**
   * Get invoiced
   * @return invoiced
  */
  
  public Boolean getInvoiced() {
    return invoiced;
  }

  public void setInvoiced(Boolean invoiced) {
    this.invoiced = invoiced;
  }

  public CardTransaction proprietaryBankTransactionCode(String proprietaryBankTransactionCode) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CardTransaction cardTransaction = (CardTransaction) o;
    return Objects.equals(this.cardTransactionId, cardTransaction.cardTransactionId) &&
        Objects.equals(this.terminalId, cardTransaction.terminalId) &&
        Objects.equals(this.transactionDate, cardTransaction.transactionDate) &&
        Objects.equals(this.acceptorTransactionDateTime, cardTransaction.acceptorTransactionDateTime) &&
        Objects.equals(this.bookingDate, cardTransaction.bookingDate) &&
        Objects.equals(this.valueDate, cardTransaction.valueDate) &&
        Objects.equals(this.transactionAmount, cardTransaction.transactionAmount) &&
        Objects.equals(this.grandTotalAmount, cardTransaction.grandTotalAmount) &&
        Objects.equals(this.currencyExchange, cardTransaction.currencyExchange) &&
        Objects.equals(this.originalAmount, cardTransaction.originalAmount) &&
        Objects.equals(this.markupFee, cardTransaction.markupFee) &&
        Objects.equals(this.markupFeePercentage, cardTransaction.markupFeePercentage) &&
        Objects.equals(this.cardAcceptorId, cardTransaction.cardAcceptorId) &&
        Objects.equals(this.cardAcceptorAddress, cardTransaction.cardAcceptorAddress) &&
        Objects.equals(this.cardAcceptorPhone, cardTransaction.cardAcceptorPhone) &&
        Objects.equals(this.merchantCategoryCode, cardTransaction.merchantCategoryCode) &&
        Objects.equals(this.maskedPAN, cardTransaction.maskedPAN) &&
        Objects.equals(this.transactionDetails, cardTransaction.transactionDetails) &&
        Objects.equals(this.invoiced, cardTransaction.invoiced) &&
        Objects.equals(this.proprietaryBankTransactionCode, cardTransaction.proprietaryBankTransactionCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardTransactionId, terminalId, transactionDate, acceptorTransactionDateTime, bookingDate, valueDate, transactionAmount, grandTotalAmount, currencyExchange, originalAmount, markupFee, markupFeePercentage, cardAcceptorId, cardAcceptorAddress, cardAcceptorPhone, merchantCategoryCode, maskedPAN, transactionDetails, invoiced, proprietaryBankTransactionCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardTransaction {\n");
    sb.append("    cardTransactionId: ").append(toIndentedString(cardTransactionId)).append("\n");
    sb.append("    terminalId: ").append(toIndentedString(terminalId)).append("\n");
    sb.append("    transactionDate: ").append(toIndentedString(transactionDate)).append("\n");
    sb.append("    acceptorTransactionDateTime: ").append(toIndentedString(acceptorTransactionDateTime)).append("\n");
    sb.append("    bookingDate: ").append(toIndentedString(bookingDate)).append("\n");
    sb.append("    valueDate: ").append(toIndentedString(valueDate)).append("\n");
    sb.append("    transactionAmount: ").append(toIndentedString(transactionAmount)).append("\n");
    sb.append("    grandTotalAmount: ").append(toIndentedString(grandTotalAmount)).append("\n");
    sb.append("    currencyExchange: ").append(toIndentedString(currencyExchange)).append("\n");
    sb.append("    originalAmount: ").append(toIndentedString(originalAmount)).append("\n");
    sb.append("    markupFee: ").append(toIndentedString(markupFee)).append("\n");
    sb.append("    markupFeePercentage: ").append(toIndentedString(markupFeePercentage)).append("\n");
    sb.append("    cardAcceptorId: ").append(toIndentedString(cardAcceptorId)).append("\n");
    sb.append("    cardAcceptorAddress: ").append(toIndentedString(cardAcceptorAddress)).append("\n");
    sb.append("    cardAcceptorPhone: ").append(toIndentedString(cardAcceptorPhone)).append("\n");
    sb.append("    merchantCategoryCode: ").append(toIndentedString(merchantCategoryCode)).append("\n");
    sb.append("    maskedPAN: ").append(toIndentedString(maskedPAN)).append("\n");
    sb.append("    transactionDetails: ").append(toIndentedString(transactionDetails)).append("\n");
    sb.append("    invoiced: ").append(toIndentedString(invoiced)).append("\n");
    sb.append("    proprietaryBankTransactionCode: ").append(toIndentedString(proprietaryBankTransactionCode)).append("\n");
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

