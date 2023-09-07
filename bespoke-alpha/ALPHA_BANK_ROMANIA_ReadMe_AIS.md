## Alpha Bank
[Current open problems on our end][1]

## BIP

|                                       |                                                                             |
|---------------------------------------|-----------------------------------------------------------------------------|
| **Country of origin**                 | Romania                                                                     |
| **Site Id**                           | cbbd4f37-dbf2-457b-9368-ecb27fe91209                                        |
| **Standard**                          | Custom (Web Banking API)                                                    |
| **General Support Contact**           | e-mail: ApidevSupport@alphabank.ro                                          |
| **Skype Contact**                     |                                                                             |
| **AIS Standard version**              | 1.4.0                                                                       |
| **Mutual TLS Authentication Support** |                                                                             |
| **Signing algorithms used**           | S256                                                                        |
| **Account SubTypes**                  | Current Account (also Debit Cards are included), Savings (Deposit Accounts) |
| **Requires PSU IP address**           | optional                                                                    |
| **Auto-onboarding**                   |                                                                             |
| **IP Whitelisting**                   | No                                                                          |
| **Type of needed certificate**        | eIDAS certificate is Required: QSealC                                       |
| **Repository**                        | https://git.yolt.io/providers/bespoke-alpha                                 |


## Links - development


|                                    |                                                    |
|------------------------------------|----------------------------------------------------|
| **Developer Portal**               | https://developer.api.alphabank.eu/                |
| **Sandbox Base URL**               | https://gw.api.alphabank.eu/api/sandbox            |
| **Sandbox Authorization Endpoint** | https://gw.api.alphabank.eu/sandbox/auth/authorize |
| **Sandbox Token Endpoint**         | https://gw.api.alphabank.eu/sandbox/auth/authorize |

## Links - production


|                                       |                                                 |
|---------------------------------------|-------------------------------------------------|
| **Base URL**                          | https://gw.api.alphabank.eu/ro/api/             |
| **Authorization Endpoint**            | https://openbanking.alphabank.ro/auth/authorize |
| **Token Endpoint**                    | https://openbanking.alphabank.ro/auth/token     |
| **Client Credentials Token Endpoint** | https://openbanking.alphabank.ro/auth/ccToken   |

## Client configuration overview



|                                  |                                         |
|----------------------------------|-----------------------------------------|
| **Signing key id**               | eIDAS signing key id                    |
| **Signing certificate**          | eIDAS signing certificate               |
| **Certificate Agreement Number** |                                         |
| **Client id**                    | Client ID taken from registered app     |
| **Client Secret**                | Client Secret taken from registered app |

## Registration details

Registration manual at dev portal, In order to use any App(Sandbox, Prod), 

## Connection Overview

## Sandbox overview

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method.

## Business and technical decisions

## External links
* [Current open problems on our end][1]

[1]: https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20%22AlPHA%22
