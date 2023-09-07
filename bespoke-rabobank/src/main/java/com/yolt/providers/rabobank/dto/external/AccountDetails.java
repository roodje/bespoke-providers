package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * The ASPSP shall give at least one of the account reference identifiers:    - iban    - bban  If the account is a multicurrency account currency code in \&quot;currency\&quot; is set to \&quot;XXX\&quot;.
 */
public class AccountDetails {
    @JsonProperty("_links")
    private LinksAccountDetails links = null;

    @JsonProperty("currency")
    private String currency = null;

    @JsonProperty("iban")
    private String iban = null;

    @JsonProperty("ownerName")
    private String ownerName = null;

    @JsonProperty("resourceId")
    private String resourceId = null;

    @JsonProperty("status")
    private AccountStatus status = null;

    public AccountDetails links(LinksAccountDetails links) {
        this.links = links;
        return this;
    }

    /**
     * Get links
     *
     * @return links
     **/
    @Valid
    public LinksAccountDetails getLinks() {
        return links;
    }

    public void setLinks(LinksAccountDetails links) {
        this.links = links;
    }

    public AccountDetails currency(String currency) {
        this.currency = currency;
        return this;
    }

    /**
     * ISO 4217 Alpha 3 currency code
     *
     * @return currency
     **/
   
    @NotNull
    @Pattern(regexp = "[A-Z]{3}")
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public AccountDetails iban(String iban) {
        this.iban = iban;
        return this;
    }

    /**
     * IBAN of an account
     *
     * @return iban
     **/
   
    @NotNull
    @Pattern(regexp = "[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}")
    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public AccountDetails ownerName(String ownerName) {
        this.ownerName = ownerName;
        return this;
    }

    /**
     * Get ownerName
     *
     * @return ownerName
     **/
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public AccountDetails resourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    /**
     * Account id.
     *
     * @return resourceId
     **/
    @NotNull
    @Size(max = 100)
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public AccountDetails status(AccountStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     *
     * @return status
     **/
    @NotNull
    @Valid
    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountDetails accountDetails = (AccountDetails) o;
        return Objects.equals(this.links, accountDetails.links) &&
                Objects.equals(this.currency, accountDetails.currency) &&
                Objects.equals(this.iban, accountDetails.iban) &&
                Objects.equals(this.ownerName, accountDetails.ownerName) &&
                Objects.equals(this.resourceId, accountDetails.resourceId) &&
                Objects.equals(this.status, accountDetails.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(links, currency, iban, ownerName, resourceId, status);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountDetails {\n");

        sb.append("    links: ").append(toIndentedString(links)).append("\n");
        sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
        sb.append("    iban: ").append(toIndentedString(iban)).append("\n");
        sb.append("    ownerName: ").append(toIndentedString(ownerName)).append("\n");
        sb.append("    resourceId: ").append(toIndentedString(resourceId)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

