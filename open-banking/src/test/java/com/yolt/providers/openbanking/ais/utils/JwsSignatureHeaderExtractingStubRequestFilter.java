package com.yolt.providers.openbanking.ais.utils;

import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestWrapper;
import com.github.tomakehurst.wiremock.extension.requestfilter.StubRequestFilter;
import com.github.tomakehurst.wiremock.http.Request;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

import java.util.Optional;

/**
 * It is a Wiremock's StubRequestFilter implementation responsible for parsing
 * JWS signature back into JsonWebSignature object and extracts all JWS headers
 * as a JSON string from it. Then JWS headers in the form of JSON string is injected
 * as a new ephemeral HTTP header named 'jwsSignatureExtractedHeaderJson'
 * and then that new HTTP header can be used inside request stub to match
 * against specific JWS header claims.
 *
 * Implementation background:
 *  JWS signature is a string matching pattern: /.+\.{2}.+^ which as a plain text
 *  doesn't tell us anything about how JWS header structure looks like and there is no simple
 *  way to match a stub request based on particular JWS header claims values.
 *  This is an important feature missed for integration testing - sometimes there is need
 *  to match specific stub request based on some JWS header claims values,
 *  i.e. we can check whether specific claim is included in the JWS signature and has proper value assigned.
 *
 *  Usage:
 *   - If your original request contains 'x-jws-signature' header, then you can simply
 *   add desired matcher definition for 'jwsSignatureExtractedHeaderJson' inside
 *   'headers' section in your request stub, ex.
 *
 *   {
 *   "request" : {
 *     "url" : "/v3.1/pisp/domestic-payments",
 *     "method" : "POST",
 *     "headers" : {
 *       ... other headers omitted for brevity...,
 *       "jwsSignatureExtractedHeaderJson": {
 *         "matchesJsonPath": "$.[?(!@.b64 && !(@.crit contains 'b64'))]"
 *       }
 *     }
 *   },
 *   "response" : {
 *     "status" : 201,
 *     ...response body omitted for brevity...
 *   }
 * }
 */
public class JwsSignatureHeaderExtractingStubRequestFilter extends StubRequestFilter {

    public static final String X_JWS_SIGNATURE_HEADER_KEY = "x-jws-signature";
    public static final String JWS_SIGNATURE_EXTRACTED_HEADER_JSON = "jwsSignatureExtractedHeaderJson";

    @Override
    public RequestFilterAction filter(Request request) {
        if (request.containsHeader(X_JWS_SIGNATURE_HEADER_KEY)) {
            return RequestFilterAction.continueWith(extractJwsHeaderAsJson(request.getHeader(X_JWS_SIGNATURE_HEADER_KEY))
                    .map(jwsHeaderJson -> RequestWrapper.create()
                            .addHeader(JWS_SIGNATURE_EXTRACTED_HEADER_JSON, jwsHeaderJson)
                            .wrap(request))
                    .orElse(request));
        }
        return RequestFilterAction.continueWith(request);
    }

    @Override
    public String getName() {
        return "jws-signature-header-extracting-filter";
    }

    private Optional<String> extractJwsHeaderAsJson(final String compactSignature) {
        JsonWebSignature jws = new JsonWebSignature();
        try {
            jws.setCompactSerialization(compactSignature);
            return Optional.ofNullable(jws.getHeaders().getFullHeaderAsJsonString());
        } catch (JoseException e) {
            return Optional.empty();
        }
    }
}
