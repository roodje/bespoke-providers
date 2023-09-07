package com.yolt.providers.yoltprovider;

import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.*;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.*;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;
import static nl.ing.lovebird.providerdomain.ServiceType.PIS;

@Service
public class YoltProviderAisDetailsProvider implements AisDetailsProvider {

    public static final String YOLT_PROVIDER_SITE_ID = "33aca8b9-281a-4259-8492-1b37706af6db";
    public static final String SCENERIO_1_SITE_ID = "333e1b97-1055-4b86-a112-bc1db801145f";
    public static final String SCENERIO_2_SITE_ID = "840d4df3-07d2-4d2e-b177-6db8f4cea479";
    public static final String SCENERIO_3_SITE_ID = "035ba2f1-f751-4d71-be88-3e6649ad1051";
    public static final String SCENARIO_4_SITE_ID = "DECC7E83-DA09-4BCA-B9CF-BDFD8AEFDD7B";
    public static final String SCENARIO_5_SITE_ID = "c9624c3b-5082-461f-8c02-ecfa6805fc0d";
    public static final String SCENARIO_6_SITE_ID = "6f16d556-2845-45c4-a3bd-73054dacada5";
    public static final String SCENARIO_7_SITE_ID = "3acc39d0-3e38-4140-b8c4-ff53c9b0f5d3";
    public static final String SCENARIO_8_SITE_ID = "a0f4753f-94ca-4024-8f67-dc122f86f593";
    public static final String SCENARIO_9_SITE_ID = "c493be6a-137c-4aa8-bb22-d6cedea2efce";
    public static final String SCENARIO_10_SITE_ID = "f22b571a-6557-42d6-a3a3-0b5cd7f58c9c";
    public static final String SCENARIO_11_SITE_ID = "475e541e-94ae-4b9f-9b26-4004d81d6374";
    public static final String SCENARIO_12_SITE_ID = "fa1fa884-c6db-11ec-9d64-0242ac120002";
    public static final String SCENARIO_13_SITE_ID = "6f08fdc5-bc8d-444c-8c02-456715731689";
    public static final String SCENARIO_14_SITE_ID = "8ef927ce-18c3-4fb6-b1d5-7b92ae2e99d7";
    public static final String SCENARIO_15_SITE_ID = "e9a8490e-8d3a-4766-a94d-70ef20fe4012";
    public static final String SCENARIO_16_SITE_ID = "16382d08-ed34-4989-a60b-f4d6afcdb15c";

    public static final String YOLT_PROVIDER_KEY = "YOLT_PROVIDER";
    //<editor-fold desc="AIS_SITE_DETAILS">
    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(YOLT_PROVIDER_SITE_ID, "Yolt test bank", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD, SAVINGS_ACCOUNT, PREPAID_ACCOUNT, PENSION, INVESTMENT), of(GB, FR, IT, ES, BE, DE, NL, PL, CZ))
                    .usesStepTypes(Map.of(AIS, List.of(LoginRequirement.REDIRECT), PIS, List.of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENERIO_1_SITE_ID, "Scen. 1: FR Bank Migration Success direct connection", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD, SAVINGS_ACCOUNT, PREPAID_ACCOUNT, PENSION, INVESTMENT), of(FR))
                    .groupingBy("Scen. 1: FR Bank Migration Success")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENERIO_2_SITE_ID, "Scen. 2: FR Bank Migration Success Remainder still on scraping - direct connection", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(FR))
                    .groupingBy("Scen. 2: FR Bank Migration Success Remainder still on scraping")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENERIO_3_SITE_ID, "Scen. 3: FR Bank Migration Partial migration (noLongerSupported) direct connection", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(FR))
                    .groupingBy("Scen. 3: FR Bank Migration Partial migration (noLongerSupported)")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENARIO_4_SITE_ID, "Scenario region selection", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM, LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.FORM, LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENARIO_5_SITE_ID, "Scenario IBAN text field", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM, LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.FORM, LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENARIO_6_SITE_ID, "Scenario bank selection", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM, LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.FORM, LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENARIO_7_SITE_ID, "Scenario account type selection", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM, LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.FORM, LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENARIO_8_SITE_ID, "Scenario IBAN and language selection", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM, LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.FORM, LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENARIO_9_SITE_ID, "Scenario branch and account text fields", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM, LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.FORM, LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENARIO_10_SITE_ID, "Scenario username text field", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM, LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.FORM, LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENARIO_11_SITE_ID, "Scenario email text field", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM, LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.FORM, LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENARIO_12_SITE_ID, "Scenario language selection", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM, LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.FORM, LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENARIO_13_SITE_ID, "Scenario LoginID text field", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM, LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.FORM, LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENARIO_14_SITE_ID, "Scenario iban and username text fields", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR, RO))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM, LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.FORM, LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENARIO_15_SITE_ID, "Scenario username and password text fields", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM, LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.FORM, LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build(),
            site(SCENARIO_16_SITE_ID, "Scenario embedded flow berlin", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR, DE))
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM)))
                    .loginRequirements(of(LoginRequirement.FORM))
                    .isTestSite(true)
                    .build()

    );
    //</editor-fold>

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }
}
