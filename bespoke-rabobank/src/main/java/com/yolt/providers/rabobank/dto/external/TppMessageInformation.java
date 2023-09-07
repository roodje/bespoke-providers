package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * TppMessageInformation
 */
public class TppMessageInformation {
    @JsonProperty("category")
    private CategoryEnum category = null;
    @JsonProperty("code")
    private CodeEnum code = null;
    @JsonProperty("path")
    private String path = null;
    @JsonProperty("text")
    private String text = null;

    public TppMessageInformation category(CategoryEnum category) {
        this.category = category;
        return this;
    }

    /**
     * Indicates the type of the information.
     *
     * @return category
     **/
    @NotNull
    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public TppMessageInformation code(CodeEnum code) {
        this.code = code;
        return this;
    }

    /**
     * Gives details about what to do next or indicates the reason of the request message or a data element requested.
     *
     * @return code
     **/
    @NotNull
    public CodeEnum getCode() {
        return code;
    }

    public void setCode(CodeEnum code) {
        this.code = code;
    }

    public TppMessageInformation path(String path) {
        this.path = path;
        return this;
    }

    /**
     * Indicates data element of the request message or a data element requested.
     *
     * @return path
     **/
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public TppMessageInformation text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get text
     *
     * @return text
     **/
    @Size(min = 2, max = 512)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TppMessageInformation tppMessageInformation = (TppMessageInformation) o;
        return Objects.equals(this.category, tppMessageInformation.category) &&
                Objects.equals(this.code, tppMessageInformation.code) &&
                Objects.equals(this.path, tppMessageInformation.path) &&
                Objects.equals(this.text, tppMessageInformation.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, code, path, text);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TppMessageInformation {\n");

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
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Indicates the type of the information.
     */
    public enum CategoryEnum {
        ERROR("ERROR"),

        WARNING("WARNING");

        private String value;

        CategoryEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static CategoryEnum fromValue(String text) {
            for (CategoryEnum b : CategoryEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + text + "'");
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }

    /**
     * Gives details about what to do next or indicates the reason of the request message or a data element requested.
     */
    public enum CodeEnum {
        CERTIFICATE_INVALID("CERTIFICATE_INVALID"),

        CERTIFICATE_EXPIRED("CERTIFICATE_EXPIRED"),

        CERTIFICATE_BLOCKED("CERTIFICATE_BLOCKED"),

        CERTIFICATE_REVOKED("CERTIFICATE_REVOKED"),

        CERTIFICATE_MISSING("CERTIFICATE_MISSING"),

        SIGNATURE_INVALID("SIGNATURE_INVALID"),

        SIGNATURE_MISSING("SIGNATURE_MISSING"),

        FORMAT_ERROR("FORMAT_ERROR"),

        PARAMETER_NOT_CONSISTENT("PARAMETER_NOT_CONSISTENT"),

        PARAMETER_NOT_SUPPORTED("PARAMETER_NOT_SUPPORTED"),

        PSU_CREDENTIALS_INVALID("PSU_CREDENTIALS_INVALID"),

        SERVICE_INVALID_PAYLOAD("SERVICE_INVALID_PAYLOAD"),

        SERVICE_INVALID_HTTP("SERVICE_INVALID_HTTP"),

        SERVICE_BLOCKED("SERVICE_BLOCKED"),

        CORPORATE_ID_INVALID("CORPORATE_ID_INVALID"),

        CONSENT_UNKNOWN_PATH("CONSENT_UNKNOWN_PATH"),

        CONSENT_UNKNOWN_PAYLOAD("CONSENT_UNKNOWN_PAYLOAD"),

        CONSENT_INVALID("CONSENT_INVALID"),

        CONSENT_EXPIRED("CONSENT_EXPIRED"),

        TOKEN_UNKNOWN("TOKEN_UNKNOWN"),

        TOKEN_INVALID("TOKEN_INVALID"),

        TOKEN_EXPIRED("TOKEN_EXPIRED"),

        RESOURCE_UNKNOWN_PATH_ACCOUNTID("RESOURCE_UNKNOWN_PATH_ACCOUNTID"),

        RESOURCE_UNKNOWN_PATH_OTHER("RESOURCE_UNKNOWN_PATH_OTHER"),

        RESOURCE_UNKNOWN_PAYLOAD("RESOURCE_UNKNOWN_PAYLOAD"),

        RESOURCE_EXPIRED_PATH("RESOURCE_EXPIRED_PATH"),

        RESOURCE_EXPIRED_PAYLOAD("RESOURCE_EXPIRED_PAYLOAD"),

        RESOURCE_BLOCKED("RESOURCE_BLOCKED"),

        TIMESTAMP_INVALID("TIMESTAMP_INVALID"),

        PERIOD_INVALID("PERIOD_INVALID"),

        SCA_METHOD_UNKNOWN("SCA_METHOD_UNKNOWN"),

        STATUS_INVALID("STATUS_INVALID"),

        PRODUCT_INVALID("PRODUCT_INVALID"),

        PRODUCT_UNKNOWN("PRODUCT_UNKNOWN"),

        PAYMENT_FAILED("PAYMENT_FAILED"),

        REQUIRED_KID_MISSING("REQUIRED_KID_MISSING"),

        EXECUTION_DATE_INVALID("EXECUTION_DATE_INVALID"),

        UNKNOWN("UNKNOWN");

        private String value;

        CodeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static CodeEnum fromValue(String text) {
            for (CodeEnum b : CodeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + text + "'");
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}

