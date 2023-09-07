package com.yolt.providers.stet.generic.service.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class StetInitiatePaymentPaymentIdExtractor implements PaymentIdExtractor<StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> {

    @Getter
    private final Supplier<String> paymentIdQueryParameterSupplier;

    public StetInitiatePaymentPaymentIdExtractor() {
        paymentIdQueryParameterSupplier = () -> "paymentRequestResourceId";
    }

    @Override
    public String extractPaymentId(StetPaymentInitiationResponseDTO responseDTO,
                                   StetInitiatePreExecutionResult unused) {
        String authorizationUrl = responseDTO.getLinks()
                .getConsentApproval()
                .getHref();

        return UriComponentsBuilder.fromUriString(authorizationUrl)
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get(paymentIdQueryParameterSupplier.get());
    }
}
