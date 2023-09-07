package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Base Credit Transfer information.
 */
public class CreditTransfer {
    @JsonProperty("creditorAccount")
    private CreditorAccount creditorAccount = null;

    @JsonProperty("creditorAddress")
    private CreditorAddress creditorAddress = null;

    @JsonProperty("creditorAgent")
    private String creditorAgent = null;

    @JsonProperty("creditorName")
    private String creditorName = null;

    @JsonProperty("debtorAccount")
    private DebtorAccount debtorAccount = null;

    @JsonProperty("endToEndIdentification")
    private String endToEndIdentification = null;

    @JsonProperty("instructedAmount")
    private InstructedAmount instructedAmount = null;

    @JsonProperty("remittanceInformationUnstructured")
    private String remittanceInformationUnstructured = null;

    @JsonProperty("requestedExecutionDate")
    private LocalDate requestedExecutionDate = null;

    public CreditTransfer creditorAccount(CreditorAccount creditorAccount) {
        this.creditorAccount = creditorAccount;
        return this;
    }

    /**
     * Get creditorAccount
     *
     * @return creditorAccount
     **/
    @NotNull
    @Valid
    public CreditorAccount getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(CreditorAccount creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public CreditTransfer creditorAddress(CreditorAddress creditorAddress) {
        this.creditorAddress = creditorAddress;
        return this;
    }

    /**
     * Get creditorAddress
     *
     * @return creditorAddress
     **/
    @Valid
    public CreditorAddress getCreditorAddress() {
        return creditorAddress;
    }

    public void setCreditorAddress(CreditorAddress creditorAddress) {
        this.creditorAddress = creditorAddress;
    }

    public CreditTransfer creditorAgent(String creditorAgent) {
        this.creditorAgent = creditorAgent;
        return this;
    }

    /**
     * Get creditorAgent
     *
     * @return creditorAgent
     **/
    @Pattern(regexp = "[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")
    public String getCreditorAgent() {
        return creditorAgent;
    }

    public void setCreditorAgent(String creditorAgent) {
        this.creditorAgent = creditorAgent;
    }

    public CreditTransfer creditorName(String creditorName) {
        this.creditorName = creditorName;
        return this;
    }

    /**
     * Name of Creditor.
     *
     * @return creditorName
     **/
    @NotNull
    @Size(min = 2, max = 70)
    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public CreditTransfer debtorAccount(DebtorAccount debtorAccount) {
        this.debtorAccount = debtorAccount;
        return this;
    }

    /**
     * Get debtorAccount
     *
     * @return debtorAccount
     **/
    @Valid
    public DebtorAccount getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(DebtorAccount debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public CreditTransfer endToEndIdentification(String endToEndIdentification) {
        this.endToEndIdentification = endToEndIdentification;
        return this;
    }

    /**
     * end to end identification.
     *
     * @return endToEndIdentification
     **/
    @Size(min = 0, max = 35)
    public String getEndToEndIdentification() {
        return endToEndIdentification;
    }

    public void setEndToEndIdentification(String endToEndIdentification) {
        this.endToEndIdentification = endToEndIdentification;
    }

    public CreditTransfer instructedAmount(InstructedAmount instructedAmount) {
        this.instructedAmount = instructedAmount;
        return this;
    }

    /**
     * Get instructedAmount
     *
     * @return instructedAmount
     **/
    @NotNull
    @Valid
    public InstructedAmount getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(InstructedAmount instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public CreditTransfer remittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
        return this;
    }

    /**
     * Remittance Information Unstructured.
     *
     * @return remittanceInformationUnstructured
     **/
    @Size(min = 0, max = 140)
    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public CreditTransfer requestedExecutionDate(LocalDate requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
        return this;
    }

    /**
     * A ISO8601 date of the excution of the payment initiation. (must be future/current date)
     *
     * @return requestedExecutionDate
     **/
    @Valid
    public LocalDate getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public void setRequestedExecutionDate(LocalDate requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreditTransfer creditTransfer = (CreditTransfer) o;
        return Objects.equals(this.creditorAccount, creditTransfer.creditorAccount) &&
                Objects.equals(this.creditorAddress, creditTransfer.creditorAddress) &&
                Objects.equals(this.creditorAgent, creditTransfer.creditorAgent) &&
                Objects.equals(this.creditorName, creditTransfer.creditorName) &&
                Objects.equals(this.debtorAccount, creditTransfer.debtorAccount) &&
                Objects.equals(this.endToEndIdentification, creditTransfer.endToEndIdentification) &&
                Objects.equals(this.instructedAmount, creditTransfer.instructedAmount) &&
                Objects.equals(this.remittanceInformationUnstructured, creditTransfer.remittanceInformationUnstructured) &&
                Objects.equals(this.requestedExecutionDate, creditTransfer.requestedExecutionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creditorAccount, creditorAddress, creditorAgent, creditorName, debtorAccount, endToEndIdentification, instructedAmount, remittanceInformationUnstructured, requestedExecutionDate);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CreditTransfer {\n");

        sb.append("    creditorAccount: ").append(toIndentedString(creditorAccount)).append("\n");
        sb.append("    creditorAddress: ").append(toIndentedString(creditorAddress)).append("\n");
        sb.append("    creditorAgent: ").append(toIndentedString(creditorAgent)).append("\n");
        sb.append("    creditorName: ").append(toIndentedString(creditorName)).append("\n");
        sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
        sb.append("    endToEndIdentification: ").append(toIndentedString(endToEndIdentification)).append("\n");
        sb.append("    instructedAmount: ").append(toIndentedString(instructedAmount)).append("\n");
        sb.append("    remittanceInformationUnstructured: ").append(toIndentedString(remittanceInformationUnstructured)).append("\n");
        sb.append("    requestedExecutionDate: ").append(toIndentedString(requestedExecutionDate)).append("\n");
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

