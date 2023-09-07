package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Body of the JSON response for an authorisation confirmation request.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class AuthorisationConfirmationResponse   {

  @JsonProperty("scaStatus")
  private ScaStatusAuthorisationConfirmation scaStatus;

  @JsonProperty("_links")
  private LinksAuthorisationConfirmation links;

  @JsonProperty("psuMessage")
  private String psuMessage;

  public AuthorisationConfirmationResponse scaStatus(ScaStatusAuthorisationConfirmation scaStatus) {
    this.scaStatus = scaStatus;
    return this;
  }

  /**
   * Get scaStatus
   * @return scaStatus
  */
  @NotNull @Valid 
  public ScaStatusAuthorisationConfirmation getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(ScaStatusAuthorisationConfirmation scaStatus) {
    this.scaStatus = scaStatus;
  }

  public AuthorisationConfirmationResponse links(LinksAuthorisationConfirmation links) {
    this.links = links;
    return this;
  }

  /**
   * Get links
   * @return links
  */
  @NotNull @Valid 
  public LinksAuthorisationConfirmation getLinks() {
    return links;
  }

  public void setLinks(LinksAuthorisationConfirmation links) {
    this.links = links;
  }

  public AuthorisationConfirmationResponse psuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
    return this;
  }

  /**
   * Text to be displayed to the PSU.
   * @return psuMessage
  */
  @Size(max = 500) 
  public String getPsuMessage() {
    return psuMessage;
  }

  public void setPsuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuthorisationConfirmationResponse authorisationConfirmationResponse = (AuthorisationConfirmationResponse) o;
    return Objects.equals(this.scaStatus, authorisationConfirmationResponse.scaStatus) &&
        Objects.equals(this.links, authorisationConfirmationResponse.links) &&
        Objects.equals(this.psuMessage, authorisationConfirmationResponse.psuMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaStatus, links, psuMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthorisationConfirmationResponse {\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
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

