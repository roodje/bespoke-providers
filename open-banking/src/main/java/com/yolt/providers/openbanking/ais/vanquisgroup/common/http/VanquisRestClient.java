package com.yolt.providers.openbanking.ais.vanquisgroup.common.http;

import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;

import static com.yolt.providers.openbanking.ais.vanquisgroup.common.http.VanquisGroupErrorHandlerV2.VANQUIS_GROUP_ERROR_HANDLER;

public class VanquisRestClient extends DefaultRestClient {

    public VanquisRestClient(final PaymentRequestSigner payloadSigner) {
        super(payloadSigner);
    }

    protected HttpErrorHandler getErrorHandler() {
        return VANQUIS_GROUP_ERROR_HANDLER;
    }

}
