package com.yolt.providers.openbanking.ais.monzogroup.common.oauth2;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.DefaultMutualTlsOauth2Client;
import com.yolt.providers.openbanking.ais.monzogroup.common.http.MonzoGroupHttpErrorHandlerV2;

public class MonzoMutualTlsOauth2Client extends DefaultMutualTlsOauth2Client {

    public MonzoMutualTlsOauth2Client(DefaultProperties properties, boolean isInPisFlow) {
        super(properties, new MonzoOauthTokenBodyProducer(), isInPisFlow);
    }

    @Override
    protected HttpErrorHandler getErrorHandler() {
        return MonzoGroupHttpErrorHandlerV2.MONZO_GROUP_ERROR_HANDLER;
    }
}
