package com.yolt.providers.stet.generic.service.authorization.tool;

import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.service.authorization.request.StepRequest;

public interface AuthorizationRedirectUrlSupplier {

    AuthorizationRedirect createAuthorizationRedirectUrl(String authUrl,
                                                         Scope accessTokenScope,
                                                         StepRequest stepRequest);
}
