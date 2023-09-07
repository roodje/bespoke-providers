package com.yolt.providers.openbanking.ais.tidegroup.common.oauth2;

import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion.ClientAssertionProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.PrivateKeyJwtOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class TideGroupPrivateKeyJwtOauth2ClientV2 extends PrivateKeyJwtOauth2Client {

    public TideGroupPrivateKeyJwtOauth2ClientV2(String oAuthTokenUrl,
                                                TokenRequestBodyProducer tokenRequestBodyProducer,
                                                ClientAssertionProducer clientAssertionProducer,
                                                boolean isInPisProvider) {
        super(oAuthTokenUrl, tokenRequestBodyProducer, clientAssertionProducer, isInPisProvider);
    }

    @Override
    protected HttpHeaders getHeaders(String authenticationHeader, String institutionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpExtraHeaders.FINANCIAL_ID_HEADER_NAME, institutionId);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }
}
