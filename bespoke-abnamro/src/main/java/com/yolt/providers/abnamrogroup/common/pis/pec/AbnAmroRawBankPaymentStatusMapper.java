package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.RawBankPaymentStatusMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class AbnAmroRawBankPaymentStatusMapper implements RawBankPaymentStatusMapper {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public RawBankPaymentStatus mapBankPaymentStatus(String rawBodyResponse) {
        var jsonNode = objectMapper.readTree(rawBodyResponse);
        var errorsNode = jsonNode.get("errors");
        if (errorsNode == null || !errorsNode.isArray()) {
            return RawBankPaymentStatus.forStatus(rawBodyResponse, "");
        }

        var errorNode = errorsNode.get(0);
        var code = errorNode.get("code").asText();
        var message = errorNode.get("message").asText();
        return RawBankPaymentStatus.forStatus(code, message);
    }
}
