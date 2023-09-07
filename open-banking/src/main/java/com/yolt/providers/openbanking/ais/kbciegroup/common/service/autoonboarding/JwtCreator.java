package com.yolt.providers.openbanking.ais.kbciegroup.common.service.autoonboarding;

import com.yolt.providers.openbanking.ais.kbciegroup.common.model.ContactDto;
import org.jose4j.jwt.JwtClaims;

import java.util.List;

public interface JwtCreator {

    JwtClaims createSsaClaims(List<String> redirectUris, String clientName, String clientDescription, String jwksUri, String organisationId, String softwareId, List<ContactDto> contactDtoList);

    JwtClaims createRegistrationClaims(String issuer, String institutionId, String softwareId, List<String> redirectUris, String scope, String signedSsa, String signingAlgorithm);
}
