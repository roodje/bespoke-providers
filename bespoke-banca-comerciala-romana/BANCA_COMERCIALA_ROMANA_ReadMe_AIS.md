# Banca Comerciala Romana
[Current open problems on our end][1]

## BIP overview 
[Main reference BIP][2]

|   |   |
|---|---|
| **Country of origin** | Romania | 
| **Site Id**  |  9c4b62f9-604f-478d-830f-37436585d91f |
| **Standard**   |  [Erste Group Standard][3] |
| **Form of Contact**  | Email - EAHSupport@erstegroup.com |
| **Base URL** | **Sandbox & Production** - https://webapi.developers.erstegroup.com/api/bcr |
| **Signing algorithms used**| No |
| **Mutual TLS Authentication Support**| Yes |
| **IP Whitelisting**| No |
| **Auto-onboarding**| Not Supported |
| **AIS Standard version**  |  1.0.0 |
| **PISP Standard version**  |  1.0.0 (not implemented in providers)|
| **Account types**| CURRENT_ACCOUNT |
| **Requires PSU IP address** |Yes|

## Client configuration overview
|   |   |
|---|---|
| **Transport key id**  |  Eidas transport key id |
| **Transport certificate** | Eidas transport certificate (QWAC) |
| **Signing key id**  |  Eidas signing key id |
| **Signing certificate** | Eidas signing certificate (QSEAL) |
| **Client id** | The client identifier that is returned during registration process|
| **Client secret** | The secret that is returned with set of client id during registration process|

### Registration details
This bank requires Application Registration on portal.

### Certificate rotation
Bank provides Portal registration with no restrictions to the number of generated client credentials.
I means that we can generate one set of credentials for each certificate.

## Connection Overview
The swagger can be viewed from here: [Banca Comerciala Romana APIs][5] 
The connection requires MTLS with QWAC eIDAS certificate but also authorization step requires OAuth2 Protocol with Proof Key for Code Exchange (PKCE) [RFC 7636][6].
According to the documentation [BCR AIS Production][7]:
* There is a pagination
* The login url has single redirect
* Consent page is valid for 90 days
* Transactions are available to be fetched only for 90 days
* Access token is valid for 3600 seconds
* Refresh flow is supported

**Consent validity rules** are implemented for AIS.

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method.

## Business and technical decisions
Currently no such information about business and technical decision

## Sandbox overview
The sandbox environment looks similar to production environment. According to documentation it do not requires any certificate.
  
## External links
* [Current open problems on our end][1]
* [Main reference BIP][2]
* [Erste Group Standard][3]
* [BCR Production Integration Documentation][4]
* [RFC 7636][6]
* [Developer portal][3]
 
[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%22Banca%20Comerciala%20Romana%22%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
[2]: 
[3]: <https://developers.erstegroup.com/>
[4]: <https://developers.erstegroup.com/docs/tutorial/security-and-authorization#security-and-authorization>
[5]: <https://developers.erstegroup.com/docs/apis/bank.bcr>
[6]: <https://tools.ietf.org/html/rfc7636>
[7]: <https://developers.erstegroup.com/docs/apis/bank.bcr/bank.bcr.v1%2Faisp>
