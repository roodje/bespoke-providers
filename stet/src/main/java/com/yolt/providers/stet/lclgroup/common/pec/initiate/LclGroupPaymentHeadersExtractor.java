package com.yolt.providers.stet.lclgroup.common.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/**
 *  LCL returns payment id in the 'Location' header,
 *  therefore additional extractor had to be implemented
 */
@RequiredArgsConstructor
public class LclGroupPaymentHeadersExtractor {

    @Getter
    private HttpHeaders headers;

    public void setHeaders(ResponseEntity<JsonNode> response) {
        this.headers = response.getHeaders();
    }
}
