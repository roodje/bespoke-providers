package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * InlineResponse2001
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class InlineResponse2001   {

  @JsonProperty("transactionsDetails")
  private TransactionDetailsBody transactionsDetails;

  public InlineResponse2001 transactionsDetails(TransactionDetailsBody transactionsDetails) {
    this.transactionsDetails = transactionsDetails;
    return this;
  }

  /**
   * Get transactionsDetails
   * @return transactionsDetails
  */
  @NotNull @Valid 
  public TransactionDetailsBody getTransactionsDetails() {
    return transactionsDetails;
  }

  public void setTransactionsDetails(TransactionDetailsBody transactionsDetails) {
    this.transactionsDetails = transactionsDetails;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InlineResponse2001 inlineResponse2001 = (InlineResponse2001) o;
    return Objects.equals(this.transactionsDetails, inlineResponse2001.transactionsDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionsDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse2001 {\n");
    sb.append("    transactionsDetails: ").append(toIndentedString(transactionsDetails)).append("\n");
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

