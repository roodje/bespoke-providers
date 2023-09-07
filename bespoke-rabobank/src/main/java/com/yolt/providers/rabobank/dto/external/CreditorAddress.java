package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Information over creditor address.
 */
public class CreditorAddress {
    @JsonProperty("buildingNumber")
    private String buildingNumber = null;

    @JsonProperty("country")
    private String country = null;

    @JsonProperty("postcode")
    private String postcode = null;

    @JsonProperty("streetName")
    private String streetName = null;

    @JsonProperty("townName")
    private String townName = null;

    public CreditorAddress buildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
        return this;
    }

    /**
     * Creditor building number.
     *
     * @return buildingNumber
     **/
    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public CreditorAddress country(String country) {
        this.country = country;
        return this;
    }

    /**
     * Code to identify a country, a dependency, or another area of particular geopolitical interest, on the basis of country names obtained from the United Nations (ISO 3166, Alpha-2 code).
     *
     * @return country
     **/
   
    @NotNull
    @Pattern(regexp = "^[A-Z]{2,2}$")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public CreditorAddress postcode(String postcode) {
        this.postcode = postcode;
        return this;
    }

    /**
     * Creditor postal code number.
     *
     * @return postcode
     **/
    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public CreditorAddress streetName(String streetName) {
        this.streetName = streetName;
        return this;
    }

    /**
     * Creditor street.
     *
     * @return streetName
     **/
    @Size(min = 0, max = 50)
    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public CreditorAddress townName(String townName) {
        this.townName = townName;
        return this;
    }

    /**
     * Creditor City name.
     *
     * @return townName
     **/
    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreditorAddress creditorAddress = (CreditorAddress) o;
        return Objects.equals(this.buildingNumber, creditorAddress.buildingNumber) &&
                Objects.equals(this.country, creditorAddress.country) &&
                Objects.equals(this.postcode, creditorAddress.postcode) &&
                Objects.equals(this.streetName, creditorAddress.streetName) &&
                Objects.equals(this.townName, creditorAddress.townName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildingNumber, country, postcode, streetName, townName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CreditorAddress {\n");

        sb.append("    buildingNumber: ").append(toIndentedString(buildingNumber)).append("\n");
        sb.append("    country: ").append(toIndentedString(country)).append("\n");
        sb.append("    postcode: ").append(toIndentedString(postcode)).append("\n");
        sb.append("    streetName: ").append(toIndentedString(streetName)).append("\n");
        sb.append("    townName: ").append(toIndentedString(townName)).append("\n");
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

