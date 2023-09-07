package com.yolt.providers.knabgroup.common.http;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpErrorMessages {

    public static final String REQUEST_FORMED_INCORRECTLY_MESSAGE = "Request formed incorrectly: HTTP ";
    public static final String NOT_AUTHORIZED_MESSAGE = "We are not authorized to call endpoint: HTTP ";
    public static final String ACCESS_FORBIDDEN_MESSAGE = "Access to call is forbidden: HTTP ";
    public static final String ERROR_ON_THE_BANK_SIDE_MESSAGE = "Something went wrong on the bank side: HTTP ";
    public static final String UNKNOWN_EXCEPTION_MESSAGE = "Unknown exception: HTTP ";
}