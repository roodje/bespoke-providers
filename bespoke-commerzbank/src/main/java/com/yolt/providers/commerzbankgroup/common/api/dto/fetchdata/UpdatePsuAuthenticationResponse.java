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
 * Body of the JSON response for a successful update PSU authentication request.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class UpdatePsuAuthenticationResponse   {

  @JsonProperty("transactionFees")
  private Amount transactionFees;

  @JsonProperty("currencyConversionFees")
  private Amount currencyConversionFees;

  @JsonProperty("estimatedTotalAmount")
  private Amount estimatedTotalAmount;

  @JsonProperty("estimatedInterbankSettlementAmount")
  private Amount estimatedInterbankSettlementAmount;

  @JsonProperty("chosenScaMethod")
  private AuthenticationObject chosenScaMethod;

  @JsonProperty("challengeData")
  private ChallengeData challengeData;

  @JsonProperty("scaMethods")
  @Valid
  private List<AuthenticationObject> scaMethods = null;

  @JsonProperty("_links")
  private LinksUpdatePsuAuthentication links;

  @JsonProperty("scaStatus")
  private ScaStatus scaStatus;

  @JsonProperty("psuMessage")
  private String psuMessage;

  @JsonProperty("authorisationId")
  private String authorisationId;

  public UpdatePsuAuthenticationResponse transactionFees(Amount transactionFees) {
    this.transactionFees = transactionFees;
    return this;
  }

  /**
   * Get transactionFees
   * @return transactionFees
  */
  @Valid 
  public Amount getTransactionFees() {
    return transactionFees;
  }

  public void setTransactionFees(Amount transactionFees) {
    this.transactionFees = transactionFees;
  }

  public UpdatePsuAuthenticationResponse currencyConversionFees(Amount currencyConversionFees) {
    this.currencyConversionFees = currencyConversionFees;
    return this;
  }

  /**
   * Get currencyConversionFees
   * @return currencyConversionFees
  */
  @Valid 
  public Amount getCurrencyConversionFees() {
    return currencyConversionFees;
  }

  public void setCurrencyConversionFees(Amount currencyConversionFees) {
    this.currencyConversionFees = currencyConversionFees;
  }

  public UpdatePsuAuthenticationResponse estimatedTotalAmount(Amount estimatedTotalAmount) {
    this.estimatedTotalAmount = estimatedTotalAmount;
    return this;
  }

  /**
   * Get estimatedTotalAmount
   * @return estimatedTotalAmount
  */
  @Valid 
  public Amount getEstimatedTotalAmount() {
    return estimatedTotalAmount;
  }

  public void setEstimatedTotalAmount(Amount estimatedTotalAmount) {
    this.estimatedTotalAmount = estimatedTotalAmount;
  }

  public UpdatePsuAuthenticationResponse estimatedInterbankSettlementAmount(Amount estimatedInterbankSettlementAmount) {
    this.estimatedInterbankSettlementAmount = estimatedInterbankSettlementAmount;
    return this;
  }

  /**
   * Get estimatedInterbankSettlementAmount
   * @return estimatedInterbankSettlementAmount
  */
  @Valid 
  public Amount getEstimatedInterbankSettlementAmount() {
    return estimatedInterbankSettlementAmount;
  }

  public void setEstimatedInterbankSettlementAmount(Amount estimatedInterbankSettlementAmount) {
    this.estimatedInterbankSettlementAmount = estimatedInterbankSettlementAmount;
  }

  public UpdatePsuAuthenticationResponse chosenScaMethod(AuthenticationObject chosenScaMethod) {
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

  public UpdatePsuAuthenticationResponse challengeData(ChallengeData challengeData) {
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

  public UpdatePsuAuthenticationResponse scaMethods(List<AuthenticationObject> scaMethods) {
    this.scaMethods = scaMethods;
    return this;
  }

  public UpdatePsuAuthenticationResponse addScaMethodsItem(AuthenticationObject scaMethodsItem) {
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

  public UpdatePsuAuthenticationResponse links(LinksUpdatePsuAuthentication links) {
    this.links = links;
    return this;
  }

  /**
   * Get links
   * @return links
  */
  @Valid 
  public LinksUpdatePsuAuthentication getLinks() {
    return links;
  }

  public void setLinks(LinksUpdatePsuAuthentication links) {
    this.links = links;
  }

  public UpdatePsuAuthenticationResponse scaStatus(ScaStatus scaStatus) {
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

  public UpdatePsuAuthenticationResponse psuMessage(String psuMessage) {
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

  public UpdatePsuAuthenticationResponse authorisationId(String authorisationId) {
    this.authorisationId = authorisationId;
    return this;
  }

  /**
   * Resource identification of the related SCA.
   * @return authorisationId
  */
  
  public String getAuthorisationId() {
    return authorisationId;
  }

  public void setAuthorisationId(String authorisationId) {
    this.authorisationId = authorisationId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdatePsuAuthenticationResponse updatePsuAuthenticationResponse = (UpdatePsuAuthenticationResponse) o;
    return Objects.equals(this.transactionFees, updatePsuAuthenticationResponse.transactionFees) &&
        Objects.equals(this.currencyConversionFees, updatePsuAuthenticationResponse.currencyConversionFees) &&
        Objects.equals(this.estimatedTotalAmount, updatePsuAuthenticationResponse.estimatedTotalAmount) &&
        Objects.equals(this.estimatedInterbankSettlementAmount, updatePsuAuthenticationResponse.estimatedInterbankSettlementAmount) &&
        Objects.equals(this.chosenScaMethod, updatePsuAuthenticationResponse.chosenScaMethod) &&
        Objects.equals(this.challengeData, updatePsuAuthenticationResponse.challengeData) &&
        Objects.equals(this.scaMethods, updatePsuAuthenticationResponse.scaMethods) &&
        Objects.equals(this.links, updatePsuAuthenticationResponse.links) &&
        Objects.equals(this.scaStatus, updatePsuAuthenticationResponse.scaStatus) &&
        Objects.equals(this.psuMessage, updatePsuAuthenticationResponse.psuMessage) &&
        Objects.equals(this.authorisationId, updatePsuAuthenticationResponse.authorisationId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionFees, currencyConversionFees, estimatedTotalAmount, estimatedInterbankSettlementAmount, chosenScaMethod, challengeData, scaMethods, links, scaStatus, psuMessage, authorisationId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdatePsuAuthenticationResponse {\n");
    sb.append("    transactionFees: ").append(toIndentedString(transactionFees)).append("\n");
    sb.append("    currencyConversionFees: ").append(toIndentedString(currencyConversionFees)).append("\n");
    sb.append("    estimatedTotalAmount: ").append(toIndentedString(estimatedTotalAmount)).append("\n");
    sb.append("    estimatedInterbankSettlementAmount: ").append(toIndentedString(estimatedInterbankSettlementAmount)).append("\n");
    sb.append("    chosenScaMethod: ").append(toIndentedString(chosenScaMethod)).append("\n");
    sb.append("    challengeData: ").append(toIndentedString(challengeData)).append("\n");
    sb.append("    scaMethods: ").append(toIndentedString(scaMethods)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
    sb.append("    authorisationId: ").append(toIndentedString(authorisationId)).append("\n");
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

