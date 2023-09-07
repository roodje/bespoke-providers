package com.yolt.providers.alpha.common.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Information that locates and identifies a specific address, as defined by postal services or in free format text.
 */
@ApiModel(description = "Information that locates and identifies a specific address, as defined by postal services or in free format text.")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-07-18T15:36:09.705982700+02:00[Europe/Warsaw]")

public class RiskDeliveryAddress {
    @JsonProperty("StreetName")
    private String streetName = null;

    @JsonProperty("CountrySubDivision")
    @Valid
    private List<String> countrySubDivision = null;

    @JsonProperty("AddressLine")
    @Valid
    private List<String> addressLine = null;

    @JsonProperty("BuildingNumber")
    private String buildingNumber = null;

    @JsonProperty("TownName")
    private String townName = null;

    @JsonProperty("Country")
    private String country = null;

    @JsonProperty("PostCode")
    private String postCode = null;

    public RiskDeliveryAddress streetName(String streetName) {
        this.streetName = streetName;
        return this;
    }

    /**
     * Name of a street or thoroughfare
     *
     * @return streetName
     **/
    @ApiModelProperty(value = "Name of a street or thoroughfare")

    @Size(min = 1, max = 70)
    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public RiskDeliveryAddress countrySubDivision(List<String> countrySubDivision) {
        this.countrySubDivision = countrySubDivision;
        return this;
    }

    public RiskDeliveryAddress addCountrySubDivisionItem(String countrySubDivisionItem) {
        if (this.countrySubDivision == null) {
            this.countrySubDivision = new ArrayList<>();
        }
        this.countrySubDivision.add(countrySubDivisionItem);
        return this;
    }

    /**
     * Identifies a subdivision of a country, for instance state, region, county.
     *
     * @return countrySubDivision
     **/
    @ApiModelProperty(value = "Identifies a subdivision of a country, for instance state, region, county.")

    @Size(min = 0, max = 2)
    public List<String> getCountrySubDivision() {
        return countrySubDivision;
    }

    public void setCountrySubDivision(List<String> countrySubDivision) {
        this.countrySubDivision = countrySubDivision;
    }

    public RiskDeliveryAddress addressLine(List<String> addressLine) {
        this.addressLine = addressLine;
        return this;
    }

    public RiskDeliveryAddress addAddressLineItem(String addressLineItem) {
        if (this.addressLine == null) {
            this.addressLine = new ArrayList<>();
        }
        this.addressLine.add(addressLineItem);
        return this;
    }

    /**
     * Information that locates and identifies a specific address, as defined by postal services, that is presented in free format text.
     *
     * @return addressLine
     **/
    @ApiModelProperty(value = "Information that locates and identifies a specific address, as defined by postal services, that is presented in free format text.")

    @Size(min = 0, max = 2)
    public List<String> getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(List<String> addressLine) {
        this.addressLine = addressLine;
    }

    public RiskDeliveryAddress buildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
        return this;
    }

    /**
     * Number that identifies the position of a building on a street.
     *
     * @return buildingNumber
     **/
    @ApiModelProperty(value = "Number that identifies the position of a building on a street.")

    @Size(min = 1, max = 16)
    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public RiskDeliveryAddress townName(String townName) {
        this.townName = townName;
        return this;
    }

    /**
     * Name of a built-up area, with defined boundaries, and a local government.
     *
     * @return townName
     **/
    @ApiModelProperty(value = "Name of a built-up area, with defined boundaries, and a local government.")

    @Size(min = 1, max = 35)
    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public RiskDeliveryAddress country(String country) {
        this.country = country;
        return this;
    }

    /**
     * Nation with its own government, occupying a particular territory.
     *
     * @return country
     **/
    @ApiModelProperty(value = "Nation with its own government, occupying a particular territory.")

    @Pattern(regexp = "^[A-Z]{2,2}$")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public RiskDeliveryAddress postCode(String postCode) {
        this.postCode = postCode;
        return this;
    }

    /**
     * Identifier consisting of a group of letters and/or numbers that is added to a postal address to assist the sorting of mail
     *
     * @return postCode
     **/
    @ApiModelProperty(value = "Identifier consisting of a group of letters and/or numbers that is added to a postal address to assist the sorting of mail")

    @Size(min = 1, max = 16)
    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RiskDeliveryAddress riskDeliveryAddress = (RiskDeliveryAddress) o;
        return Objects.equals(this.streetName, riskDeliveryAddress.streetName) &&
                Objects.equals(this.countrySubDivision, riskDeliveryAddress.countrySubDivision) &&
                Objects.equals(this.addressLine, riskDeliveryAddress.addressLine) &&
                Objects.equals(this.buildingNumber, riskDeliveryAddress.buildingNumber) &&
                Objects.equals(this.townName, riskDeliveryAddress.townName) &&
                Objects.equals(this.country, riskDeliveryAddress.country) &&
                Objects.equals(this.postCode, riskDeliveryAddress.postCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streetName, countrySubDivision, addressLine, buildingNumber, townName, country, postCode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RiskDeliveryAddress {\n");

        sb.append("    streetName: ").append(toIndentedString(streetName)).append("\n");
        sb.append("    countrySubDivision: ").append(toIndentedString(countrySubDivision)).append("\n");
        sb.append("    addressLine: ").append(toIndentedString(addressLine)).append("\n");
        sb.append("    buildingNumber: ").append(toIndentedString(buildingNumber)).append("\n");
        sb.append("    townName: ").append(toIndentedString(townName)).append("\n");
        sb.append("    country: ").append(toIndentedString(country)).append("\n");
        sb.append("    postCode: ").append(toIndentedString(postCode)).append("\n");
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

