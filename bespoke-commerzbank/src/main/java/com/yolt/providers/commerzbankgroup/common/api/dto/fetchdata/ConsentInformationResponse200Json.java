package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Body of the JSON response for a successfull get consent request.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class ConsentInformationResponse200Json   {

  @JsonProperty("access")
  private AccountAccess access;

  @JsonProperty("recurringIndicator")
  private Boolean recurringIndicator;

  @JsonProperty("validUntil")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate validUntil;

  @JsonProperty("frequencyPerDay")
  private Integer frequencyPerDay;

  @JsonProperty("lastActionDate")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate lastActionDate;

  @JsonProperty("consentStatus")
  private ConsentStatus consentStatus;

  @JsonProperty("_links")
  private LinksGetConsent links;

  public ConsentInformationResponse200Json access(AccountAccess access) {
    this.access = access;
    return this;
  }

  /**
   * Get access
   * @return access
  */
  @NotNull @Valid 
  public AccountAccess getAccess() {
    return access;
  }

  public void setAccess(AccountAccess access) {
    this.access = access;
  }

  public ConsentInformationResponse200Json recurringIndicator(Boolean recurringIndicator) {
    this.recurringIndicator = recurringIndicator;
    return this;
  }

  /**
   * \"true\", if the consent is for recurring access to the account data.  \"false\", if the consent is for one access to the account data. 
   * @return recurringIndicator
  */
  @NotNull 
  public Boolean getRecurringIndicator() {
    return recurringIndicator;
  }

  public void setRecurringIndicator(Boolean recurringIndicator) {
    this.recurringIndicator = recurringIndicator;
  }

  public ConsentInformationResponse200Json validUntil(LocalDate validUntil) {
    this.validUntil = validUntil;
    return this;
  }

  /**
   * This parameter is defining a valid until date (including the mentioned date) for the requested consent.  The content is the local ASPSP date in ISO-Date format, e.g. 2017-10-30.  Future dates might get adjusted by ASPSP.   If a maximal available date is requested, a date in far future is to be used: \"9999-12-31\".   In both cases the consent object to be retrieved by the get consent request will contain the adjusted date. 
   * @return validUntil
  */
  @NotNull @Valid 
  public LocalDate getValidUntil() {
    return validUntil;
  }

  public void setValidUntil(LocalDate validUntil) {
    this.validUntil = validUntil;
  }

  public ConsentInformationResponse200Json frequencyPerDay(Integer frequencyPerDay) {
    this.frequencyPerDay = frequencyPerDay;
    return this;
  }

  /**
   * This field indicates the requested maximum frequency for an access without PSU involvement per day. For a one-off access, this attribute is set to \"1\".  The frequency needs to be greater equal to one.   If not otherwise agreed bilaterally between TPP and ASPSP, the frequency is less equal to 4. 
   * minimum: 1
   * @return frequencyPerDay
  */
  @NotNull @Min(1) 
  public Integer getFrequencyPerDay() {
    return frequencyPerDay;
  }

  public void setFrequencyPerDay(Integer frequencyPerDay) {
    this.frequencyPerDay = frequencyPerDay;
  }

  public ConsentInformationResponse200Json lastActionDate(LocalDate lastActionDate) {
    this.lastActionDate = lastActionDate;
    return this;
  }

  /**
   * This date is containing the date of the last action on the consent object either through  the XS2A interface or the PSU/ASPSP interface having an impact on the status. 
   * @return lastActionDate
  */
  @NotNull @Valid 
  public LocalDate getLastActionDate() {
    return lastActionDate;
  }

  public void setLastActionDate(LocalDate lastActionDate) {
    this.lastActionDate = lastActionDate;
  }

  public ConsentInformationResponse200Json consentStatus(ConsentStatus consentStatus) {
    this.consentStatus = consentStatus;
    return this;
  }

  /**
   * Get consentStatus
   * @return consentStatus
  */
  @NotNull @Valid 
  public ConsentStatus getConsentStatus() {
    return consentStatus;
  }

  public void setConsentStatus(ConsentStatus consentStatus) {
    this.consentStatus = consentStatus;
  }

  public ConsentInformationResponse200Json links(LinksGetConsent links) {
    this.links = links;
    return this;
  }

  /**
   * Get links
   * @return links
  */
  @Valid 
  public LinksGetConsent getLinks() {
    return links;
  }

  public void setLinks(LinksGetConsent links) {
    this.links = links;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsentInformationResponse200Json consentInformationResponse200Json = (ConsentInformationResponse200Json) o;
    return Objects.equals(this.access, consentInformationResponse200Json.access) &&
        Objects.equals(this.recurringIndicator, consentInformationResponse200Json.recurringIndicator) &&
        Objects.equals(this.validUntil, consentInformationResponse200Json.validUntil) &&
        Objects.equals(this.frequencyPerDay, consentInformationResponse200Json.frequencyPerDay) &&
        Objects.equals(this.lastActionDate, consentInformationResponse200Json.lastActionDate) &&
        Objects.equals(this.consentStatus, consentInformationResponse200Json.consentStatus) &&
        Objects.equals(this.links, consentInformationResponse200Json.links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(access, recurringIndicator, validUntil, frequencyPerDay, lastActionDate, consentStatus, links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentInformationResponse200Json {\n");
    sb.append("    access: ").append(toIndentedString(access)).append("\n");
    sb.append("    recurringIndicator: ").append(toIndentedString(recurringIndicator)).append("\n");
    sb.append("    validUntil: ").append(toIndentedString(validUntil)).append("\n");
    sb.append("    frequencyPerDay: ").append(toIndentedString(frequencyPerDay)).append("\n");
    sb.append("    lastActionDate: ").append(toIndentedString(lastActionDate)).append("\n");
    sb.append("    consentStatus: ").append(toIndentedString(consentStatus)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
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

