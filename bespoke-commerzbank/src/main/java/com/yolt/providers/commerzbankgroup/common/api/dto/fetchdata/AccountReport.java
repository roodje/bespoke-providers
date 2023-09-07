package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JSON based account report. This account report contains transactions resulting from the query parameters.  &#39;booked&#39; shall be contained if bookingStatus parameter is set to \&quot;booked\&quot; or \&quot;both\&quot;.  &#39;pending&#39; is not contained if the bookingStatus parameter is set to \&quot;booked\&quot; or \&quot;information\&quot;.  &#39;information&#39; Only contained if the bookingStatus is set to \&quot;information\&quot; and if supported by ASPSP. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class AccountReport   {

  @JsonProperty("booked")
  @Valid
  private List<Transactions> booked = null;

  @JsonProperty("pending")
  @Valid
  private List<Transactions> pending = null;

  @JsonProperty("information")
  @Valid
  private List<Transactions> information = null;

  @JsonProperty("_links")
  private LinksAccountReport links;

  public AccountReport booked(List<Transactions> booked) {
    this.booked = booked;
    return this;
  }

  public AccountReport addBookedItem(Transactions bookedItem) {
    if (this.booked == null) {
      this.booked = new ArrayList<>();
    }
    this.booked.add(bookedItem);
    return this;
  }

  /**
   * Array of transaction details.
   * @return booked
  */
  @Valid 
  public List<Transactions> getBooked() {
    return booked;
  }

  public void setBooked(List<Transactions> booked) {
    this.booked = booked;
  }

  public AccountReport pending(List<Transactions> pending) {
    this.pending = pending;
    return this;
  }

  public AccountReport addPendingItem(Transactions pendingItem) {
    if (this.pending == null) {
      this.pending = new ArrayList<>();
    }
    this.pending.add(pendingItem);
    return this;
  }

  /**
   * Array of transaction details.
   * @return pending
  */
  @Valid 
  public List<Transactions> getPending() {
    return pending;
  }

  public void setPending(List<Transactions> pending) {
    this.pending = pending;
  }

  public AccountReport information(List<Transactions> information) {
    this.information = information;
    return this;
  }

  public AccountReport addInformationItem(Transactions informationItem) {
    if (this.information == null) {
      this.information = new ArrayList<>();
    }
    this.information.add(informationItem);
    return this;
  }

  /**
   * Array of transaction details.
   * @return information
  */
  @Valid 
  public List<Transactions> getInformation() {
    return information;
  }

  public void setInformation(List<Transactions> information) {
    this.information = information;
  }

  public AccountReport links(LinksAccountReport links) {
    this.links = links;
    return this;
  }

  /**
   * Get links
   * @return links
  */
  @NotNull @Valid 
  public LinksAccountReport getLinks() {
    return links;
  }

  public void setLinks(LinksAccountReport links) {
    this.links = links;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountReport accountReport = (AccountReport) o;
    return Objects.equals(this.booked, accountReport.booked) &&
        Objects.equals(this.pending, accountReport.pending) &&
        Objects.equals(this.information, accountReport.information) &&
        Objects.equals(this.links, accountReport.links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(booked, pending, information, links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountReport {\n");
    sb.append("    booked: ").append(toIndentedString(booked)).append("\n");
    sb.append("    pending: ").append(toIndentedString(pending)).append("\n");
    sb.append("    information: ").append(toIndentedString(information)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
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

