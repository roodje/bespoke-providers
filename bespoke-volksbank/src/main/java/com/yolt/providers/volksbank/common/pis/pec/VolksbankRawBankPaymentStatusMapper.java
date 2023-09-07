package com.yolt.providers.volksbank.common.pis.pec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.RawBankPaymentStatusMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class VolksbankRawBankPaymentStatusMapper implements RawBankPaymentStatusMapper {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public RawBankPaymentStatus mapBankPaymentStatus(String rawBodyResponse) {
        var jsonNode = objectMapper.readTree(rawBodyResponse);
        var tppMessagesNode = jsonNode.findPath("tppMessages");
        if (tppMessagesNode == null || tppMessagesNode.isMissingNode()) {
            return RawBankPaymentStatus.unknown(rawBodyResponse);
        }

        var tppMessageNode = tppMessagesNode.get(0);
        var code = tppMessageNode.get("code").asText();
        var text = tppMessageNode.get("text").asText();
        return RawBankPaymentStatus.forStatus(code, text);
    }
}
