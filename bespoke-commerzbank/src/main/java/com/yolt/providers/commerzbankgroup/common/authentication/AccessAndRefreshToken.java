package com.yolt.providers.commerzbankgroup.common.authentication;

public record AccessAndRefreshToken(String accessToken, String refreshToken,
                                    Integer expiresIn) {
}
