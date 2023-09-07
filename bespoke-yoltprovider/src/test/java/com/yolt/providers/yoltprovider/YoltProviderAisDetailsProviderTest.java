package com.yolt.providers.yoltprovider;

import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.*;
import static com.yolt.providers.common.providerdetail.dto.LoginRequirement.FORM;
import static com.yolt.providers.common.providerdetail.dto.LoginRequirement.REDIRECT;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static com.yolt.providers.yoltprovider.YoltProviderAisDetailsProvider.*;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.*;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;
import static nl.ing.lovebird.providerdomain.ServiceType.PIS;
import static org.assertj.core.api.Assertions.assertThat;

class YoltProviderAisDetailsProviderTest {

    private YoltProviderAisDetailsProvider detailsProvider = new YoltProviderAisDetailsProvider();

    @Test
    public void testDetails() {
        //given
        List expected = List.of(
                site(YOLT_PROVIDER_SITE_ID, "Yolt test bank", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD, SAVINGS_ACCOUNT, PREPAID_ACCOUNT, PENSION, INVESTMENT), of(GB, FR, IT, ES, BE, DE, NL, PL, CZ))
                        .usesStepTypes(Map.of(AIS, List.of(REDIRECT), PIS, List.of(REDIRECT)))
                        .loginRequirements(List.of(REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENERIO_1_SITE_ID, "Scen. 1: FR Bank Migration Success direct connection", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD, SAVINGS_ACCOUNT, PREPAID_ACCOUNT, PENSION, INVESTMENT), of(FR))
                        .groupingBy("Scen. 1: FR Bank Migration Success")
                        .usesStepTypes(Map.of(AIS, of(REDIRECT)))
                        .loginRequirements(List.of(REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENERIO_2_SITE_ID, "Scen. 2: FR Bank Migration Success Remainder still on scraping - direct connection", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(FR))
                        .groupingBy("Scen. 2: FR Bank Migration Success Remainder still on scraping")
                        .usesStepTypes(Map.of(AIS, of(REDIRECT)))
                        .loginRequirements(List.of(REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENERIO_3_SITE_ID, "Scen. 3: FR Bank Migration Partial migration (noLongerSupported) direct connection", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(FR))
                        .groupingBy("Scen. 3: FR Bank Migration Partial migration (noLongerSupported)")
                        .usesStepTypes(Map.of(AIS, of(REDIRECT)))
                        .loginRequirements(List.of(REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENARIO_4_SITE_ID, "Scenario region selection", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                        .usesStepTypes(Map.of(AIS, of(FORM, REDIRECT)))
                        .loginRequirements(List.of(FORM, REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENARIO_5_SITE_ID, "Scenario IBAN text field", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                        .usesStepTypes(Map.of(AIS, of(FORM, REDIRECT)))
                        .loginRequirements(List.of(FORM, REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENARIO_6_SITE_ID, "Scenario bank selection", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                        .usesStepTypes(Map.of(AIS, of(FORM, REDIRECT)))
                        .loginRequirements(List.of(FORM, REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENARIO_7_SITE_ID, "Scenario account type selection", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                        .usesStepTypes(Map.of(AIS, of(FORM, REDIRECT)))
                        .loginRequirements(List.of(FORM, REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENARIO_8_SITE_ID, "Scenario IBAN and language selection", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                        .usesStepTypes(Map.of(AIS, of(FORM, REDIRECT)))
                        .loginRequirements(List.of(FORM, REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENARIO_9_SITE_ID, "Scenario branch and account text fields", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                        .usesStepTypes(Map.of(AIS, of(FORM, REDIRECT)))
                        .loginRequirements(List.of(FORM, REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENARIO_10_SITE_ID, "Scenario username text field", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                        .usesStepTypes(Map.of(AIS, of(FORM, REDIRECT)))
                        .loginRequirements(List.of(FORM, REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENARIO_11_SITE_ID, "Scenario email text field", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                        .usesStepTypes(Map.of(AIS, of(FORM, REDIRECT)))
                        .loginRequirements(List.of(FORM, REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENARIO_12_SITE_ID, "Scenario language selection", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                        .usesStepTypes(Map.of(AIS, of(FORM, REDIRECT)))
                        .loginRequirements(List.of(FORM, REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENARIO_13_SITE_ID, "Scenario LoginID text field", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                        .usesStepTypes(Map.of(AIS, of(FORM, REDIRECT)))
                        .loginRequirements(List.of(FORM, REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENARIO_14_SITE_ID, "Scenario iban and username text fields", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR, RO))
                        .usesStepTypes(Map.of(AIS, of(FORM, REDIRECT)))
                        .loginRequirements(List.of(FORM, REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENARIO_15_SITE_ID, "Scenario username and password text fields", "YOLT_PROVIDER", DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR))
                        .usesStepTypes(Map.of(AIS, of(FORM, REDIRECT)))
                        .loginRequirements(List.of(FORM, REDIRECT))
                        .isTestSite(true)
                        .build(),
                site(SCENARIO_16_SITE_ID, "Scenario embedded flow berlin", YOLT_PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL, FR, DE))
                        .usesStepTypes(Map.of(AIS, of(LoginRequirement.FORM)))
                        .loginRequirements(of(LoginRequirement.FORM))
                        .isTestSite(true)
                        .build()

        );

        //when
        List<AisSiteDetails> detailsList = detailsProvider.getAisSiteDetails();

        //then
        assertThat(expected).isEqualTo(detailsList);
    }
}