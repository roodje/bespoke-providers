package com.yolt.providers.stet.bnpparibasgroup.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BnpParibasGroupRegistrationRequestUpdateDTO {
    @JsonProperty("client_id")
    private String clientId = null;

    @JsonProperty("redirect_uris")
    private List<String> redirectUris = new ArrayList<>();

    @JsonProperty("token_endpoint_auth_method")
    private TokenEndpointAuthMethodEnum tokenEndpointAuthMethod = null;

    @JsonProperty("grant_types")
    private List<GrantTypesEnum> grantTypes = new ArrayList<>();

    @JsonProperty("response_types")
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

    public BnpParibasGroupRegistrationRequestUpdateDTO clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Get clientId
     *
     * @return clientId
     **/
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO redirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
        return this;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO addRedirectUrisItem(String redirectUrisItem) {
        this.redirectUris.add(redirectUrisItem);
        return this;
    }

    /**
     * Array of redirection URIs for use in redirect-based flows
     *
     * @return redirectUris
     **/
    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO tokenEndpointAuthMethod(TokenEndpointAuthMethodEnum tokenEndpointAuthMethod) {
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
        return this;
    }

    /**
     * Requested authentication method for the token endpoint.
     *
     * @return tokenEndpointAuthMethod
     **/
    public TokenEndpointAuthMethodEnum getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    public void setTokenEndpointAuthMethod(TokenEndpointAuthMethodEnum tokenEndpointAuthMethod) {
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO grantTypes(List<GrantTypesEnum> grantTypes) {
        this.grantTypes = grantTypes;
        return this;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO addGrantTypesItem(GrantTypesEnum grantTypesItem) {
        this.grantTypes.add(grantTypesItem);
        return this;
    }

    /**
     * Array of OAuth 2.0 grant types that the client may use
     *
     * @return grantTypes
     **/
    public List<GrantTypesEnum> getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(List<GrantTypesEnum> grantTypes) {
        this.grantTypes = grantTypes;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO responseTypes(List<ResponseTypesEnum> responseTypes) {
        this.responseTypes = responseTypes;
        return this;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO addResponseTypesItem(ResponseTypesEnum responseTypesItem) {
        if (this.responseTypes == null) {
            this.responseTypes = new ArrayList<ResponseTypesEnum>();
        }
        this.responseTypes.add(responseTypesItem);
        return this;
    }

    /**
     * Array of the OAuth 2.0 response types that the client may use
     *
     * @return responseTypes
     **/
    public List<ResponseTypesEnum> getResponseTypes() {
        return responseTypes;
    }

    public void setResponseTypes(List<ResponseTypesEnum> responseTypes) {
        this.responseTypes = responseTypes;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO clientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    /**
     * Human-readable name of the client to be presented to the user.
     *
     * @return clientName
     **/
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO clientUri(String clientUri) {
        this.clientUri = clientUri;
        return this;
    }

    /**
     * URL of a web page providing information about the client
     *
     * @return clientUri
     **/
    public String getClientUri() {
        return clientUri;
    }

    public void setClientUri(String clientUri) {
        this.clientUri = clientUri;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO logoUri(String logoUri) {
        this.logoUri = logoUri;
        return this;
    }

    /**
     * URL that references a logo for the client
     *
     * @return logoUri
     **/
    public String getLogoUri() {
        return logoUri;
    }

    public void setLogoUri(String logoUri) {
        this.logoUri = logoUri;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO scope(String scope) {
        this.scope = scope;
        return this;
    }

    /**
     * Space-separated list of OAuth 2.0 scope values (aisp, pisp and cbpii)
     *
     * @return scope
     **/
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO contacts(List<String> contacts) {
        this.contacts = contacts;
        return this;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO addContactsItem(String contactsItem) {
        this.contacts.add(contactsItem);
        return this;
    }

    /**
     * Array of strings representing ways to contact people responsible for this client, typically email addresses
     *
     * @return contacts
     **/
    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO tosUri(String tosUri) {
        this.tosUri = tosUri;
        return this;
    }

    /**
     * URL that points to a human-readable terms of service document for the client
     *
     * @return tosUri
     **/
    public String getTosUri() {
        return tosUri;
    }

    public void setTosUri(String tosUri) {
        this.tosUri = tosUri;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO policyUri(String policyUri) {
        this.policyUri = policyUri;
        return this;
    }

    /**
     * URL that points to a human-readable policy document for the client
     *
     * @return policyUri
     **/
    public String getPolicyUri() {
        return policyUri;
    }

    public void setPolicyUri(String policyUri) {
        this.policyUri = policyUri;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO providerLegalId(String providerLegalId) {
        this.providerLegalId = providerLegalId;
        return this;
    }

    /**
     * Authorization number of the TPP according to ETSI specification on eIDAS certificates for PSD2
     *
     * @return providerLegalId
     **/
    public String getProviderLegalId() {
        return providerLegalId;
    }

    public void setProviderLegalId(String providerLegalId) {
        this.providerLegalId = providerLegalId;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO clientLegalId(String clientLegalId) {
        this.clientLegalId = clientLegalId;
        return this;
    }

    /**
     * Authorization number of the agent
     *
     * @return clientLegalId
     **/
    public String getClientLegalId() {
        return clientLegalId;
    }

    public void setClientLegalId(String clientLegalId) {
        this.clientLegalId = clientLegalId;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO logo(String logo) {
        this.logo = logo;
        return this;
    }

    /**
     * base64 encoded value of the logo
     *
     * @return logo
     **/
    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO jwks(Object jwks) {
        this.jwks = jwks;
        return this;
    }

    /**
     * Client&#x27;s JSON Web Key Set [RFC7517] document value, which contains the client&#x27;s public keys.
     *
     * @return jwks
     **/
    public Object getJwks() {
        return jwks;
    }

    public void setJwks(Object jwks) {
        this.jwks = jwks;
    }

    public BnpParibasGroupRegistrationRequestUpdateDTO context(ContextEnum context) {
        this.context = context;
        return this;
    }

    /**
     * Define the client context
     *
     * @return context
     **/
    public ContextEnum getContext() {
        return context;
    }

    public void setContext(ContextEnum context) {
        this.context = context;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BnpParibasGroupRegistrationRequestUpdateDTO registrationRequestUpdate = (BnpParibasGroupRegistrationRequestUpdateDTO) o;
        return Objects.equals(this.clientId, registrationRequestUpdate.clientId) &&
                Objects.equals(this.redirectUris, registrationRequestUpdate.redirectUris) &&
                Objects.equals(this.tokenEndpointAuthMethod, registrationRequestUpdate.tokenEndpointAuthMethod) &&
                Objects.equals(this.grantTypes, registrationRequestUpdate.grantTypes) &&
                Objects.equals(this.responseTypes, registrationRequestUpdate.responseTypes) &&
                Objects.equals(this.clientName, registrationRequestUpdate.clientName) &&
                Objects.equals(this.clientUri, registrationRequestUpdate.clientUri) &&
                Objects.equals(this.logoUri, registrationRequestUpdate.logoUri) &&
                Objects.equals(this.scope, registrationRequestUpdate.scope) &&
                Objects.equals(this.contacts, registrationRequestUpdate.contacts) &&
                Objects.equals(this.tosUri, registrationRequestUpdate.tosUri) &&
                Objects.equals(this.policyUri, registrationRequestUpdate.policyUri) &&
                Objects.equals(this.providerLegalId, registrationRequestUpdate.providerLegalId) &&
                Objects.equals(this.clientLegalId, registrationRequestUpdate.clientLegalId) &&
                Objects.equals(this.logo, registrationRequestUpdate.logo) &&
                Objects.equals(this.jwks, registrationRequestUpdate.jwks) &&
                Objects.equals(this.context, registrationRequestUpdate.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, redirectUris, tokenEndpointAuthMethod, grantTypes, responseTypes, clientName, clientUri, logoUri, scope, contacts, tosUri, policyUri, providerLegalId, clientLegalId, logo, jwks, context);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RegistrationRequestUpdate {\n");

        sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
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
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
