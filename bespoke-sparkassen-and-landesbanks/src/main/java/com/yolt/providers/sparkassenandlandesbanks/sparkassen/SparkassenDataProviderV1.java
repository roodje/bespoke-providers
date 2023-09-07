package com.yolt.providers.sparkassenandlandesbanks.sparkassen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.sparkassenandlandesbanks.common.Department;
import com.yolt.providers.sparkassenandlandesbanks.common.SparkassenAndLandesbanksAuthMeans;
import com.yolt.providers.sparkassenandlandesbanks.common.SparkassenAndLandesbanksDataProvider;
import com.yolt.providers.sparkassenandlandesbanks.common.dto.ConsentUrlData;
import com.yolt.providers.sparkassenandlandesbanks.common.dto.SparkassenAndLandesbanksProviderState;
import com.yolt.providers.sparkassenandlandesbanks.common.exception.LoginNotFoundException;
import com.yolt.providers.sparkassenandlandesbanks.common.service.SparkassenAndLandesbanksAuthenticationService;
import com.yolt.providers.sparkassenandlandesbanks.common.service.SparkassenAndLandesbanksFetchDataService;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.SelectField;
import nl.ing.lovebird.providershared.form.SelectOptionValue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class SparkassenDataProviderV1 extends SparkassenAndLandesbanksDataProvider {

    private final SparkassenAndLandesbanksAuthenticationService authenticationService;
    private final SparkassenProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public SparkassenDataProviderV1(@Qualifier("SparkassenAuthenticationService") SparkassenAndLandesbanksAuthenticationService authenticationService,
                                    @Qualifier("SparkassenAndLandesbanksObjectMapper") ObjectMapper objectMapper,
                                    @Qualifier("SparkassenFetchDataService") SparkassenAndLandesbanksFetchDataService sparkassenAndLandesbanksFetchDataService,
                                    SparkassenProperties properties,
                                    Clock clock) {
        super(authenticationService, objectMapper, sparkassenAndLandesbanksFetchDataService, clock);
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.authenticationService = authenticationService;
        this.clock = clock;
    }

    @Override
    public Step getLoginInfo(UrlGetLoginRequest urlGetLogin) {
        SelectField selectField = new SelectField("bank", "Bank", 0, 0, false, true);
        List<Department> departments = properties.getDepartments();
        departments.sort(Comparator.comparing(Department::getDisplayName));
        for (Department departmentData : departments) {
            selectField.addSelectOptionValue(new SelectOptionValue(departmentData.getFormValue(), departmentData.getDisplayName()));
        }

        Form selectForm = new Form(Collections.singletonList(selectField), null, null);
        return new FormStep(selectForm,
                EncryptionDetails.noEncryption(),
                Instant.now(clock).plus(Duration.ofHours(1)),
                null);
    }

    @Override
    public AccessMeansOrStepDTO createNewAccessMeans(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        return StringUtils.isEmpty(urlCreateAccessMeans.getFilledInUserSiteFormValues())
                ? super.createNewAccessMeans(urlCreateAccessMeans)
                : returnProperLoginUrlForSparkassen(urlCreateAccessMeans);
    }

    @Override
    public String getProviderIdentifier() {
        return "SPARKASSEN";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Sparkassen";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_1;
    }
    
    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
    
    private AccessMeansOrStepDTO returnProperLoginUrlForSparkassen(UrlCreateAccessMeansRequest urlCreateAccessMeans) {
        SparkassenAndLandesbanksAuthMeans authMeans = SparkassenAndLandesbanksAuthMeans
                .createAuthMeans(urlCreateAccessMeans.getAuthenticationMeans(), getProviderIdentifier());

        try {
            Department department = properties.getSelectedDepartment(urlCreateAccessMeans.getFilledInUserSiteFormValues());
            ConsentUrlData consentUrlData = authenticationService.generateLoginUrl(
                    authMeans,
                    department,
                    getProviderIdentifierDisplayName(),
                    urlCreateAccessMeans.getRestTemplateManager(),
                    urlCreateAccessMeans.getBaseClientRedirectUrl(),
                    urlCreateAccessMeans.getPsuIpAddress(),
                    urlCreateAccessMeans.getState());
            String providerState = objectMapper.writeValueAsString(new SparkassenAndLandesbanksProviderState(consentUrlData.getCodeVerifier(), department, consentUrlData.getWellKnownEndpoint(), consentUrlData.getConsentId()));
            return new AccessMeansOrStepDTO(new RedirectStep(consentUrlData.getConsentUrl(), null, providerState));
        } catch (Exception e) {
            throw new LoginNotFoundException(e);
        }
    }
}
