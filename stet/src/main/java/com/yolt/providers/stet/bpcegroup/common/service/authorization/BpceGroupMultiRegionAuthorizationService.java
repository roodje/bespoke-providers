package com.yolt.providers.stet.bpcegroup.common.service.authorization;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.MultiRegionAuthorizationService;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenStrategy;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansOrStepRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.AuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.tool.AuthorizationCodeExtractor;
import com.yolt.providers.stet.generic.service.authorization.tool.AuthorizationRedirectUrlSupplier;
import org.springframework.util.StringUtils;

public class BpceGroupMultiRegionAuthorizationService extends MultiRegionAuthorizationService {
    public BpceGroupMultiRegionAuthorizationService(RefreshTokenStrategy refreshTokenStrategy, AuthorizationRestClient restClient, ProviderStateMapper providerStateMapper, Scope accessTokenScope, DefaultProperties properties, AuthorizationCodeExtractor authCodeExtractor, AuthorizationRedirectUrlSupplier authRedirectUrlSupplier, DateTimeSupplier dateTimeSupplier) {
        super(refreshTokenStrategy, restClient, providerStateMapper, accessTokenScope, properties, authCodeExtractor, authRedirectUrlSupplier, dateTimeSupplier);
    }

    @Override
    public String createAccessTokenRequestRedirectUrl(AccessMeansOrStepRequest request) {
        String redirectUrlPostedBackFromSite = request.getRedirectUrlPostedBackFromSite();
        if (StringUtils.hasText(redirectUrlPostedBackFromSite)) {
            return redirectUrlPostedBackFromSite.substring(0, redirectUrlPostedBackFromSite.indexOf('?'));
        }
        return super.createAccessTokenRequestRedirectUrl(request);
    }
}
