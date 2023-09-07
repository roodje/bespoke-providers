package com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata;

import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.transaction.DefaultTransactionMapper;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

public class BnpParibasGroupTransactionMapper extends DefaultTransactionMapper {
    public BnpParibasGroupTransactionMapper(DateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
    }

    @Override
    protected String mapToDescription(List<String> unstructuredRemittanceInformation) {
        return ObjectUtils.isEmpty(unstructuredRemittanceInformation) ? "" : String.join(" ", unstructuredRemittanceInformation);
    }
}
