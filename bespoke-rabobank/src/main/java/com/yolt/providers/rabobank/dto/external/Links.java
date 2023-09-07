package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;

/**
 * Links
 */
public class Links {
    @JsonProperty("scaRedirect")
    private HrefType scaRedirect = null;

    @JsonProperty("scaStatus")
    private HrefType scaStatus = null;

    @JsonProperty("self")
    private HrefType self = null;

    @JsonProperty("startAuthorisationWithAuthenticationMethodSelection")
    private HrefType startAuthorisationWithAuthenticationMethodSelection = null;

    @JsonProperty("startAuthorisationWithEncryptedPsuAuthentication")
    private HrefType startAuthorisationWithEncryptedPsuAuthentication = null;

    @JsonProperty("startAuthorisationWithPsuAuthentication")
    private HrefType startAuthorisationWithPsuAuthentication = null;

    @JsonProperty("startAuthorisationWithPsuIdentification")
    private HrefType startAuthorisationWithPsuIdentification = null;

    @JsonProperty("startAuthorisationWithTransactionAuthorisation")
    private HrefType startAuthorisationWithTransactionAuthorisation = null;

    @JsonProperty("starztAuthorisation")
    private HrefType starztAuthorisation = null;

    @JsonProperty("status")
    private HrefType status = null;

    public Links scaRedirect(HrefType scaRedirect) {
        this.scaRedirect = scaRedirect;
        return this;
    }

    /**
     * Get scaRedirect
     *
     * @return scaRedirect
     **/
    @Valid
    public HrefType getScaRedirect() {
        return scaRedirect;
    }

    public void setScaRedirect(HrefType scaRedirect) {
        this.scaRedirect = scaRedirect;
    }

    public Links scaStatus(HrefType scaStatus) {
        this.scaStatus = scaStatus;
        return this;
    }

    /**
     * Get scaStatus
     *
     * @return scaStatus
     **/
    @Valid
    public HrefType getScaStatus() {
        return scaStatus;
    }

    public void setScaStatus(HrefType scaStatus) {
        this.scaStatus = scaStatus;
    }

    public Links self(HrefType self) {
        this.self = self;
        return this;
    }

    /**
     * Get self
     *
     * @return self
     **/
    @Valid
    public HrefType getSelf() {
        return self;
    }

    public void setSelf(HrefType self) {
        this.self = self;
    }

    public Links startAuthorisationWithAuthenticationMethodSelection(HrefType startAuthorisationWithAuthenticationMethodSelection) {
        this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
        return this;
    }

    /**
     * Get startAuthorisationWithAuthenticationMethodSelection
     *
     * @return startAuthorisationWithAuthenticationMethodSelection
     **/
    @Valid
    public HrefType getStartAuthorisationWithAuthenticationMethodSelection() {
        return startAuthorisationWithAuthenticationMethodSelection;
    }

    public void setStartAuthorisationWithAuthenticationMethodSelection(HrefType startAuthorisationWithAuthenticationMethodSelection) {
        this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
    }

