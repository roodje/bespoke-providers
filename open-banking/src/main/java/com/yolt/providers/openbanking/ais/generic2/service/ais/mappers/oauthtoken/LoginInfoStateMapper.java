package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken;

import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;

public interface LoginInfoStateMapper<T extends LoginInfoState> {
    String toJson(T loginState);

    T fromJson(String serializedLoginState);
}
