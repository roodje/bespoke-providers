package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.exception.UnexpectedJsonElementException;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.Collections;

@Slf4j
public class DefaultLoginInfoStateMapper<T extends LoginInfoState> implements LoginInfoStateMapper<T> {
    private final ObjectMapper objectMapper;
    private final Class<T> type;

    public DefaultLoginInfoStateMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        type = (Class<T>) LoginInfoState.class;
    }

    @Override
    public T fromJson(String loginInfoState) {
        try {
            return objectMapper.readValue(loginInfoState, type);
        } catch (IOException e) {
            throw new GetAccessTokenFailedException("Unable to deserialize loginInfoState");
        }
    }

    @Override
    public String toJson(T loginInfoState) {
        try {
            return objectMapper.writeValueAsString(loginInfoState);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Unable to serialize loginInfoState");
        }
    }
}
