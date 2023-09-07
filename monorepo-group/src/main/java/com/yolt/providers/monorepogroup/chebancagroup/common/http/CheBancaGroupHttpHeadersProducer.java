package com.yolt.providers.monorepogroup.chebancagroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.internal.SignatureDTO;
import org.springframework.http.HttpHeaders;

public interface CheBancaGroupHttpHeadersProducer {

    HttpHeaders createAuthorizationHttpHeaders(final SignatureDTO signatureDTO, final Signer signer, final byte[] body);

    HttpHeaders createGetTokenHttpHeaders(final SignatureDTO signatureDTO, final Signer signer, final byte[] body);

    HttpHeaders getFetchDataHeaders(final SignatureDTO signatureDTO, final Signer signer, final byte[] body, final String clientAccessToken);
}
