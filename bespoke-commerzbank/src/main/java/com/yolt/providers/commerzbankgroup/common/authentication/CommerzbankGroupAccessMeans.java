package com.yolt.providers.commerzbankgroup.common.authentication;

public record CommerzbankGroupAccessMeans(String accessToken, String refreshToken,
                                          String consentId) { }
