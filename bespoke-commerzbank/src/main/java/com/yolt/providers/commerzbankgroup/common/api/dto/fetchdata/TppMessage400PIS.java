package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * TppMessage400PIS
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class TppMessage400PIS   {

  @JsonProperty("category")
  private TppMessageCategory category;

  @JsonProperty("code")
  private MessageCode400PIS code;

  @JsonProperty("path")
  private String path;

  @JsonProperty("text")
  private String text;

  public TppMessage400PIS category(TppMessageCategory category) {
    this.category = category;
    return this;
  }

  /**
   * Get category
   * @return category
  */
  @NotNull @Valid 
  public TppMessageCategory getCategory() {
    return category;
  }

  public void setCategory(TppMessageCategory category) {
    this.category = category;
  }

  public TppMessage400PIS code(MessageCode400PIS code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * @return code
  */
  @NotNull @Valid 
  public MessageCode400PIS getCode() {
    return code;
  }

  public void setCode(MessageCode400PIS code) {
    this.code = code;
  }

  public TppMessage400PIS path(String path) {
    this.path = path;
    return this;
  }

  /**
   * Get path
   * @return path
  */
  
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public TppMessage400PIS text(String text) {
    this.text = text;
    return this;
  }

  /**
   * Additional explaining text to the TPP.
   * @return text
  */
  @Size(max = 500) 
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TppMessage400PIS tppMessage400PIS = (TppMessage400PIS) o;
    return Objects.equals(this.category, tppMessage400PIS.category) &&
        Objects.equals(this.code, tppMessage400PIS.code) &&
        Objects.equals(this.path, tppMessage400PIS.path) &&
        Objects.equals(this.text, tppMessage400PIS.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, code, path, text);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TppMessage400PIS {\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    text: ").append(toIndentedString(text)).append("\n");
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

