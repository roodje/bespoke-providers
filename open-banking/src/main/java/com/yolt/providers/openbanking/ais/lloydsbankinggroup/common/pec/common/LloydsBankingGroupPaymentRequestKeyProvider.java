package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.pec.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.exception.UnexpectedJsonElementException;
import com.yolt.providers.openbanking.ais.generic2.pec.common.PaymentRequestIdempotentKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class LloydsBankingGroupPaymentRequestKeyProvider implements PaymentRequestIdempotentKeyProvider {

    private final ObjectMapper objectMapper;

    @Override
    public String provideIdempotentKey(Object parameter) {
        try {
            byte[] data = objectMapper.writeValueAsString(parameter).getBytes(StandardCharsets.UTF_8);
            return DigestUtils.md5DigestAsHex(data);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Could not convert request body to byte array");
        }
    }
}
