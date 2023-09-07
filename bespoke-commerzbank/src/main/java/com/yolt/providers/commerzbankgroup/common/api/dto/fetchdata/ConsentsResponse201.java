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
 * Body of the JSON response for a successful consent request.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class ConsentsResponse201   {

  @JsonProperty("consentStatus")
  private ConsentStatus consentStatus;

  @JsonProperty("consentId")
  private String consentId;

  @JsonProperty("scaMethods")
  @Valid
  private List<AuthenticationObject> scaMethods = null;

  @JsonProperty("chosenScaMethod")
  private AuthenticationObject chosenScaMethod;

  @JsonProperty("challengeData")
  private ChallengeData challengeData;

  @JsonProperty("_links")
  private LinksConsents links;

  @JsonProperty("psuMessage")
  private String psuMessage;

  public ConsentsResponse201 consentStatus(ConsentStatus consentStatus) {
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

  public ConsentsResponse201 consentId(String consentId) {
    this.consentId = consentId;
    return this;
  }

  /**
   * ID of the corresponding consent object as returned by an account information consent request. 
   * @return consentId
  */
  @NotNull 
  public String getConsentId() {
    return consentId;
  }

  public void setConsentId(String consentId) {
    this.consentId = consentId;
  }

  public ConsentsResponse201 scaMethods(List<AuthenticationObject> scaMethods) {
    this.scaMethods = scaMethods;
    return this;
  }

  public ConsentsResponse201 addScaMethodsItem(AuthenticationObject scaMethodsItem) {
    if (this.scaMethods == null) {
      this.scaMethods = new ArrayList<>();
    }
    this.scaMethods.add(scaMethodsItem);
    return this;
  }

  /**
   * This data element might be contained, if SCA is required and if the PSU has a choice between different authentication methods.  Depending on the risk management of the ASPSP this choice might be offered before or after the PSU has been identified with the first relevant factor, or if an access token is transported.  If this data element is contained, then there is also a hyperlink of type 'startAuthorisationWithAuthenticationMethodSelection' contained in the response body.  These methods shall be presented towards the PSU for selection by the TPP. 
   * @return scaMethods
  */
  @Valid 
  public List<AuthenticationObject> getScaMethods() {
    return scaMethods;
  }

  public void setScaMethods(List<AuthenticationObject> scaMethods) {
    this.scaMethods = scaMethods;
  }

  public ConsentsResponse201 chosenScaMethod(AuthenticationObject chosenScaMethod) {
    this.chosenScaMethod = chosenScaMethod;
    return this;
  }

  /**
   * Get chosenScaMethod
   * @return chosenScaMethod
  */
  @Valid 
  public AuthenticationObject getChosenScaMethod() {
    return chosenScaMethod;
  }

  public void setChosenScaMethod(AuthenticationObject chosenScaMethod) {
    this.chosenScaMethod = chosenScaMethod;
  }

  public ConsentsResponse201 challengeData(ChallengeData challengeData) {
    this.challengeData = challengeData;
    return this;
  }

  /**
   * Get challengeData
   * @return challengeData
  */
  @Valid 
  public ChallengeData getChallengeData() {
    return challengeData;
  }

  public void setChallengeData(ChallengeData challengeData) {
    this.challengeData = challengeData;
  }

  public ConsentsResponse201 links(LinksConsents links) {
    this.links = links;
    return this;
  }

  /**
   * Get links
   * @return links
  */
  @NotNull @Valid 
  public LinksConsents getLinks() {
    return links;
  }

  public void setLinks(LinksConsents links) {
    this.links = links;
  }

  public ConsentsResponse201 psuMessage(String psuMessage) {
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
    ConsentsResponse201 consentsResponse201 = (ConsentsResponse201) o;
    return Objects.equals(this.consentStatus, consentsResponse201.consentStatus) &&
        Objects.equals(this.consentId, consentsResponse201.consentId) &&
        Objects.equals(this.scaMethods, consentsResponse201.scaMethods) &&
        Objects.equals(this.chosenScaMethod, consentsResponse201.chosenScaMethod) &&
        Objects.equals(this.challengeData, consentsResponse201.challengeData) &&
        Objects.equals(this.links, consentsResponse201.links) &&
        Objects.equals(this.psuMessage, consentsResponse201.psuMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentStatus, consentId, scaMethods, chosenScaMethod, challengeData, links, psuMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentsResponse201 {\n");
    sb.append("    consentStatus: ").append(toIndentedString(consentStatus)).append("\n");
    sb.append("    consentId: ").append(toIndentedString(consentId)).append("\n");
    sb.append("    scaMethods: ").append(toIndentedString(scaMethods)).append("\n");
    sb.append("    chosenScaMethod: ").append(toIndentedString(chosenScaMethod)).append("\n");
    sb.append("    challengeData: ").append(toIndentedString(challengeData)).append("\n");
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

