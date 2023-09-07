package com.yolt.providers.alpha.common.auth.dto;

import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.Objects;

/**
 * List of Product Identifiers
 */
@ApiModel(description = "List of Product Identifiers")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-07-18T15:36:09.705982700+02:00[Europe/Warsaw]")

public class ProductIdentifiers extends ArrayList<ProductIdentifier> {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProductIdentifiers {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
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

