package com.yolt.providers.yoltprovider.pis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.RawBankPaymentStatusMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;

public class YoltBankPaymentRawBankStatusMapper implements RawBankPaymentStatusMapper {

    private static final String CODE_LABEL = "code";
    private static final String MESSAGE_LABEL = "message";
    private final ObjectMapper objectMapper;

    public YoltBankPaymentRawBankStatusMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public RawBankPaymentStatus mapBankPaymentStatus(String rawBodyResponse) {
        try {
            JsonNode json = objectMapper.readTree(rawBodyResponse);
            String code = json.get(CODE_LABEL).asText();
            String message = json.get(MESSAGE_LABEL).asText();
            return RawBankPaymentStatus.forStatus(code, message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
