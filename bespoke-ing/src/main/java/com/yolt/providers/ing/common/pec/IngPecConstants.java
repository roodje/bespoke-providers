package com.yolt.providers.ing.common.pec;

import org.springframework.http.HttpMethod;

public class IngPecConstants {

    public static final String INITIATE_PAYMENT_ENDPOINT = "/v1/payments/sepa-credit-transfers";
    public static final String INITIATE_PERIODIC_PAYMENT_ENDPOINT = "/v1/periodic-payments/sepa-credit-transfers";
    public static final String STATUS_PAYMENT_ENDPOINT = INITIATE_PAYMENT_ENDPOINT + "/%s/status";
    public static final String STATUS_PERIODIC_PAYMENT_ENDPOINT = INITIATE_PERIODIC_PAYMENT_ENDPOINT + "/%s/status";
    public static final String SUBMIT_PAYMENT_ENDPOINT = STATUS_PAYMENT_ENDPOINT;
    public static final String SUBMIT_PERIODIC_PAYMENT_ENDPOINT = STATUS_PERIODIC_PAYMENT_ENDPOINT;
    public static final HttpMethod INITIATE_PAYMENT_HTTP_METHOD = HttpMethod.POST;
    public static final HttpMethod GET_PAYMENT_STATUS_HTTP_METHOD = HttpMethod.GET;
    public static final HttpMethod SUBMIT_PAYMENT_HTTP_METHOD = GET_PAYMENT_STATUS_HTTP_METHOD;

}
