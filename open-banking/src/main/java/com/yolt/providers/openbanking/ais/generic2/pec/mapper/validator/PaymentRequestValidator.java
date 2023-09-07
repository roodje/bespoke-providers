package com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator;

public interface PaymentRequestValidator<T> {

    void validateRequest(T dataInitiation);
}
