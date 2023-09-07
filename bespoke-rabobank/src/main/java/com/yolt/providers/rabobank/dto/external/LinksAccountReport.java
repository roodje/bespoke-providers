package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * LinksAccountReport
 */
public class LinksAccountReport {
    @JsonProperty("account")
    private HrefType account = null;

    @JsonProperty("next")
    private HrefType next = null;

    public LinksAccountReport account(HrefType account) {
        this.account = account;
        return this;
    }

    /**
     * Get account
     *
     * @return account
     **/
    @NotNull
    @Valid
    public HrefType getAccount() {
        return account;
    }

    public void setAccount(HrefType account) {
        this.account = account;
    }

    public LinksAccountReport next(HrefType next) {
        this.next = next;
        return this;
    }

    /**
     * Get next
     *
     * @return next
     **/
    @Valid
    public HrefType getNext() {
        return next;
    }

    public void setNext(HrefType next) {
        this.next = next;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinksAccountReport linksAccountReport = (LinksAccountReport) o;
        return Objects.equals(this.account, linksAccountReport.account) &&
                Objects.equals(this.next, linksAccountReport.next);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, next);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LinksAccountReport {\n");

        sb.append("    account: ").append(toIndentedString(account)).append("\n");
        sb.append("    next: ").append(toIndentedString(next)).append("\n");
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

