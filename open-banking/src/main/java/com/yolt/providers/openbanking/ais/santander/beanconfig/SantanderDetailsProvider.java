package com.yolt.providers.openbanking.ais.santander.beanconfig;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerdetail.PisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.providerdetail.dto.AisSiteDetails.site;
import static com.yolt.providers.common.providerdetail.dto.CountryCode.GB;
import static com.yolt.providers.common.providerdetail.dto.DynamicFieldNames.REMITTANCE_INFORMATION_STRUCTURED;
import static com.yolt.providers.common.providerdetail.dto.ProviderBehaviour.STATE;
import static com.yolt.providers.common.providerdetail.dto.ProviderType.DIRECT_CONNECTION;
import static java.util.List.of;
import static nl.ing.lovebird.providerdomain.AccountType.*;
import static nl.ing.lovebird.providerdomain.ServiceType.AIS;
import static nl.ing.lovebird.providerdomain.ServiceType.PIS;

@Service
public class SantanderDetailsProvider implements AisDetailsProvider, PisDetailsProvider {

    public static final String SANTANDER_SITE_ID = "1ed6cc15-60fe-4b00-adf8-bd8e04e7804e";
    public static final String PROVIDER_NAME = "Santander";
    public static final String PROVIDER_KEY = "SANTANDER";

    public static final List<PisSiteDetails> PIS_SITE_DETAILS_LIST = List.of(
            PisSiteDetails.builder()
                    .id(UUID.fromString(SANTANDER_SITE_ID))
                    .providerKey(PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Map.of(REMITTANCE_INFORMATION_STRUCTURED, new DynamicFieldOptions(true)))
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.UKDOMESTIC)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    public static final List<AisSiteDetails> AIS_SITE_DETAILS_LIST = List.of(
            site(SANTANDER_SITE_ID, PROVIDER_NAME, PROVIDER_KEY, DIRECT_CONNECTION, of(STATE), of(CURRENT_ACCOUNT, CREDIT_CARD, SAVINGS_ACCOUNT), of(GB))
                    .groupingBy(PROVIDER_NAME)
                    .usesStepTypes(Map.of(AIS, of(LoginRequirement.REDIRECT), PIS, of(LoginRequirement.REDIRECT)))
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
