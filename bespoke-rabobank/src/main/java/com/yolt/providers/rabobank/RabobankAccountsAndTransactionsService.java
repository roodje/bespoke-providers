package com.yolt.providers.rabobank;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.rabobank.dto.AccessTokenResponseDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.springframework.web.client.RestTemplate;

import java.security.cert.CertificateEncodingException;
import java.time.Instant;
import java.util.List;

public interface RabobankAccountsAndTransactionsService {

    List<ProviderAccountDTO> getAccountsAndTransactions(final RestTemplate restTemplate,
                                                        final Instant from,
                                                        final String psuIpAddress,
                                                        final AccessTokenResponseDTO accessToken,
                                                        final RabobankAuthenticationMeans authenticationMeans,
                                                        Signer signer) throws CertificateEncodingException;
}
