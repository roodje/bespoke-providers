package com.yolt.providers.monorepogroup.libragroup.common;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraGroupConsentRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.util.UUID;

public interface LibraSigningService {
    HttpHeaders getSigningHeaders(MultiValueMap<String, String> payload,
                                  String signingCertificateSerialNumber,
                                  UUID signingKeyId,
                                  String signingCertificate,
                                  Signer signer) throws TokenInvalidException;

    HttpHeaders getSigningHeaders(LibraGroupConsentRequest payload,
                                  String signingCertificateSerialNumber,
                                  UUID signingKeyId,
                                  String signingCertificate,
                                  Signer signer) throws TokenInvalidException;
}
