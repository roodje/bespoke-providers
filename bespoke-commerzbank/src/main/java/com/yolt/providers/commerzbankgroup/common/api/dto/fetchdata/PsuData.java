package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.Objects;

/**
 * PSU Data for Update PSU authentication.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class PsuData   {

  @JsonProperty("password")
  private String password;

  @JsonProperty("encryptedPassword")
  private String encryptedPassword;

  @JsonProperty("additionalPassword")
  private String additionalPassword;

  @JsonProperty("additionalEncryptedPassword")
  private String additionalEncryptedPassword;

  public PsuData password(String password) {
    this.password = password;
    return this;
  }

  /**
   * Password.
   * @return password
  */
  
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public PsuData encryptedPassword(String encryptedPassword) {
    this.encryptedPassword = encryptedPassword;
    return this;
  }

  /**
   * Encrypted password.
   * @return encryptedPassword
  */
  
  public String getEncryptedPassword() {
    return encryptedPassword;
  }

  public void setEncryptedPassword(String encryptedPassword) {
    this.encryptedPassword = encryptedPassword;
  }

  public PsuData additionalPassword(String additionalPassword) {
    this.additionalPassword = additionalPassword;
    return this;
  }

  /**
   * Additional password in plaintext.
   * @return additionalPassword
  */
  
  public String getAdditionalPassword() {
    return additionalPassword;
  }

  public void setAdditionalPassword(String additionalPassword) {
    this.additionalPassword = additionalPassword;
  }

  public PsuData additionalEncryptedPassword(String additionalEncryptedPassword) {
    this.additionalEncryptedPassword = additionalEncryptedPassword;
    return this;
  }

  /**
   * Additional encrypted password.
   * @return additionalEncryptedPassword
  */
  
  public String getAdditionalEncryptedPassword() {
    return additionalEncryptedPassword;
  }

  public void setAdditionalEncryptedPassword(String additionalEncryptedPassword) {
    this.additionalEncryptedPassword = additionalEncryptedPassword;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PsuData psuData = (PsuData) o;
    return Objects.equals(this.password, psuData.password) &&
        Objects.equals(this.encryptedPassword, psuData.encryptedPassword) &&
        Objects.equals(this.additionalPassword, psuData.additionalPassword) &&
        Objects.equals(this.additionalEncryptedPassword, psuData.additionalEncryptedPassword);
  }

  @Override
  public int hashCode() {
    return Objects.hash(password, encryptedPassword, additionalPassword, additionalEncryptedPassword);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PsuData {\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
    sb.append("    encryptedPassword: ").append(toIndentedString(encryptedPassword)).append("\n");
    sb.append("    additionalPassword: ").append(toIndentedString(additionalPassword)).append("\n");
    sb.append("    additionalEncryptedPassword: ").append(toIndentedString(additionalEncryptedPassword)).append("\n");
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

