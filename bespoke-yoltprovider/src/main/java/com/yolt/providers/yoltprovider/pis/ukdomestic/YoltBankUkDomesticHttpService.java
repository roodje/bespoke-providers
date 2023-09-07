package com.yolt.providers.yoltprovider.pis.ukdomestic;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticScheduledConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticStandingOrderConsent1;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

public interface YoltBankUkDomesticHttpService {

    ResponseEntity<JsonNode> postInitiateSinglePayment(HttpEntity<OBWriteDomesticConsent1> httpEntity);

    ResponseEntity<JsonNode> postInitiateScheduledPayment(HttpEntity<OBWriteDomesticScheduledConsent1> httpEntity);

    ResponseEntity<JsonNode> postInitiatePeriodicPayment(HttpEntity<OBWriteDomesticStandingOrderConsent1> httpEntity);

    ResponseEntity<JsonNode> postSubmitSinglePayment(HttpEntity<ConfirmPaymentRequest> httpEntity);

    ResponseEntity<JsonNode> postSubmitScheduledPayment(HttpEntity<ConfirmPaymentRequest> httpEntity);

    ResponseEntity<JsonNode> postSubmitPeriodicPayment(HttpEntity<ConfirmPaymentRequest> httpEntity);

    ResponseEntity<JsonNode> getSinglePaymentStatus(HttpEntity<Void> httpEntity, String paymentId);

    ResponseEntity<JsonNode> getScheduledPaymentStatus(HttpEntity<Void> httpEntity, String paymentId);

    ResponseEntity<JsonNode> getPeriodicPaymentStatus(HttpEntity<Void> httpEntity, String paymentId);
}
