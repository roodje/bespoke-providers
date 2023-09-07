## Knab Bank (PIS)
[Current open problems on our end][1]

## BIP overview 
[Main reference BIP][1]

|                                       |                                            |
|---------------------------------------|--------------------------------------------|
| **Country of origin**                 | Netherland                                 | 
| **Site Id**                           | d47c60db-5034-4c87-bf96-91142aff6107       |
| **Standard**                          | [Berlin group Standard][2]                 |
| **Contact**                           | E-mail: openbanking@knab.nl                |
| **Developer Portal**                  | https://developer.knab.nl                  |
| **IP Whitelisting**                   |
| **PIS Standard version**              | V2                                         |
| **Requires PSU IP address**           | Yes                                        |
| **Type of certificate**               | eIdas                                      |                               
| **Signing algorithms used**           | SHA-256                                    |
| **Mutual TLS Authentication Support** | Yes                                        |
| **Repository**                        | https://git.yolt.io/providers/bespoke-knab |

## Links - sandbox

|                          |                                         |
|--------------------------|-----------------------------------------|
| **Well-known Endpoint**  | 
| **Base URL**             | https://api.knab.nl/openbanking-sandbox |
| **Authorization URL**    | https://login.knab.io                   |
| **Token Endpoint**       | https://login.knab.io/connect/token     |

## Links - production

|                          |                                       |
|--------------------------|---------------------------------------|
| **Well-known Endpoint**  |  
| **Base URL**             | https://tpp-loket.knab.nl/openbanking |
| **Authorization URL**    | https://login.knab.nl                 |
| **Token Endpoint**       | https://login.knab.nl/connect/token   |

## Client configuration overview
|                           |                                              |
|---------------------------|----------------------------------------------|
| **Client id**             | Received via email after manual registration | 
| **Client secret**         | Received via email after manual registration |
| **Signing key id**        | Eidas signing key id                         | 
| **Signing certificate**   | Eidas signing certificate                    | 
| **Transport key id**      | Eidas transport key id                       |
| **Transport certificate** | Eidas transport certificate                  |

### Registration details
Manual - via email message - we received clientId and clientSecret

### Certificate rotation

## Connection Overview

## Sandbox overview
Full Authorization flow + accounts fetch available in Knab-sandbox-postman-collection.json when imported into postman  

## Business decisions

**Payment Flow Additional Information**

|                                                                                                        |                   |
|--------------------------------------------------------------------------------------------------------|-------------------|
| **When exactly is the payment executed ( executed-on-submit/executed-on-consent)?**                    | execute-on-submit |
| **it is possible to initiate a payment having no debtor account**                                      | NO                |
| **At which payment status we can be sure that the money was transferred from the debtor to creditor?** | ACCP status       |

## External links
* [Main reference BIP][1]
* [Berlin group Standard][2]
* [Developer portal][3]
 
[1]: <https://yolt.atlassian.net/wiki/spaces/LOV/pages/3913577/BIP+Knab>
[2]: <https://www.berlin-group.org/nextgenpsd2-downloads>
[3]: <https://developer.knab.nl>
