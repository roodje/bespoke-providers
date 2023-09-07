package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP.  Type of links admitted in this response:   - \&quot;download\&quot;: a link to a resource, where the transaction report might be downloaded from in    case where transaction reports have a huge size.  Remark: This feature shall only be used where camt-data is requested which has a huge size. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class LinksDownload extends HashMap<String, HrefType>  {

  @JsonProperty("download")
  private HrefType download;

  public LinksDownload download(HrefType download) {
    this.download = download;
    return this;
  }

  /**
   * Get download
   * @return download
  */
  @NotNull @Valid 
  public HrefType getDownload() {
    return download;
  }

  public void setDownload(HrefType download) {
    this.download = download;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinksDownload linksDownload = (LinksDownload) o;
    return Objects.equals(this.download, linksDownload.download) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(download, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksDownload {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    download: ").append(toIndentedString(download)).append("\n");
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

