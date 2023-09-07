package com.yolt.providers.stet.generic.service.authorization.tool;

public interface AuthorizationCodeExtractor {

    String extractAuthorizationCode(String redirectUrlPostedBackFromSite);
}
