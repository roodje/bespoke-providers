package com.yolt.providers.stet.generic.service.pec.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.RawBankPaymentStatusMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;

@RequiredArgsConstructor
public class StetRawBankPaymentStatusMapper implements RawBankPaymentStatusMapper {

    private static final String STATUS_FIELD_NAME = "error";
    private static final String REASON_FIELD_NAME = "message";

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public RawBankPaymentStatus mapBankPaymentStatus(String rawResponseBody) {
        String responseBody = Objects.isNull(rawResponseBody) ? "" : rawResponseBody;

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        if (ObjectUtils.isEmpty(jsonNode) || jsonNode.isMissingNode() || !jsonNode.has(STATUS_FIELD_NAME)) {
            return RawBankPaymentStatus.unknown(responseBody);
        } else {
            String code = jsonNode.get(STATUS_FIELD_NAME).asText();
            if (!jsonNode.has(REASON_FIELD_NAME)) {
                return RawBankPaymentStatus.forStatus(code, responseBody);
            } else {
                String text = jsonNode.get(REASON_FIELD_NAME).asText();
                return RawBankPaymentStatus.forStatus(code, text);
            }
        }
    }
}
