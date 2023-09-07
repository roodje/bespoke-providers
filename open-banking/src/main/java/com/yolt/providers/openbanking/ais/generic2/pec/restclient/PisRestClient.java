package com.yolt.providers.openbanking.ais.generic2.pec.restclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

public interface PisRestClient {

    ResponseEntity<JsonNode> createPayment(HttpClient httpClient,
                                           String exchangePath,
                                           HttpEntity<?> httpEntity) throws TokenInvalidException;

    ResponseEntity<JsonNode> submitPayment(HttpClient httpClient,
                                           String exchangePath,
                                           HttpEntity<?> httpEntity) throws TokenInvalidException;

    ResponseEntity<JsonNode> getPaymentStatus(HttpClient httpClient,
                                              String exchangePath,
                                              HttpEntity<?> httpEntity) throws TokenInvalidException;

    ResponseEntity<JsonNode> getConsentStatus(HttpClient httpClient,
                                              String exchangePath,
                                              HttpEntity<?> httpEntity) throws TokenInvalidException;
}
