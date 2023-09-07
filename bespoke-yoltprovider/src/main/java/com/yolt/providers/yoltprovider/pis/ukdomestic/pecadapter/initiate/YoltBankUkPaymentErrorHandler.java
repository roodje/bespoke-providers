package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate;

import com.yolt.providers.common.rest.ExternalResponseErrorHandler;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.YoltBankPostRequestErrorTranslator;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class YoltBankUkPaymentErrorHandler extends ExternalResponseErrorHandler {

    private final YoltBankPostRequestErrorTranslator errorTranslator;

    public YoltBankUkPaymentErrorHandler(YoltBankPostRequestErrorTranslator errorTranslator) {
        this.errorTranslator = errorTranslator;
    }

    @Override
    protected void handleError(ClientHttpResponse response, HttpStatus statusCode) throws IOException {
        errorTranslator.translate(new String(getResponseBody(response)));
        super.handleError(response, statusCode);
    }
}
