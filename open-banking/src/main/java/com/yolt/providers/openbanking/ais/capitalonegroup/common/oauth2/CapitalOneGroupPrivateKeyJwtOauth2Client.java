package com.yolt.providers.openbanking.ais.capitalonegroup.common.oauth2;

import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion.ClientAssertionProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.PrivateKeyJwtOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class CapitalOneGroupPrivateKeyJwtOauth2Client extends PrivateKeyJwtOauth2Client {

    public CapitalOneGroupPrivateKeyJwtOauth2Client(final String oAuthTokenUrl,
                                                    final TokenRequestBodyProducer tokenRequestBodyProducer,
                                                    final ClientAssertionProducer clientAssertionProducer,
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
