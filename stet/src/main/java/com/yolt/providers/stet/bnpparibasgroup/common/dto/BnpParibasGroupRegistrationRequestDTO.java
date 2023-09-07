package com.yolt.providers.stet.bnpparibasgroup.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BnpParibasGroupRegistrationRequestDTO {
    @JsonProperty("redirect_uris")
    @Valid
    private List<String> redirectUris = new ArrayList<>();

    @JsonProperty("token_endpoint_auth_method")
    private TokenEndpointAuthMethodEnum tokenEndpointAuthMethod = null;

    @JsonProperty("grant_types")
    @Valid
    private List<GrantTypesEnum> grantTypes = new ArrayList<>();

    @JsonProperty("response_types")
    @Valid
    private List<ResponseTypesEnum> responseTypes = null;

    @JsonProperty("client_name")
    private String clientName = null;

    @JsonProperty("client_uri")
    private String clientUri = null;

    @JsonProperty("logo_uri")
    private String logoUri = null;

    @JsonProperty("scope")
    private String scope = null;

    @JsonProperty("contacts")
    @Valid
    private List<String> contacts = new ArrayList<>();

    @JsonProperty("tos_uri")
    private String tosUri = null;

    @JsonProperty("policy_uri")
    private String policyUri = null;

    @JsonProperty("provider_legal_id")
    private String providerLegalId = null;

    @JsonProperty("client_legal_id")
    private String clientLegalId = null;

    @JsonProperty("logo")
    private String logo = null;

    @JsonProperty("jwks")
    private Object jwks = null;

    @JsonProperty("context")
    private ContextEnum context = null;

    public BnpParibasGroupRegistrationRequestDTO redirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
        return this;
    }

    public BnpParibasGroupRegistrationRequestDTO addRedirectUrisItem(String redirectUrisItem) {
        this.redirectUris.add(redirectUrisItem);
        return this;
    }

    /**
     * Array of redirection URIs for use in redirect-based flows
     *
     * @return redirectUris
     **/
    @ApiModelProperty(required = true, value = "Array of redirection URIs for use in redirect-based flows")
    @NotNull

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public BnpParibasGroupRegistrationRequestDTO tokenEndpointAuthMethod(TokenEndpointAuthMethodEnum tokenEndpointAuthMethod) {
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
        return this;
    }

    /**
     * Requested authentication method for the token endpoint.
     *
     * @return tokenEndpointAuthMethod
     **/
    @ApiModelProperty(required = true, value = "Requested authentication method for the token endpoint.")
    @NotNull

    public TokenEndpointAuthMethodEnum getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    public void setTokenEndpointAuthMethod(TokenEndpointAuthMethodEnum tokenEndpointAuthMethod) {
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    public BnpParibasGroupRegistrationRequestDTO grantTypes(List<GrantTypesEnum> grantTypes) {
        this.grantTypes = grantTypes;
        return this;
    }

    public BnpParibasGroupRegistrationRequestDTO addGrantTypesItem(GrantTypesEnum grantTypesItem) {
        this.grantTypes.add(grantTypesItem);
        return this;
    }

    /**
     * Array of OAuth 2.0 grant types that the client may use
     *
     * @return grantTypes
     **/
    @ApiModelProperty(required = true, value = "Array of OAuth 2.0 grant types that the client may use")
    @NotNull

    public List<GrantTypesEnum> getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(List<GrantTypesEnum> grantTypes) {
        this.grantTypes = grantTypes;
    }

    public BnpParibasGroupRegistrationRequestDTO responseTypes(List<ResponseTypesEnum> responseTypes) {
        this.responseTypes = responseTypes;
        return this;
    }

    public BnpParibasGroupRegistrationRequestDTO addResponseTypesItem(ResponseTypesEnum responseTypesItem) {
        if (this.responseTypes == null) {
            this.responseTypes = new ArrayList<>();
        }
        this.responseTypes.add(responseTypesItem);
        return this;
    }

    /**
     * Array of the OAuth 2.0 response types that the client may use
     *
     * @return responseTypes
     **/
    @ApiModelProperty(value = "Array of the OAuth 2.0 response types that the client may use")

    public List<ResponseTypesEnum> getResponseTypes() {
        return responseTypes;
    }

    public void setResponseTypes(List<ResponseTypesEnum> responseTypes) {
        this.responseTypes = responseTypes;
    }

    public BnpParibasGroupRegistrationRequestDTO clientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    /**
     * Human-readable name of the client to be presented to the user. ‘ are not allowed but will be soon. If you have a client_name with ‘, please contact the support.
     *
     * @return clientName
     **/
    @ApiModelProperty(required = true, value = "Human-readable name of the client to be presented to the user. ‘ are not allowed but will be soon. If you have a client_name with ‘, please contact the support.")
    @NotNull

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public BnpParibasGroupRegistrationRequestDTO clientUri(String clientUri) {
        this.clientUri = clientUri;
        return this;
    }

    /**
     * URL of a web page providing information about the client
     *
     * @return clientUri
     **/
    @ApiModelProperty(value = "URL of a web page providing information about the client")

    public String getClientUri() {
        return clientUri;
    }

    public void setClientUri(String clientUri) {
        this.clientUri = clientUri;
    }

    public BnpParibasGroupRegistrationRequestDTO logoUri(String logoUri) {
        this.logoUri = logoUri;
        return this;
    }

    /**
     * URL that references a logo for the client
     *
     * @return logoUri
     **/
    @ApiModelProperty(value = "URL that references a logo for the client")

    public String getLogoUri() {
        return logoUri;
    }

    public void setLogoUri(String logoUri) {
        this.logoUri = logoUri;
    }

    public BnpParibasGroupRegistrationRequestDTO scope(String scope) {
        this.scope = scope;
        return this;
    }

    /**
     * Space-separated list of OAuth 2.0 scope values, this field is only required when the context value is equal to psd2
     *
     * @return scope
     **/
    @ApiModelProperty(value = "Space-separated list of OAuth 2.0 scope values, this field is only required when the context value is equal to psd2")

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public BnpParibasGroupRegistrationRequestDTO contacts(List<String> contacts) {
        this.contacts = contacts;
        return this;
    }

    public BnpParibasGroupRegistrationRequestDTO addContactsItem(String contactsItem) {
        this.contacts.add(contactsItem);
        return this;
    }

    /**
     * Array of strings representing ways to contact people responsible for this client, typically email addresses
     *
     * @return contacts
     **/
    @ApiModelProperty(required = true, value = "Array of strings representing ways to contact people responsible for this client, typically email addresses")
    @NotNull

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public BnpParibasGroupRegistrationRequestDTO tosUri(String tosUri) {
        this.tosUri = tosUri;
        return this;
    }

    /**
     * URL that points to a human-readable terms of service document for the client
     *
     * @return tosUri
     **/
    @ApiModelProperty(value = "URL that points to a human-readable terms of service document for the client")

    public String getTosUri() {
        return tosUri;
    }

    public void setTosUri(String tosUri) {
        this.tosUri = tosUri;
    }

    public BnpParibasGroupRegistrationRequestDTO policyUri(String policyUri) {
        this.policyUri = policyUri;
        return this;
    }

    /**
     * URL that points to a human-readable policy document for the client
     *
     * @return policyUri
     **/
    @ApiModelProperty(value = "URL that points to a human-readable policy document for the client")

    public String getPolicyUri() {
        return policyUri;
    }

    public void setPolicyUri(String policyUri) {
        this.policyUri = policyUri;
    }

    public BnpParibasGroupRegistrationRequestDTO providerLegalId(String providerLegalId) {
        this.providerLegalId = providerLegalId;
        return this;
    }

    /**
     * Authorization number of the TPP according to ETSI specification on eIDAS certificates for PSD2
     *
     * @return providerLegalId
     **/
    @ApiModelProperty(required = true, value = "Authorization number of the TPP according to ETSI specification on eIDAS certificates for PSD2")
    @NotNull

    public String getProviderLegalId() {
        return providerLegalId;
    }

    public void setProviderLegalId(String providerLegalId) {
        this.providerLegalId = providerLegalId;
    }

    public BnpParibasGroupRegistrationRequestDTO clientLegalId(String clientLegalId) {
        this.clientLegalId = clientLegalId;
        return this;
    }

    /**
     * Authorization number of the agent
     *
     * @return clientLegalId
     **/
    @ApiModelProperty(value = "Authorization number of the agent")

    public String getClientLegalId() {
        return clientLegalId;
    }

    public void setClientLegalId(String clientLegalId) {
        this.clientLegalId = clientLegalId;
    }

    public BnpParibasGroupRegistrationRequestDTO logo(String logo) {
        this.logo = logo;
        return this;
    }

    /**
     * base64 encoded value of the logo
     *
     * @return logo
     **/
    @ApiModelProperty(value = "base64 encoded value of the logo")

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public BnpParibasGroupRegistrationRequestDTO jwks(Object jwks) {
        this.jwks = jwks;
        return this;
    }

    /**
     * Client's JSON Web Key Set [RFC7517] document value, which contains the client's public keys.
     *
     * @return jwks
     **/
    @ApiModelProperty(value = "Client's JSON Web Key Set [RFC7517] document value, which contains the client's public keys.")

    public Object getJwks() {
        return jwks;
    }

    public void setJwks(Object jwks) {
        this.jwks = jwks;
    }

    public BnpParibasGroupRegistrationRequestDTO context(ContextEnum context) {
        this.context = context;
        return this;
    }

    /**
     * Define the client context
     *
     * @return context
     **/
    @ApiModelProperty(required = true, value = "Define the client context")
    @NotNull

    public ContextEnum getContext() {
        return context;
    }

    public void setContext(ContextEnum context) {
        this.context = context;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BnpParibasGroupRegistrationRequestDTO registrationRequest = (BnpParibasGroupRegistrationRequestDTO) o;
        return Objects.equals(this.redirectUris, registrationRequest.redirectUris) &&
                Objects.equals(this.tokenEndpointAuthMethod, registrationRequest.tokenEndpointAuthMethod) &&
                Objects.equals(this.grantTypes, registrationRequest.grantTypes) &&
                Objects.equals(this.responseTypes, registrationRequest.responseTypes) &&
                Objects.equals(this.clientName, registrationRequest.clientName) &&
                Objects.equals(this.clientUri, registrationRequest.clientUri) &&
                Objects.equals(this.logoUri, registrationRequest.logoUri) &&
                Objects.equals(this.scope, registrationRequest.scope) &&
                Objects.equals(this.contacts, registrationRequest.contacts) &&
                Objects.equals(this.tosUri, registrationRequest.tosUri) &&
                Objects.equals(this.policyUri, registrationRequest.policyUri) &&
                Objects.equals(this.providerLegalId, registrationRequest.providerLegalId) &&
                Objects.equals(this.clientLegalId, registrationRequest.clientLegalId) &&
                Objects.equals(this.logo, registrationRequest.logo) &&
                Objects.equals(this.jwks, registrationRequest.jwks) &&
                Objects.equals(this.context, registrationRequest.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(redirectUris, tokenEndpointAuthMethod, grantTypes, responseTypes, clientName, clientUri, logoUri, scope, contacts, tosUri, policyUri, providerLegalId, clientLegalId, logo, jwks, context);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RegistrationRequest {\n");

        sb.append("    redirectUris: ").append(toIndentedString(redirectUris)).append("\n");
        sb.append("    tokenEndpointAuthMethod: ").append(toIndentedString(tokenEndpointAuthMethod)).append("\n");
        sb.append("    grantTypes: ").append(toIndentedString(grantTypes)).append("\n");
        sb.append("    responseTypes: ").append(toIndentedString(responseTypes)).append("\n");
        sb.append("    clientName: ").append(toIndentedString(clientName)).append("\n");
        sb.append("    clientUri: ").append(toIndentedString(clientUri)).append("\n");
        sb.append("    logoUri: ").append(toIndentedString(logoUri)).append("\n");
        sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
        sb.append("    contacts: ").append(toIndentedString(contacts)).append("\n");
        sb.append("    tosUri: ").append(toIndentedString(tosUri)).append("\n");
        sb.append("    policyUri: ").append(toIndentedString(policyUri)).append("\n");
        sb.append("    providerLegalId: ").append(toIndentedString(providerLegalId)).append("\n");
        sb.append("    clientLegalId: ").append(toIndentedString(clientLegalId)).append("\n");
        sb.append("    logo: ").append(toIndentedString(logo)).append("\n");
        sb.append("    jwks: ").append(toIndentedString(jwks)).append("\n");
        sb.append("    context: ").append(toIndentedString(context)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
