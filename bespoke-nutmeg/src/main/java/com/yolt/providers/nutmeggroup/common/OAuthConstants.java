package com.yolt.providers.nutmeggroup.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuthConstants {
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Params {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String AUDIENCE = "audience";
        public static final String STATE = "state";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Values {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE = "code";
    }
}
