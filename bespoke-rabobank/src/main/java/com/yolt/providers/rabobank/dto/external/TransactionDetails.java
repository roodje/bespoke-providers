package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * TransactionDetails
 */
public class TransactionDetails {
    @JsonProperty("bookingDate")
    private String bookingDate = null;

    @JsonProperty("creditorAccount")
    private AccountReference creditorAccount = null;

    @JsonProperty("creditorAgent")
    private String creditorAgent = null;

    @JsonProperty("creditorId")
    private String creditorId = null;

    @JsonProperty("creditorName")
    private String creditorName = null;

    @JsonProperty("debtorAccount")
    private AccountReference debtorAccount = null;

    @JsonProperty("debtorAgent")
    private String debtorAgent = null;

    @JsonProperty("debtorName")
    private String debtorName = null;

    @JsonProperty("endToEndId")
    private String endToEndId = null;

    @JsonProperty("entryReference")
    private String entryReference = null;

    @JsonProperty("currencyExchange")
    private CurrencyExchangeList currencyExchange = null;

    @JsonProperty("initiatingPartyName")
    private String initiatingPartyName = null;

    @JsonProperty("instructedAmount")
    private InstructedAmountType instructedAmount = null;

    @JsonProperty("mandateId")
    private String mandateId = null;

    @JsonProperty("numberOfTransactions")
    private BigDecimal numberOfTransactions = null;

    @JsonProperty("paymentInformationIdentification")
    private String paymentInformationIdentification = null;

    @JsonProperty("purposeCode")
    private PurposeCode purposeCode = null;

    @JsonProperty("raboBookingDateTime")
    private String raboBookingDateTime = null;

    @JsonProperty("raboDetailedTransactionType")
    private String raboDetailedTransactionType = null;

    @JsonProperty("raboTransactionTypeName")
    private String raboTransactionTypeName = null;

    @JsonProperty("reasonCode")
    private String reasonCode = null;

    @JsonProperty("remittanceInformationStructured")
    private String remittanceInformationStructured = null;

    @JsonProperty("remittanceInformationUnstructured")
    private String remittanceInformationUnstructured = null;

    @JsonProperty("transactionAmount")
    private Amount transactionAmount = null;

    @JsonProperty("ultimateCreditor")
    private String ultimateCreditor = null;

    @JsonProperty("ultimateDebtor")
    private String ultimateDebtor = null;

    @JsonProperty("valueDate")
    private String valueDate = null;

