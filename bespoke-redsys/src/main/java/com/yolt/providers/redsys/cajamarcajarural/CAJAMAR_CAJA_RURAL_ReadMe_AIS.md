## Cajamar Caja Rural (AIS)
[Current open problems on our end][1]

## BIP overview

|                                       |                                                              |
|---------------------------------------|--------------------------------------------------------------|
| **Country of origin**                 | Spain                                                        |
| **Site Id**                           | 4da888ad-df8b-4ed2-b9ea-c68553b69223                         |
| **Standard**                          | Berlin Group                                                 |
| **Contact**                           | E-mail: psd2.hub.soporte@redsys.es                           |
| **Developer Portal**                  | https://market.apis-i.redsys.es/psd2/xs2a/nodos/grupocajamar |
| **Account SubTypes**                  | Current Accounts                                             |
| **IP Whitelisting**                   | No                                                           |
| **AIS Standard version**              | 1.7.3                                                        |
| **Auto-onboarding**                   | No                                                           |
| **Requires PSU IP address**           | Yes                                                          |
| **Type of certificate**               | eIDAS qWAC and qSeal                                         |
| **Signing algorithms used**           | RS256                                                        |
| **Mutual TLS Authentication Support** | Yes                                                          |
| **Repository**                        | https://git.yolt.io/providers/bespoke-bank-name              |

## Links - sandbox

|                            |                                                                                    |
|----------------------------|------------------------------------------------------------------------------------|
| **Base URL**               | https://apis-i.redsys.es:20443/psd2/xs2a/api-entrada-xs2a/services/grupocajamar    |
| **Authorization Base URL** | https://apis-i.redsys.es:20443/psd2/xs2a/api-oauth-xs2a/services/rest/grupocajamar |


## Links - production
|                            |                                                                     |
|----------------------------|---------------------------------------------------------------------|
| **Base URL**               | https://psd2.redsys.es/api-entrada-xs2a/services/grupocajamar       |
| **Authorization Base URL** | https://hubpsd2.redsys.es/api-oauth-xs2a/services/rest/grupocajamar |



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

## Sandbox overview
not used

## Business and technical decisions
In Redsys group returned balance types depends on bank. Due to that fact we decided to prepare logic based on preferences
list. It means that as _Current Balance_ CLOSING_BOOKED, INTERIM_BOOKED, INTERIM_AVAILABLE, OPENING_BOOKED, EXPECTED types
are mapped in presented order. The same mechanism is implemented for _Available Balance_ and the order is as follows
INTERIM_AVAILABLE, INTERIM_BOOKED, OPENING_BOOKED, CLOSING_BOOKED, EXPECTED.
CLOSINGBOOKED is returned.

There is forwardAvailable type of balance too.

Generic implementation is able to map both _pending_ and _booked_ transaction types.
Only booked transactions are returned during tests, but probably both can be returned.

## Certificate rotation

Rotation of certificates is easy process. Due to the fact that there is no registration required, we can just update
authentication means on our side. Remember that certificates have to contain the same Global Unique Reference Number.
Otherwise we also have to update _clientId_ authentication means and users will have to start from consent step, because
from bank's perspective we will be treated as other TPP.

## External links
* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%22Cajamar%20Caja%20Rural%22>
