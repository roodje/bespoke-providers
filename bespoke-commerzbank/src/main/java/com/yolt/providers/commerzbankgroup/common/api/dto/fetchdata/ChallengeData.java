package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * It is contained in addition to the data element &#39;chosenScaMethod&#39; if challenge data is needed for SCA. In rare cases this attribute is also used in the context of the &#39;startAuthorisationWithPsuAuthentication&#39; link. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class ChallengeData   {

  @JsonProperty("image")
  private byte[] image;

  @JsonProperty("data")
  @Valid
  private List<String> data = null;

  @JsonProperty("imageLink")
  private String imageLink;

  @JsonProperty("otpMaxLength")
  private Integer otpMaxLength;

  /**
   * The format type of the OTP to be typed in. The admitted values are \"characters\" or \"integer\".
   */
  public enum OtpFormatEnum {
    CHARACTERS("characters"),
    
    INTEGER("integer");

    private String value;

    OtpFormatEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static OtpFormatEnum fromValue(String value) {
      for (OtpFormatEnum b : OtpFormatEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @JsonProperty("otpFormat")
  private OtpFormatEnum otpFormat;

  @JsonProperty("additionalInformation")
  private String additionalInformation;

  public ChallengeData image(byte[] image) {
    this.image = image;
    return this;
  }

  /**
   * PNG data (max. 512 kilobyte) to be displayed to the PSU, Base64 encoding, cp. [RFC4648]. This attribute is used only, when PHOTO_OTP or CHIP_OTP is the selected SCA method. 
   * @return image
  */
  
  public byte[] getImage() {
    return image;
  }

  public void setImage(byte[] image) {
    this.image = image;
  }

  public ChallengeData data(List<String> data) {
    this.data = data;
    return this;
  }

  public ChallengeData addDataItem(String dataItem) {
    if (this.data == null) {
      this.data = new ArrayList<>();
    }
    this.data.add(dataItem);
    return this;
  }

  /**
   * A collection of strings as challenge data.
   * @return data
  */
  
  public List<String> getData() {
    return data;
  }

  public void setData(List<String> data) {
    this.data = data;
  }

  public ChallengeData imageLink(String imageLink) {
    this.imageLink = imageLink;
    return this;
  }

  /**
   * A link where the ASPSP will provides the challenge image for the TPP.
   * @return imageLink
  */
  
  public String getImageLink() {
    return imageLink;
  }

  public void setImageLink(String imageLink) {
    this.imageLink = imageLink;
  }

  public ChallengeData otpMaxLength(Integer otpMaxLength) {
    this.otpMaxLength = otpMaxLength;
    return this;
  }

  /**
   * The maximal length for the OTP to be typed in by the PSU.
   * @return otpMaxLength
  */
  
  public Integer getOtpMaxLength() {
    return otpMaxLength;
  }

  public void setOtpMaxLength(Integer otpMaxLength) {
    this.otpMaxLength = otpMaxLength;
  }

  public ChallengeData otpFormat(OtpFormatEnum otpFormat) {
    this.otpFormat = otpFormat;
    return this;
  }

  /**
   * The format type of the OTP to be typed in. The admitted values are \"characters\" or \"integer\".
   * @return otpFormat
  */
  
  public OtpFormatEnum getOtpFormat() {
    return otpFormat;
  }

  public void setOtpFormat(OtpFormatEnum otpFormat) {
    this.otpFormat = otpFormat;
  }

  public ChallengeData additionalInformation(String additionalInformation) {
    this.additionalInformation = additionalInformation;
    return this;
  }

  /**
   * Additional explanation for the PSU to explain e.g. fallback mechanism for the chosen SCA method. The TPP is obliged to show this to the PSU. 
   * @return additionalInformation
  */
  
  public String getAdditionalInformation() {
    return additionalInformation;
  }

  public void setAdditionalInformation(String additionalInformation) {
    this.additionalInformation = additionalInformation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChallengeData challengeData = (ChallengeData) o;
    return Arrays.equals(this.image, challengeData.image) &&
        Objects.equals(this.data, challengeData.data) &&
        Objects.equals(this.imageLink, challengeData.imageLink) &&
        Objects.equals(this.otpMaxLength, challengeData.otpMaxLength) &&
        Objects.equals(this.otpFormat, challengeData.otpFormat) &&
        Objects.equals(this.additionalInformation, challengeData.additionalInformation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(image), data, imageLink, otpMaxLength, otpFormat, additionalInformation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ChallengeData {\n");
    sb.append("    image: ").append(toIndentedString(image)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    imageLink: ").append(toIndentedString(imageLink)).append("\n");
    sb.append("    otpMaxLength: ").append(toIndentedString(otpMaxLength)).append("\n");
    sb.append("    otpFormat: ").append(toIndentedString(otpFormat)).append("\n");
    sb.append("    additionalInformation: ").append(toIndentedString(additionalInformation)).append("\n");
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

