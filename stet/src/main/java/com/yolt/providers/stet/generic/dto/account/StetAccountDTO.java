package com.yolt.providers.stet.generic.dto.account;

import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;
import java.util.Map;

@ProjectedPayload
public interface StetAccountDTO {

    @JsonPath("$.resourceId")
    String getResourceId();

    @JsonPath("$.bicFi")
    String getBicFi();

    @JsonPath("$.accountId.iban")
    String getIban();

    @JsonPath("$.accountId.other")
    Map<String, String> getOther();

    @JsonPath("$.accountId.area")
    Map<String, String> getArea();

    @JsonPath({"$.currency", "$.accountId.currency"})
    CurrencyCode getCurrency();

    @JsonPath("$.name")
    String getName();

    @JsonPath("$.cashAccountType")
    StetAccountType getType();

    @JsonPath("$.usage")
    StetAccountUsage getUsage();

    @JsonPath("$.details")
    String getDetails();

    @JsonPath("$.linkedAccount")
    String getLinkedAccount();

    @JsonPath("$.product")
    String getProduct();

    @JsonPath("$.balances")
    List<StetBalanceDTO> getBalances();

    @JsonPath("$._links.transactions.href")
    String getTransactionsUrl();
}