    public TransactionDetails bookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
        return this;
    }

    /**
     * Date on which the transaction is booked at the Rabobank in the local timezone (UTC) of the Rabobank. It can contain every date (also weekend and holidays). Book date in format CCYY-MM-DD. C = Century, Y = Year, M = Month, D = Day
     *
     * @return bookingDate
     **/
    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public TransactionDetails creditorAccount(AccountReference creditorAccount) {
        this.creditorAccount = creditorAccount;
        return this;
    }

    /**
     * Get creditorAccount
     *
     * @return creditorAccount
     **/
    @Valid
    public AccountReference getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(AccountReference creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public TransactionDetails creditorAgent(String creditorAgent) {
        this.creditorAgent = creditorAgent;
        return this;
    }

    /**
     * BIC of the financial institution (agent) of the creditor.
     *
     * @return creditorAgent
     **/
    public String getCreditorAgent() {
        return creditorAgent;
    }

    public void setCreditorAgent(String creditorAgent) {
        this.creditorAgent = creditorAgent;
    }

    public TransactionDetails creditorId(String creditorId) {
        this.creditorId = creditorId;
        return this;
    }

    /**
     * Identification of the creditor.
     *
     * @return creditorId
     **/
    @Size(max = 35)
    public String getCreditorId() {
        return creditorId;
    }

    public void setCreditorId(String creditorId) {
        this.creditorId = creditorId;
    }

    public TransactionDetails creditorName(String creditorName) {
        this.creditorName = creditorName;
        return this;
    }

    /**
     * Name of the creditor.
     *
     * @return creditorName
     **/
    @Size(max = 70)
    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public TransactionDetails debtorAccount(AccountReference debtorAccount) {
        this.debtorAccount = debtorAccount;
        return this;
    }

    /**
     * Get debtorAccount
     *
     * @return debtorAccount
     **/
    @Valid
    public AccountReference getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(AccountReference debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public TransactionDetails debtorAgent(String debtorAgent) {
        this.debtorAgent = debtorAgent;
        return this;
    }

    /**
     * BIC of the financial institution (agent) of the debtor.
     *
     * @return debtorAgent
     **/
    public String getDebtorAgent() {
        return debtorAgent;
    }

    public void setDebtorAgent(String debtorAgent) {
        this.debtorAgent = debtorAgent;
    }

    public TransactionDetails debtorName(String debtorName) {
        this.debtorName = debtorName;
        return this;
    }

    /**
     * Name of the debtor.
     *
     * @return debtorName
     **/
    @Size(max = 70)
    public String getDebtorName() {
        return debtorName;
    }

    public void setDebtorName(String debtorName) {
        this.debtorName = debtorName;
    }

    public TransactionDetails endToEndId(String endToEndId) {
        this.endToEndId = endToEndId;
        return this;
    }

    /**
     * Unique payment reference assigned by the initiating party to identify the transaction.
     *
     * @return endToEndId
     **/
    @Size(max = 35)
    public String getEndToEndId() {
        return endToEndId;
    }

    public void setEndToEndId(String endToEndId) {
        this.endToEndId = endToEndId;
    }

    public TransactionDetails entryReference(String entryReference) {
        this.entryReference = entryReference;
        return this;
    }

    /**
     * Unique sequential number of the transaction on the specific account. It is adviced to use this field as identifier of the transaction and to validate if you have received all transactions. It is also advices to sort transactions based on this field. Rabobank starts counting at 1 on every account after is has been opened and every next transaction on the account gets one number higher.
     *
     * @return entryReference
     **/
    @Size(max = 35)
    public String getEntryReference() {
        return entryReference;
    }

    public void setEntryReference(String entryReference) {
        this.entryReference = entryReference;
    }

    public TransactionDetails currencyExchange(CurrencyExchangeList currencyExchange) {
        this.currencyExchange = currencyExchange;
        return this;
    }

    /**
     * Get currencyExchange
     *
     * @return currencyExchange
     **/
    @Valid
    public CurrencyExchangeList getCurrencyExchange() {
        return currencyExchange;
    }

    public void setCurrencyExchange(CurrencyExchangeList currencyExchange) {
        this.currencyExchange = currencyExchange;
    }

    public TransactionDetails initiatingPartyName(String initiatingPartyName) {
        this.initiatingPartyName = initiatingPartyName;
        return this;
    }

    /**
     * Name of the initiating party if available.
     *
     * @return initiatingPartyName
     **/
    public String getInitiatingPartyName() {
        return initiatingPartyName;
    }

    public void setInitiatingPartyName(String initiatingPartyName) {
        this.initiatingPartyName = initiatingPartyName;
    }

    public TransactionDetails instructedAmount(InstructedAmountType instructedAmount) {
        this.instructedAmount = instructedAmount;
        return this;
    }

    /**
     * Get instructedAmount
     *
     * @return instructedAmount
     **/
    @Valid
    public InstructedAmountType getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(InstructedAmountType instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public TransactionDetails mandateId(String mandateId) {
        this.mandateId = mandateId;
        return this;
    }

    /**
     * Unique identification, as assigned by the creditor, to unambiguously identify the mandate.
     *
     * @return mandateId
     **/
    @Size(max = 35)
    public String getMandateId() {
        return mandateId;
    }

    public void setMandateId(String mandateId) {
        this.mandateId = mandateId;
    }

    public TransactionDetails numberOfTransactions(BigDecimal numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
        return this;
    }

    /**
     * Number of underlying transactions in a batch booking. Only filled in case of batch booking.
     *
     * @return numberOfTransactions
     **/
    @Valid

    public BigDecimal getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(BigDecimal numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    public TransactionDetails paymentInformationIdentification(String paymentInformationIdentification) {
        this.paymentInformationIdentification = paymentInformationIdentification;
        return this;
    }

    /**
     * Unique identification of a batch booking. Only filled in case of batch booking.
     *
     * @return paymentInformationIdentification
     **/
    public String getPaymentInformationIdentification() {
        return paymentInformationIdentification;
    }

    public void setPaymentInformationIdentification(String paymentInformationIdentification) {
        this.paymentInformationIdentification = paymentInformationIdentification;
    }

    public TransactionDetails purposeCode(PurposeCode purposeCode) {
        this.purposeCode = purposeCode;
        return this;
    }

    /**
     * Get purposeCode
     *
     * @return purposeCode
     **/
    @Valid
    public PurposeCode getPurposeCode() {
        return purposeCode;
    }

    public void setPurposeCode(PurposeCode purposeCode) {
        this.purposeCode = purposeCode;
    }

    public TransactionDetails raboBookingDateTime(String raboBookingDateTime) {
        this.raboBookingDateTime = raboBookingDateTime;
        return this;
    }

    /**
     * Date on which the transaction is booked at the Rabobank using the Zulu time standard which is in GMT/UTC timezone.
     *
     * @return raboBookingDateTime
     **/
    public String getRaboBookingDateTime() {
        return raboBookingDateTime;
    }

    public void setRaboBookingDateTime(String raboBookingDateTime) {
        this.raboBookingDateTime = raboBookingDateTime;
    }

    public TransactionDetails raboDetailedTransactionType(String raboDetailedTransactionType) {
        this.raboDetailedTransactionType = raboDetailedTransactionType;
        return this;
    }

    /**
     * The detailed bank transaction code is filled with a numeric code of max. 4 positions. See https://www.rabobank.nl/seb-en under section ‘Transaction reporting / Transaction type codes’. These codes are mostly used for companies to reconcile.
     *
     * @return raboDetailedTransactionType
     **/
    public String getRaboDetailedTransactionType() {
        return raboDetailedTransactionType;
    }

    public void setRaboDetailedTransactionType(String raboDetailedTransactionType) {
        this.raboDetailedTransactionType = raboDetailedTransactionType;
    }

    public TransactionDetails raboTransactionTypeName(String raboTransactionTypeName) {
        this.raboTransactionTypeName = raboTransactionTypeName;
        return this;
    }

    /**
     * This bank transaction code name is filled with the description of the letter codes. These groups are mostly used for private customer to categories the transactions into groups. See https://www.rabobank.nl/seb-en under section ‘Transaction reporting / Transaction type codes’, go to appendix 1 of this document.
     *
     * @return raboTransactionTypeName
     **/
    public String getRaboTransactionTypeName() {
        return raboTransactionTypeName;
    }

    public void setRaboTransactionTypeName(String raboTransactionTypeName) {
        this.raboTransactionTypeName = raboTransactionTypeName;
    }

    public TransactionDetails reasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
        return this;
    }

    /**
     * The reason of the correction, reversal or rejection See for Direct debit: https://www.rabobank.nl/bedrijven/betalen/klanten-laten-betalen/incasso/foutcodes/english/ See for Credit Transfer: https://www.rabobank.nl/bedrijven/betalen/zelf-betalingen-doen/eurobetaling/foutcodes/
     *
     * @return reasonCode
     **/
    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public TransactionDetails remittanceInformationStructured(String remittanceInformationStructured) {
        this.remittanceInformationStructured = remittanceInformationStructured;
        return this;
    }

    /**
     * Payment reference.  Different from other places the content is containt in plain form not in form of a structered field.
     *
     * @return remittanceInformationStructured
     **/
    @Size(max = 140)
    public String getRemittanceInformationStructured() {
        return remittanceInformationStructured;
    }

    public void setRemittanceInformationStructured(String remittanceInformationStructured) {
        this.remittanceInformationStructured = remittanceInformationStructured;
    }

    public TransactionDetails remittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
        return this;
    }

    /**
     * Description lines.
     *
     * @return remittanceInformationUnstructured
     **/
    @Size(max = 140)
    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public TransactionDetails transactionAmount(Amount transactionAmount) {
        this.transactionAmount = transactionAmount;
        return this;
    }

    /**
     * Get transactionAmount
     *
     * @return transactionAmount
     **/
    @NotNull
    @Valid
    public Amount getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(Amount transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public TransactionDetails ultimateCreditor(String ultimateCreditor) {
        this.ultimateCreditor = ultimateCreditor;
        return this;
    }

    /**
     * Name of the ultimate creditor.
     *
     * @return ultimateCreditor
     **/
    @Size(max = 70)
    public String getUltimateCreditor() {
        return ultimateCreditor;
    }

    public void setUltimateCreditor(String ultimateCreditor) {
        this.ultimateCreditor = ultimateCreditor;
    }

    public TransactionDetails ultimateDebtor(String ultimateDebtor) {
        this.ultimateDebtor = ultimateDebtor;
        return this;
    }

    /**
     * Name of the ultimate debtor.
     *
     * @return ultimateDebtor
     **/
    @Size(max = 70)
    public String getUltimateDebtor() {
        return ultimateDebtor;
    }

    public void setUltimateDebtor(String ultimateDebtor) {
        this.ultimateDebtor = ultimateDebtor;
    }

    public TransactionDetails valueDate(String valueDate) {
        this.valueDate = valueDate;
        return this;
    }

    /**
     * Value date / interest date in format CCYY-MM-DD. C = Century, Y = Year, M = Month, D = Day
     *
     * @return valueDate
     **/
    public String getValueDate() {
        return valueDate;
    }

    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionDetails transactionDetails = (TransactionDetails) o;
        return Objects.equals(this.bookingDate, transactionDetails.bookingDate) &&
                Objects.equals(this.creditorAccount, transactionDetails.creditorAccount) &&
                Objects.equals(this.creditorAgent, transactionDetails.creditorAgent) &&
                Objects.equals(this.creditorId, transactionDetails.creditorId) &&
                Objects.equals(this.creditorName, transactionDetails.creditorName) &&
                Objects.equals(this.debtorAccount, transactionDetails.debtorAccount) &&
                Objects.equals(this.debtorAgent, transactionDetails.debtorAgent) &&
                Objects.equals(this.debtorName, transactionDetails.debtorName) &&
                Objects.equals(this.endToEndId, transactionDetails.endToEndId) &&
                Objects.equals(this.entryReference, transactionDetails.entryReference) &&
                Objects.equals(this.currencyExchange, transactionDetails.currencyExchange) &&
                Objects.equals(this.initiatingPartyName, transactionDetails.initiatingPartyName) &&
                Objects.equals(this.instructedAmount, transactionDetails.instructedAmount) &&
                Objects.equals(this.mandateId, transactionDetails.mandateId) &&
                Objects.equals(this.numberOfTransactions, transactionDetails.numberOfTransactions) &&
                Objects.equals(this.paymentInformationIdentification, transactionDetails.paymentInformationIdentification) &&
                Objects.equals(this.purposeCode, transactionDetails.purposeCode) &&
                Objects.equals(this.raboBookingDateTime, transactionDetails.raboBookingDateTime) &&
                Objects.equals(this.raboDetailedTransactionType, transactionDetails.raboDetailedTransactionType) &&
                Objects.equals(this.raboTransactionTypeName, transactionDetails.raboTransactionTypeName) &&
                Objects.equals(this.reasonCode, transactionDetails.reasonCode) &&
                Objects.equals(this.remittanceInformationStructured, transactionDetails.remittanceInformationStructured) &&
                Objects.equals(this.remittanceInformationUnstructured, transactionDetails.remittanceInformationUnstructured) &&
                Objects.equals(this.transactionAmount, transactionDetails.transactionAmount) &&
                Objects.equals(this.ultimateCreditor, transactionDetails.ultimateCreditor) &&
                Objects.equals(this.ultimateDebtor, transactionDetails.ultimateDebtor) &&
                Objects.equals(this.valueDate, transactionDetails.valueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingDate, creditorAccount, creditorAgent, creditorId, creditorName, debtorAccount, debtorAgent, debtorName, endToEndId, entryReference, currencyExchange, initiatingPartyName, instructedAmount, mandateId, numberOfTransactions, paymentInformationIdentification, purposeCode, raboBookingDateTime, raboDetailedTransactionType, raboTransactionTypeName, reasonCode, remittanceInformationStructured, remittanceInformationUnstructured, transactionAmount, ultimateCreditor, ultimateDebtor, valueDate);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TransactionDetails {\n");

        sb.append("    bookingDate: ").append(toIndentedString(bookingDate)).append("\n");
        sb.append("    creditorAccount: ").append(toIndentedString(creditorAccount)).append("\n");
        sb.append("    creditorAgent: ").append(toIndentedString(creditorAgent)).append("\n");
        sb.append("    creditorId: ").append(toIndentedString(creditorId)).append("\n");
        sb.append("    creditorName: ").append(toIndentedString(creditorName)).append("\n");
        sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
        sb.append("    debtorAgent: ").append(toIndentedString(debtorAgent)).append("\n");
        sb.append("    debtorName: ").append(toIndentedString(debtorName)).append("\n");
        sb.append("    endToEndId: ").append(toIndentedString(endToEndId)).append("\n");
        sb.append("    entryReference: ").append(toIndentedString(entryReference)).append("\n");
        sb.append("    currencyExchange: ").append(toIndentedString(currencyExchange)).append("\n");
        sb.append("    initiatingPartyName: ").append(toIndentedString(initiatingPartyName)).append("\n");
        sb.append("    instructedAmount: ").append(toIndentedString(instructedAmount)).append("\n");
        sb.append("    mandateId: ").append(toIndentedString(mandateId)).append("\n");
        sb.append("    numberOfTransactions: ").append(toIndentedString(numberOfTransactions)).append("\n");
        sb.append("    paymentInformationIdentification: ").append(toIndentedString(paymentInformationIdentification)).append("\n");
        sb.append("    purposeCode: ").append(toIndentedString(purposeCode)).append("\n");
        sb.append("    raboBookingDateTime: ").append(toIndentedString(raboBookingDateTime)).append("\n");
        sb.append("    raboDetailedTransactionType: ").append(toIndentedString(raboDetailedTransactionType)).append("\n");
        sb.append("    raboTransactionTypeName: ").append(toIndentedString(raboTransactionTypeName)).append("\n");
        sb.append("    reasonCode: ").append(toIndentedString(reasonCode)).append("\n");
        sb.append("    remittanceInformationStructured: ").append(toIndentedString(remittanceInformationStructured)).append("\n");
        sb.append("    remittanceInformationUnstructured: ").append(toIndentedString(remittanceInformationUnstructured)).append("\n");
        sb.append("    transactionAmount: ").append(toIndentedString(transactionAmount)).append("\n");
        sb.append("    ultimateCreditor: ").append(toIndentedString(ultimateCreditor)).append("\n");
        sb.append("    ultimateDebtor: ").append(toIndentedString(ultimateDebtor)).append("\n");
        sb.append("    valueDate: ").append(toIndentedString(valueDate)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

