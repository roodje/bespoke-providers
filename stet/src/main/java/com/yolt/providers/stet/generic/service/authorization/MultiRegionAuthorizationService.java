package com.yolt.providers.stet.generic.service.authorization;

import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenStrategy;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansOrStepRequest;
import com.yolt.providers.stet.generic.service.authorization.request.StepRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.AuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.tool.AuthorizationCodeExtractor;
import com.yolt.providers.stet.generic.service.authorization.tool.AuthorizationRedirectUrlSupplier;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.SelectField;
import nl.ing.lovebird.providershared.form.SelectOptionValue;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import static java.util.Comparator.comparing;

public class MultiRegionAuthorizationService extends SingleRegionAuthorizationService {

    protected static final String REGION_FIELD_ID = "region";
    protected static final String REGION_FIELD_DISPLAY_NAME = "Région";

    public MultiRegionAuthorizationService(RefreshTokenStrategy refreshTokenStrategy,
                                           AuthorizationRestClient restClient,
                                           ProviderStateMapper providerStateMapper,
                                           Scope accessTokenScope,
                                           DefaultProperties properties,
                                           AuthorizationCodeExtractor authCodeExtractor,
                                           AuthorizationRedirectUrlSupplier authRedirectUrlSupplier,
                                           DateTimeSupplier dateTimeSupplier) {
        super(refreshTokenStrategy, restClient, providerStateMapper, accessTokenScope, properties, authCodeExtractor, authRedirectUrlSupplier, dateTimeSupplier);
    }

    @Override
    protected Region getRegion(String regionCode) {
        return properties.getRegionByCode(regionCode);
    }

    @Override
    public Step getStep(StepRequest request) {
        SelectField selectField = new SelectField(REGION_FIELD_ID, REGION_FIELD_DISPLAY_NAME, 0, 0, false, true);
        properties.getRegions().stream()
                .sorted(comparing(Region::getName))
                .map(region -> new SelectOptionValue(region.getCode(), region.getName()))
                .forEachOrdered(selectField::addSelectOptionValue);

        Form selectForm = new Form(Collections.singletonList(selectField), null, null);
        String jsonProviderState = providerStateMapper.mapToJson(DataProviderState.emptyState());

        Duration duration = getFormStepExpiryDuration();
        Instant timeoutTime = dateTimeSupplier.getDefaultInstant().plus(duration);
        return new FormStep(selectForm, EncryptionDetails.noEncryption(), timeoutTime, jsonProviderState);
    }

    private Duration getFormStepExpiryDuration() {
        long expiryDurationMillis = properties.getFormStepExpiryDurationMillis();
        if (properties.getFormStepExpiryDurationMillis() > 0) {
            return Duration.ofMillis(expiryDurationMillis);
        }
        return Duration.ofHours(1);
    }

    @Override
    public AccessMeansOrStepDTO createAccessMeansOrGetStep(HttpClient httpClient, AccessMeansOrStepRequest request) throws TokenInvalidException {
        if (StringUtils.isEmpty(request.getRedirectUrlPostedBackFromSite())) {
            String selectedRegion = request.getFilledInUserSiteFormValues().get(REGION_FIELD_ID);

            StepRequest stepRequest = StepRequest.regionAwareStepRequest(
                    request.getAuthMeans(),
                    request.getBaseClientRedirectUrl(),
                    request.getState(),
                    selectedRegion);

            return new AccessMeansOrStepDTO(super.getStep(stepRequest));
        } else {
            return super.createAccessMeansOrGetStep(httpClient, request);
        }
    }
}
