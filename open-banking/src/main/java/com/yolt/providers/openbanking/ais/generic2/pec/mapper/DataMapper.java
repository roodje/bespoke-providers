package com.yolt.providers.openbanking.ais.generic2.pec.mapper;

public interface DataMapper<T, U> {

    T map(U requestDTO);
}
