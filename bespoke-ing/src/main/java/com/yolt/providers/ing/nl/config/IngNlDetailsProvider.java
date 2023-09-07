package com.yolt.providers.ing.nl.config;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.PisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.AisSiteDetails;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import com.yolt.providers.common.providerdetail.dto.PaymentMethod;
import com.yolt.providers.common.providerdetail.dto.PisSiteDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.NL;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static com.yolt.providers.ing.nl.config.IngNlProperties.PROVIDER_IDENTIFIER;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;
import static nl.ing.lovebird.providerdomain.ServiceType.PIS;

@Service
public class IngNlDetailsProvider implements AisDetailsProvider, PisDetailsProvider {

    public static final String ING_NL_SITE_ID = "2967f2c0-f0e6-4f1f-aeba-e4357b82ca7a";
    public static final String ING_NL_TEST_SITE_ID = "828e4f90-2773-45c2-9199-cbf9264ef1cc";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS = List.of(
            site(ING_NL_SITE_ID, "ING (NL)", PROVIDER_IDENTIFIER, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL))
                    .groupingBy("ING (NL)")
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT), PIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build(),
            site(ING_NL_TEST_SITE_ID, "ING NL Test bank", PROVIDER_IDENTIFIER, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(NL))
                    .consentExpiryInDays(null)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .isTestSite(true)
                    .build());

    private static final List<PisSiteDetails> PIS_SITE_DETAILS = List.of(
            PisSiteDetails.builder()
                    .id(UUID.fromString(ING_NL_SITE_ID))
                    .providerKey(PROVIDER_IDENTIFIER)
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Collections.emptyMap())
                    .requiresSubmitStep(false)
                    .paymentMethod(PaymentMethod.SEPA)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build(),
            PisSiteDetails.builder()
                    .id(UUID.fromString(ING_NL_SITE_ID))
                    .providerKey(PROVIDER_IDENTIFIER)
                    .supported(true)
                    .paymentType(PaymentType.SCHEDULED)
                    .dynamicFields(Collections.emptyMap())
                    .requiresSubmitStep(false)
                    .paymentMethod(PaymentMethod.SEPA)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build(),
            PisSiteDetails.builder()
                    .id(UUID.fromString(ING_NL_SITE_ID))
                    .providerKey(PROVIDER_IDENTIFIER)
                    .supported(true)
                    .paymentType(PaymentType.PERIODIC)
                    .dynamicFields(Collections.emptyMap())
                    .requiresSubmitStep(false)
                    .paymentMethod(PaymentMethod.SEPA)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS;
    }

    @Override
    public List<PisSiteDetails> getPisSiteDetails() {
        return PIS_SITE_DETAILS;
    }
}
