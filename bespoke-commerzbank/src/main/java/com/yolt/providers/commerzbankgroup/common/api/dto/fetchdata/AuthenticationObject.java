package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Authentication object. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class AuthenticationObject   {

  @JsonProperty("authenticationType")
  private AuthenticationType authenticationType;

  @JsonProperty("authenticationVersion")
  private String authenticationVersion;

  @JsonProperty("authenticationMethodId")
  private String authenticationMethodId;

  @JsonProperty("name")
  private String name;

  @JsonProperty("explanation")
  private String explanation;

  public AuthenticationObject authenticationType(AuthenticationType authenticationType) {
    this.authenticationType = authenticationType;
    return this;
  }

  /**
   * Get authenticationType
   * @return authenticationType
  */
  @NotNull @Valid 
  public AuthenticationType getAuthenticationType() {
    return authenticationType;
  }

  public void setAuthenticationType(AuthenticationType authenticationType) {
    this.authenticationType = authenticationType;
  }

  public AuthenticationObject authenticationVersion(String authenticationVersion) {
    this.authenticationVersion = authenticationVersion;
    return this;
  }

  /**
   * Depending on the \"authenticationType\". This version can be used by differentiating authentication tools used within performing OTP generation in the same authentication type. This version can be referred to in the ASPSP?s documentation. 
   * @return authenticationVersion
  */
  
  public String getAuthenticationVersion() {
    return authenticationVersion;
  }

  public void setAuthenticationVersion(String authenticationVersion) {
    this.authenticationVersion = authenticationVersion;
  }

  public AuthenticationObject authenticationMethodId(String authenticationMethodId) {
    this.authenticationMethodId = authenticationMethodId;
    return this;
  }

  /**
   * An identification provided by the ASPSP for the later identification of the authentication method selection. 
   * @return authenticationMethodId
  */
  @NotNull @Size(max = 35) 
  public String getAuthenticationMethodId() {
    return authenticationMethodId;
  }

  public void setAuthenticationMethodId(String authenticationMethodId) {
    this.authenticationMethodId = authenticationMethodId;
  }

  public AuthenticationObject name(String name) {
    this.name = name;
    return this;
  }

  /**
   * This is the name of the authentication method defined by the PSU in the Online Banking frontend of the ASPSP. Alternatively this could be a description provided by the ASPSP like \"SMS OTP on phone +49160 xxxxx 28\". This name shall be used by the TPP when presenting a list of authentication methods to the PSU, if available. 
   * @return name
  */
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AuthenticationObject explanation(String explanation) {
    this.explanation = explanation;
    return this;
  }

  /**
   * Detailed information about the SCA method for the PSU. 
   * @return explanation
  */
  
  public String getExplanation() {
    return explanation;
  }

  public void setExplanation(String explanation) {
    this.explanation = explanation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuthenticationObject authenticationObject = (AuthenticationObject) o;
    return Objects.equals(this.authenticationType, authenticationObject.authenticationType) &&
        Objects.equals(this.authenticationVersion, authenticationObject.authenticationVersion) &&
        Objects.equals(this.authenticationMethodId, authenticationObject.authenticationMethodId) &&
        Objects.equals(this.name, authenticationObject.name) &&
        Objects.equals(this.explanation, authenticationObject.explanation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authenticationType, authenticationVersion, authenticationMethodId, name, explanation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthenticationObject {\n");
    sb.append("    authenticationType: ").append(toIndentedString(authenticationType)).append("\n");
    sb.append("    authenticationVersion: ").append(toIndentedString(authenticationVersion)).append("\n");
    sb.append("    authenticationMethodId: ").append(toIndentedString(authenticationMethodId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    explanation: ").append(toIndentedString(explanation)).append("\n");
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

