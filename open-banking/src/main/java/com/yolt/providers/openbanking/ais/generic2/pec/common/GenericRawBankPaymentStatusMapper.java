package com.yolt.providers.openbanking.ais.generic2.pec.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.RawBankPaymentStatusMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedRawBodyResponseException;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBError1;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBErrorResponse1;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class GenericRawBankPaymentStatusMapper implements RawBankPaymentStatusMapper {

    private static final String STATUS_PROPERTY_NAME = "Status";

    private final ObjectMapper objectMapper;

    @Override
    public RawBankPaymentStatus mapBankPaymentStatus(String rawBodyResponse) {
        return tryToParseErrorResponse(rawBodyResponse)
                .orElseGet(() -> tryToParse2xxResponse(rawBodyResponse));
    }

    private Optional<RawBankPaymentStatus> tryToParseErrorResponse(String rawBodyResponse) {
        OBErrorResponse1 errorResponse;
        try {
            errorResponse = objectMapper.readValue(rawBodyResponse, OBErrorResponse1.class);
        } catch (JsonProcessingException ex) {
            return Optional.empty();
        }

        return StringUtils.isEmpty(errorResponse.getCode()) ? Optional.empty() : Optional.of(RawBankPaymentStatus
                .forStatus(errorResponse.getCode(),
                        prepareBankReason(errorResponse.getErrors())));
    }

    private String prepareBankReason(List<OBError1> errors) {
        try {
            return objectMapper.writeValueAsString(errors);
        } catch (JsonProcessingException e) {
            throw new MalformedRawBodyResponseException("Unable to serialize errors array from bank", e);
        }
    }

    private RawBankPaymentStatus tryToParse2xxResponse(String rawBodyResponse) {
        JsonNode rootNode = parse2xxResponse(rawBodyResponse);
        var statusNode = rootNode.findPath(STATUS_PROPERTY_NAME);
        if (statusNode != null && !statusNode.isMissingNode()) {
            return RawBankPaymentStatus.forStatus(statusNode.asText(), rawBodyResponse);
        }
        return RawBankPaymentStatus.unknown(rawBodyResponse);
    }

    private JsonNode parse2xxResponse(String rawBodyResponse) {
        try {
            return objectMapper.readTree(rawBodyResponse);
        } catch (JsonProcessingException e) {
            throw new MalformedRawBodyResponseException("Unable to parse rawBodyResponse from bank", e);
        }
    }
}
