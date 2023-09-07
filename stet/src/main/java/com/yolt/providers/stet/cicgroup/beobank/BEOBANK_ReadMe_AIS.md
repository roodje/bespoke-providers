## Beobank (AIS)

[Current open problems on our end][1]

Beobank is a Belgian bank owned by a French financial conglomerate Crédit Mutuel Nord Europe.

## BIP overview

|                                       |                                                     |
|---------------------------------------|-----------------------------------------------------|
| **Country of origin**                 | Belgium                                             | 
| **Site Id**                           | d6d3ccb3-656c-4cba-bb2e-fca4568adda1                |
| **Standard**                          | STET                                                |
| **Contact**                           | E-mail: openbanking@e-i.com                         |
| **Developer Portal**                  | [https://online.beobank.be/oauth2/fr/devportal/][2] | 
| **Account SubTypes**                  | Current, Credit Card                                |
| **IP Whitelisting**                   | No                                                  |
| **AIS Standard version**              | STET v1.4.2                                         |
| **Auto-onboarding**                   | Yes                                                 |
| **Requires PSU IP address**           | No                                                  |
| **Type of certificate**               | eidas                                               |
| **Signing algorithms used**           | SHA 256 with RSA                                    |
| **Mutual TLS Authentication Support** | Yes                                                 |
| **Repository**                        | https://git.yolt.io/providers/stet                  |

## Links - sandbox

|                            |                                                             |
|----------------------------|-------------------------------------------------------------|
| **Base URL**               | https://oauth2-apiii.e-i.com/sandbox/beobank/               | 
| **Authorization Endpoint** | https://online.beobank.be/oauth2/{lang}/sandbox/signin.html |
| **Token endpoint**         | https://oauth2-apiii.e-i.com/sandbox/beobank/oauth2         |

The {lang} tag can be replaced by one of the following values: ["en","fr","nl"]

## Links - production

|                            |                                                                          |
|----------------------------|--------------------------------------------------------------------------|
| **Base URL**               | https://oauth2-apiii.e-i.com/beobank/                                    | 
| **Authorization Endpoint** | https://online.beobank.be/oauth2/{lang}/banque/oauth2_authorization.aspx |
| **Token endpoint**         | https://oauth2-apiii.e-i.com/sandbox/beobank/oauth2                      |

The {lang} tag can be replaced by one of the following values: ["en","fr","nl"]

## Client configuration overview

|                           |                             |
|---------------------------|-----------------------------|
| **Transport key id**      | Eidas transport key id      |
| **Transport certificate** | Eidas transport certificate |

## Registration details

Beobank has dynamic registration, and it is well described on bank's [developer portal][4]

The bank requires dynamic registration, autoonboarding is enabled. The registration is pretty normal, except
one thing, when registering one has to provide a Client Name (this field which will be later displayed in the client's
application - it is good to consult this value before typing it ).

There is also a possibility of multiple registration (information from the bank):
_Yes, you can use the OAuth 2 registration API to register multiple OAuth 2 client IDs. You can register multiple client
IDs for the same certificate, and each OAuth 2 client ID can have its own user-visible name, and will have its own set
of OAuth 2 tokens (authorization codes, refresh tokens, access tokens)._

During registration, you can register more than one redirect urls.

### Certificate rotation

TBD

## Connection Overview

Connection is well described on bank's [developer portal][3]

## Sandbox overview

Sandbox is available but wasn't used during implementation

## Consent validity rules

Beobank AIS uses dynamic flow, thus we are unable to determine consent validity rules for AIS.

## User Site deletion

This provider does NOT implement `onUserSiteDelete` method.

## Business and technical decisions

## External links

* [Current open problems on our end][1]

[1]: <https://yolt.atlassian.net/issues/?jql=project%20%3D%20%22C4PO%22%20AND%20component%20%3D%20BEOBANK%20AND%20status%20!%3D%20Done%20AND%20Resolution%20%3D%20Unresolved%20ORDER%20BY%20status>

[2]: <https://online.beobank.be/oauth2/fr/devportal/>

[3]: <https://online.beobank.be/oauth2/fr/devportal/sca-workflows.html>

[4]: <https://online.beobank.be/oauth2/fr/devportal/registration.html>
