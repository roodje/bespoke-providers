package com.yolt.providers.openbanking.ais.generic2.pec.mapper;

public interface PaymentRequestAdjuster<T> {

    T adjust(T dataInitiation);
}
