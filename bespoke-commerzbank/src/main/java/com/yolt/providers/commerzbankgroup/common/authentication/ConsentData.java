package com.yolt.providers.commerzbankgroup.common.authentication;

public record ConsentData(String authorizationUrl, String consentId,
                          String codeVerifier) {

}
