package com.yolt.providers.openbanking.ais.amexgroup.common.service.ais.mappers.oauthtoken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.openbanking.ais.amexgroup.common.domain.AmexLoginInfoState;
import com.yolt.providers.openbanking.ais.exception.UnexpectedJsonElementException;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.LoginInfoStateMapper;

import java.io.IOException;

public class AmexGroupLoginInfoStateMapper implements LoginInfoStateMapper<AmexLoginInfoState> {

    private final ObjectMapper objectMapper;

    public AmexGroupLoginInfoStateMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public AmexLoginInfoState fromJson(String loginInfoState) {
        try {
            return objectMapper.readValue(loginInfoState, AmexLoginInfoState.class);
        } catch (IOException e) {
            throw new GetAccessTokenFailedException("Unable to deserialize loginInfoState");
        }
    }

    @Override
    public String toJson(AmexLoginInfoState loginInfoState) {
        try {
            return objectMapper.writeValueAsString(loginInfoState);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Unable to serialize loginInfoState");
        }
    }
}
