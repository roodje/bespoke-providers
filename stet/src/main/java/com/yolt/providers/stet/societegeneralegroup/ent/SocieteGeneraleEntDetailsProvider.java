package com.yolt.providers.stet.societegeneralegroup.ent;

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
import static com.yolt.providers.common.providerdetail.dto.CountryCode.FR;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;
import static nl.ing.lovebird.providerdomain.ServiceType.PIS;

@Service
public class SocieteGeneraleEntDetailsProvider implements AisDetailsProvider, PisDetailsProvider {

    public static final String SITE_ID = "8d56424d-12bb-4842-8810-1d1090ca4307";
    public static final String PROVIDER_NAME = "Société Générale Entreprises";
    public static final String PROVIDER_KEY = "SOCIETE_GENERALE_ENT";

    private static final List<AisSiteDetails> AIS_SITE_DETAILS_LIST = of(
            site(SITE_ID, PROVIDER_NAME, PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT), of(FR))
                    .groupingBy(PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT), PIS, of(LoginRequirement.REDIRECT)))
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    private static final List<PisSiteDetails> PIS_SITE_DETAILS_LIST = List.of(
            PisSiteDetails.builder()
                    .id(UUID.fromString(SITE_ID))
                    .providerKey(PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Collections.emptyMap())
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.SEPA)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<AisSiteDetails> getAisSiteDetails() {
        return AIS_SITE_DETAILS_LIST;
    }

    @Override
    public List<PisSiteDetails> getPisSiteDetails() {
        return PIS_SITE_DETAILS_LIST;
    }
}