    public Links startAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
        this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
        return this;
    }

    /**
     * Get startAuthorisationWithEncryptedPsuAuthentication
     *
     * @return startAuthorisationWithEncryptedPsuAuthentication
     **/
    @Valid
    public HrefType getStartAuthorisationWithEncryptedPsuAuthentication() {
        return startAuthorisationWithEncryptedPsuAuthentication;
    }

    public void setStartAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
        this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
    }

    public Links startAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
        this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
        return this;
    }

    /**
     * Get startAuthorisationWithPsuAuthentication
     *
     * @return startAuthorisationWithPsuAuthentication
     **/
    @Valid
    public HrefType getStartAuthorisationWithPsuAuthentication() {
        return startAuthorisationWithPsuAuthentication;
    }

    public void setStartAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
        this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
    }

    public Links startAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
        this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
        return this;
    }

    /**
     * Get startAuthorisationWithPsuIdentification
     *
     * @return startAuthorisationWithPsuIdentification
     **/
    @Valid
    public HrefType getStartAuthorisationWithPsuIdentification() {
        return startAuthorisationWithPsuIdentification;
    }

    public void setStartAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
        this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
    }

    public Links startAuthorisationWithTransactionAuthorisation(HrefType startAuthorisationWithTransactionAuthorisation) {
        this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
        return this;
    }

    /**
     * Get startAuthorisationWithTransactionAuthorisation
     *
     * @return startAuthorisationWithTransactionAuthorisation
     **/
    @Valid
    public HrefType getStartAuthorisationWithTransactionAuthorisation() {
        return startAuthorisationWithTransactionAuthorisation;
    }

    public void setStartAuthorisationWithTransactionAuthorisation(HrefType startAuthorisationWithTransactionAuthorisation) {
        this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
    }

    public Links starztAuthorisation(HrefType starztAuthorisation) {
        this.starztAuthorisation = starztAuthorisation;
        return this;
    }

    /**
     * Get starztAuthorisation
     *
     * @return starztAuthorisation
     **/
    @Valid
    public HrefType getStarztAuthorisation() {
        return starztAuthorisation;
    }

    public void setStarztAuthorisation(HrefType starztAuthorisation) {
        this.starztAuthorisation = starztAuthorisation;
    }

    public Links status(HrefType status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     *
     * @return status
     **/
    @Valid
    public HrefType getStatus() {
        return status;
    }

    public void setStatus(HrefType status) {
        this.status = status;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Links links = (Links) o;
        return Objects.equals(this.scaRedirect, links.scaRedirect) &&
                Objects.equals(this.scaStatus, links.scaStatus) &&
                Objects.equals(this.self, links.self) &&
                Objects.equals(this.startAuthorisationWithAuthenticationMethodSelection, links.startAuthorisationWithAuthenticationMethodSelection) &&
                Objects.equals(this.startAuthorisationWithEncryptedPsuAuthentication, links.startAuthorisationWithEncryptedPsuAuthentication) &&
                Objects.equals(this.startAuthorisationWithPsuAuthentication, links.startAuthorisationWithPsuAuthentication) &&
                Objects.equals(this.startAuthorisationWithPsuIdentification, links.startAuthorisationWithPsuIdentification) &&
                Objects.equals(this.startAuthorisationWithTransactionAuthorisation, links.startAuthorisationWithTransactionAuthorisation) &&
                Objects.equals(this.starztAuthorisation, links.starztAuthorisation) &&
                Objects.equals(this.status, links.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scaRedirect, scaStatus, self, startAuthorisationWithAuthenticationMethodSelection, startAuthorisationWithEncryptedPsuAuthentication, startAuthorisationWithPsuAuthentication, startAuthorisationWithPsuIdentification, startAuthorisationWithTransactionAuthorisation, starztAuthorisation, status);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Links {\n");

        sb.append("    scaRedirect: ").append(toIndentedString(scaRedirect)).append("\n");
        sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
        sb.append("    self: ").append(toIndentedString(self)).append("\n");
        sb.append("    startAuthorisationWithAuthenticationMethodSelection: ").append(toIndentedString(startAuthorisationWithAuthenticationMethodSelection)).append("\n");
        sb.append("    startAuthorisationWithEncryptedPsuAuthentication: ").append(toIndentedString(startAuthorisationWithEncryptedPsuAuthentication)).append("\n");
        sb.append("    startAuthorisationWithPsuAuthentication: ").append(toIndentedString(startAuthorisationWithPsuAuthentication)).append("\n");
        sb.append("    startAuthorisationWithPsuIdentification: ").append(toIndentedString(startAuthorisationWithPsuIdentification)).append("\n");
        sb.append("    startAuthorisationWithTransactionAuthorisation: ").append(toIndentedString(startAuthorisationWithTransactionAuthorisation)).append("\n");
        sb.append("    starztAuthorisation: ").append(toIndentedString(starztAuthorisation)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

