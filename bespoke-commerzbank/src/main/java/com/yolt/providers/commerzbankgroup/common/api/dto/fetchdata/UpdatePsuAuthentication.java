package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Content of the body of a Update PSU authentication request  Password subfield is used. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class UpdatePsuAuthentication   {

  @JsonProperty("psuData")
  private PsuData psuData;

  public UpdatePsuAuthentication psuData(PsuData psuData) {
    this.psuData = psuData;
    return this;
  }

  /**
   * Get psuData
   * @return psuData
  */
  @NotNull @Valid 
  public PsuData getPsuData() {
    return psuData;
  }

  public void setPsuData(PsuData psuData) {
    this.psuData = psuData;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdatePsuAuthentication updatePsuAuthentication = (UpdatePsuAuthentication) o;
    return Objects.equals(this.psuData, updatePsuAuthentication.psuData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(psuData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdatePsuAuthentication {\n");
    sb.append("    psuData: ").append(toIndentedString(psuData)).append("\n");
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

