package com.yolt.providers.commerzbankgroup.common.authentication;

public class LoginNotFoundCommerzbankException extends RuntimeException {

    public LoginNotFoundCommerzbankException(Throwable cause) {
        super(cause);
    }

    public LoginNotFoundCommerzbankException(String message) {
        super(message);
    }
}
