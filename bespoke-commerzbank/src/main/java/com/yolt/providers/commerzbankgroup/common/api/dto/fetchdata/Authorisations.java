package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An array of all authorisationIds.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class Authorisations   {

  @JsonProperty("authorisationIds")
  @Valid
  private List<String> authorisationIds = new ArrayList<>();

  public Authorisations authorisationIds(List<String> authorisationIds) {
    this.authorisationIds = authorisationIds;
    return this;
  }

  public Authorisations addAuthorisationIdsItem(String authorisationIdsItem) {
    this.authorisationIds.add(authorisationIdsItem);
    return this;
  }

  /**
   * An array of all authorisationIds.
   * @return authorisationIds
  */
  @NotNull 
  public List<String> getAuthorisationIds() {
    return authorisationIds;
  }

  public void setAuthorisationIds(List<String> authorisationIds) {
    this.authorisationIds = authorisationIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Authorisations authorisations = (Authorisations) o;
    return Objects.equals(this.authorisationIds, authorisations.authorisationIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authorisationIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Authorisations {\n");
    sb.append("    authorisationIds: ").append(toIndentedString(authorisationIds)).append("\n");
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

