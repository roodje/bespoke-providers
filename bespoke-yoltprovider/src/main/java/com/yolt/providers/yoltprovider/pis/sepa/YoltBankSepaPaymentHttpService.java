package com.yolt.providers.yoltprovider.pis.sepa;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

public interface YoltBankSepaPaymentHttpService {

    ResponseEntity<JsonNode> postInitiateSinglePaymentRequest(HttpEntity<byte[]> requestEntity);

    ResponseEntity<JsonNode> postInitiatePeriodicPaymentRequest(HttpEntity<byte[]> requestEntity);

    ResponseEntity<JsonNode> postSubmitSinglePaymentRequest(HttpEntity<Void> requestEntity, String paymentId);

    ResponseEntity<JsonNode> postSubmitPeriodicPaymentRequest(HttpEntity<Void> requestEntity, String paymentId);

    ResponseEntity<JsonNode> getSingleStatus(HttpEntity<Void> requestEntity, String paymentId);

    ResponseEntity<JsonNode> getPeriodicStatus(HttpEntity<Void> requestEntity, String paymentId);
}
