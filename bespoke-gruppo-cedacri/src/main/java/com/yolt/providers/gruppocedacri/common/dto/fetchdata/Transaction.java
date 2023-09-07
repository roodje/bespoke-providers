package com.yolt.providers.gruppocedacri.common.dto.fetchdata;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.util.List;

@ProjectedPayload
public interface Transaction {

    @JsonPath("$.transactionAmount.currency")
    String getCurrency();

    @JsonPath("$.transactionAmount.amount")
    String getAmount();

    @JsonPath("$.bookingDate")
    String getBookingDate();

    @JsonPath("$.remittanceInformationUnstructured")
    String getRemittanceInformationUnstructured();

    @JsonPath("$.remittanceInformationUnstructuredArray")
    List<String> getRemittanceInformationUnstructuredArray();

    default BigDecimal getDecimalAmount() {
        return new BigDecimal(getAmount());
    }
}
