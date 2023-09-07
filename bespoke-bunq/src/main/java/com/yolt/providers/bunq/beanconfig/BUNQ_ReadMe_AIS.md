## Bunq (AIS)

From Bunq'a website:

"We’re here to break free from the status quo, and create the bank with you in mind. Giving you the freedom to live life
on your terms. We've been a fully-fledged bank since 2014, getting our official banking licence from the Dutch Central
Bank (DNB). Since then, we've been making life easy for our users in 30 European countries."

## BIP overview

|                                       |                                             |
|---------------------------------------|---------------------------------------------|
| **Country of origin**                 | Netherlands                                 | 
| **Site Id**                           | ee622d86-22cf-4f09-a475-198377971ff3        |
| **Standard**                          | bespoke                                     |
| **Contact**                           | E-mail: apipartner@bunq.com                 |
| **Developer Portal**                  | [https://developer.bunq.com/][2]            | 
| **Documentation**                     | [https://beta.doc.bunq.com/][3]             |
| **IP Whitelisting**                   | No                                          |
| **AIS Standard version**              | 1.0                                         |
| **Auto-onboarding**                   | Yes                                         |
| **Requires PSU IP address**           | No                                          |
| **Type of certificate**               | QSEAL                                       |
| **Signing algorithms used**           | SHA256withRSA                               |
| **Mutual TLS Authentication Support** | No                                          |
| **Repository**                        | https://git.yolt.io/providers/bespoke-bunq/ |

## Links - sandbox

|                            |                                             |
|----------------------------|---------------------------------------------|
| **Base URL**               | https://public-api.sandbox.bunq.com/v1/     |
| **Authorization Endpoint** | https://oauth.sandbox.bunq.com/auth         |
| **Token Endpoint**         | https://api-oauth.sandbox.bunq.com/v1/token |

## Links - production

|                            |                                     |
|----------------------------|-------------------------------------|
| **Base URL**               | https://api.bunq.com/v1             |
| **Authorization Endpoint** | https://oauth.bunq.com/auth         |
| **Token Endpoint**         | https://api.oauth.bunq.com/v1/token |

## Client configuration overview

|                                   |                                                                                                                          |
|-----------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| **Oauth User id**                 | Id of Oauth user on Bunq's site - this value is received during registration                                             |
| **Client id**                     | Client id used to authorize access to Bunq's api - this value is received during registration                            | 
| **Client secret**                 | Client secret used to authorize access to Bunq's api - this value is received during registration                        |
| **PSD2 User id**                  | Id of financial institution allowed to consume api as PDS2 Service Provider - this value is received during registration |
| **PSD2 Api key**                  | Api key which is used to creating Bunq's session as PSD2 Service Provider - this value is received during registration   |
| **Signing certificate**           | QSEAL certificate                                                                                                        |
| **Signing certificate chain**     | QSEAL certificate chain id order leaf, intermediate, root                                                                |
| **Client Signing certificate id** | QSEAL certificate id in HSM                                                                                              |

## Registration details

Dynamic registration for Bunq are implemented. AIS and PIS share common registration. You need to have client
certificate id from HSM, client certificate and client certificate chain to perform registration. All those values are
available in APY.

## Connection Overview

First step is to authorize user using OAuth, which is described on [developer portal][4] (let's notice that you don't
have to register an OAuth client, because it was done during registration). Exchanged token has to be used to create
Bunq user session as an API Key, which is also described on [developer portal][5], diagram can be found [here][6].
Session token will be returned as a result of above operations'. It has to be used as authorization header during
fetching accounts, balances, transactions. To refresh Bung session you have to call POST /session-server passing old
session token as API Key. Authorization url is the same for AIS and PIS. It is possible that in case of connecting AIS,
if initiated payment exists, then consent for PIS will be displayed instead of AIS one. Authorization code received
during PIS authorization can be exchanged to access token, which can be used to create Bunq User Session, which can be
used to consuming AIS API.

## Sandbox overview

Sandbox is described in [documentation][3] but was never actively used by us when working with this bank connection.

## User Site deletion
This provider does NOT implement `onUserSiteDelete` method. 

## Business and technical decisions

**Consent validity rules** are set to EMPTY_RULES_SET for AIS Bunq bank due to error page shares same HTML code as a
correct one.

## External links

* [Current open problems on our end][1]
* [Developer portal][2]
* [Documentation][3]

[1]: <https://yolt.atlassian.net/browse/C4PO-9622?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20Bunq%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>

[2]: <https://developer.bunq.com/>

[3]: <https://beta.doc.bunq.com/>

[4]: <https://beta.doc.bunq.com/basics/oauth#authorization-request>

[5]: <https://beta.doc.bunq.com/quickstart/opening-a-session#call-sequence>

[6]: <https://beta.doc.bunq.com/#getting-started>
