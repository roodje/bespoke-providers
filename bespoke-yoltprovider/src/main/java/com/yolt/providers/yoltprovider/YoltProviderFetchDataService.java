package com.yolt.providers.yoltprovider;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Slf4j
class YoltProviderFetchDataService {

    DataProviderResponse getAccountsAndTransactions(final RestTemplate restTemplate,
                                                    final String accessToken,
                                                    final Instant fetchStartTime,
                                                    final X509Certificate signingCertificate,
                                                    final Signer signer,
                                                    final UUID signingKid) throws ProviderFetchDataException, TokenInvalidException {

        try {
            log.debug("Going to fetch accounts and transactions.");
            String encodedCertificate = Base64.toBase64String(signingCertificate.getEncoded());
            String authorizationHeaderValue = "Bearer " + accessToken;
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            headers.add("tpp-signature-certificate", encodedCertificate);
            // We make up something here, there is no body to sign. We can just pick some arbitrary data over which we provide a signature.
            // For now we'll just pick the accessToken, because that changes per call/user. This is a pretty simple signature not backed with
            // any specification. Usually it's some more complex input that needs to be signed (specific headers + body)
            final String signedHeaders = signer.sign(authorizationHeaderValue.getBytes(StandardCharsets.UTF_8), signingKid, SignatureAlgorithm.SHA512_WITH_RSA);
            headers.add("signature", signedHeaders);

            ResponseEntity<List<ProviderAccountDTO>> accountResponse =
                    restTemplate.exchange("/accounts?fromBookingDateTime={from-date}", GET, new HttpEntity<>(headers),
                            new ParameterizedTypeReference<List<ProviderAccountDTO>>() {
                            }, fetchStartTime.truncatedTo(ChronoUnit.MILLIS).toString());

            return new DataProviderResponse(accountResponse.getBody());

        } catch (HttpClientErrorException e) {
            if (HttpStatus.UNAUTHORIZED.equals(e.getStatusCode())) {
                log.warn("YoltProvider token invalid.", e);
                throw new TokenInvalidException(e.getMessage());
            }
            log.warn("Failed to fetch accounts and transactions from YoltProvider.", e);
            throw new ProviderFetchDataException(e.getMessage());
        } catch (RestClientException | CertificateEncodingException e) {
            log.warn("Failed to retrieve accounts and transactions from YoltProvider.", e);
            throw new ProviderFetchDataException(e.getMessage());
        }
    }

    Optional<String> getPersonaTokenFromYoltBank(@NonNull RestTemplate restTemplate, @Nullable UUID clientId) {
        try {
            log.debug("Going to fetch persona-token.");
            var personaTokenResponse =
                    restTemplate.exchange("/persona-tokens", POST, new HttpEntity<>(new PersonaTokenRequestDTO(clientId)),
                            PersonaTokenResponseDTO.class);
            return ofNullable(personaTokenResponse.getBody())
                    .map(PersonaTokenResponseDTO::getPersonaToken);
        } catch (RestClientException e) {
            log.warn("Failed to retrieve a persona-token from YoltProvider.", e);
        }
        return empty();
    }

    @Value
    static class PersonaTokenRequestDTO {
        UUID clientId;
    }

    @Value
    static class PersonaTokenResponseDTO {
        String personaToken;
    }
}
