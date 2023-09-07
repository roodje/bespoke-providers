package com.yolt.providers.monorepogroup.chebancagroup.common.dto.external;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.time.LocalDate;

@ProjectedPayload
public interface Transaction {

    @JsonPath("$.index")
    String getIndex();

    @JsonPath("$.dateAccountingCurrency")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    LocalDate getDateAccountingCurrency();

    @JsonPath("$.amountTransaction.currency")
    String getAmountTransactionsCurrency();

    @JsonPath("$.amountTransaction.amount")
    BigDecimal getAmountTransactionsAmount();

    @JsonPath("$.shortDescription")
    String getDescription();


    @JsonPath("$.idMoneyTransfer")
    String getIdMoneyTransfer();
}
