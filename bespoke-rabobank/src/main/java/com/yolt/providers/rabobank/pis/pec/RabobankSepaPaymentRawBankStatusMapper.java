package com.yolt.providers.rabobank.pis.pec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.RawBankPaymentStatusMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
public class RabobankSepaPaymentRawBankStatusMapper implements RawBankPaymentStatusMapper {

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public RawBankPaymentStatus mapBankPaymentStatus(String rawBodyResponse) {
            JsonNode json = objectMapper.readTree(rawBodyResponse);
            JsonNode messagesNode = json.get("tppMessages");
            if (ObjectUtils.isEmpty(messagesNode) || !messagesNode.isArray()) {
                return RawBankPaymentStatus.forStatus(rawBodyResponse, "");
            }
            ArrayNode messagesArray = (ArrayNode) messagesNode;
            String code = messagesArray.get(0).get("code").asText();
            String message = messagesArray.get(0).get("text").asText();
            return RawBankPaymentStatus.forStatus(code, message);
    }
}
