package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Body of the JSON response with SCA Status.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class ScaStatusResponse   {

  @JsonProperty("scaStatus")
  private ScaStatus scaStatus;

  @JsonProperty("psuMessage")
  private String psuMessage;

  @JsonProperty("trustedBeneficiaryFlag")
  private Boolean trustedBeneficiaryFlag;

  @JsonProperty("_links")
  private LinksAll links;

  @JsonProperty("tppMessage")
  @Valid
  private List<TppMessageGeneric> tppMessage = null;

  public ScaStatusResponse scaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
    return this;
  }

  /**
   * Get scaStatus
   * @return scaStatus
  */
  @NotNull @Valid 
  public ScaStatus getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
  }

  public ScaStatusResponse psuMessage(String psuMessage) {
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

  public ScaStatusResponse trustedBeneficiaryFlag(Boolean trustedBeneficiaryFlag) {
    this.trustedBeneficiaryFlag = trustedBeneficiaryFlag;
    return this;
  }

  /**
   * Additional Service: Trusted Benificiaries Within this data element, the ASPSP might optionally communicate towards the TPP whether the creditor was part of the related trusted beneficiary list.  This attribute is only contained in case of a final scaStatus. 
   * @return trustedBeneficiaryFlag
  */
  
  public Boolean getTrustedBeneficiaryFlag() {
    return trustedBeneficiaryFlag;
  }

  public void setTrustedBeneficiaryFlag(Boolean trustedBeneficiaryFlag) {
    this.trustedBeneficiaryFlag = trustedBeneficiaryFlag;
  }

  public ScaStatusResponse links(LinksAll links) {
    this.links = links;
    return this;
  }

  /**
   * Get links
   * @return links
  */
  @Valid 
  public LinksAll getLinks() {
    return links;
  }

  public void setLinks(LinksAll links) {
    this.links = links;
  }

  public ScaStatusResponse tppMessage(List<TppMessageGeneric> tppMessage) {
    this.tppMessage = tppMessage;
    return this;
  }

  public ScaStatusResponse addTppMessageItem(TppMessageGeneric tppMessageItem) {
    if (this.tppMessage == null) {
      this.tppMessage = new ArrayList<>();
    }
    this.tppMessage.add(tppMessageItem);
    return this;
  }

  /**
   * Messages to the TPP on operational issues.
   * @return tppMessage
  */
  @Valid 
  public List<TppMessageGeneric> getTppMessage() {
    return tppMessage;
  }

  public void setTppMessage(List<TppMessageGeneric> tppMessage) {
    this.tppMessage = tppMessage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScaStatusResponse scaStatusResponse = (ScaStatusResponse) o;
    return Objects.equals(this.scaStatus, scaStatusResponse.scaStatus) &&
        Objects.equals(this.psuMessage, scaStatusResponse.psuMessage) &&
        Objects.equals(this.trustedBeneficiaryFlag, scaStatusResponse.trustedBeneficiaryFlag) &&
        Objects.equals(this.links, scaStatusResponse.links) &&
        Objects.equals(this.tppMessage, scaStatusResponse.tppMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaStatus, psuMessage, trustedBeneficiaryFlag, links, tppMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScaStatusResponse {\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
    sb.append("    trustedBeneficiaryFlag: ").append(toIndentedString(trustedBeneficiaryFlag)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    tppMessage: ").append(toIndentedString(tppMessage)).append("\n");
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

