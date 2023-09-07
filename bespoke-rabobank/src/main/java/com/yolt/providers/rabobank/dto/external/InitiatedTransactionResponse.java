package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * InitiatedTransactionResponse
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-08-02T10:19:56.414332200+02:00[Europe/Warsaw]")

public class InitiatedTransactionResponse {
    @JsonProperty("_links")
    private Links links = null;

    @JsonProperty("paymentId")
    private UUID paymentId = null;

    @JsonProperty("psuMessage")
    private String psuMessage = null;

    @JsonProperty("tppMessages")
    @Valid
    private List<TppMessageInformation> tppMessages = null;

    @JsonProperty("transactionStatus")
    private TransactionStatus transactionStatus = null;

    public InitiatedTransactionResponse links(Links links) {
        this.links = links;
        return this;
    }

    /**
     * Get links
     *
     * @return links
     **/
    @Valid
    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public InitiatedTransactionResponse paymentId(UUID paymentId) {
        this.paymentId = paymentId;
        return this;
    }

    /**
     * Resource identification of the generated payment initiation resource.
     *
     * @return paymentId
     **/
    @NotNull
    @Valid
    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public InitiatedTransactionResponse psuMessage(String psuMessage) {
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

    public InitiatedTransactionResponse tppMessages(List<TppMessageInformation> tppMessages) {
        this.tppMessages = tppMessages;
        return this;
    }

    public InitiatedTransactionResponse addTppMessagesItem(TppMessageInformation tppMessagesItem) {
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

    public InitiatedTransactionResponse transactionStatus(TransactionStatus transactionStatus) {
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
        InitiatedTransactionResponse initiatedTransactionResponse = (InitiatedTransactionResponse) o;
        return Objects.equals(this.links, initiatedTransactionResponse.links) &&
                Objects.equals(this.paymentId, initiatedTransactionResponse.paymentId) &&
                Objects.equals(this.psuMessage, initiatedTransactionResponse.psuMessage) &&
                Objects.equals(this.tppMessages, initiatedTransactionResponse.tppMessages) &&
                Objects.equals(this.transactionStatus, initiatedTransactionResponse.transactionStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(links, paymentId, psuMessage, tppMessages, transactionStatus);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class InitiatedTransactionResponse {\n");

        sb.append("    links: ").append(toIndentedString(links)).append("\n");
        sb.append("    paymentId: ").append(toIndentedString(paymentId)).append("\n");
        sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
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

