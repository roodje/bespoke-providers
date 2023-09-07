## UniCaja Banco (AIS)
[Current open problems on our end][1]

## BIP overview

|                                       |                                                                                                                                                                                                                 |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Country of origin**                 | ES                                                                                                                                                                                                              |
| **Site Id**                           | ab528687-f175-4cd0-a151-3b6de1d60821                                                                                                                                                                            |
| **Standard**                          | Berlin                                                                                                                                                                                                          |
| **Contact**                           | e-mail: psd2.hub.soporte@redsys.es.<br/> Email should contain [YOLT][{Bank name}] in its title.<br/> Attach filled Query_details.xlsx.<br/> If any video is sent remember to send it as compressed ZIP archive. |
| **Developer Portal**                  | [Developer portal][2]                                                                                                                                                                                           |
| **Account SubTypes**                  | Current, Credits                                                                                                                                                                                                |
| **IP Whitelisting**                   | No                                                                                                                                                                                                              |
| **AIS Standard version**              | 1.7.4                                                                                                                                                                                                           |
| **Auto-onboarding**                   | no                                                                                                                                                                                                              |
| **Requires PSU IP address**           | yes                                                                                                                                                                                                             |
| **Type of certificate**               | eIDAS qWAC & qSeal Certificates are required                                                                                                                                                                    |
| **Signing algorithms used**           | RS256                                                                                                                                                                                                           |
| **Mutual TLS Authentication Support** | yes                                                                                                                                                                                                             |
| **Repository**                        | https://git.yolt.io/providers/bespoke-redsys                                                                                                                                                                    |

## Links - sandbox

|                            |                                                                                    |
|----------------------------|------------------------------------------------------------------------------------|
| **Base URL**               | https://apis-i.redsys.es:20443/psd2/xs2a/api-entrada-xs2a/services/unicajabanco    |
| **Authorization Endpoint** | https://apis-i.redsys.es:20443/psd2/xs2a/api-oauth-xs2a/services/rest/unicajabanco |

## Links - production

|                            |                                                                     |
|----------------------------|---------------------------------------------------------------------|
| **Base URL**               | https://psd2.redsys.es/api-entrada-xs2a/services/unicajabanco       |
| **Authorization Endpoint** | https://hubpsd2.redsys.es/api-oauth-xs2a/services/rest/unicajabanco |

## Client configuration overview

|                           |                                                       |
|---------------------------|-------------------------------------------------------|
| **Client id**             | Global Unique Reference Number from eIDAS certificate |
| **Transport certificate** | Eidas transport certificate                           |
| **Transport key id**      | Eidas transport key id                                |      
| **Signing certificate**   | Eidas signing certificate                             | 
| **Signing key id**        | Eidas signing key id                                  | 

## Registration details
In whole Redsys group there is no manual or dynamic registration. It is enough to use a valid eIDAS certificate (QWAC & QSEAL)
with a unique organization identifier (GURN - Global Unique Reference Number) during mutual TLS authentication and signing the request.
The Global Unique Reference Number is used as _Client Id_.


## Connection Overview
Swagger can be downloaded from developer portal, but during implementation process we discovered that it is deprecated,
so all model DTOs in our implementation were created manually based on documentation.

Documentation for the bank and swaggers can be found on their developer portal. To access those data you don't have
to login using any credentials.

## Sandbox overview
During implementation of whole group sandbox (Caixa Bank) was user. We were able to verify authorization API, but there was no possibility
to perform calls for AIS, because of requirement of eIDAS certificate for signature signing.

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

## Business and technical decisions
In Redsys group returned balance types depends on bank. Due to that fact we decided to prepare logic based on preferences
list. It means that as _Current Balance_ CLOSING_BOOKED, INTERIM_BOOKED, INTERIM_AVAILABLE, OPENING_BOOKED, EXPECTED types
are mapped in presented order. The same mechanism is implemented for _Available Balance_ and the order is as follows
INTERIM_AVAILABLE, INTERIM_BOOKED, OPENING_BOOKED, CLOSING_BOOKED, EXPECTED. For UniCaja Banco only CLOSING_BOOKED and
INTERIM_AVAILABLE are returned.

##Consent validity rules
Consent validity rules are set to EMPTY_RULES_SET. We either get consent page, or we get error status.

## Additional information about consent
|                                                                                                           |
|-----------------------------------------------------------------------------------------------------------|
| **Consent with access type "availableAccounts" for list of accounts available without balances.**         |
| **Consent with access type "availableAccountsWithBalances" for list of accounts available with balances** |
| **Consent with access type "allPsd2"**                                                                    |

Details of other consent than Global Consent are at "Manage Consent" Table : [Developer portal][2]

## Certificate rotation

Rotation of certificates is easy process. Due to the fact that there is no registration required, we can just update
authentication means on our side. Remember that certificates have to contain the same Global Unique Reference Number.
Otherwise we also have to update _clientId_ authentication means and users will have to start from consent step, because
from bank's perspective we will be treated as other TPP.

## External links
* [Current open problems on our end][1]
* [Developer portal][2]

[1]: https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%22UniCaja%20Banco%22
[2]: https://market.apis-i.redsys.es/psd2/xs2a/nodos/unicajabanco

