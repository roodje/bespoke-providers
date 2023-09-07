package com.yolt.providers.bunq.common.pis.pec;

import com.yolt.providers.bunq.common.configuration.BunqProperties;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultEndpointUrlProvider {

    private static final String INITIATE_DRAFT_PAYMENT_URL = "/user/%d/payment-service-provider-draft-payment";
    private static final String STATUS_DRAFT_PAYMENT_URL = "/user/%d/payment-service-provider-draft-payment/%d";
    private static final String INSTALLATION = "/installation";
    private static final String SESSION_SERVER = "/session-server";
    private static final String DEVICE_SERVER = "/device-server";

    private final BunqProperties properties;

    public String getInitiateDraftPaymentUrl(final long psd2UserId) {
        return String.format(properties.getBaseUrl() + INITIATE_DRAFT_PAYMENT_URL, psd2UserId);
    }

    public String getStatusDraftPaymentUrl(final long psd2UserId, final int paymentId) {
        return String.format(properties.getBaseUrl() + STATUS_DRAFT_PAYMENT_URL, psd2UserId, paymentId);
    }

    public String getSessionServerUrl() {
        return properties.getBaseUrl() + SESSION_SERVER;
    }

    public String getInstallationUrl() {
        return properties.getBaseUrl() + INSTALLATION;
    }

    public String getDeviceServerUrl() {
        return properties.getBaseUrl() + DEVICE_SERVER;
    }
}
