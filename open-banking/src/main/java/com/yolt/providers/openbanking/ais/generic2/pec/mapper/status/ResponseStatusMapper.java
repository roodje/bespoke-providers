package com.yolt.providers.openbanking.ais.generic2.pec.mapper.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;

public interface ResponseStatusMapper<T> {

    EnhancedPaymentStatus mapToEnhancedPaymentStatus(T status);
}
