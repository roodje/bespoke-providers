package com.yolt.providers.starlingbank.common.paymentexecutioncontext.mapper;

import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.RawBankPaymentStatusMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;

public class StarlingBankRawBankPaymentStatusMapper implements RawBankPaymentStatusMapper {

    @Override
    public RawBankPaymentStatus mapBankPaymentStatus(String rawBodyResponse) {
        return RawBankPaymentStatus.unknown();
    }
}
