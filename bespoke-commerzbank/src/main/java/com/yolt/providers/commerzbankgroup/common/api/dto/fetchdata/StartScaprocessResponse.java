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
 * Body of the JSON response for a Start SCA authorisation request.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class StartScaprocessResponse   {

  @JsonProperty("scaStatus")
  private ScaStatus scaStatus;

  @JsonProperty("authorisationId")
  private String authorisationId;

  @JsonProperty("scaMethods")
  @Valid
  private List<AuthenticationObject> scaMethods = null;

  @JsonProperty("chosenScaMethod")
  private AuthenticationObject chosenScaMethod;

  @JsonProperty("challengeData")
  private ChallengeData challengeData;

  @JsonProperty("_links")
  private LinksStartScaProcess links;

  @JsonProperty("psuMessage")
  private String psuMessage;

  public StartScaprocessResponse scaStatus(ScaStatus scaStatus) {
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

  public StartScaprocessResponse authorisationId(String authorisationId) {
    this.authorisationId = authorisationId;
    return this;
  }

  /**
   * Resource identification of the related SCA.
   * @return authorisationId
  */
  @NotNull 
  public String getAuthorisationId() {
    return authorisationId;
  }

  public void setAuthorisationId(String authorisationId) {
    this.authorisationId = authorisationId;
  }

  public StartScaprocessResponse scaMethods(List<AuthenticationObject> scaMethods) {
    this.scaMethods = scaMethods;
    return this;
  }

  public StartScaprocessResponse addScaMethodsItem(AuthenticationObject scaMethodsItem) {
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

  public StartScaprocessResponse chosenScaMethod(AuthenticationObject chosenScaMethod) {
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

  public StartScaprocessResponse challengeData(ChallengeData challengeData) {
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

  public StartScaprocessResponse links(LinksStartScaProcess links) {
    this.links = links;
    return this;
  }

  /**
   * Get links
   * @return links
  */
  @NotNull @Valid 
  public LinksStartScaProcess getLinks() {
    return links;
  }

  public void setLinks(LinksStartScaProcess links) {
    this.links = links;
  }

  public StartScaprocessResponse psuMessage(String psuMessage) {
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
    StartScaprocessResponse startScaprocessResponse = (StartScaprocessResponse) o;
    return Objects.equals(this.scaStatus, startScaprocessResponse.scaStatus) &&
        Objects.equals(this.authorisationId, startScaprocessResponse.authorisationId) &&
        Objects.equals(this.scaMethods, startScaprocessResponse.scaMethods) &&
        Objects.equals(this.chosenScaMethod, startScaprocessResponse.chosenScaMethod) &&
        Objects.equals(this.challengeData, startScaprocessResponse.challengeData) &&
        Objects.equals(this.links, startScaprocessResponse.links) &&
        Objects.equals(this.psuMessage, startScaprocessResponse.psuMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaStatus, authorisationId, scaMethods, chosenScaMethod, challengeData, links, psuMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StartScaprocessResponse {\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    authorisationId: ").append(toIndentedString(authorisationId)).append("\n");
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

