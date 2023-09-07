package com.yolt.providers.openbanking.ais.hsbcgroup.hsbc.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
@FunctionalInterface
public interface SupplierWithTokenInvalidException<T> {

  T get() throws TokenInvalidException;
}
