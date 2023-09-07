## Argenta
[Current open problems on our end][1] 

## BIP overview 

|   |   |
|---|---|
| **Country of origin** | BE | 
| **Site Id**  | d51cb308-e0ac-11eb-ba80-0242ac130004 |
| **Standard**   | Berlin Group |
| **Contact**  |E-mail: InfoPSD2@argenta.be |
| **Developer Portal** | https://portal.payments.argenta.be/ | 
| **Account SubTypes**| CURRENT, CREDIT, SAVINGS |
| **IP Whitelisting**| no |
| **AIS Standard version**  | 1.3 |
| **PISP Standard version**  | 1.3 |
| **Auto-onboarding**| no |
| **Type of certificate** | EIDAS QWAC/QSEAL|

## Links - sandbox
|   |   |
|---|---|
| **OAuth2 Server Authorisation** | https://login.test-payments.argenta.be/psd2/v1/berlingroup-auth |
| **OAuth2 Server Token** | https://api.test-payments.argenta.be/psd2/v1/berlingroup-auth/token |
| **Account information server** | https://api.test-payments.argenta.be/berlingroup/v1/accounts |
| **Consent Management for Accounts Information** | https://api.test-payments.argenta.be/berlingroup/v1/consents |
| **OAuth2 Server Token** | https://api.test-payments.argenta.be/psd2/v1/berlingroup-auth/token |

## Links - production 
|   |   |
|---|---|
| **OAuth2 Server Authorisation** | https://login.payments.argenta.be/psd2/v1/berlingroup-auth |
| **OAuth2 Server Token** | https://api.payments.argenta.be/psd2/v1/berlingroup-auth/token |
| **Account information server** | https://api.payments.argenta.be/berlingroup/v1/accounts |
| **Consent Management for Accounts Information** | https://api.payments.argenta.be/berlingroup/v1/consents |
| **OAuth2 Server Token** | https://api.payments.argenta.be/psd2/v1/berlingroup-auth/token |


## Client configuration overview
Example fields (for each bank fields might be different)


|   |   |
|---|---|
| **Signing key id** || 
| **Signing certificate** | | 
| **Transport key id**  |   |
| **Transport certificate** | |
| **Certificate Agreement Number**   |  |
| **Client id** | | 

## Registration details

## Connection Overview

## Sandbox overview

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 
  
## Business and technical decisions
  
## External links
* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/browse/C4PO-8533?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%20Argenta%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>
