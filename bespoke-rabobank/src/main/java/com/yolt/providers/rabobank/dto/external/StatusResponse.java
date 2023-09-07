package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * StatusResponse
 */
public class StatusResponse {
    @JsonProperty("psuMessage")
    private String psuMessage = null;

    @JsonProperty("scaStatus")
    private String scaStatus = null;

    @JsonProperty("tppMessages")
    @Valid
    private List<TppMessageInformation> tppMessages = null;

    @JsonProperty("transactionStatus")
    private TransactionStatus transactionStatus = null;

    public StatusResponse psuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
        return this;
    }

    /**
     * Text to be displayed to the PSU.
     *
     * @return psuMessage
     **/
    public String getPsuMessage() {
        return psuMessage;
    }

    public void setPsuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
    }

    public StatusResponse scaStatus(String scaStatus) {
        this.scaStatus = scaStatus;
        return this;
    }

    /**
     * This data element is containing information about the status of the SCA method applied. This is free text but might be coded in a future version of the specification.
     *
     * @return scaStatus
     **/

    public String getScaStatus() {
        return scaStatus;
    }

    public void setScaStatus(String scaStatus) {
        this.scaStatus = scaStatus;
    }

    public StatusResponse tppMessages(List<TppMessageInformation> tppMessages) {
        this.tppMessages = tppMessages;
        return this;
    }

    public StatusResponse addTppMessagesItem(TppMessageInformation tppMessagesItem) {
        if (this.tppMessages == null) {
            this.tppMessages = new ArrayList<>();
        }
        this.tppMessages.add(tppMessagesItem);
        return this;
    }

    /**
     * Message array to be handled by the TPP.
     *
     * @return tppMessages
     **/
   

    @Valid
    public List<TppMessageInformation> getTppMessages() {
        return tppMessages;
    }

    public void setTppMessages(List<TppMessageInformation> tppMessages) {
        this.tppMessages = tppMessages;
    }

    public StatusResponse transactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
        return this;
    }

    /**
     * Get transactionStatus
     *
     * @return transactionStatus
     **/
    @NotNull
    @Valid
    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StatusResponse statusResponse = (StatusResponse) o;
        return Objects.equals(this.psuMessage, statusResponse.psuMessage) &&
                Objects.equals(this.scaStatus, statusResponse.scaStatus) &&
                Objects.equals(this.tppMessages, statusResponse.tppMessages) &&
                Objects.equals(this.transactionStatus, statusResponse.transactionStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(psuMessage, scaStatus, tppMessages, transactionStatus);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class StatusResponse {\n");

        sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
        sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
        sb.append("    tppMessages: ").append(toIndentedString(tppMessages)).append("\n");
        sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
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

