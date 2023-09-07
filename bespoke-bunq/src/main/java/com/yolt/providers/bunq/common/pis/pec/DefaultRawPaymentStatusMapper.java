package com.yolt.providers.bunq.common.pis.pec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.RawBankPaymentStatusMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;

@RequiredArgsConstructor
public class DefaultRawPaymentStatusMapper implements RawBankPaymentStatusMapper {

    public static final String ERROR_ARRAY_NAME = "Error";
    public static final String ERROR_DESCRIPTION_FIELD_NAME = "error_description";
    public static final String ERROR_DESCRIPTION_TRANSLATED_FIELD_NAME = "error_description_translated";
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public RawBankPaymentStatus mapBankPaymentStatus(String rawBodyResponse) {
        var errorResponse = objectMapper.readTree(rawBodyResponse);
        var errorArray = errorResponse.get(ERROR_ARRAY_NAME);
        if (ObjectUtils.isEmpty(errorArray) || !errorArray.isArray()) {
            return RawBankPaymentStatus.unknown(rawBodyResponse);
        }
        var errorNode = errorArray.get(0);
        var description = errorNode.get(ERROR_DESCRIPTION_FIELD_NAME).asText();
        var descriptionTranslated = errorNode.get(ERROR_DESCRIPTION_TRANSLATED_FIELD_NAME).asText();
        return RawBankPaymentStatus.unknown(description + ". " + descriptionTranslated);
    }
}
