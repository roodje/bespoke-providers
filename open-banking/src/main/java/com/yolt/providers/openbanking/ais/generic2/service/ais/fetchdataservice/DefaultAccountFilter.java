package com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class DefaultAccountFilter implements UnaryOperator<List<OBAccount6>> {

    private static final String SWITCH_ACCOUNT_STATUS = "UK.CASS.SwitchCompleted";

    @Override
    public List<OBAccount6> apply(List<OBAccount6> accountList) {
        return accountList.stream().filter(account -> !SWITCH_ACCOUNT_STATUS.equals(account.getSwitchStatus())).collect(Collectors.toList());
    }
}
