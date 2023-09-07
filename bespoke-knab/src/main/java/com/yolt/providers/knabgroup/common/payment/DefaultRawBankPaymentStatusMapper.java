package com.yolt.providers.knabgroup.common.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.RawBankPaymentStatusMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class DefaultRawBankPaymentStatusMapper implements RawBankPaymentStatusMapper {

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows(JsonProcessingException.class)
    public RawBankPaymentStatus mapBankPaymentStatus(String rawBodyResponse) {
        var jsonNode = objectMapper.readTree(rawBodyResponse);
        var errorsNode = jsonNode.get("tppMessages");
        if (errorsNode == null || !errorsNode.isArray()) {
            return RawBankPaymentStatus.forStatus(rawBodyResponse, "");
        }

        var errorNode = errorsNode.get(0);
        var code = errorNode.get("code").asText();
        var message = errorNode.get("text").asText();
        return RawBankPaymentStatus.forStatus(code, message);
    }
}
