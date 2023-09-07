package com.yolt.providers.stet.generic.mapper.providerstate;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.PaymentProviderState;

public interface ProviderStateMapper {

    <T> String mapToJson(T providerState);

    DataProviderState mapToDataProviderState(String jsonProviderState) throws TokenInvalidException;

    PaymentProviderState mapToPaymentProviderState(String jsonProviderState);
}
