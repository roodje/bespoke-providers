package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * AccountReport
 */
public class AccountReport {
    @JsonProperty("_links")
    private LinksAccountReport links = null;

    @JsonProperty("booked")
    private TransactionList booked = null;

    @JsonProperty("pending")
    private TransactionList pending = null;

    public AccountReport links(LinksAccountReport links) {
        this.links = links;
        return this;
    }

    /**
     * Get links
     *
     * @return links
     **/
    @NotNull
    @Valid
    public LinksAccountReport getLinks() {
        return links;
    }

    public void setLinks(LinksAccountReport links) {
        this.links = links;
    }

    public AccountReport booked(TransactionList booked) {
        this.booked = booked;
        return this;
    }

    /**
     * Get booked
     *
     * @return booked
     **/
    @NotNull
    @Valid
    public TransactionList getBooked() {
        return booked;
    }

    public void setBooked(TransactionList booked) {
        this.booked = booked;
    }

    public AccountReport pending(TransactionList pending) {
        this.pending = pending;
        return this;
    }

    /**
     * Get pending
     *
     * @return pending
     **/
    @Valid
    public TransactionList getPending() {
        return pending;
    }

    public void setPending(TransactionList pending) {
        this.pending = pending;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountReport accountReport = (AccountReport) o;
        return Objects.equals(this.links, accountReport.links) &&
                Objects.equals(this.booked, accountReport.booked) &&
                Objects.equals(this.pending, accountReport.pending);
    }

    @Override
    public int hashCode() {
        return Objects.hash(links, booked, pending);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountReport {\n");

        sb.append("    links: ").append(toIndentedString(links)).append("\n");
        sb.append("    booked: ").append(toIndentedString(booked)).append("\n");
        sb.append("    pending: ").append(toIndentedString(pending)).append("\n");
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

