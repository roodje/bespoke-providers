package com.yolt.providers.openbanking.ais.barclaysgroup.common.service.ais.fetchdataservice;

import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class BarclaysGroupAccountFilter extends DefaultAccountFilter {

    @Override
    public List<OBAccount6> apply(List<OBAccount6> accountList) {
        return super.apply(accountList).stream()
                .filter(this::isNotTravelWalletAccount)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("java:S2589") //We have previously registered cases of this field being null.
    private boolean isNotTravelWalletAccount(OBAccount6 account) {
        return !(account.getAccountType() == null
                && OBExternalAccountSubType1Code.CURRENTACCOUNT.equals(account.getAccountSubType())
                && StringUtils.isNotBlank(account.getDescription())
                && account.getDescription().contains("travel wallet"));
    }
}
