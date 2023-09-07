package com.yolt.providers.stet.generic.dto;

import com.yolt.providers.stet.generic.dto.account.StetAccountType;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountUsage;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import lombok.Builder;
import lombok.Getter;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class TestStetAccountDTO implements StetAccountDTO {
    
    private String resourceId;
    private String bicFi;
    private String iban;
    private Map<String, String> other;
    private Map<String, String> area;
    private CurrencyCode currency;
    private String name;
    private StetAccountType type;
    private StetAccountUsage usage;
    private String details;
    private String linkedAccount;
    private String product;
    private List<StetBalanceDTO> balances;
    private String transactionsUrl;
}
