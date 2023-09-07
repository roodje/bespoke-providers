package com.yolt.providers.yoltprovider;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.providerdetail.PisDetailsProvider;
import com.yolt.providers.common.providerdetail.dto.LoginRequirement;
import com.yolt.providers.common.providerdetail.dto.PaymentMethod;
import com.yolt.providers.common.providerdetail.dto.PisSiteDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.yolt.providers.yoltprovider.YoltProviderAisDetailsProvider.YOLT_PROVIDER_KEY;
import static com.yolt.providers.yoltprovider.YoltProviderAisDetailsProvider.YOLT_PROVIDER_SITE_ID;
import static java.util.List.of;

@Service
public class YoltProviderPisDetailsProvider implements PisDetailsProvider {

    private static final List<PisSiteDetails> PIS_SITE_DETAILS = of(
            PisSiteDetails.builder()
                    .id(UUID.fromString(YOLT_PROVIDER_SITE_ID))
                    .providerKey(YOLT_PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Collections.emptyMap())
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.SEPA)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build(),
            PisSiteDetails.builder()
                    .id(UUID.fromString(YOLT_PROVIDER_SITE_ID))
                    .providerKey(YOLT_PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.SCHEDULED)
                    .dynamicFields(Collections.emptyMap())
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.SEPA)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build(),
            PisSiteDetails.builder()
                    .id(UUID.fromString(YOLT_PROVIDER_SITE_ID))
                    .providerKey(YOLT_PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.PERIODIC)
                    .dynamicFields(Collections.emptyMap())
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.SEPA)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build(),
            PisSiteDetails.builder()
                    .id(UUID.fromString(YOLT_PROVIDER_SITE_ID))
                    .providerKey(YOLT_PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.SINGLE)
                    .dynamicFields(Collections.emptyMap())
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.UKDOMESTIC)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build(),
            PisSiteDetails.builder()
                    .id(UUID.fromString(YOLT_PROVIDER_SITE_ID))
                    .providerKey(YOLT_PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.SCHEDULED)
                    .dynamicFields(Collections.emptyMap())
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.UKDOMESTIC)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build(),
            PisSiteDetails.builder()
                    .id(UUID.fromString(YOLT_PROVIDER_SITE_ID))
                    .providerKey(YOLT_PROVIDER_KEY)
                    .supported(true)
                    .paymentType(PaymentType.PERIODIC)
                    .dynamicFields(Collections.emptyMap())
                    .requiresSubmitStep(true)
                    .paymentMethod(PaymentMethod.UKDOMESTIC)
                    .loginRequirements(of(LoginRequirement.REDIRECT))
                    .build()
    );

    @Override
    public List<PisSiteDetails> getPisSiteDetails() {
        return PIS_SITE_DETAILS;
    }
}
